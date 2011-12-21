package com.ingdirect.dgng.dgmap;

import java.util.HashMap;
import java.util.Map;

public class Graph {
	
	public Map<String, DGNode> nodeList;

	public Graph() {
		this.nodeList = new HashMap<String, DGNode>();
	}
}
