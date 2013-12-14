package de.fraunhofer.sit.codescan.typestate.analysis;

import soot.NullType;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;

public class Abstraction implements Cloneable {
	
	protected Unit valueAddStmt;
	protected Value valueGroup;
	protected Value modelValue;
	protected boolean modelValueChanged;
	
	public final static Abstraction ZERO = new Abstraction() {
		{
			this.valueGroup = new JimpleLocal("", NullType.v());
			this.modelValue = new JimpleLocal("", NullType.v());
		}
		public String toString() { return "<ZERO>"; };
	};
	
	public Abstraction(Value valueGroup) {
		this.valueGroup = valueGroup;
	}
	
	public Value getValueGroupLocal() {
		return valueGroup;
	}
	
	private Abstraction() {
	}
	
	public Abstraction derive(Value lhs, Value rhs) {
		if(valueGroup.equals(rhs)||lhs.equals(rhs)) {
			Abstraction copy = copy();
			if(copy.valueGroup.equals(rhs))
				copy.valueGroup = lhs;
			if(copy.modelValue.equals(rhs))
				copy.modelValue = lhs;
			return copy;
		} else
			return this;
	}
	
	public Abstraction markedAsTainted() {
		Abstraction copy = copy();
		copy.modelValueChanged = true;
		return copy;
	}
		
	public Abstraction markedAsFlushed() {
		Abstraction copy = copy();
		copy.modelValueChanged = false;
		return copy;
	}	
	
	public Abstraction valueAdded(Stmt valueAddStmt) {
		Abstraction copy = copy();
		copy.valueAddStmt = valueAddStmt;
		copy.modelValue = valueAddStmt.getInvokeExpr().getArg(0);		
		return copy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((valueAddStmt == null) ? 0
						: valueAddStmt.hashCode());
		result = prime * result
				+ ((modelValue == null) ? 0 : modelValue.hashCode());
		result = prime * result + (modelValueChanged ? 1231 : 1237);
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
		if (valueAddStmt == null) {
			if (other.valueAddStmt != null)
				return false;
		} else if (!valueAddStmt.equals(other.valueAddStmt))
			return false;
		if (modelValue == null) {
			if (other.modelValue != null)
				return false;
		} else if (!modelValue.equals(other.modelValue))
			return false;
		if (modelValueChanged != other.modelValueChanged)
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
	
}