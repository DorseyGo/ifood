# 1. DevOps

## 2. GitLab CI

### 2.1 CI/CD pipelines

<font color="#a00"><b>Pipelines</b></font> are the *top-level* component of continuous integration, delivery, and deployment.

<font color="#a00"><b>Pipelines</b></font> comprise:

- **Jobs**, which defines *what* to do
- **Stages**, which defines *when* to run the jobs.

<font color="#a00"><b>Jobs</b></font> are executed by `runners`. Multiple jobs in the same stage are executed in *parallel*, if there are enough concurrent runners.

If *all* jobs in a stage succeed, the pipeline moves onto the next stage.

If *any* job in a stage fails, the next stage is not (usually) executed and the pipeline ends early.

### 2.2 Types of pipelines

<font color="#a00"><b>Pipelines</b></font> can be configured in many different ways:

- ***Basic pipelines***, runs everything in each stage concurrently, followed by the next stage.
- ***Directed Acyclic Graph Pipeline (DAG) pipelines***, based on relationships between jobs and can run more quickly than basic pipelines.
- ***Multi-project pipelines***, combine pipelines for different projects together
- ***Parent-Child pipelines***, break down complex pipelines into one parent pipeline that can trigger multiple child sub-pipelines, which all run in the same project and with *the same SHA*
- ***Pipelines for Merge Requests***, run for merge requests only
- ***Pipelines for Merged Results***, are merge request pipelines that acts as though the changes from the source branch have already been merged into the target branch.
- ***Merge Trains***, use pipelines for merged results to queue merges one after the other.

### 2.3 Configure a pipeline

<font color="#a00"><b>Pipelines</b></font> and their component *jobs* and *stages* are defined in the CI/CD pipeline configuration file for each project.

- *Jobs* are basic configuration component.
- *Stages* are defined by using the `Stages` keyword.

#### 2.3.1 Jobs

##### 2.3.1.1 Stages

<font color="#a00">Stages</font> is used to define stages that contain groups of *jobs*. <font color="#a00">Stages</font> is defined *globally* for the pipeline. Use `stage` in a job to define which stage the job is run on.

The order of the `stages` items defines the *execution* order for jobs:

- *Jobs* in the **same** stage run in parallel.
- *Jobs* in the next stage run after the jobs from previous stage complete *successfully*.

``` yaml
stages:
  - build
  - test
  - deploy
```

> ***Notice***:
>
> If no `stages` are defined in `.gitlab-ci.yml`, then `build`, `test` and `deploy` are the default pipeline stages
>
> if a job does not specify a `stage`, the job is assigned the `test` stage
>
> To make a job start earlier and ignore the stage order, use the `needs` keyword

##### 2.3.1.2 include

<font color="#a00">Include</font> keyword is used to include external YAML files in your CI/CD configuration.

`include` requires the external YAML file to have extensions `.yml` or `.yaml`, otherwise the external file is not included.

`extends` keyword is used instead to include external YAML file.

`include` supports following inclusion methods:

| Keyword    | Method                                                       |
| ---------- | ------------------------------------------------------------ |
| `local`    | Include a file from the local project repository             |
| `file`     | Include a file from a different project repository           |
| `remote`   | Include a file from a remote URL. Must be publicly accessible |
| `template` | Include templates that are provided by GitLab                |

`include:local`

`include:local` includes file from the **same** repository as `.gitlab-ci.yml`. It's referenced with full paths relative to the root directory (`/`).

if you `include:local`, make sure that both `.gitlab-ci.yml` and the local file are on the same branch.

``` yaml
include:
  - local: '/templates/.gitlab-ci-template.yml'
#: or you can shortly using followings
include: '.gitlab-ci-production.yml'
```

`include:file`

To include files from another private project on the same GitLab instance, use `include:file`. you can use `include:file` in combination with `include:project` only.

The included file is referenced with a full path, relative to the root directory (`/`).

`ref` can be specified, if you wanna a different version other than `HEAD`.

``` yaml
include:
  - project: 'my-group/my-project'
    ref: master
    file: '/templates/.gitlab-ci-template.yml'
```

> Multiple files from a project can be specified as:
>
> ``` yaml
> include:
>   - project: 'my-group/my-project'
>     ref: master
>     file: 
>       - '/templates/.builds.yml'
>       - '/templates/.tests.yml'
> ```

`include:remote`

`include:remote` can be used to include a file from a different location, using HTTP/HTTPS, referenced by the full URL.

``` yaml
include:
  - remote: 'https://gitlab.com/awesome-project/raw/master/.gitlab-ci-template.yml'
```

> ***Notice***:
>
> The remote file *must* be publicly accessible by a *GET* method

##### 2.3.1.3 script

`script` is the only required keyword that a job needs. It's a shell script that is executed by the runner.

