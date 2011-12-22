package com.octo.cda2neo4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Cda2Graph {

	Cda2Graphviz data = new Cda2Graphviz();

	private static final String ING_FILTER = "com.ingdirect";
	
	private static final String [] ING_PACKAGE_FILTER_LIST = {"com.ingdirect.epiphany",
		"com.ingdirect.shared",
		"com.ingdirect.afp.business",
		"com.ingdirect.afp.type",
		"com.ingdirect.afp.util"
	};

	private static final String [] ING_CLASS_FILTER_LIST = {		
		//".XMLBusinessCommand",
		//".BusinessCommand",
		".EdgeCommand",
		//"Exception",
		".AllServices",
		".DirectGatewayClientBusinessCommand",
		".ServiceLocator",
		".DGClientBusinessCommand",
		"client.service.Services",
		".AbstractDaoProfile"
	};

	public Cda2Graph() {
		this.data.graph = new Graph();
		data.nbDependenciesPerCommand = new HashMap<CdaNode, Integer>();
	}

	// main with super GUI :-)
	public static void main(String... args) throws IOException {
		Options opt = new Options();

		opt.addOption(getOption("h", false, false, "Print usage"));
		opt.addOption(getOption("in", true, true, "The log file to parse"));
		opt.addOption(getOption("out", true, true,
				"Output directory or file for a -list"));

		BasicParser parser = new BasicParser();
		CommandLine cl;
		try {
			cl = parser.parse(opt, args);
		} catch (MissingOptionException e) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp(e.getMessage(), opt);
			System.exit(1);
			return;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		if (cl.hasOption('h')) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp("OptionsTip", opt);
			System.exit(0);
			return;
		}
		long begin = System.currentTimeMillis();
		String inputFile = FilenameUtils.normalize(cl.getOptionValue("in"));
		String outputFile = FilenameUtils.normalize(cl.getOptionValue("out"));
		// here we work
		Cda2Graph cda2Graphviz = new Cda2Graph();
		cda2Graphviz.makeGraph(inputFile, outputFile);
		//cda2Graphviz.makeiPhoneGraph(outputFile);		
		cda2Graphviz.createNeo4jGraph();
		long time = System.currentTimeMillis() - begin;
		System.out.println("Took : " + time + " ms to execute" );
	}

	private static Option getOption(String opt, boolean hasArg,
			boolean required, String description) {
		Option p = new Option(opt, hasArg, description);
		p.setRequired(required);
		return p;
	}

	public void makeGraph(String inputFile, String outputFile) {
		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			docBuilderFactory.setValidating(false);
			docBuilderFactory.setNamespaceAware(false);
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File(inputFile));
			doc.getDocumentElement().normalize();
			parseCdaXml(doc);
			System.out.println(" Graph contains : " + data.graph.nodeList.size());

		} catch (SAXParseException err) {
			System.out.println("** Parsing error" + ", line "
					+ err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());

		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private void parseCdaXml(Document doc) {
		NodeList containers = doc.getElementsByTagName("container");
		for (int idx = 0; idx < containers.getLength(); idx++) {
			Node container = containers.item(idx);
			if (container.getNodeType() == Node.ELEMENT_NODE) {
				Element containerElement = (Element) container;
				String containerName = containerElement.getAttribute("name");
				System.out.println("Container Name " + containerName);
				getNamespacesForDoc(doc, containerName);
			}
		}
		
	}

	private void getNamespacesForDoc(Document doc, String containerName) {
		
		NodeList namespaces = doc.getElementsByTagName("namespace");
		for (int idx = 0; idx < namespaces.getLength(); idx++) {
			Node namespace = namespaces.item(idx);
			if (namespace.getNodeType() == Node.ELEMENT_NODE) {
				Element namespaceElement = (Element) namespace;
				String packageName = namespaceElement.getAttribute("name");
				getTypesForNameSpace(namespaceElement, packageName, containerName);

			}
		}
	}

	private void getTypesForNameSpace(Element namespaceElement,
			String packageName, String containerName) {
		if (packageName.startsWith(ING_FILTER)) {
			NodeList typesNodes = namespaceElement.getChildNodes();
			for (int idy = 0; idy < typesNodes.getLength(); idy++) {
				Node typeNode = typesNodes.item(idy);
				if (typeNode.getNodeType() == Node.ELEMENT_NODE) {
					Element typeElement = (Element) typeNode;
					String name = typeElement.getAttribute("name");
					if (!toBeIgnored(name)) {
						CdaNode node = getNewNode(name, containerName);
						node.type = typeElement.getAttribute("classification");
						getDependenciesForType(typeElement, node, containerName);
						data.graph.nodeList.put(node.name, node);
					}
				}
			}

		}
	}

	private void getDependenciesForType(Element typeElement, CdaNode node, String containerName) {
		NodeList dependencies = typeElement.getChildNodes();
		for (int idz = 0; idz < dependencies.getLength(); idz++) {
			Node dependenciesNode = dependencies.item(idz);
			if (dependenciesNode.getNodeType() == Node.ELEMENT_NODE) {
				Element dependenciesElement = (Element) dependenciesNode;
				getDependsOnForDependencies(dependenciesElement, node, containerName);
			}
		}
	}

	private void getDependsOnForDependencies(Element dependenciesElement,
			CdaNode node, String containerName) {
		NodeList dependsOnList = dependenciesElement.getChildNodes();
		for (int i = 0; i < dependsOnList.getLength(); i++) {
			Node dependsOnNode = dependsOnList.item(i);
			if (dependsOnNode.getNodeType() == Node.ELEMENT_NODE) {
				Element dependsOnElement = (Element) dependsOnNode;
				extractHierarchy(dependsOnElement, node, containerName);
			}
		}
	}

	private void extractHierarchy(Element dependsOnElement, CdaNode node, String containerName) {
		String name = dependsOnElement.getAttribute("name");
		if (!name.startsWith(ING_FILTER)) {
			return;
		}
		if (toBeIgnored(name)) {
			return;
		}
		String classification = dependsOnElement.getAttribute("classification");
		CdaNode nodeToAdd = getNewNode(name, containerName);
		if (classification.equals("extends")) {
			node.parent = nodeToAdd;
		} else if (classification.equals("implements")) {
			node.implementz.add(nodeToAdd);
		} else if (classification.equals("uses")) {
			node.useds.add(nodeToAdd);
		}
	}

	private CdaNode getNewNode(String name, String containerName) {
		CdaNode nodeToAdd = data.graph.nodeList.get(name);
		
		if (nodeToAdd == null) {
			nodeToAdd = new CdaNode(name, containerName);
			data.graph.nodeList.put(name, nodeToAdd);
		}

		return nodeToAdd;
	}
	
	private void createNeo4jGraph() {
		Cda2Neo4j neo4j = new Cda2Neo4j(this.data.graph);
		neo4j.insertGraphToNeo4j();
	}
	

	private boolean toBeIgnored(String name) {
		return name == null || excludePackage(name) || excludeClass(name);
	}
	
	private boolean excludePackage(String name) {
		for (String pack : ING_PACKAGE_FILTER_LIST) {
			if (name.startsWith(pack)) {
				return true;
			}
		}
		return false;
	}

	private boolean excludeClass(String name) {
		for (String pack : ING_CLASS_FILTER_LIST) {
			if (name.endsWith(pack)) {
				return true;
			}
		}
		return false;
	}
	
}
