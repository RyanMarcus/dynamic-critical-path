# dynamic-critical-path

![Codeship status](https://codeship.com/projects/8bcaf1a0-b45f-0134-be92-665c05a5a8f8/status?branch=master) [![Clojars Project](https://img.shields.io/clojars/v/dynamic-critical-path.svg)](https://clojars.org/dynamic-critical-path)

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
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
<dependency>
  <groupId>dynamic-critical-path</groupId>
  <artifactId>dynamic-critical-path</artifactId>
  <version>0.3.0</version>
</dependency>
```


## Usage

The `generateDAG` function takes in a map of vertices (strings) to weights (integers) and returns a DAG. The `dynamic-critical-path` function takes in that DAG and returns a set of clusters, where each cluster corresponds to a processor. 

Clojure example:

```clojure
(def allEdges '(("A" "B" 5)
                ("B" "D" 6)
                ("A" "C" 4)
                ("C" "D" 7)))

(def vertexWeights {"A" 3 "B" 9 "C" 10 "D" 4})


(def dag (generateDAG vertexWeights allEdges))
(println (dynamic-critical-path dag))
```

Java example:

```java
Map<String, Integer> weights = new HashMap<>();
weights.put("A", 3);
weights.put("B", 9);
weights.put("C", 10);
weights.put("D", 4);

Map<String, Map<String, Integer>> edges = new HashMap<>();

Map<String, Integer> toAdd = new HashMap<>();
toAdd.put("B", 5);
toAdd.put("C", 4);
edges.put("A", toAdd);

toAdd = new HashMap<>();
toAdd.put("D", 6);
edges.put("B", toAdd);

toAdd = new HashMap<>();
toAdd.put("D", 7);
edges.put("C", toAdd);


System.out.println(DynamicCriticalPath.schedule(weights, edges));
```

## Performance

This implementation is parallel and will absolutely soak up 100% of your CPU on a sufficiently large DAG. Kwok and Ahmad show that the algorithm runs in `O(v^3)` time. I find that this code takes almost an hour to process a DAG with around 400 nodes.
