package com.octo.cda2neo4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.core.NodeManager;

import scala.collection.Iterator;

public class QueriesTest {

	private GraphDatabaseService graphDb;
	private Index<Node> nodeIndex;
	ExecutionEngine engine;

	private static final String DB_PATH = "neo4j-store";

	@Before
	public void setup() {
		Map<String, String> config = EmbeddedGraphDatabase
				.loadConfigurations("src/main/resources/neo4j.properties");
		graphDb = new EmbeddedGraphDatabase(DB_PATH, config);
		Cda2Neo4j.registerShutdownHook(graphDb);
		nodeIndex = graphDb.index().forNodes("nodes");
		engine = new ExecutionEngine(graphDb);
	}

	@Test
	public void testEnvIsOk() {
		assertEquals("nodes", nodeIndex.getName());
	}

	// query on index without cypher
	@Test
	public void testQuery() {
		IndexHits<Node> nodes = nodeIndex.get("className",
				"com.ingdirect.afp.mq.server.MQXMLCommand");
		for (Node n : nodes) {
			System.out.println(n.getProperty("className"));
			System.out.println(n.getProperty("containerName"));
			for (Relationship r : n.getRelationships()) {
				System.out.println(r.getType().toString() + " "
						+ r.getEndNode().getProperty("className"));
			}
		}
		System.out.println(nodes.size());
		assertNotNull(nodes);
	}

	// search node by his name
	@Test
	public void searchNodeWithCypher() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("query",
				"className:com.ingdirect.afp.mq.server.MQXMLCommand");
		ExecutionResult result = engine.execute(
				"start n=node:nodes({query}) return n", params);
		Iterator<Node> it = result.columnAs("n");
		while (it.hasNext()) {
			Node res = it.next();
			System.out.println(res.getProperty("className"));
			System.out.println(res.getProperty("containerName"));
		}
	}
	
	// search outgoing relations ships for a given node with its className
	@Test
	public void searchRelationShipWithCyper() {

		ExecutionResult result = engine
				.execute("START r=relationship(0) RETURN r");
		Iterator<Relationship> it = result.columnAs("r");
		while (it.hasNext()) {
			Relationship res = it.next();
			System.out.println(res.getType().toString());

		}
	}

	// search outgoing relations ships for a given node with its className
	@Test
	public void serachNodeLinkedByOutgoingRelationShips() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("query",
				"className:com.ingdirect.afp.mq.server.MQXMLCommand");
		ExecutionResult result = engine.execute(
				"START n=node:nodes({query}) MATCH (n)-->(x) RETURN x", params);
		Iterator<Node> it = result.columnAs("x");
		while (it.hasNext()) {
			Node res = it.next();
			System.out.println(res.getProperty("className"));
			System.out.println(res.getProperty("containerName"));

		}
	}

	// search relations ships for a given node with its className
	@Test
	public void searchRelationShipsForNode() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("query",
				"className:com.ingdirect.afp.mq.server.MQXMLCommand");
		ExecutionResult result = engine.execute(
				"START n=node:nodes({query}) MATCH (n)-[r]->() RETURN r",
				params);
		Iterator<Relationship> it = result.columnAs("r");
		while (it.hasNext()) {
			Relationship res = it.next();
			System.out.println(res.getType().toString());

		}
	}

	// Search a node with its id
	@Test
	public void searcbyId() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", 1);
		ExecutionResult result = engine.execute("START n=node({id}) RETURN n",
				params);
		Iterator<Node> it = result.columnAs("n");
		while (it.hasNext()) {
			Node res = it.next();
			System.out.println(res.getProperty("className"));
			System.out.println(res.getProperty("containerName"));

		}
	}

	// find all node with no relationships
	@Test
	public void findOrpheanNodes() {
		NodeManager nodeManager = ((EmbeddedGraphDatabase) graphDb).getConfig()
				.getGraphDbModule().getNodeManager();
		long number = nodeManager.getNumberOfIdsInUse(Node.class);
		int counter = 0;
		for (int idx = 0; idx < number; idx++) {
			Node n = nodeManager.getNodeById(idx);
			if (!n.hasRelationship()) {
				System.out.println("This one has no relationships");
				counter++;
				for (String key : n.getPropertyKeys()) {
					System.out.println(key + " : " + n.getProperty(key));

				}
				System.out.println();
			}
		}
		System.out.println("Counter: " + counter);
	}
	
	@Test
	public void findNodesWithNoEntranceLinks() {
		NodeManager nodeManager = ((EmbeddedGraphDatabase) graphDb).getConfig()
				.getGraphDbModule().getNodeManager();
		long number = nodeManager.getNumberOfIdsInUse(Node.class);
		int counter = 0;
		for (int idx = 0; idx < number; idx++) {
			Node n = nodeManager.getNodeById(idx);
			int nb = 0;
			for (@SuppressWarnings("unused") Relationship r : n.getRelationships(Direction.INCOMING)) {
				nb++;
			}
			if (nb == 0) {
				for (String key : n.getPropertyKeys()) {
					if (key.startsWith("className") && ! ((String)n.getProperty(key)).endsWith("Command")) {
						counter++;
						System.out.println(key + " : " + n.getProperty(key));
					}
					
				}
			}
			
		}
		System.out.println("Number of node without incoming relations : " + counter + " theses node represent dead code (classes not used), you should delete application entrance point of this list");
	}



	@After
	public void after() {
		graphDb.shutdown();
	}

}
