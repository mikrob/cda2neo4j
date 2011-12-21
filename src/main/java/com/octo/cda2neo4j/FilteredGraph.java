package com.octo.cda2neo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Make a graph with a base list of entry points.
 * 
 * @author mikael
 * 
 */
public class FilteredGraph {

	private Map<String, CdaNode> baseGraph;

	private Set<CdaNode> graphFiltered;

	public Set<CdaNode> getGraphFiltered() {
		return graphFiltered;
	}

	public void setGraphFiltered(Set<CdaNode> graphFiltered) {
		this.graphFiltered = graphFiltered;
	}

	private String[] entryPoints;

	private static final String[] iPhoneFilters = {
			"com.ingdirect.dgng.ws.account.AccountWebService",
			"com.ingdirect.dgng.ws.account.operation.OperationWebService",
			"com.ingdirect.dgng.ws.login.LoginFirstStepWebService",
			"com.ingdirect.dgng.ws.login.LoginSecondStepWebService",
			"com.ingdirect.dgng.ws.offre.OfferDetailWebService",
			"com.ingdirect.dgng.ws.security.PinWebService",
			"com.ingdirect.dgng.ws.security.TokenWebService",
			"com.ingdirect.dgng.ws.status.StatusWebService",
			"com.ingdirect.dgng.ws.virement.MoveMoneyWebService",
			"com.ingdirect.dgng.ws.virement.PendingTransfersWebService" };

	public FilteredGraph(Map<String, CdaNode> graph) {
		this.entryPoints = iPhoneFilters;
		this.baseGraph = graph;
		graphFiltered = new HashSet<CdaNode>();
	}

	public FilteredGraph(Map<String, CdaNode> graph, String[] filters) {
		this.entryPoints = filters;
		this.baseGraph = graph;

	}

	public void filterGraph() {
		for (String filter : entryPoints) {
			CdaNode wsNode = baseGraph.get(filter);
			if (wsNode != null) {
				addNodeDepenciesToGraph(wsNode);
			}
		}
		System.out.println("Filter graph size : " + graphFiltered.size());
	}

	private void addNodeDepenciesToGraph(CdaNode node) {
		if (!graphFiltered.contains(node)) {
			graphFiltered.add(node);
		} else {
			return;
		}
		if (node.parent != null) {
			graphFiltered.add(node.parent);
			addNodeDepenciesToGraph(node.parent);
		}
		for (CdaNode impl : node.implementz) {
			for (CdaNode clazz : this.getClassesForInterface(impl.name)) {
				addNodeDepenciesToGraph(baseGraph.get(clazz.name));
			}
		}
		graphFiltered.addAll(node.implementz);
		for (CdaNode used : node.useds) {
			for (CdaNode clazz : this.getClassesForInterface(used.name)) {
				addNodeDepenciesToGraph(baseGraph.get(clazz.name));
			}
		}
		graphFiltered.addAll(node.useds);
	}

	private List<CdaNode> getClassesForInterface(String interfaceName) {
		List<CdaNode> result = new ArrayList<CdaNode>();
		for (Map.Entry<String, CdaNode> entry : baseGraph.entrySet()) {
			for (CdaNode nodeInterface : entry.getValue().implementz) {
				if (nodeInterface.name.equals(interfaceName)) {
					result.add(entry.getValue());
				}
			}
		}
		return result;
	}

	public Map<String, CdaNode> getBaseGraph() {
		return baseGraph;
	}

	public void setBaseGraph(Map<String, CdaNode> baseGraph) {
		this.baseGraph = baseGraph;
	}
	
	protected void makeiPhoneGraph(String outputFile) {
		Cda2Graphviz graphviz = new Cda2Graphviz();
		Map<String, CdaNode> map = new HashMap<String, CdaNode>();
		for (CdaNode node : graphFiltered) {
			map.put(node.name, node);
		}
		try {
			String nbFileName = outputFile.substring(0, outputFile.indexOf("."));
			nbFileName += "_iPhone.dot";
			graphviz.writeGraphViz(map, nbFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
