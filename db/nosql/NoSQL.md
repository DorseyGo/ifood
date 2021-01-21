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