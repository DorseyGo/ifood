# Postgres

## 1 Basics

### **1.1 Architecture Fundamentals**

PostgreSQL uses a <font color="#aa0">client/server</font> model.

A PostgreSQL session consists of followings:

- a server process, which <font color="#aa0">manages</font> the database files, <font color="#aa0">accepts</font> new connections to database from client applications, and <font color="#aa0">performs</font> database actions on behalf of clients.
- the user's client want to perform database actions.

The client and server communicate over <font color="#aa0">*TCP/IP*</font> network connection.

### **1.2 Creating a Database**

``` shell
createdb $DB_NAME // create a database
dropdb $DB_NAME // drop the specific database
```

### 1.3 Accessing A Database

Once you have created a database, you can <font color="#aa0">access</font> it by:

- Running the PostgreSQL interactive terminal program
- an existing graphical fronted tool
- writing a custom application, using one of several available language bindings

you probably want to start up `psql` to try the examples

``` shell
psql $DB_NAME
```

> if no database name specified, then the database equals to the user is accessed, otherwise the specific database will be accessed.

## 2 The SQL Language

### 2.1 Populating a Table with Rows

You could also have used `copy` to load large amounts of data from <font color="#aa0">flat-text</font> files.

``` shell
COPY $TABLE_NAME FROM /path/to/file
```

> In some database systems, including older version <font color="#aa0">PostgreSQL</font>, the implementation of `DISTINCT` automatically orders the rows and so `ORDER BY` is unnecessary.

### 2.2 Identifiers and Key Words

Key words and unquoted identifiers are case *insensitive*.

A second kind of identifier: the *delimited identifier* or *quoted identifier*. It is formed by enclosing an arbitrary sequence of characters in double-quotes(`"`).

### 2.3 Positional Parameters

A positional parameter reference is used to indicate a value that is supplied externally to an SQL statement.

``` sql
CREATE FUNCTION dept(text) RETURNS dept
	AS $$ SELECT * FROM dept WHERE name = $1 $$
	LANGUAGE SQL;
```

> `$number` is a positional parameter, which used in SQL function definitions and in prepared queries.

### 2.4 Type Casts

A type cast specifies a conversion from one data type to another

``` sql
CAST ( expression AS type)
expression::type
typename (expression)
```

### 2.5 Scalar Subqueries

A <font color="#aa0">scalar subquery</font> is an ordinary `SELECT` query in parentheses that returns exactly one row with one column. The `SELECT` query is executed and the single returned value is used in the surrounding value expression.

``` sql
SELECT name, (SELECT max(pop) FROM cities WHERE cities.state = states.name) FROM states;
```

### 2.6 Array Constructors

An array constructor is an expression that builds an array value using value for its member elements. A simple array constructor consists of the key word `ARRAY`, a left square bracket `[,` a list of expressions (separated by commas) for the array element values, and finally a right square bracket `]`.

``` sql
SELECT ARRAY[1,2,3+4];
```

It is possible to construct an array from the results of a subquery. In this form, the array constructor is written with the key word `ARRAY` followed by a parenthesized subquery.

``` sql
SELECT ARRAY(SELECT oid FROm pg_proc WHERE proname LIKE 'bytea%');
```

### 2.7 Calling Functions

PostgreSQL allows functions that have named parameters to be called using either *positional* or *named* notation. *Named* notation is especially for functions that have a large number of parameters, since it makes the associations between parameters and actual arguments more explicit and reliable.

``` sql
CREATE FUNCTION concat_lower_or_upper(a text, b text, uppercase boolean DEFAULT false)
RETURNS text
$$
SELECT CASE WHEN $3 THEN UPPER ($1 || ' ' ||$2)
ELSE LOWER($1 || ' ' || $2)
END;
$$
LANGUAGE SQL IMMUTABLE STRICT;
```

> Notice:
>
> ***Named*** notation:
>
> ``` sql
> SELECT concat_lower_or_upper(a => 'hello', b => 'world');
> ```
>
> > Named and mixed call notations currently cannot be used when calling an aggregate function.

### 2.8 Generated Columns

A <font color="#aa0">generated column</font> is a special column that is always computed from other columns. 

