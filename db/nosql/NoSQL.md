# NoSQL

## 1. Redis

### 1.2 Introduction

<font color="#a00"><b>Redis</b></font> is an open source, *in-memory* **data structure store**, used as database, *cache*, and *message broker*.

### 1.3 Data type and abstractions

<font color="#a00"><b>Redis</b></font> is **NOT** a *plain* key-value store, it is actually a *data structures server*, supporting different kinds of values. In <font color="#a00">Redis</font> the value is not limited to a simple string, but also hold more complex data structures.

- `Binary-safe strings`
- `Lists`: collections of string elements sorted according to the order of insertion.
- `Sets`: collections of *unique*, *unsorted* string elements
- `Sorted sets`, similar to `Sets` but where every string element is associated to a floating number value, called *score*. The elements are always taken sorted by their score.
- `Hashes`, which are maps composed of fields associated with values. Both are strings.
- `Bit arrays`, using special commands, to handle string values like an array of *bit*s.
- `HyperLogLogs`, a probabilistic data structure which is used in order to estimate the cardinality of a set.
- `Streams`: append-only collections of map-like entries that provide an abstract log data type.

#### 1.3.1 Redis Keys

Few rules about keys:

- *very long* keys are not a good idea. Because the lookup of the key in the dataset may require several *costly key-comparisons*.
- *very short* keys are often not a good idea. (balance the readable and consumption on memory)
- try to *stick with a schema*.
- The *maximum* allowed key size is **512**MB

##### 1.3.1.1 `Redis Expires`

Quick info about <font color="#a00">Redis expires</font>:

- can be set both using `seconds` or `milliseconds` precision.
- the expire time resolution is *always* 1 milliseconds
- information about expires are *replicated and persisted* on disk, the time virtually passed when your Redis server remains stopped.

> ***Tips***:
>
> `TTL` command can be used to check the remaining time to live for the key. `PEXPIRE` and `PTTL` can be used to set and check expires in *milliseconds* as well.

#### 1.3.2 Redis Lists

<font color="#a00"><b>Redis lists</b></font> are implemented via **Linked Lists**. This means even if you have millions of elements in a list, the operation of adding a new element in the head or in the tail of list is performed in *constant time*.

> **Tips**
>
> `LRANGE` takes tow indexes, the first and the last element of the range to return. Both indexes can be negative, telling  Redis to start counting from the end: `-1` is the last element, `-2` is the penultimate element of the list, and so forth.

Two very representative use cases are:

- Remember the *latest updates posted* by users into a social network.
- Communication between processes, using a *consumer-producer* pattern. 

##### 1.3.2.1 `Capped lists`

<font color="#a00"><b>Redis</b></font> allows us to use lists as a *capped collection*, only remembering the ***latest N*** items and discarding all the oldest items using the `LTRIM` command.

The `LTRIM` command is similar to `LRANGE`, but **instead of displaying the specified range of elements** it sets this range as the new list value. All the elements outside the given range are *removed*.

##### 1.3.2.2 `Blocking operations on lists`

Imagine you want to push item into a list with one process, and use a different process in order to actually do some kind of work with those items. This is usual *producer/consumer* setup:

-  push items into the list, producer call `LPUSH`
- extract/process items from the list, consumers call `RPOP`

The <font color="#a0a">drawbacks</font>:

