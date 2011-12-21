package com.octo.cda2neo4j;

import java.util.ArrayList;
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

	private Map<String, DGNode> baseGraph;

	private Set<DGNode> graphFiltered;

	public Set<DGNode> getGraphFiltered() {
		return graphFiltered;
	}

	public void setGraphFiltered(Set<DGNode> graphFiltered) {
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

	public FilteredGraph(Map<String, DGNode> graph) {
		this.entryPoints = iPhoneFilters;
		this.baseGraph = graph;
		graphFiltered = new HashSet<DGNode>();
	}

	public FilteredGraph(Map<String, DGNode> graph, String[] filters) {
		this.entryPoints = filters;
		this.baseGraph = graph;

	}

	public void filterGraph() {
		for (String filter : entryPoints) {
			DGNode wsNode = baseGraph.get(filter);
			if (wsNode != null) {
				addNodeDepenciesToGraph(wsNode);
			}
		}
		System.out.println("Filter graph size : " + graphFiltered.size());
	}

	private void addNodeDepenciesToGraph(DGNode node) {
		if (!graphFiltered.contains(node)) {
			graphFiltered.add(node);
		} else {
			return;
		}
		if (node.parent != null) {
			graphFiltered.add(node.parent);
			addNodeDepenciesToGraph(node.parent);
		}
		for (DGNode impl : node.implementz) {
			for (DGNode clazz : this.getClassesForInterface(impl.name)) {
				addNodeDepenciesToGraph(baseGraph.get(clazz.name));
			}
		}
		graphFiltered.addAll(node.implementz);
		for (DGNode used : node.useds) {
			for (DGNode clazz : this.getClassesForInterface(used.name)) {
				addNodeDepenciesToGraph(baseGraph.get(clazz.name));
			}
		}
		graphFiltered.addAll(node.useds);
	}

	private List<DGNode> getClassesForInterface(String interfaceName) {
		List<DGNode> result = new ArrayList<DGNode>();
		for (Map.Entry<String, DGNode> entry : baseGraph.entrySet()) {
			for (DGNode nodeInterface : entry.getValue().implementz) {
				if (nodeInterface.name.equals(interfaceName)) {
					result.add(entry.getValue());
				}
			}
		}
		return result;
	}

	public Map<String, DGNode> getBaseGraph() {
		return baseGraph;
	}

	public void setBaseGraph(Map<String, DGNode> baseGraph) {
		this.baseGraph = baseGraph;
	}

}
