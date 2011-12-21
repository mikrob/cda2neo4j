package com.ingdirect.dgng.dgmap;

import java.util.HashSet;
import java.util.Set;

public class DGNode {

	public String name;

	public DGNode parent;

	public String type;

	public Set<DGNode> implementz = new HashSet<DGNode>();

	public Set<DGNode> useds = new HashSet<DGNode>();

	public DGNode(String name2) {
		this.name = name2;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public boolean hasSameName(DGNode obj) {
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
		DGNode other = (DGNode) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