Two kinds of generated columns: *<font color="#aa0">stored</font>* and *<font color="#aa0">virtual</font>*.

- *stored* generated column is <u>computed when it is written (inserted or updated)</u> and occupies storage as if it were a normal column
- *virtual* generated column occupies no storage when it is *read*

To create a generated column, use the `GENERATED ALWAYS AS` clause in `CREATE TABLE`

``` sql
CREATE TABLE people (
	height_cm numeric,
    height_in numeric GENERATED ALWAYS AS (height_cm / 2.54) STORED
)
```

Additional considerations apply to the use of generated columns,

- maintain *access privileges* separately from their underlying base columns
- updated after `BEFORE` triggers have run. therefore, changes made to base columns in a `BEFORE` trigger will be reflected in a generated columns. But conversely, it is not allowed to access generated columns in `BEFORE` triggers.

### 2.9 Constraints

Constraints give you as much control over the data in your tables as you wish.

A check constraint is the most generic constraint type. It allows you to specify that the value in a certain column must satisfy a Boolean (true-value) expression.

``` sql
CREATE TABLE products (
	product_no integer,
    name text,
    price numeric CHECK (price > 0)
)
```

### 2.10 System Columns

Every table has several *system columns* that are implicitly defined by the system. Therefore, these names cannot be used as names of user-defined columns.

- `tableoid` - the **OID** of the table containing this row. Without it, it's difficult to tell which individual table a row came from.

- `xmin` - The identity (<font color="#aa0">transaction id</font>) of the inserting transaction for this row version.

- `xmax` - The identity (<font color="#aa0">transaction id</font>) of the deleting transaction, or zero for an undeleted row version.

- `cmin` - The command identifier (starting at zero) within the inserting transaction.

- `cmax` - The command identifier within the deleting transaction, or zero

- `ctid` - the <font color="#aa0">physical location</font> of the row version within its table.

  > ***Note***
  >
  > Although `ctid` can be used to locate the row version very quickly, a row's `ctid` will change if it is updated or moved by `VACUUM FULL`. Therefore `ctid` is useless as long-term row identifier.

### 2.11 Privileges

When an object is created, it is assigned an owner. The owner is normally the *<font color="#aa0">role</font>* that executed the creation statement.

There are different kinds of privileges: `SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER, CREATE, CONNECT, TEMPORARY, EXCEUTE,` and `USAGE`.

To <font color="#aa0">revoke</font> a previously-granted privilege, use the fittingly named `REVOKE` command

``` sql
REVOKE ALL ON accounts FROM PUBLIC;
```

>The special "ROLE" named `PUBLIC` can be used to grant a privilege to every role on the system.

### 2.12 Row Security Policies

PostgreSQL, also allowed tables can have *<font color="#aa0">row security policies</font>* that restrict, on a per-user basis, which rows can be returned by normal queries or inserted, updated, or deleted by data modification commands. This is also known as *<font color="#aa0">Row-Level Security</font>*.

``` SQL
ALTER TABLE ... ENABLE ROW LEVEL SECURITY
```

Operations that apply to the whole table, such as `TRUNCATE` and `REFERENCES`, are not subject to row security. *Row security policies* can be specific to *commands, or to roles*, or to both. A policy can be specified to apply to `ALL` commands, or to `SELECT`,`INSERT`,`UPDATE`, or `DELETE`.

If no role is specified, or the special user name `PUBLIC` is used, then the policy applies to all users on the system. To allow all users to access only their own row in a `users` table, a simple policy can be used:

``` sql
CREATE POLICY user_policy ON users
	USING (user_name = current_user);
```

### 2.13 Schemas

A database contains one ore more named *<font color="#aa0">schemas</font>*, which in turn contain tables.

Several reasons why one might want to use schemas:

- Allow many users to use one <u>database without interfering with each other</u>.
- Organize database objects into <u>logical groups to make them more manageable</u>.
- <u>Third-party applications can be put into separate schemas</u> so they do not collide with the names of other objects.

<u>**Creating Schema**</u>

``` sql
CREATE SCHEMA $schema_name;
```

>**Note:**
>
>Actually, a more general syntax:
>
>```sql
>database.schema.table
>```

