// < begin copyright > 
// Copyright Ryan Marcus 2017
// 
// This file is part of dynamic-critical-path.
// 
// dynamic-critical-path is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// dynamic-critical-path is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with dynamic-critical-path.  If not, see <http://www.gnu.org/licenses/>.
// 
// < end copyright > 
package info.rmarcus.dynamic_critical_path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class DynamicCriticalPath {
	
    private static IFn assoc = Clojure.var("clojure.core", "assoc");
    private static IFn list = Clojure.var("clojure.core", "list");
    private static IFn conj = Clojure.var("clojure.core", "conj");
    private static IFn generateDAG = Clojure.var("dynamic-critical-path.core", "generateDAG");
    private static IFn dcp = Clojure.var("dynamic-critical-path.core", "dynamic-critical-path");
	
    static {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("dynamic-critical-path.core"));
    }
	
    public static void main(String[] args) {
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
		
		
        System.out.println(schedule(weights, edges));
    }
	
    public static Set<List<String>> schedule(Map<String, Integer> vertexWeights, Map<String, Map<String, Integer>> edges) {
        Object vWeights = Clojure.read("{}");
		
        for (Entry<String, Integer> e : vertexWeights.entrySet()) {
            vWeights = assoc.invoke(vWeights, str(e.getKey()), num(e.getValue()));
        }
		
        Object edgeList = list.invoke();
        for (Entry<String, Map<String, Integer>> e : edges.entrySet()) {
            Object vertex = str(e.getKey());
            for (Entry<String, Integer> ed : e.getValue().entrySet()) {
                Object child = str(ed.getKey());
                Object weight = num(ed.getValue());
				
                Object l = list.invoke(vertex, child, weight);
                edgeList = conj.invoke(edgeList, l);
            }
        }
		
		
        Object dag = generateDAG.invoke(vWeights, edgeList);
        Object schedule = dcp.invoke(dag);
		
        // treat the resulting schedule as a Collection<Collection<String>>
        @SuppressWarnings("unchecked")
            Collection<Collection<String>> clusters = (Collection<Collection<String>>) schedule;
		
        return clusters.stream()
            .map(cluster -> new ArrayList<String>(cluster))
            .collect(Collectors.toSet());
		
    }
	
    private static Object str(String s) {
        return Clojure.read("\"" + s + "\"");
    }
	
    private static Object num(int i) {
        return Clojure.read(String.valueOf(i));
    }
}
