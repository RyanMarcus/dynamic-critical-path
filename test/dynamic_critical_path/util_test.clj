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
(ns dynamic-critical-path.util-test
  (:require [clojure.test :refer :all]
            [dynamic-critical-path.util :refer :all]
            [dynamic-critical-path.core :refer :all]))

(def allEdges '(("A" "B" 5)
                ("B" "D" 6)
                ("A" "C" 4)
                ("C" "D" 7)))

(def vertexWeights {"A" 3 "B" 9 "C" 10 "D" 4})


(def dag (generateDAG vertexWeights allEdges))


(deftest basic-dag-operations
  (testing "basic DAG operations"
    (is (= (count (allVertices dag)) 4) "all-vertices has correct count")
    (is (= (getChildren "C" dag) #{"D"}) "get-children returns single child")
    (is (= (getChildren "A" dag) #{"B" "C"}) "get-children returns multiple children")
    (is (= (getParents "A" dag) #{}) "get-parents is empty for root")
    (is (= (getParents "B" dag) #{"A"} ) "get-parents for single parent")
    (is (= (getParents "D" dag) #{"B" "C"}) "get-parents for multiple parents")
    (is (= (getEdgeWeight "A" "B" dag) 5) "get-edge-weight correct value")
    (is (= (getEdgeWeight "C" "D" dag) 7) "get-edge-weight correct value")
    (is (= (getEdgeWeight "D" "A" dag) nil) "get-edge-weight missing value")
    (is (= (getWeight "A" dag) 3) "get-weight correct value")
    (is (= (getWeight "Q" dag) nil) "get-weight missing value")
    ))

(deftest dag-mutations
  (testing "DAG mutations"
    (is (= (count (allVertices (removeVertex "A" dag))) 3) "remove vertex")
    (is (= (count (allVertices (removeVertex "Q" dag))) 4) "remove fake vertex")
    (is (= (getEdgeWeight "A" "B" (removeVertex "A" dag)) nil) "removing vertex removes edges")

    (is (= (getEdgeWeight "A" "B" (remove-edge "A" "B" dag)) nil) "remove edge")
    (is (= (getEdgeWeight "A" "D" (add-edge "A" "D" 20 dag)) 20) "add edge")
    
    (is (= (getEdgeWeight "A" "B" (zero-edge "A" "B" dag)) 0) "zero existing edge")
    (is (= (getEdgeWeight "A" "D" (zero-edge "A" "D" dag)) 0) "zero new edge")
    ))

(deftest dag-operations
  (testing "DAG operations"
    (is (= (getEntryVertices dag) #{"A"}) "entry vertices")
    (is (= (getEntryVertices (removeVertex "A" dag)) #{"B" "C"}) "entry vertices")

    (is (= (count (toposort dag)) 4) "toposort result size")
    (is (= (first (toposort dag)) "A") "toposort first result")
    (is (= (last (toposort dag)) "D") "toposort last result")
    ))

(deftest utility-functions
  (testing "utility functions"
    (is (= (sort-cluster-by-topo '("C" "D" "A") dag) '("A" "C" "D")) "sort-cluster-by-topo")
    ))

(deftest dcp-operations
  (testing "t- and b-level"
    (is (= (count (t-levels dag)) 4) "t-levels count")
    (is (= (t-levels dag) {"A" 0 "C" 7 "B" 8 "D" 24}) "t-levels values")

    (is (= (count (b-levels dag)) 4) "b-levels count")
    (is (= (b-levels dag) {"D" 4, "B" 19, "C" 21, "A" 28}) "b-levels values")

    (is (= (first (priorities dag)) "B") "first priority")

    (is (= (next-to-schedule (allVertices dag) dag) '("B" "D")) "next-to-schedule"))

  (testing "scheduling ops"
    (is (= (next-to-schedule #{"A" "C" "D"} dag) '("A" "B")) "next-to-schedule")
    (is (= (next-to-schedule #{"A" "B"} dag) '("B" "D")) "next-to-schedule")
    (is (= (next-to-schedule #{"D"} dag) '("D" nil)) "next-to-schedule")

    (let [result (evaluate-cluster "B" "D" '() dag)
          cluster (first result)
          value (second result)
          dag (second (rest result))]
      (is (= cluster '()) "correct cluster selected")
      (is (= value 32) "found correct value for cluster")
      (is (= (count (allVertices dag)) 4) "vertex count valid"))

    (let [result (evaluate-cluster "C" "D" '("B") dag)
          cluster (first result)
          value (second result)
          dag (second (rest result))]
      (is (= cluster '("B")) "correct cluster selected")
      (is (= value 39) "found correct value for cluster")
      (is (= (count (allVertices dag)) 4) "vertex count valid"))

    (let [result (evaluate-clusters "C" "D" '(("B") ()) dag)
          cluster (first result)
          res-dag (second result)]
      (is (= cluster '()) "selected correct cluster")
      (is (= (count (allVertices res-dag)) 4) "vertex count valid"))

    (let [result (evaluate-clusters "A" "B" '(("B") ("C") ()) dag)
          cluster (first result)
          res-dag (second result)]
      (is (= cluster '("B")) "selected correct cluster")
      (is (= (count (allVertices res-dag)) 4) "vertex count valid")
      (is (= (getEdgeWeight "A" "B" res-dag) 0) "edge zero'd"))
    ))