If you wan to create a schema <font color="#aa0">owned by</font> someone else (since this is one of ways to restrict the activities of your users to well-defined namespaces).

``` sql
CREATE SCHEMA $schema_name AUTHORIZATION $user_name;
```

> if $user_name is absent, then current user will be set as an authorized user.

The system determines which table is meant by following a *search path*, which is a list of schemas to look in. The first matching table in the search path is taken to be the one wanted.

To show the current search path, use the following command

``` Sql
SHOW search_path;
```

<u>**Usage Patterns**</u>

Schemas can be used to organize your data in many ways. A *secure schema usage pattern* prevents *<font color="#aa0">untrusted</font>* users from changing the behavior of the other users' queries.

- Constrain ordinary users to <font color="#aa0">user-private</font> schemas. To implement this, issue `REVOKE CREATE ON SCHEMA public FROM PUBLIC`, and create a schema for each user with the same name as that user.
- Remove the public schema from the default search path, by modifying `postgresql.conf` or by issuing `ALTER ROLE ALL SET search_path = "$user"`.
- keep the default.

### 2.14 Partition

<font color="#aa0">Partitioning</font> refers to splitting which is logically one large table into smaller physical pieces, it has several benefits:

- *Query performance* can be improved dramatically in certain situations, particularly when most of the heavily accessed rows of the table are in a single partition or a small number of partitions.
- When *queries or updates* access a large percentage of a single partition, performance can be improved by using a *sequential scan of that partition* instead of using an index, which would require random-access reads scattered across the whole table.
- *Bulk loads and deletes* can be accomplished by adding or removing partitions, if the usage pattern is accounted for in the partitioning design.
- *Seldom-used* data can be migrated to cheaper and slower storage media.

Built-in support for the following forms of partitioning,

- <font color="#aa0">Range Partitioning</font>: The table is partitioned into "ranges" defined by a key column or set of columns, with no overlap between ranges of values assigned to different partitions.
- <font color="#aa0">List Partitioning</font>: The table is partitioned by explicitly listing which key value(s) appear in each partition.
- <font color="#aa0">Hash Partitioning</font>: the table is partitioned by specifying a modules and a reminder for each partition.

To use declarative partitioning in this case, use the following steps:

1. Create the table as a partitioned table by specifying the `PARTITION BY` clause

   ``` sql
   CREATE TABLE measurement(
   	city_id int not null,
       logdate data not null,
       peaktemp int
   ) PARTITIONED BY (logdate);
   ```

2. Create partitions

   ``` sql
   CREATE TABLE measurement_y2006m02 PARTITION OF measurement
   	FOR VALUES FROM ('2006-02-01') TO ('2006-03-01');
   ```

3. Create an index on column(s)

4. ensure that `enable_partition_pruning` configuration parameter is not disabled in `postgres.conf`.

<u>**Partition Maintenance**</u>

The simplest option for *removing old data* is to drop the partition that is no longer necessary

``` sql
DROP TABLE measurement_y2006m02;
```

Another option that is often preferable is to remove the partition from the partitioned table but <font color="#aa0">*retain access*</font> to it as a table in its own right

``` sql
ALTER TABLE measurement DETACH PARTITION measurement_y2006m02;
```

### 2.15 Returning Data from Modified Rows

The `INSERT`, `UPDATE`, and `DELETE` commands all have an optional `RETURNING` clause that support this. Use of `RETURNING` avoids performing an extra database query to collect the data, and is especially valuable when it would  otherwise be difficult to identify the modified rows reliably.

``` sql
INSERT INTO $table_name VALUES () RETURNING id;
```

### 2.16 Queries

The general syntax of the `SELECT` command is

``` sql
[WITH $with_queries] SELECT $select_list FROM $table_expression [ $sort_specification ]
```

If a table has been grouped using `GROUP BY`, but only groups are of interest, the `HAVING` clause can be used, much like a `WHERE` clause, to eliminate groups from the result.

``` sql
SELECT $select_list FROM ... [ where ... ] GROUP BY ... HAVING $bool_expression
```

<font color="#aa0">*grouping sets*</font>, which data is selected by the `FROM` and `WHERE` clause is grouped separately by each specified grouping set, aggregates computed for each group just as for simple `GROUP BY` clauses, and then the results returned.

