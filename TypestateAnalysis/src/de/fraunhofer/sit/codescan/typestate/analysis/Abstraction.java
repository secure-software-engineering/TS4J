package de.fraunhofer.sit.codescan.typestate.analysis;

import java.util.HashSet;

import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;

public class Abstraction implements Cloneable {
	
	protected Unit constrCallToValueGroup;
	protected Value valueGroup;
	protected HashSet<Value> elements = new HashSet<Value>();
	protected boolean modelValueAdded;
	
	public final static Abstraction ZERO = new Abstraction(); 
	
	public Abstraction(AssignStmt constrCallToValueGroup) {
		this.constrCallToValueGroup = constrCallToValueGroup;
		this.valueGroup = (Value) constrCallToValueGroup.getLeftOp();
		modelValueAdded = false;
	}
	
	public Value getValueGroupLocal() {
		return valueGroup;
	}
	
	private Abstraction() {		
	}
	
	public Abstraction derive(Value newLocal) {
		Abstraction copy = copy();
		copy.valueGroup = newLocal;
		return copy;
	}
	
	public Abstraction markedAsTainted() {
		Abstraction copy = copy();
		copy.modelValueAdded = true;
		return copy;
	}
		
	public Abstraction markedAsFlushed() {
		Abstraction copy = copy();
		copy.modelValueAdded = false;
		return copy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((constrCallToValueGroup == null) ? 0
						: constrCallToValueGroup.hashCode());
		result = prime * result
				+ ((elements == null) ? 0 : elements.hashCode());
		result = prime * result + (modelValueAdded ? 1231 : 1237);
		result = prime * result
				+ ((valueGroup == null) ? 0 : valueGroup.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Abstraction other = (Abstraction) obj;
		if (constrCallToValueGroup == null) {
			if (other.constrCallToValueGroup != null)
				return false;
		} else if (!constrCallToValueGroup.equals(other.constrCallToValueGroup))
			return false;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		if (modelValueAdded != other.modelValueAdded)
			return false;
		if (valueGroup == null) {
			if (other.valueGroup != null)
				return false;
		} else if (!valueGroup.equals(other.valueGroup))
			return false;
		return true;
	}

	@SuppressWarnings("unchecked")
	private Abstraction copy() {
		try {
			Abstraction clone = (Abstraction) super.clone();
			clone.elements = (HashSet<Value>) elements.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
}