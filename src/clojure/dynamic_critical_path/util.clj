; < begin copyright > 
; Copyright Ryan Marcus 2017
; 
; This file is part of dynamic-critical-path.
; 
; dynamic-critical-path is free software: you can redistribute it and/or modify
; it under the terms of the GNU General Public License as published by
; the Free Software Foundation, either version 3 of the License, or
; (at your option) any later version.
; 
; dynamic-critical-path is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU General Public License for more details.
; 
; You should have received a copy of the GNU General Public License
; along with dynamic-critical-path.  If not, see <http://www.gnu.org/licenses/>.
; 
; < end copyright > 
; < begin copyright > 
; Copyright Ryan Marcus 2017
; 
; This file is part of dynamic-critical-path.
; 
; dynamic-critical-path is free software: you can redistribute it and/or modify
; it under the terms of the GNU General Public License as published by
; the Free Software Foundation, either version 3 of the License, or
; (at your option) any later version.
; 
; dynamic-critical-path is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU General Public License for more details.
; 
; You should have received a copy of the GNU General Public License
; along with dynamic-critical-path.  If not, see <http://www.gnu.org/licenses/>.
; 
; < end copyright > 
(ns dynamic-critical-path.util
  (:require [clojure.math.combinatorics :as combo])
  (:use [clojure.set]))

(defmacro mfilter [func m]
  `(into {} (filter ~func ~m)))

(defmacro mmap [func m]
  `(into {} (map ~func ~m)))

(defrecord DAG [weights edges])


(defn allVertices [dag]
  (set (keys (.weights dag))))

(defn getChildren [vertex dag]
  (set (map first (get (.edges dag) vertex {}))))

(defn getParents [vertex dag]
  (set (filter #(contains? (getChildren % dag) vertex)
               (allVertices dag))))

(defn getEdgeWeight [parent child dag]
  (second (first (filter #(= child (first %)) (get (.edges dag) parent)))))

(defn getWeight [vertex dag]
  (get (.weights dag) vertex))

(defn removeVertex [vertex dag]
  (letfn [(eqVert [v] (not= (first v) vertex))]
    (->DAG (mfilter eqVert (.weights dag))
           (mfilter eqVert (apply hash-map
                                  (mapcat #(list (first %)
                                                 (filter eqVert (second %)))
                                          (.edges dag)))))))
(defn remove-edge
  [parent child dag]
  (let [edges (.edges dag)]
    (->DAG (.weights dag)
           (assoc edges parent (filter #(not= (first %) child)
                                       (get edges parent))))))

(defn add-edge
  [parent child weight inpdag]
  (let [dag (remove-edge parent child inpdag)
        edges (.edges dag)]
    (->DAG (.weights dag)
           (assoc edges parent (conj (get edges parent) (list child weight))))))

(defn zero-edge [parent child dag]
  (add-edge parent child 0 dag))

(defn getEntryVertices [dag]
  (clojure.set/difference (set (allVertices dag))
                          (set (mapcat #(getChildren % dag) (allVertices dag)))))

(defn toposort [dag]
  (let [entryPoints (getEntryVertices dag)]
    (cond (empty? entryPoints) '()
          :else (conj (toposort (removeVertex (first entryPoints) dag))
                      (first entryPoints)))))

(defn t-level-for-node [vertex tlist dag]
  (let [parents (getParents vertex dag)]
    (cond (empty? parents) 0
          :else  (reduce max
                         (map #(+ (get tlist % 0)
                                  (getWeight % dag)
                                  (getEdgeWeight % vertex dag))
                              parents)))))

(defn levels-recur [toplist tlist dag func]
  (cond (empty? toplist) tlist
        :else  (let [curr-vertex (first toplist)]
                 (levels-recur
                  (rest toplist)
                  (assoc tlist curr-vertex
                         (func curr-vertex tlist dag))
                  dag func))))

(defn t-levels [dag]
  (let [toplist (toposort dag)
        tlist (zipmap toplist (repeat 0))]
    (levels-recur toplist tlist dag t-level-for-node)))

(defn b-level-for-node [vertex blist dag]
  (let [children (getChildren vertex dag)
        my-weight (getWeight vertex dag)]
    (cond (empty? children) my-weight
          :else (+ (reduce max
                           (map #(+ (get blist % 0)
                                    (getEdgeWeight vertex % dag))
                                children))
                   my-weight))))

(defn b-levels [dag]
  (let [rtoplist (reverse (toposort dag))
        blist (zipmap rtoplist (repeat 0))]
    (levels-recur rtoplist blist dag b-level-for-node)))

(defn priorities 
  "Gets the vertices sorted in priority order. The priority is the sum of the t and b levels, breaking ties by selecting the smallest t level."
  [dag]
  (let [tlist (t-levels dag)
        blist (b-levels dag)
        mobility (merge-with #(+ %1 %2) tlist blist)]
    (sort-by (juxt #(get mobility %) #(get t-levels %))
             (allVertices dag))))

(defn next-to-schedule
  "The next node to schedule is the node with the smallest mobility, i.e. the smallest sum of t and b levels, breaking ties by selecting the smallest t level. This function returns the node with the highest priority and that node's critical child (the child node with the highest priority). If the node has no critical child, nil is used in its place. Only vertices listed in candidates are considered."
  [candidates dag]
  (let [p-list (priorities dag)
        applicable (filter #(contains? candidates %) p-list)
        highest-prior (first applicable)
        children (getChildren highest-prior dag)]
    (list highest-prior (first (filter #(contains? children %) p-list)))))


(defn sort-cluster-by-topo
  [cluster dag]
  (let [toplist (zipmap (toposort dag) (range))]
    (sort-by #(get toplist %) cluster)))

(defn evaluate-cluster
  "Gets the sum of the start times for the two tasks given if they were added to the given cluster. Returns three elements: the cluster, the sum of the start times and the new DAG with zeroed edges between the in-cluster nodes."
  [vertex child cluster dag]
  (let [sorted-cluster (sort-cluster-by-topo (conj cluster vertex) dag)
        sum-values (fn [m] (reduce + (vals m)))
        mod-dag (reduce #(zero-edge (first %2) (second %2) %1)
                        dag
                        (map #(sort-cluster-by-topo % dag)
                             (combo/combinations sorted-cluster 2)))]
    (list cluster
          (sum-values (select-keys (t-levels mod-dag) [vertex child]))
          mod-dag)))

(defn evaluate-clusters
  "Takes as input a vertex, the critical child of that vertex, and a set of clusters and returns: the cluster where placing the vertex would minimize the sum of the start times for the vertex and the critical child, and the updated DAG"
  [vertex child clusters dag]
  (let [candidates (map #(evaluate-cluster vertex child % dag) clusters)
        best-result (reduce #(cond (< (second %1) (second %2)) %1 :else %2)
                            candidates)]
    (list (first best-result) (second (rest best-result)))))
