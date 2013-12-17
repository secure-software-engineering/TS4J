package de.fraunhofer.sit.codescan.typestate.analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.NullType;
import soot.Unit;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;

public class Abstraction implements Cloneable {
	
	protected Unit constrCallToValueGroup;
	protected Unit taintStmt;
	protected Value valueGroup;
	protected Value modelValue;
	protected boolean flushed = true;
	
	public final static Abstraction ZERO = new Abstraction() {
		{
			this.valueGroup = new JimpleLocal("", NullType.v());
			this.modelValue = new JimpleLocal("", NullType.v());
		}
		public String toString() { return "<ZERO>"; };
	};
	
	public Abstraction(Stmt s) {
		this.constrCallToValueGroup = s;
		InstanceInvokeExpr iie = (InstanceInvokeExpr) s.getInvokeExpr();
		this.valueGroup = (Value) iie.getBase();
	}
	
	public Value getValueGroupLocal() {
		return valueGroup;
	}
	
	public Value getModelValueLocal() {
		return modelValue;
	}
	
	public boolean isFlushed() {
		return flushed;
	}
	
	public Unit getConstrCallToValueGroup() {
		return constrCallToValueGroup;
	}
	
	public Unit getTaintStmt() {
		return taintStmt;
	}
	
	private Abstraction() {
	}
	
	/**
	 * If this abstraction contains fromVal then this method returns
	 * a copy of this abstraction where fromVal was replaced by toVal.
	 * Otherwise it returns <code>this</code>.
	 */
	public Abstraction replaceValue(Value fromVal, Value toVal) {
		if(valueGroup.equals(fromVal)||toVal.equals(fromVal)) {
			Abstraction copy = copy();
			if(copy.valueGroup!=null && copy.valueGroup.equals(fromVal))
				copy.valueGroup = toVal;
			if(copy.modelValue!=null && copy.modelValue.equals(fromVal))
				copy.modelValue = toVal;
			return copy;
		} else
			return this;
	}
	
	/**
	 * This method is usually called for computing the abstractions to be passed
	 * at call and return flow functions. This method returns one copy of this abstraction
	 * for every value in from that is contained in this abstraction. Within that copy,
	 * the from-value has been replaced by the corresponding to-value from the other list.
	 * If an entry is <code>null</code> in the from or to list, then this entry is not processed.   
	 */
	public Set<Abstraction> replaceValues(List<Value> from, List<ParameterRef> to) {
		assert(from.size()==to.size());
		Set<Abstraction> res = new HashSet<Abstraction>();
		for(int i=0; i<from.size(); i++) {
			Value fromVal = from.get(i);
			Value toVal = to.get(i);
			if(fromVal!=null && toVal!=null) {
				Abstraction derived = replaceValue(fromVal,toVal);
				if(derived!=this) {
					res.add(derived);
				}
			}
			i++;
		}
		return res;
	}
	
	public Abstraction markedAsTainted(Stmt taintStmt) {
		Abstraction copy = copy();
		copy.flushed = false;
		copy.taintStmt = taintStmt;
		return copy;
	}
		
	public Abstraction markedAsFlushed() {
		Abstraction copy = copy();
		copy.flushed = true;
		return copy;
	}	
	
	public Set<Abstraction> valueAdded(Value addedValue) {
		if(modelValue==null) {
			final Abstraction copy = copy();
			copy.modelValue = addedValue;
			Set<Abstraction> res = new HashSet<Abstraction>();
			res.add(this);
			res.add(copy);
			return res; 
		} else
			return Collections.singleton(this);
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
				+ ((modelValue == null) ? 0 : modelValue.hashCode());
		result = prime * result + (flushed ? 1231 : 1237);
		result = prime * result
				+ ((taintStmt == null) ? 0 : taintStmt.hashCode());
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
		if (modelValue == null) {
			if (other.modelValue != null)
				return false;
		} else if (!modelValue.equals(other.modelValue))
			return false;
		if (flushed != other.flushed)
			return false;
		if (taintStmt == null) {
			if (other.taintStmt != null)
				return false;
		} else if (!taintStmt.equals(other.taintStmt))
			return false;
		if (valueGroup == null) {
			if (other.valueGroup != null)
				return false;
		} else if (!valueGroup.equals(other.valueGroup))
			return false;
		return true;
	}

	private Abstraction copy() {
		try {
			Abstraction clone = (Abstraction) super.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "Abstraction [valueGroup=" + valueGroup + ", modelValue="
				+ modelValue + ", flushed=" + flushed + "]";
	}
}