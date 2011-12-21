package com.octo.cda2neo4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class Cda2Graphviz {
	public Graph graph;
	public Map<CdaNode, Integer> nbDependenciesPerCommand;

	public Cda2Graphviz() {
		nbDependenciesPerCommand = new HashMap<CdaNode, Integer> ();
	}
	
	
	protected void writeGraphViz(Map<String, CdaNode> graphToWrite, String outputFile) throws Exception {
		File file = new File(outputFile);
		System.out.println(file.getAbsolutePath());
		FileWriter writer = new FileWriter(file);
		BufferedWriter bufferedWriter = new BufferedWriter(writer, 512);
		PrintWriter printWriter = new PrintWriter(bufferedWriter, true);
		Set<CdaNode> classes = new HashSet<CdaNode>();
		Set<CdaNode> interfaces = new HashSet<CdaNode>();
		for (Map.Entry<String, CdaNode> entry : graphToWrite.entrySet()) {
			if (entry.getValue().type != null
					&& entry.getValue().type.equals("interface")) {
				interfaces.add(entry.getValue());
			} else {
				classes.add(entry.getValue());
			}
		}

		printWriter.print("digraph PhiloDilemma {");
		// printWriter.print("digraph TrafficLights {");
		printWriter.println();
		printWriter.print("node [shape=box]; ");
		for (CdaNode clazz : classes) {
			String name = clazz.name;
			printWriter.println(printNameGv(name) + ";");
		}
		printWriter.println();
		printWriter.print("node [shape=circle,fixedsize=false];  ");
		for (CdaNode clazz : interfaces) {
			String name = clazz.name;
			printWriter.println(printNameGv(name) + ";");
		}
		printWriter.println();
		// print edge
		int nbedge = 0;
		for (Map.Entry<String, CdaNode> entry : graphToWrite.entrySet()) {
			CdaNode currentNode = entry.getValue(); // iterate over node datas.
			int nbDep = 0;
			if (currentNode.parent != null) {
				String color = "[color=\"red\"]";
				printWriter.println(printNameGv(currentNode.name) + "->"
						+ printNameGv(currentNode.parent.name) + color + ";");
				nbedge++;
				nbDep++;
			}
			for (CdaNode interf : currentNode.implementz) {
				String color = "[color=\"blue\"]";
				printWriter.println(printNameGv(currentNode.name) + "->"
						+ printNameGv(interf.name) + color + ";");
				nbedge++;
				nbDep++;
			}

			for (CdaNode uzed : currentNode.useds) {
				String color = "[color=\"green\"]";
				printWriter.println(printNameGv(currentNode.name) + "->"
						+ printNameGv(uzed.name) + color + ";");
				nbedge++;
				nbDep++;
			}
			nbDependenciesPerCommand.put(currentNode, nbDep);

		}
		printWriter.println();
		printWriter.println();
		printWriter.println("fontsize=8;");
		printWriter.println("}");
		printWriter.flush();
		printWriter.close();
		System.out.println("NB Edege : " + nbedge);

	}
	
	
	private String printNameGv(String name) {
		String[] splitted = StringUtils.split(name, ".");
		String finalName = splitted[splitted.length - 3] + "." + splitted[splitted.length - 2] + "." + splitted[splitted.length - 1];
		return "\"" + finalName + "\"";
	}
}