1. **Forces** Redis and clients to process useless commands (all requests when the list is empty will get no actual work done, they'll just return `NULL`)
2. **Adds a day** to the processing of items, since after a worker receives a `NULL`, it waits some time.

To deal with this, `BRPOP` and `BLPOP` command is introduced.

> **Tips**
>
> `BRPOP` and `BLPOP`, they'll return to the caller only when a new element is added to the list, or when a user defined time out is reached.

> **Notice**
>
> Few things note about `BRPOP`:
>
> 1. Clients are served in an ordered way: the first client that blocked waiting for a list, is served first when an element is pushed by some other client, and so forth.
> 2. The return value is different compared to `RPOP`: it is a two-element array since it also includes the name of the key.
> 3. If the timeout is reached, `NULL` is returned.

##### 1.3.2.3 `Automatic creation and removal of keys`

Three rules:

1. When we add an element to an aggregate data type, if the target key does not exit, an empty aggregate data type is created before adding the element.
2. When we remove elements from an aggregate data type, if the value remains empty, the key is **automatically** destroyed. The ***Stream*** data type is the only exception to this rule.
3. Calling a read-only command such as `LLEN`, or a write command removing elements, with an empty key, always produces the same result as if the key is holding an empty aggregate type of the type the command expects to find.

#### 1.3.3 Redis Hashes

<font color="#a00"><b>Redis hashes</b></font> look exactly how one might expect a "**hash**" to look, with **field-value** pairs.

<font color="#a0a">Hashes</font> are handy to represent *objects*, actually the number of fields you can put inside a hash has no practical limits.

> **Tips**
>
> `HMSET`, `HGET`, `HINCRBY`

#### 1.3.4 Redis Sets

<font color="#a00"><b>Redis Sets</b></font> are *unordered* collections of strings.

<font color="#a0a">Sets</font> are good for *expressing relations* between objects.

> **Tips**
>
> `SADD`, `SMEMBERS`

#### 1.3.5 Redis Sorted sets

<font color="#a00"><b>Sorted sets</b></font> are a data type which is similar to a mix between a <font color="#a0a">Set</font> and a <font color="#a0a">Hash</font>. 

The elements in a sorted sets are *taken in order*. They are ordered according to the following rule:

- If A and B are two elements with different score, then A > B if A.score > B.score
- If A and B has exactly the same score, then A > B if the A string is lexicographically greater than B string. 

> **Notice**
>
> <font color="#a00">Sorted sets</font> are implemented via a dual-ported data structure containing both skip list and a hash table, so every time we add an element Redis performs an O(log(N)) operation.

##### 1.3.5.1 `Updating the score: leader boards`

<font color="#a00"><b>Sorted sets'</b></font> scores can be updated at any time. Just calling `ZADD` against an element already included the sorted set will update its score with O(log(N)) time complexity.

#### 1.3.6 Bitmaps

<font color="#a00"><b>Bitmaps</b></font> are not an actual data type, but a set of *bit-oriented* operations defined on the String type. 

<font color="#a0a">Bit</font> operations are divided into two groups: constant-time single bit operations like setting a bit to 1 or 0, or getting its value, and operations on groups of bits.

> **Tips**
>
> One of the biggest advantages of bitmaps is that they often provide extreme space savings when storing information.

<font color="#a0a">Bits</font> are set and retrieved using the `SETBIT` and `GETBIT` commands.

Common use cases for bitmaps are:

- Real time analytics of all kinds.
- Storing space efficient but high performance boolean information associated with object IDs.

### 1.4 Distributed locks with Redis

<font color="#a00"><b>Distributed locks</b></font> are a very useful primitive in many environments where different processes must operate with shared resources in a mutually exclusive way.

Three properties that, are the minimum guarantees needed to use distributed locks in an effective way.

1. `Safety property`: mutual exclusion. At any given moment, only one client can hold a lock
2. `Liveness property A`: *deadlock free*. Eventually it is always possible to acquire a lock, even if the client that locked a resource crashes or gets partitioned.
3. `Liveness property B`: *Fault tolerance*. As long as the majority of Redis nodes are up, clients are able to acquire and release locks.

#### 1.4.1 Correct implementation with a single instance

To *acquire* the lock, 

``` shell
SET resource_name my_random_value NX PX 30000
```

> **Tips**
>
> The command above will set the value if it does not already exist (NX option), with an expire of 30000 milliseconds (PX option). *The value must be unique across all clients and all lock request*.

Basically the random value is used in order to release the lock in a safe way, with script that tells <font color="#a0a">Redis</font>: 

> **Notice**
>
> Remove the key only if it exits and the value stored at the key is exactly the one I expect to be.

``` lua
if redis.call("get", KEYS[1] == ARGV[1]) then
    return redis.call("del", KEYS[1])
else
    return 0
end
```

> **Tips**
>
> The time we use as the key *time to live*, is called the "*lock validity time*". It is both the auto release time, and the time the client has in order to perform the operation required before another client may be able to acquire the lock again without technically violating the mutual exclusion guarantee, which is only limited to the given window of time from the moment the lock is acquired.

<font color="#aaa">`Drawback`</font>

*A single point of failure will cause race condition*:

1. Client A acquires the lock in the master
2. The master crashes before the write to the key is transmitted to the slave
3. the slave gets promoted to master
4. Client B acquires the lock to the same resource A already holds a lock for. **SAFETY VIOLATION**

#### 1.4.2 The Redlock algorithm

In order to acquire the lock, the client performs the following operations

1. it gets the current time in milliseconds
2. it tries to *acquire* the lock in *all the N instances sequentially*, using the same key name and random value in all instances. During step 2, when setting the lock in each instance, the client uses a timeout which is smaller compared to the total lock auto-release time in order to acquire it.
3. The client computes *how much time elapsed* in order to acquire the lock, by substracting from the current time, the timestamp obtained in step 1. 

### 1.5 Expire

Set a *timeout* on `key`. After the timeout has expired, the key will automatically be *deleted*. A key with an associated timeout is often said to be *volatile* in <font color="#a0a">Redis</font> terminology.

The *timeout* will only be cleared by commands that *delete* or *overwrite* the contents of key, including `DEL`, `SET`, `GETSET` and all the **STORE** commands. This means that all the operations that conceptually *alter* the value stored at the key without replacing it with a new one will leave the timeout *untouched*.

If a key is overwritten by `RENAME`, like in case of an existing key `Key_A` that is overwritten by a call like `RENAME Key_B Key_A`, it does not matter if the original `Key_A` had a timeout associated or not, the new key `Key_A` will inherit all the characteristics of `Key_B`.

#### 1.5.1 Refreshing expires

It is possible to call `EXPIRE` using an argument a key that already has an existing expire set. In this case the *time to live* (**TTL**) is *updated* to the new value. 

#### 1.5.2 Return value

***Integer reply***, specifically:

- `1` if the timeout was set
- `0` if `key` does not exist

Normally <font color="#a0a">Redis</font> keys are created *without* an associated TTL. The key will simply live forever, unless it is removed by the user in an explicit way, for instance using the `DEL` command.

Keys expiring information is stored as absolute Unix timestamps. This means that the *time is flowing* even when <font color="#a0a">Redis</font> instance is **INACTIVE**.

<font color="#a00">Redis</font> keys are expired in two ways: a *passive* way, and an *active* way.

> **Tips**
>
> A key is *passively* expired simply when some client tries to access it, and the key is found to be timed out.
>
> Periodically <font color="#a0a">Redis</font> tests a few keys at random among keys with an expire set. All the keys that are already expired are deleted from the key space.
>
> Specifically this is what <font color="#a0a">Redis</font> does 10 times per second:
>
> 1. test 20 random keys from the set of keys with an associated expire
> 2. Delete all keys found expired
> 3. If more than **25%** of keys were expired, start again from step 1.

> **Notice**
>
> In order to obtain a correct behavior without sacrificing consistency, when a key expires, a `DEL` operation is synthesized in both of AOF file and gains all the attached replicas nodes. This way the expiration process is centralized in the master instance, and there is no chance of consistency errors.

### 1.6 Transactions

`MULTI`, `EXEC`, `DISCARD` and `WATCH` are the foundation of transactions in <font color="#a0a">Redis</font>. They allow the execution of a group of commands in a single step, with two important guarantees:

- All the commands in a *transaction* are *serialized* and *executed* sequentially. It can never happen that a request issued by another client is served **in the middle** of the execution of a <font color="#a0a">Redis</font> transaction.

- Either all of the commands or none are processed, so a <font color="#a0a">Redis</font> transaction is also ***atomic***.

  > **Notice**
  >
  > Using `append-only file (AOF)`
  >
  > If the <font color="#a0a">Redis</font> server crashes or is killed by the system administrator in some hard way it is possible that only a partial number of operations are registered.
  >
  > <font color="#a0a">Redis</font> will detect this condition at restart, and will exit with an error. Using the `redis-check-aof` tool it is possible to fix the append only file that will remove the partial transaction so that the server start again.

#### 1.6.1 Usage

A <font color="#a0a">Redis</font> transaction is entered using the `MULTI` command. The command *always* replies with an `OK`. At this point we can issue multiple commands. Instead of executing these commands, <font color="#a0a">Redis</font> will queue them. All commands are executed once `EXEC` is called.

Calling `DISCARD` instead will flush the transaction queue and will *exit* the transaction.

``` shell
MULTI // reply with `ok`
INCR foo // reply with `QUEUED`
INCR bar // reply with `QUEUED`
EXEC
```

It's important to note that **even when a command fails, all the other commands in the queue are processed** - <font color="#a0a">Redis</font> will *not* stop the processing of commands.

> **Tips**
>
> <font color="#a0a">Redis</font> does **not** support rollback

#### 1.6.2 Optimistic locking using check-and-set

`WATCH` is used to provide a *check-and-set* (**CAS**) behavior to <font color="#a0a">Redis</font> transactions. `WATCH`ed keys are monitored in order to detect changes against them. If at least one watched key is *modified* before the `EXEC` command, the whole transaction aborts, and `EXEC` returns a `NULL reply` to notify that the transaction failed.

``` shell
WATCH my_key
val = GET my_key
val = val + 1
MULTI
SET my_key $val
EXEC
```

### 1.7 Using Redis as an LRU cache

When <font color="#a00"><b>Redis</b></font> is used as a cache, often it is handy to let it automatically *evict* old data as you add new data.

<font color="#a0a"><b>LRU</b></font> is actually only one of the supported eviction methods. 

> **Tips**
>
> Staring with <font color="#a00"><b>Redis</b></font> version 4.0, a new <font color="#a0a"><b>LFU</b></font> (Least Frequently Used) eviction policy is introduced

#### 1.7.1 Max memory configuration directive

The `maxmemory` configuration directive is used in order to configure <font color="#a00"><b>Redis</b></font> to use a specified amount of memory for the dataset. 

> **Notice**
>
> Set the configuration directive using the `redis.conf` file, or later using the `CONFIG SET` command at runtime

``` shell
maxmemory 100mb
```

> **Tips**
>
> Setting `maxmemory` to `zero` results into no memory limits. This is the default behavior for 64 bit systems, while 32 bit systems use an implicit memory limit of 3GB

#### 1.7.2 Eviction policies

The exact behavior <font color="#a0a"><b>Redis</b></font> follows when the `maxmemory` limit is reached is configured using the `maxmemory-policy` configuration directive.

Available:

- **noeviction**: returns *errors* when the memory limit was reached and the client is trying to execute commands that could result in more memory to be used.
- **allkeys-lru**: *evict* keys by trying to remove the less recently used (LRU) keys first, in order to make space for the new data added.
- **volatile-lru**: *evict* keys by trying to remove the less recently used (LRU) keys first, but only among keys that have an **expire set**, in order to make space for the new data added
- **allkeys-random**: *evict* keys randomly in order to make space for the new data added.
- **volatile-random**: *evict* keys randomly in order to make space for the new data added, only *evict* keys with an **expire set**.
- **volatile-ttl**: *evict* keys with an **expire set**, and try to *evict* keys with a shorter time to live (TTL) first, in order to make space for the new data added.

In general as a rule of thumb:

- Use the **allkeys-lru** policy when you expect a power-law distribution in the popularity of your requests, that is, you expect that a *subset* of elements will be accessed far more often than the rest. **A good pick if you are unsure**.
- Use the **allkeys-random** if you have a cyclic access where all the keys are scanned continuously, or when you expect the distribution to be uniform
- Use the **volatile-ttl** if you want to be able to provide hints to <font color="#a0a"><b>Redis</b></font> about what are good candidate for expiration by using different **TTL** values when you create your cache objects.

#### 1.7.3 How the eviction process works

It is important to understand that the eviction process works like:

- a client runs a new command, resulting in more data added
- <font color="#a0a">Redis</font> checks the memory usage, and if it is greater than the `maxmemory` limit, it evicts keys according to the policy.
- A new command is executed, and so forth

#### 1.7.4 LFU mode

Think at <font color="#a0a"><b>LRU</b></font>, an item that was recently accessed but is actually almost never requested, will not get expired, so the risk is to *evict* a key that has a higher chance to be requested in the future.

To configure the <font color="#a0a"><b>LFU</b></font> mode, the following policies are available:

- `volatile-lfu`: *evict* using approximated **LFU** among the keys with an expire set.
- `allkeys-lfu`: *evict* an key using approximated **LFU**

By default <font color="#a0a"><b>Redis</b></font> 4.0 is configured to:

- *Saturate* the counter at, around, one million requests
- *Decay* the counter every one minute.

``` shell
lfu-log-factor 10
lfu-decay-time 1
```

> **Tips**
>
> The *decay time* is the amount of minutes a counter should be decayed.
>
> The counter *logarithm factor* changes how many hits are needed in order to saturate the frequency counter, which is just the range *0-255*. The higher the factor, the more accesses are needed to reach the maximum. The lower the factor, the better is the resolution of the counter for low accesses.

Basically the **factor** is a trade off between *better distinguishing items with low accesses* **VS** *distinguishing items with high accesses*.

### 1.8 Partition

<font color="#a00">Partitioning</font> is the process of *splitting* your data into multiple <font color="#a00">Redis</font> instances, so that every instances will only contain a *subset* of your keys.

#### 1.8.1 Why 

It serves *two* main goals:

- it allows for much larger databases, using the *sum of the memory of many computers*.
- allows *scaling* the computational power to multiple cores and multiple computers, and the network bandwidth to multiple computers and network adapters.

#### 1.8.2 Basics

Simplest way to perform partitioning is with **range partitioning**, and is accomplished by mapping ranges of objects into specific <font color="#a00">Redis</font> instances.

> **Notice**
>
> disadvantages:
>
> *requires a table that map ranges to instances*

Another way is **hash partitioning**:

- Take the key name and us a *hash* function (e.g., `crc32`)
- Use a *modulo* operation with this number in order to turn it into a number between 0 and `number of Redis instances`, so that this number can be mapped to one of my Redis instances.

> **Tips**
>
> One advanced form of hash partitioning is called **consistent hashing**.

#### 1.8.3 Different implementations

Available implementations:

- **Client side partitioning** means that the clients directly select the right node where to write or read a given key.

- **Proxy assisted partitioning** means that our client send requests to a proxy that is able to speak the <font color="#a00">Redis</font> protocol, instead of sending requests directly to the right <font color="#a00">Redis</font> instance. The Redis and Memcached proxy `Twemproxy` implements proxy assisted partitioning.

- **Query routing** means that you can send your query to a random instance, and the instance will make sure to forward your query to the right node.

  > **Tips**
  >
  > The request is not directly forwarded from a Redis instance to another, but the client gets *redirected* to the right node.

#### 1.8.4 Disadvantages

Some don't play well with partitioning:

- Operations involving multiple keys are usually not supported
- Redis transactions involving multiple keys can not be used.
- The partitioning granularity is the key, so it is not possible to shard a dataset with a single huge key like a very big sorted set.
- When partitioning is used, data handling is more complex
- Adding and removing capacity can be complex

#### 1.8.5 Data store or cache

Main concept here is the following:

- If Redis is used as a cache **scaling up and down** using consistent hashing is easy
- If Redis is used as a store, **a fixed keys-to-nodes map is used, so the number of nodes must be fixed and cannot vary**. Otherwise, a system is needed that is able to *rebalance* keys between nodes when nodes are needed or removed.

#### 1.8.6 Presharding

Since <font color="#a00">Redis</font> has an extremely small footprint and is lightweight (a small instance uses 1MB of memory), a simple approach to this problem is to start with a lot of instances from the start.

Using <font color="#a00">Redis</font> replication:

- Start empty instances in your new server.
- Move data configuring these new instances as slaves for your source instances
- Stop your clients
- Update the configuration of the moved instances with the new server IP address
- Send the `SLAVEOF NO ONE` command to the slaves in the new server
- Restart your clients with the new updated configuration
- Finally shutdown the no longer used instances in the old server

### 1.9 Administration

#### 1.9.1 redis-cli

`redis-cli` is the <font color="#a00">Redis</font> command line interface, a simple program that allows to send commands to Redis, and reads the replies sent by the server, directly from the terminal.

Two main modes:

- interactive mode
- another mode where the command is sent as arguments of `redis-cli`, *executed*, and *printed* on the standard output.

##### 1.9.1.1 continuously run the same command

This feature is controlled by two options: `-r <count>` and `-i <delay>`. The first states *how many times* to run a command, the second configures the *delay* between the different command calls, in seconds.

``` shell
redis-cli -r 5 incr foo
```

> **Tips**
>
> To run the same command forever, use `-1` as count.

##### 1.9.1.2 Running Lua scripts

`redis-cli` can be used to run script from a file in a way more comfortable way compared to typing the script interactively into the shell or as an argument:

``` shell
cat /tmp/script.lua // return redis.call('set', KEYS[1], ARGV[1])
redis-cli --eval /tmp/script.lua foo , bar // OK
```

The <font color="#a00">Redis</font> `EVAL` command takes the list of keys the script uses, and the other non key arguments, as different arrays.

The `--eval` option is useful when writing simple scripts.

> **Notice**
>
> When a reconnection is performed, `redis-cli` automatically re-select the last database number selected. However the other state about the connection is lost, such as the state of a transaction if we were in middle of it.

##### 1.9.1.3 continuous stats mode

`--stat` is one of the lesser known features of `redis-cli`, and one very useful in order to monitor <font color="#a00">Redis</font> instances in real time.

In this mode, a new line is printed every second with useful information and the differences between old data point. You can easily understand what's happening with *memory usage*, *clients connected*, and so forth.

The `-i <interval>` option in this case works as a modifier in order to change the frequency at which new lines are emitted. The default is one *second*.

``` shell
redis-cli --stat -i 3
```

##### 1.9.1.4 Scanning for big keys

In this special mode, `redis-cli` works as a *key space analyzer*. It scans the dataset for big keys, but also provides information about the data types that the data set consists of.

``` shell
redis-cli --bigkeys
```

The program uses the `SCAN` command, so it can be executed against a busy server without impacting the operations, however the `-i` option can be used in order to throttle the scanning process of the specified fraction of second for each 100 keys requested.

##### 1.9.1.5 Getting a list of keys

It is also possible to scan the key space, again in a way that does not block the <font color="#a00">Redis</font> server, and print all the key names, or filter them for specific patterns.

``` shell
redis-cli --scan | head -10
```

##### 1.9.1.6 Monitoring

The monitoring mode is entered automatically once you use the `MONITOR` mode, it will print all the commands received by a <font color="#a00">Redis</font> instance.

``` shell
redis-cli monitor
```

<font color="#a00"><b>Redis</b></font> is often used in contexts where *latency* is very critical. *Latency* involved multiple moving parts within application, from the client library to the network stack, to the Redis instance itself.

The basic *latency* checking tool is the `--latency` option.

``` shell
redis-cli --latency
```

The stats are provided in milliseconds. Usually, the average latency of a very fast instance tends to be overestimated a bit because of the latency due to the kernel scheduler of the system running `reds-cli` itself.

Sometimes it is useful to study how the maximum and average latencies evolve during time. The `--latency-history` option is used for that purpose: it works exactly like `--latency`, but every *15* seconds (by default) a new sample session is started from scratch.

``` shell
redis-cli --latency-history
```

> **Notice**
>
> You can change the sampling sessions' length with the `-i <interval>` option

##### 1.9.1.7 Remote backups of RDB files

During <font color="#a00"><b>Redis</b></font> replication's first synchronization, the master and the slave exchange the whole data in form of an RDB file. This feature is exploited by `redis-cli` in order to provide a remote backup family, that allows to transfer an **RDB** file from any Redis instance to the local computer running `redis-cli`. 

``` shell
redis-cli --rdb /tmp/dump.rdb // may raise an error
```

##### 1.9.1.8 Slave mode

The slave mode of the CLI is an advanced feature useful for <font color="#a00">Redis</font> developers and for debugging operations. it allows to inspect what a master sends to its slaves in the replication stream in order to propagate the writes to its replicas.

``` shell
redis-cli --slave
```

