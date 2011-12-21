package com.octo.cda2neo4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class QueriesTest {
	
	private GraphDatabaseService graphDb;
	private Index<Node> nodeIndex;

	private static final String DB_PATH = "neo4j-store";

	@Before
	public void setup() {
		Map<String, String> config = EmbeddedGraphDatabase
				.loadConfigurations("src/main/resources/neo4j.properties");
		graphDb = new EmbeddedGraphDatabase(DB_PATH, config);
		Cda2Neo4j.registerShutdownHook(graphDb);
		nodeIndex = graphDb.index().forNodes("nodes");
	}
	
	@Test
	public void testEnvIsOk() {
		assertEquals("nodes", nodeIndex.getName());
	}
	
	@Test
	public void testQuery() {
		IndexHits<Node> nodes = nodeIndex.get("className", "com.ingdirect.afp.mq.server.MQXMLCommand");
		for (Node n : nodes) {
			System.out.println(n.getProperty("id"));
			System.out.println(n.getProperty("className"));
			for (Relationship r : n.getRelationships()) {
				System.out.println(r.getType().toString());
				System.out.println(r.getEndNode().getProperty("className"));
			}
		}
		System.out.println(nodes.size());
		assertNotNull(nodes);
	}
	
	
	@After
	public void after() {
		graphDb.shutdown();
	}

}