``` SQL
SELECT * FROM items_sold;
brand |  size | sales
------+-------+-------
Foo   |  L    | 10
Foo   |  M    | 20
Bar   |  L    | 15
Bar   |  M    | 5

SELECT brand, size, SUM(sales) FROM items_sold GROUP BY GROUPING SETS ((brand), (size), ());
brand |  size | sum
------+-------+-------
Foo   |       | 30
Bar   |       | 20
      |  L    | 25
      |  M    | 25
      |       | 50
```

> An empty grouping set means that all rows are aggregated down to a single group

A shorthand notation is provided for specifying two common types of grouping set.

``` sql
ROLLUP (e1, e2, e3)
```

<u>**equivalent to**</u>

``` sql
GROUPING SETS (
	(e1, e2, e3),
    (e1, e3),
    (e1, e2),
    (e2, e3),
    (e3),
    (e2),
    (e1),
    ()
)
```

`CUBE` given list and all of its possible subsets, thus

``` sql
CUBE (a, b, c)
```

**equivalent to**

``` sql
GROUPING SETS (
	(a, b, c),
    ( a, b    ),
    ( a,    c ),
    ( a       ),
    (    b, c ),
    (    b    ),
    (       c ),
    (         )
)
```

`WITH` provides a way to write auxiliary statements for use in a larger query. These statements, which are often referred to as <font color="#aa0">Common Table Expressions</font> or <font color="#aa0">CTEs</font>, can be thought of as defining temporary tables that exist just for one query.

The optional `RECURSIVE` modifier changes `WITH` from a mere syntactic convenience into a feature that accomplishes things not otherwise possible in standard SQL. Using `RECURSIVE`, a `WITH` query can <font color="#aa0">refer</font> to its own output.

``` sql
WITH RECURSIVE t(n) AS (
	VALUES (1)
    UNION ALL
    SELECT n + 1 FROM t WHERE n < 100
) SELECT sum(n) FROM t;
```

When computing a tree traversal using a <font color="#aa0">recursive</font> query, you might want to order the results in either <font color="#aa0">depth-first</font> or <font color="#aa0">breadth-first</font> order. This can be done by computing an ordering column alongside the other data columns and using that to sort the results at the end. 

**depth-first**

``` sql
WITH RECURSIVE search_tree(id, link, data, path) AS (
	SELECT t.id, t.link, t.data, ARRAY[t.id]
    FROM tree t
    UNION ALL
    SELECT t.id, t.link, t.data, path || t.id
    FROM tree t, search_tree st
    WHERE t.id = st.link
) SELECT * FROM search_tree ORDER BY path;
```

**breadth-first**

```sql
WITH RECURSIVE search_tree(id, link, data, depth) AS (
	SELECT t.id, t.link, t.data, 0
    FROM tree t
    UNION ALL
    SELECT t.id, t.link, t.data, depth + 1
    FROM tree t, search_tree st
    WHERE t.id = st.link
) SELECT * FROM search_tree ORDER BY depth;
```

### 2.17 Data Types

> There is no performance difference among these three types (text, char(***n***), varchar(***n***)), apart from increased storage storage space when using the blank-padded type. In <font color="#aa0">postgreSQL</font>, in fact `character(n)`, or saying `char(n)` is usually the slowest of the three because of its additional storage costs.

> The SQL standard requires that writing just `timestamp` be equivalent to `timestamp without time zone`, and PostgreSQL honors that behavior. `timestamptz` is accepted as an abbreviation for `timestamp with time zone`; this is a <font color="#aa0">PostgreSQL</font> extension.

The `interval` type has an additional option, which is to restrict the set of stored fields by writing one of these phrases,

- `YEAR` 
- `MONTH`
- `DAY`
- `HOUR`
- `MINUTE`
- `SECOND`
- `YEAR TO MONTH`
- `DAY TO HOUR`
- `DAY TO SECOND`
- `HOUR TO MINUTE`
- `HOUR TO SECOND`
- `MINUTE TO SECOND`

<u>**XML Type**</u>

The `xml` data type can be used to store XML data. Its advantage over storing XML data in a `text` field is that it checks the input values for well-formedness, and there are support functions to perform type-safe operations on it.

