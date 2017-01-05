# dynamic-critical-path

![Codeship status](https://codeship.com/projects/8bcaf1a0-b45f-0134-be92-665c05a5a8f8/status?branch=master)

A Clojure library that implements the dynamic critical path (DCP) scheduling algorithm for DAGs. There's also a Java wrapper, so you can use this in Java projects seemlessly.

The dynamic critical path algorithm makes the following assumptions:

* You have a task dependency graph which is directed and acyclic.
* Each vertex has a weight representing it's processing time
* Each edge `(u, v)` has a weight representing the cost to be paid if `u` and `v` are not scheduled on the same processor.

The algorithm is described in detail in the following paper:

Y. Kwok and I. Ahmad, “Dynamic critical-path scheduling: an effective technique for allocating task graphs to multiprocessors,” IEEE Transactions on Parallel and Distributed Systems, vol. 7, no. 5, pp. 506–521, May 1996.

## Installation

With `leiningen` (for Clojure):

```
[dynamic-critical-path "0.3.0"]
```

... or with Maven (for Java):

```xml
<dependency>
  <groupId>dynamic-critical-path</groupId>
  <artifactId>dynamic-critical-path</artifactId>
  <version>0.3.0</version>
</dependency>
```


## Usage

The `generateDAG` function takes in a map of vertices (strings) to weights (integers) and returns a DAG. The `dynamic-critical-path` function takes in that DAG and returns a set of clusters, where each cluster corresponds to a processor. 
