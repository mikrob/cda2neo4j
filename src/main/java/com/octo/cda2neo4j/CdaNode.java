package com.octo.cda2neo4j;

import java.util.HashSet;
import java.util.Set;

public class CdaNode {

	public String name;

	public CdaNode parent;

	public String type;

	public Set<CdaNode> implementz = new HashSet<CdaNode>();

	public Set<CdaNode> useds = new HashSet<CdaNode>();

	public CdaNode(String name2) {
		this.name = name2;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public boolean hasSameName(CdaNode obj) {
		return this.name.equals(obj.name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CdaNode other = (CdaNode) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
