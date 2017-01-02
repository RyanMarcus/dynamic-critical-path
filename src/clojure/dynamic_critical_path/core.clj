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
(ns dynamic-critical-path.core
  (:use [clojure.set])
  (:use [dynamic-critical-path.util])
  (:gen-class))

(set! *warn-on-reflection* true)


(defn generateDAG [v-weights edges]
  (->DAG v-weights
        (reduce #(let [m %1 k (first %2) e (rest %2)]
                   (assoc m k (conj (get m k '()) e)))
                {}
                edges)))


(defn dynamic-critical-path
  "Clusters the vertices of the dag according to the DCP algorithm"
  ([dag] (dynamic-critical-path dag (allVertices dag) '(())))
  ([dag candidates clusters]
   (cond (empty? candidates) (set (filter #(not= % '()) clusters))
         :else
         (let [next (next-to-schedule candidates dag)
               next-vertex (first next)
               next-child (second next)
               best (evaluate-clusters next-vertex next-child clusters dag)
               best-cluster (first best)
               best-next-dag (second best)
               new-cluster (sort-cluster-by-topo (conj best-cluster next-vertex)
                                                 dag)
               new-clusters (conj (filter #(and
                                            (not= % best-cluster)
                                            (not= % '()))
                                         clusters)
                                  new-cluster '())]

           (recur best-next-dag
                                  (set (filter #(not= % next-vertex)
                                               candidates))
                                  new-clusters))))
  
  )
