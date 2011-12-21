package com.octo.cda2neo4j;

import java.util.HashMap;
import java.util.Map;

public class Graph {
	
	public Map<String, CdaNode> nodeList;

	public Graph() {
		this.nodeList = new HashMap<String, CdaNode>();
	}
}