*Creating XML values*

``` sql
XMLPARSE ( { DOCUMENT | CONTENT } $value )
xml '<foo>bar</foo>'
'<foo>bar</foo>'::xml
```

The *inverse* operation, producing a character string value from `xml`, uses the function `xmlserialize`

``` sql
XMLSERIALIZE ( { DOCUMENT | CONTENT } $value AS $TYPE )
```

> Here, `$TYPE` can be `character`, `character varing`, or `text`

**<u>JSON Type</u>**

<font color="#aa0">PostgreSQL</font> offers two types for storing JSON data: `json` and `jsonb`. To implement efficient query mechanisms for these data types, PostgreSQL also provides the `jsonpath` data type.

- `json` data type stores an exact copy of the input text, which processing functions must <font color="#aa0">reparse</font> on each execution
- `jsonb` data is stored in a <font color="#aa0">decomposed</font> binary format that makes it *slightly slower* to input, but significantly *faster* to process, since ***NO*** <font color="#aa0">reparsing</font> is needed.

In general, most applications should *prefer* to store JSON data as `jsonb`, unless there are quite specialized needs, such as legacy assumptions about ordering of object keys.

`jsonb` has an *existence* operator, which is a variation on the theme of containment. It tests whether a string (given as a `text` value) appears as an object key or array element at the ***top level*** of the `jsonb` value.

## 3 Advanced Features

### 3.1 Transactions

*<font color="#aa0">Transactions</font>* are a fundamental concept of all database systems. The essential point of a transaction is that it bundles multiple steps into a <font color="#aa0">*single, all-or-nothing*</font> operation. Intermediate states are not visible.

> When multiple transactions are running concurrently, each one should *NOT* be able to see the incomplete changes made by others.

A transaction is setup by surrounding the SQL commands of the transaction with `BEGIN` and `COMMIT` commands.

``` sql
BEGIN:
UPDATE accounts SET balance = balance - 100.00 WHERE name = 'alice';
-- etc.
COMMIT;
```

<font color="#aa0">*savepoints*</font> is a method to control the statements in a <font color="#aa0">Transaction</font> in a more granular fashion. It allows you to ***selectively discard*** part of the transaction, while committing the rest.

Using `SAVEPOINT` define a savepoint, and you can rollback to the savepoint with `ROLLBACK TO`.

``` sql
BEGIN:
UPDATE accounts SET balance = balance - 100.00 WHERE name = 'alice';
SAVEPOINT first_savepoint;
UPDATE accounts SET balance = balance + 100.00 WHERE name = 'Blob';
-- oops 
ROLLBACK TO first_savepoint;
COMMIT;
```

### 3.2 Window Functions

A <font color="#aa0">*window function*</font> performs a *calculation across* a set of table rows that are somehow related to the current row. <font color="#aa0">Window functions</font> do not cause rows to become grouped into a single output row like non-window aggregate calls would. 

A window function call always contains an `OVER` clause directly following the window function's name and argument(s). The `OVER` clause determines exactly how the rows of the query are split up for processing by the window function.

``` sql
SELECT depname, empno, salary, rank() OVER (PARTITIONED BY depname ORDER BY salary DESC) FROM empsalary;
```

The <font color="#aa0">syntax</font> of a window function calls is one of the following:

``` 
function_name ([expression [, expression...]]) [FILTER (WHERE filter_clause)] OVER window_name
function_name ([expression [, expression...]]) [FILTER (WHERE filter_clause)] OVER (window_definition)
function_name (*) [FILTER (WHERE filter_clause)] OVER window_name
function_name (*) [FILTER (WHERE filter_clause)] OVER (window_definition)
```

where *`window_definition`* has the syntax

```
[existing_window_name]
[PARITION BY expression [, ...]]
[ORDER BY expression [ASC | DESC | USING operator] [NULLS { FIRST | LAST}] [, ...]]
[frame_clause]
```

The optional *`frame_clause`* can be one of

```
{ RANGE | ROWS | GROUPS } frame_start [ frame_exclusion ]
{ RANGE | ROWS | GROUPS } BETWEEN frame_start AND frame_end [ frame_exclusion ]
```

