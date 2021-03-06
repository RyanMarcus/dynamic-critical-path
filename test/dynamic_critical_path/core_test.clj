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
(ns dynamic-critical-path.core-test
  (:require [clojure.test :refer :all]
            [dynamic-critical-path.core :refer :all]
            [dynamic-critical-path.util :refer :all]))

(set! *warn-on-reflection* true)

(def allEdges '(("A" "B" 5)
                ("B" "D" 6)
                ("A" "C" 4)
                ("C" "D" 7)))

(def vertexWeights {"A" 3 "B" 9 "C" 10 "D" 4})


(def dag (generateDAG vertexWeights allEdges))


(deftest dcp-tests
  (testing "DCP"
    (is (= (dynamic-critical-path dag) #{'("A" "C" "B" "D")}) "DCP correct")

    (let [all-edges '(("A" "B" 5) ("B" "D" 600)
                      ("A" "C" 4) ("C" "D" 7))
          v-weights {"A" 3 "B" 9 "C" 10 "D" 4}
          dag (generateDAG v-weights all-edges)]
      (is (= (dynamic-critical-path dag) #{'("A" "C") '("B" "D")}) "DCP correct"))

    (let [all-edges '(("A" "B" 1) ("B" "C" 5) ("C" "D" 1)
                      ("C" "E" 3) ("E" "F" 3) ("F" "G" 2)
                      ("A" "G" 3))
          v-weights {"A" 4 "B" 2 "C" 1 "D" 900 "E" 3 "F" 6 "G" 7}
          dag (generateDAG v-weights all-edges)]
      (is (= (dynamic-critical-path dag) #{'("A" "B" "C" "E" "F" "G")
                                           '("D")}) "DCP corret"))
    ))

(import java.util.HashMap)
(import info.rmarcus.dynamic_critical_path.DynamicCriticalPath)
(deftest dcp-java-wrapper-tests
  (testing "Java wrapper"
    (let [weights (HashMap.)
          edges (HashMap.)
          a (HashMap.)
          b (HashMap.)
          c (HashMap.)]
      (do
        (.put weights "A" (int 3))
        (.put weights "B" (int 9))
        (.put weights "C" (int 10))
        (.put weights "D" (int 4))

        (.put a "B" (int 5))
        (.put a "C" (int 4))
        (.put b "D" (int 600))
        (.put c "D" (int 7))

        (.put edges "A" a)
        (.put edges "B" b)
        (.put edges "C" c)
        (let [result (DynamicCriticalPath/schedule weights edges)
              iter (.iterator result)]
          (is (= (.size result) 2))
          (is (= (.size (.next iter)) 2))
          (is (= (.size (.next iter)) 2)))))))
          