If any special characters used in your script, please consider wrap the entire command in a single quotes. If the command already uses single quotes, you should change them to double quotes (`"`) if possible.

If any of the script commands return an exit code other than `zero`, the job fails and further commands are not executed. *Store* the exit code in a variable to avoid this behavior:

``` yaml
job:
  script:
    - false || exit_code=$?
    - if [ $exit_code -ne 0 ]; then echo "Previous command failed"; fi
```

##### 2.3.1.4 before_script

`before_script` is used to define an array of commands that should run before each job, but after artifacts are restored.

Scripts specified in `before_script` are concatenated with any scripts specified in the main script, and executed together in a single shell.

``` yaml
default:
  before_script:
    - echo "execute this script in all jobs that don't already have a before_script section."
```

##### 2.3.1.5 after_script

`after_script` is used to define an array of commands that run after each job, including failed jobs.

If a job *times out* or is *cancelled*, the `after_script` commands are not executed.

##### 2.3.1.6 Stage

`stage` is defined per-job and relies on `stages`, which is defined globally.

``` yaml
stages:
  - build
  - test
  - deploy

job 0:
  stage: .pre
job 1:
  stage: build
job 2:
  stage: .post
```

> ***Notices***:
>
> - `.pre`, which is guaranteed to always be the first stage in a pipeline
> - `.post`, which is guaranteed to always be the last stage in a pipeline

##### 2.3.1.7 extends

`extends` defines entry names that a job that uses `extends` inherits from.

`extends` is able to merge hashed but not arrays. The algorithm used for merge is "*closest scope wins*", so keys from the last member always override anything defined on other levels.

``` yaml
.only-important:
  variables:
    URL: 'http://my-url.internal'
    IMPORTANT_VAR: "the details"
  only:
    - master
    - stable
  tags:
    - production
  script:
    - echo "hello world!"
    
.in-docker:
  variables:
    URL: "http://docker-url.internal"
  tags:
    - docker
  image: alpine
  
rsepc:
  variables:
    GITLAB: "is-awesome"
  extends:
    - .only-important
    - .in-docker
  script:
    - rake rspec
```

result in:

``` yaml
rspec:
  variables:
    URL: "http://docker-url.internal"
    IMPORTANT_VAR: "the details"
    GITLAB: "is-awesome"
    
  only:
    - master
    - stable
  tags:
    - docker
  image: alpine
  script:
    - rake rspec
```

##### 2.3.1.8 only/except

<font color="#a00"><b>GitLab</b></font> supports multiple strategies, four keys are available:

- `refs`
- `variables`
- `changes`
- `kubernetes`

If you use multiple keys under `only` or `except`, the keys are evaluated as a single conjoined expression. That is:

- `only`: includes the job if **all** of the keys have at least one condition that matches
- `except`: excludes the job if **all** of keys have at least one condition that matches

> ***Notice***:
>
> With `only`, individual keys are logically joined by an `AND`. A job is added to the pipeline if the following is true:
>
> `(any of listed refs are true) AND (any listed variables are true) AND (any listed changes are true) AND (any chosen Kubernetes status matches)`

For example:

``` yaml
test:
  script: npm run test
  only:
    refs:
      - master
      - schedules
    variables:
      - $CI_COMMIT_MESSAGE =~ /run-end-to-end-tests/
    kubernetes: active
```

> ***Notice***:
>
> With `except`, individual keys are logically joined by an `OR`. A job is **not** added if the following is true:
>
> `(any listed refs are true) OR (any listed variables are true) OR (any listed changes are true) OR (a chosen kubernete status matches)`

For example:

``` yaml
test:
  script: npm run test
  except:
    refs:
      - master
    changes:
      - "README.md"
```

In the example above, the `test` job is **not** created when any of the following are true:

- the pipeline runs for the `master` branch
- there are changes to the `README.md` file in the root directory of the repository

##### 2.3.1.9 needs

Use the `needs:` keyword to execute jobs *out-of-order*. Relationships between jobs that use `needs` can be visualized as a *directed acyclic graph*.

**Requirements and limitations**

- if `needs:` is set to point to a job that is not instantiated because of `only/except` rules or otherwise does not exist, the pipeline is not created and a YAML error is shown.

- The maximum number of jobs that a single job can need in the `needs:` array is limited:

  - For <font color="#a0a">GitLab.com</font>, the limit is 50

  - For self-managed instances, the limit is: 50. the limit can be changed.

    ``` properties
    Plan.default.actual_limits.update!(ci_needs_size_limit: 100)
    ```

- If `needs:` refers to a job that is marked as `parallel:`. the current job depends on all parallel jobs being created.

- `needs:` is similar to `dependencies:` in that it must use jobs from prior stages, meaning it's impossible to create circular dependencies.

- Related to the above, stages must be explicitly defined for all jobs that have the keyword `needs:` or are referred to by one.

