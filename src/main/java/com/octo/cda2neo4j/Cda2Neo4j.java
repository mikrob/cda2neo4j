package com.octo.cda2neo4j;

import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class Cda2Neo4j {

	private Graph graph;
	private GraphDatabaseService graphDb;
	private Index<Node> nodeIndex;

	private static final String DB_PATH = "neo4j-store";

	public Cda2Neo4j(Graph graph) {
		this.graph = graph;
		Map<String, String> config = EmbeddedGraphDatabase
				.loadConfigurations("src/main/resources/neo4j.properties");
		graphDb = new EmbeddedGraphDatabase(DB_PATH, config);
		registerShutdownHook(graphDb);
		nodeIndex = graphDb.index().forNodes("nodes");
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
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
		Transaction tx = graphDb.beginTx();
		try {
			Set<Map.Entry<String, DGNode>> set = graph.nodeList.entrySet();
			for (Map.Entry<String, DGNode> node : set) {
				// get the attributes
				String className = node.getKey();
				DGNode nodeContent = node.getValue();
				// create the neo4j node
				Node neoNode = createNeo4jNode(className);
				
				// extends
				if (nodeContent.parent != null) {
					Node neoNodeParent = createNeo4jNode(nodeContent.parent.name);
					neoNode.createRelationshipTo(neoNodeParent, RelTypes.EXTENDS);
				}
				
				//implements
				for (DGNode interf : nodeContent.implementz) {
					Node interfaceImplemented = createNeo4jNode(interf.name);
					neoNode.createRelationshipTo(interfaceImplemented, RelTypes.IMPLEMENTS);
				}
				// use
				for (DGNode uzed : nodeContent.useds) {
					Node classUsed = createNeo4jNode(uzed.name);
					neoNode.createRelationshipTo(classUsed, RelTypes.IMPLEMENTS);
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}
		graphDb.shutdown();
	}

	// Create a node and add it to index
	private Node createNeo4jNode(String className) {
		Node neoNode = graphDb.createNode();
		neoNode.setProperty("className", className);
		nodeIndex.add(neoNode, "className", className);
		
		return neoNode;
	}
	

	public void cleanGraph() {
		nodeIndex.delete();
	}
	

}