> `frame_clause` specifies the set of rows constituting the *window frame*, which is a <font color="#aa0">subset</font> of the current partition.
>
> three modes:
>
> - `RANGE`
> - `ROWS`
> - `GROUPS`
>
> runs from the *`frame_start`* to the *`frame_end`* (defaults to CURRENT ROW if it is omitted)

here *`frame_start`* and *`frame_end`* can be one of 

```
UNBOUNDED PRECEDING
offset PRCEDING
CURRENT ROW
offset FOLLOWING
UNBOUNDED FOLLOWING
```

> - *`frame_start`* of `UNBOUNDED PRECEDING` means that the frame starts with the first row of the partition
>
> - *`frame_end`* of `UNBOUNDED FOLLOWING` means that the frame ends with the last row of the partition
>
> - `CURRENT ROW`
>
>   - IN `RANGE` OR `GROUPS` mode, *`frame_start`* means frame starts with the current row's first *<font color="#aa0">peer</font>* row, *`frame_end`* means ends with current row's last peer row
>   - In `ROWS` mode, just means the current row.
>
>   > *`offset`* PRECEDING and *`offset`* FOLLOWING frame options, depends on the frame mode:
>   >
>   > - `ROWS` mode, *`offset`* must yield a <font color="#aa0">non-null, non-negative integer</font>, and the option means that the frame starts or ends the specified number of rows before or after the current row
>   > - `RANGE` mode, options require that the `ORDER BY` clause specify exactly one column.
>   > - `GROUPS` mode, the *`offset`* again must yield a <font color="#aa0">non-null, non-negative integer</font>, and the option means that the frame starts or ends the specified number of *peer groups* before or after the current row's peer group. (MUST be an `ORDER BY` clause in the window definition)

and *`frame_exclusing`* can be one of 

```
EXCLUDE CURRENT ROW
EXCLUDE GROUP
EXCLUDE TIES
EXCLUDE NO OTHERS
```

A full `window_definition` using the same syntax as for defining a named window in the `WINDOW` clause. Must point out that `OVER wname` is not exactly equivalent to `OVER (wname ...)`; the latter implies copying and modifying the window function, and will be rejected if the referenced window specification includes a frame clause.

Without `PARITION BY`, all rows produced by the query are treated as a <font color="#aa0">single</font> partition.

### 3.3 inheritance

``` sql
CREATE TABLE cities (
	name text,
    population real,
    elevation int
);

CREATE TABLE capitals (
	state char(2) UNIQUE NOT NULL
) INHERITS (cities);
```

In this case, a row of `capitals` *inherits* all columns

> Although the inheritance is frequently helpful, it has not been integrated with unique constraints or foreign keys, which limits its usefulness.

## 4 Administration

### **4.1 Logical replication**

a method of <font color="#a0a">replicating</font> data objects and their changes, based upon their replication identity (usually a primary key).

term logical in contrast to <font color="#a00"><u>physical replication</u></font>, which uses exact block addresses and byte-by-byte replication. It allows fine-grained control over both <font color="#a0a">*data replication and security*</font>.

Logical replication uses a *publish* and *subscribe* model with <font color="#a0a">one or more *subscribers*</font> subscribing to <font color="#a0a">one or more *publisher*</font> node. Subscribers <u>pull</u> data from the publications they subscribe to and may subsequently <u>re-publish</u> data to allow cascading replication or more complex configurations.

Logical replication of a table typically starts with <u>taking a snapshot</u> of the data on the publisher database and copying that to the subscriber. Once that is done, the changes on the publisher are sent to subscriber as they occur in <font color="#a0a">real-time</font>.

> subscriber applies the data in the same order as the publisher so that <font color="#a0a">transaction consistency</font> is guaranteed.
>
> > sometimes referred to as transactional replication.

#### **4.1.1 Publication**

The node where a publication is defined is referred to as *publisher*. A publication is a set of changes generated from a table or a group of tables, and might also be described as a change set or replication set.

<u>Each table can be added to multiple publications if needed</u>. Publications may currently only contain tables. Objects must be added explicitly, except when a publication is created for *ALL TABLES*.

