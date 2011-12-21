package com.octo.cda2neo4j;

import java.util.Map;

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
		graphDb = new EmbeddedGraphDatabase(DB_PATH,
				config);
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
		Node root;
		Node secondNode;
		Node thirdNode;
		Relationship relationship1;
		Relationship relationship2;

		Transaction tx = graphDb.beginTx();
		try {
			root = graphDb.createNode();
			root.setProperty("name", "BusinessCommand");
			secondNode = graphDb.createNode();
			secondNode.setProperty("name", "XmlBusinessCommand");
			thirdNode = graphDb.createNode();
			thirdNode.setProperty("name", "JSONBusinessCommand");

			relationship1 = root.createRelationshipTo(secondNode,
					RelTypes.IMPLEMENTS);
			relationship1.setProperty("message", "implements");
			relationship2 = root.createRelationshipTo(thirdNode,
					RelTypes.IMPLEMENTS);
			relationship2.setProperty("message", "implements");

			nodeIndex.add(root, "name", "BusinessCommand");
			nodeIndex.add(secondNode, "name", "XmlBusinessCommand");
			nodeIndex.add(thirdNode, "name", "JSONBusinessCommand");

			// make a search in the graph
			IndexHits<Node> nodes = nodeIndex.get("name", "XmlBusinessCommand");
			for (Node node : nodes) {
				System.out.println(node.getProperty("name"));
			}

			// delete the graph
			for (Relationship relationship : root.getRelationships(
					RelTypes.IMPLEMENTS, Direction.OUTGOING)) {
				Node theNode = relationship.getEndNode();
				nodeIndex.remove(theNode, "name",
						theNode.getProperty("name"));
				theNode.delete();
				relationship.delete();
			}
			root.delete();

			tx.success();
		} finally {
			tx.finish();
		}
		graphDb.shutdown();
	}
	
	

}
