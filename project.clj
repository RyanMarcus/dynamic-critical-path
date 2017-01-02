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
(defproject dynamic-critical-path "0.2.4"
  :description "The dynamic critical path (DCP) DAG scheduling algorithm"
  :url "https://github.com/RyanMarcus/dynamic-critical-path"
  :license {:name "GNU General Public License v3"
            :url "https://www.gnu.org/licenses/gpl.txt"}
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/math.combinatorics "0.1.3"]]
  :aot :all)