A published table must have a "replica identity" configured in order to be able to replicate *UPDATE* and *DELETE* operations, so that appropriate rows to update or delete can be identified on the subscriber side. By default, this is the primary key.

> see <font color="#aa0">REPLICA IDENTITY</font> for details

A publication is created using the <font color="#a0a">CREATE PUBLICATION</font> command and my later be altered or dropped using corresponding commands.

The individual tables can be added or removed dynamically by using <font color="#a0a">ALTER PUBLICATION</font> command.

#### **4.1.2 Subscription**

A *subscription* is the downstream side of logical replication. The node where a subscription is defined is referred to as the *subscriber*. It defines the <u>*connection*</u> to another database and set of replications (one or more) to which it wants to subscribe.

> A subscriber node may have multiple subscriptions if desired. Each subscription will receive changes via one <font color="#aa0">replication slot</font>.
>
> > Subscriptions are dumped by `pg_dump` if the current user is a superuser. Otherwise a warning is written and subscriptions are skipped, because non-superusers cannot read all subscription information from the <font color="#aa0">pg_subscription</font> catalog.

The subscription is added using `CREATE SUBSCRIPTION` and can be stopped/resumed at any time using the `ALTER SUBSCRIPTION` command and removed using `DROP SUBSCRIPTION`.

>The tables are matched between the publisher and the subscriber using the full qualified table name. Different-named tables' replication is not supported.
>
>> columns of a table are also matched by name, order and type of the column are not taken into match.

Each (active) subscription receives changes from a replication slot on the remote (publishing) side. Normally, the remote replication slot is created automatically when the subscription is created using `CREATE SUBSCRIPTION` and it is dropped automatically when the subscription is dropped using `DROP SUBSCRIPTION`.

#### **4.1.3 Conflicts**

If incoming data violates any constraints the replication will stop. This is referred to as a *conflict*.

> A conflict will produce an error and will stop the replication.
>
> The resolution can be done either by changing data on the subscriber so that it does not conflict with the incoming change or by skipping the transaction that conflicts with the existing data.
>
> - transaction can be skipped by calling the `pg_replication_origin_advance()` function with a `node_name` corresponding to the subscription name, and a position.
> - the current position of origins can be seen in the `pg_replication_origin_status` system view 

#### **4.1.4 Restrictions**

Logical replication currently has the following restrictions or missing functionality.

- the database schema and DDL commands are not replicated. The initial schema can be copied by hand using `pg_dump --schema-only`. Subsequent schema changes would need to be kept in <u>sync manually</u>.

- Sequence data is not replicated. The data in serial or identity columns backed by sequences will of course be replicated as part of the table, but the sequence itself would still show the start value on the subscriber

- Replication of `TRUNCATE` commands is supported, but some care must be taken when truncating groups of tables connected by foreign keys.

  > Work correctly if all affected tables are part of the same subscription. Otherwise, not.

- Large objects are not replicated, no workaround for that

- Replication is only supported by tables, including partitioned tables.

- When replicating between partitioned tables, the actual replication originates, by default, from the leaf partitions on the publisher, so partitions on the publisher must also exist on the subscriber as valid target tables.

### 4.2 Configuration

#### 4.2.1 max_connections

> Using `SHOW max_connections` to check how many connections are allowed to be made to PostgreSQL.
>
> If an error `sorry, too many clients already` arise, then change setting `max_connections` to a larger number in <font color="#aa0">postgresql.conf</font>. 

### 4.3 Operations

#### 4.3.1 Dump the specified database

The `pg_dump` command is used to dump the specified single database, the command will be like followings:

``` sql
pg_dump -s -p 5432 $DATABASE_NAME > /path/to/sql
```

> Note
>
> <font color="#aa0">$DATABASE_NAME</font> is the name of the database that you wanna dump all its tables' structure out.

## X Appendix

### X1 Failure: could not locate a valid checkpoint record

> <b>Solution</b>:
>
> using command `docker run -it -v /root/postgres:/var/lib/postgresql/data postgres /bin/bash` to temporarily start a Postgres server, then using super account `postgres`, and execute the command `pg_resetwal -f /var/lib/postgresql/data` to reset the log.
>
> > <i>Notice</i>:
> >
> > `/root/postgres` is the data directory on the host machine