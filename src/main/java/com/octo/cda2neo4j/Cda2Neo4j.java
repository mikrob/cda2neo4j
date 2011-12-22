package com.octo.cda2neo4j;

import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class Cda2Neo4j {

	private Graph graph;
	private GraphDatabaseService graphDb;
	private Index<Node> nodeIndex;
	
	private int customid = 0;

	private static final String DB_PATH = "neo4j-store";

	public Cda2Neo4j(Graph graph) {
		this.graph = graph;
		Map<String, String> config = EmbeddedGraphDatabase
				.loadConfigurations("src/main/resources/neo4j.properties");
		graphDb = new EmbeddedGraphDatabase(DB_PATH, config);
		registerShutdownHook(graphDb);
		nodeIndex = graphDb.index().forNodes("nodes");
	}

	protected static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	public void insertGraphToNeo4j() {
		System.out.println("Begin insertion in neo4j");
		Transaction tx = graphDb.beginTx();
		try {
			Set<Map.Entry<String, CdaNode>> set = graph.nodeList.entrySet();
			for (Map.Entry<String, CdaNode> node : set) {
				// get the attributes
				String className = node.getKey();
				CdaNode nodeContent = node.getValue();
				// create the neo4j node
				Node neoNode = createNeo4jNode(className, nodeContent.containerName);
				// extends
				if (nodeContent.parent != null) {
					Node neoNodeParent = createNeo4jNode(nodeContent.parent.name, nodeContent.parent.containerName);
					neoNode.createRelationshipTo(neoNodeParent, RelTypes.EXTENDS);
				}
				
				//implements
				for (CdaNode interf : nodeContent.implementz) {
					Node interfaceImplemented = createNeo4jNode(interf.name, interf.containerName);
					neoNode.createRelationshipTo(interfaceImplemented, RelTypes.IMPLEMENTS);
				}
				// use
				for (CdaNode uzed : nodeContent.useds) {
					Node classUsed = createNeo4jNode(uzed.name, uzed.containerName);
					neoNode.createRelationshipTo(classUsed, RelTypes.USE);
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}
		System.out.println("Graph size : " + nodeIndex.getName());
		graphDb.shutdown();
		System.out.println("End insertion in neo4j");
	}

	// Create a node and add it to index
	private Node createNeo4jNode(String className, String containerName) {
		IndexHits<Node> exist = nodeIndex.get("className", className);
		Node result;
		if (exist.size() == 0) {
			Node neoNode = graphDb.createNode();
			neoNode.setProperty("className", className);
			neoNode.setProperty("containerName", containerName);
			neoNode.setProperty("id", String.valueOf(customid++));
			nodeIndex.add(neoNode, "className", className);
			
			result = neoNode;
		} else {
			result = exist.next();
		}
		return result;
	}
	

	public void cleanGraph() {
		nodeIndex.delete();
	}
	

}