##### 2.3.1.10 Tags

Use `tags` to select a *specific runner* from the list of all runners that are available for the project.

You can use `tags` to run different jobs on different platforms.

``` yaml
osx job:
  stage:
    - build
  tags:
    - osx
    
  script:
    - echo "hello, $user!"
```

##### 2.3.1.11 `allow_failure`

Use `allow_failure` when you want to let a job fail *without impacting* the rest of the CI suite. The default is `false`. 

When `allow_failure` is set to `true` and the job fails, the job shows an orange warning in the UI. However, the logical flow of the pipeline considers the job a success/passed, and is ***NOT*** blocked.

`allow_failure:exit_codes`

Use `allow_failure:exit_codes` to dynamically control if a job should be allowed to fail. You can list *which exit codes* are not considered failures. The job fails for any other exit code:

``` yaml
test_job_1:
  script:
    - echo "Run a script that results in exit code 1. this job fails"
    - exit 1
  allow_failure:
    exit_codes: 137
    
test_job_2:
  script:
    - echo "Run a script that results in exit code 137. This job is allowed to fail."
    - exit 137
  allow_failure:
    exit_codes:
      - 137
      - 255
```

##### 2.3.1.12 `when`

`when` is used to implement jobs that are run in case of failure or despite the failure.

`when` can be set to one of the following values:

1. `on_success` (default) - execute job only when all jobs in earlier stages succeed, or are considered successful because they have `allow_failure:true`
2. `on_failure` - execute job only when at least one job in an earlier stage fails.
3. `always` - execute job regardless of the status of jobs in earlier stages
4. `manual` - execute job manually
5. `delayed` - Delay the execution of a job for a specified duration.
6. `never`:
   - with `rules`, don't execute job
   - with `workflow:rules`, don't run pipeline.

``` yaml
stages:
  - build
  - cleanup_build
  - test
  - deploy
  - cleanup
  
build_job:
  stage: build
  script:
    - make build
    
cleanup_build_job:
  stage: cleanup_build
  script:
    - cleanup build when failed
  when: on_failure
  
test_job:
  stage: test
  script:
    - make test
    
deploy_job:
  stage: deploy
  script:
    - make deploy
  when: manual
  
cleanup_job:
  stage: cleanup
  script:
    - cleanup after jobs
  when: always
```

<b>`when:delayed`</b>

Use `when:delayed` to execute scripts after a waiting period. you can set the period with `start_in` key. The value of `start_in` key is an elapsed time in seconds, unless a unit is provided. It must be less than or equal to *one week*.

- `'5'`
- `5 seconds`
- `30 minutes`
- `1 day`
- `1 week`

When there is a delayed job in a stage, the pipeline doesn't progress until the delayed job has finished.

##### 2.3.1.13 `environment`

Use `environment` to define the environment that a job deploys to. If `environment` is specified and no environment under that name exits, a new one is created automatically.

``` yaml
deploy to production:
  stage: deploy
  script: git push production HEAD:master
  environment: production
```

##### 2.3.1.14 `cache`

`cache` is used to specify a list of files and directories that should be cached between jobs. You can only use paths that are in the local working copy.

If `cache` is defined outside the scope of jobs, it means it's set globally and all jobs use that definition.

Caching is *shared* between pipelines and jobs. *Caches* are restored before artifacts.

##### 2.3.1.15 `retry`

Use `retry` to configure how many times a job is retried in case of a failure.

When a job fails, the job is processed again, until the limit specified by the `retry` keyword is reached.

By default, a job is retried on all failure cases. To have better control over which failures to retry, `retry` can be hash with the following keys:

- `max`: the maximum number of retries
- `when`: the failure cases to retry

##### 2.3.1.16 `timeout`

Use `timeout` to configure a timeout for a specific job.

``` yaml
build:
  script: build.sh
  timeout: 3 hours 30 minutes
  
test:
  script: rspec
  timeout: 3h 30m
```

##### 2.3.1.17 `parallel`

Use `parallel` to configure how many instances of a job to run in parallel. The value can be from `2` to `50`.

The `parallel` keyword creates `N` instances of the same job that run in parallel.

``` yaml
test:
  script: rspec
  parallel: 5
```

##### 2.3.1.18 `trigger`

Use `trigger` to define a downstream pipeline trigger. 

##### 2.3.1.19 `variables`

<font color="#a00">CI/CD variables</font> are configurable values that are passed to jobs. They can be set globally and per-job.

Two types of variables:

- `custom variables`: you can define their values in the `.gitlab-ci.yml` file, in the GitLab UI, or by using the API.
- `predefined variables`: These values are set by the runner itself.

## Appendix

*Repos* for <font color="#a0a">kubernetes</font>:

``` properties
[kubernetes]
name=Kubernetes
baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64/
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg https://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
```

