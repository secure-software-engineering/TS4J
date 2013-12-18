package de.fraunhofer.sit.codescan.typestate.analysis;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Unit;

public class Abstraction<Var extends Enum<Var>,Val,State extends Enum<State>,StmtID extends Enum<StmtID>> implements Cloneable {
	
	protected Val[] boundValues;
	protected Unit[] stmtTrace;
	protected State state;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private final static Abstraction ZERO = new Abstraction() {
		{
			boundValues = new Object[0];
			stmtTrace = new Unit[0];
		}
		public String toString() { return "<ZERO>"; };
		public Object getValue(Enum e) { return null; };
		public boolean stateIs(Enum s) { return false; }
		public boolean equals(Object obj) { return obj==this; }
		public int hashCode() { return 23; };
	};

	@SuppressWarnings("unchecked")
	public final static <Var extends Enum<Var>,Val,State extends Enum<State>,StmtID extends Enum<StmtID>> Abstraction<Var,Val,State,StmtID> zero() {
		return ZERO;
	}
	
	private Abstraction() {
		//only used for ZERO
	}

	@SuppressWarnings("unchecked")
	public Abstraction(Var e, Val v, State s) {
		int size = e.getClass().getEnumConstants().length;		
		boundValues = (Val[]) new Object[size];
		boundValues[e.ordinal()] = v;
		state = s;
	}
	
	public Val getValue(Var e) {
		return boundValues[e.ordinal()];
	}

	public Unit getStatement(StmtID sid) {
		if(stmtTrace==null) return null;
		else return stmtTrace[sid.ordinal()];
	}
	
	/**
	 * If this abstraction contains fromVal then this method returns
	 * a copy of this abstraction where fromVal was replaced by toVal.
	 * Otherwise it returns <code>this</code>.
	 */
	public Abstraction<Var,Val,State,StmtID> replaceValue(Val fromVal, Val toVal) {
		Abstraction<Var,Val,State,StmtID> copy = null;
		for (int i = 0; i < boundValues.length; i++) {
			Val val = boundValues[i];
			if(val!=null && val.equals(fromVal)) {
				if(copy==null) copy = copy();
				copy.boundValues[i] = toVal;
			}
		}
		if(copy==null) return this;
		else return copy;
	}
	
	public Abstraction<Var,Val,State,StmtID> storeStmt(Unit u, StmtID sid) {
		if(stmtTrace==null) stmtTrace = new Unit[sid.getClass().getEnumConstants().length];
		int index = sid.ordinal();
		if(stmtTrace[index]==null || !stmtTrace[index].equals(u)) {
			Abstraction<Var,Val,State,StmtID> copy = copy();
			copy.stmtTrace[index] = u;
			return copy;
		} else {
			return this;
		}
	}
	
	/**
	 * This method is usually called for computing the abstractions to be passed
	 * at call and return flow functions. This method returns one copy of this abstraction
	 * for every value in from that is contained in this abstraction. Within that copy,
	 * the from-value has been replaced by the corresponding to-value from the other list.
	 * If an entry is <code>null</code> in the from or to list, then this entry is not processed.   
	 */
	public Set<Abstraction<Var,Val,State,StmtID>> replaceValues(List<Val> from, List<Val> to) {
		assert(from.size()==to.size());
		Set<Abstraction<Var,Val,State,StmtID>> res = new HashSet<Abstraction<Var,Val,State,StmtID>>();
		for(int i=0; i<from.size(); i++) {
			Val fromVal = from.get(i);
			Val toVal = to.get(i);
			if(fromVal!=null && toVal!=null) {
				Abstraction<Var,Val,State,StmtID> derived = replaceValue(fromVal,toVal);
				if(derived!=this) {
					res.add(derived);
				}
			}
		}
		return res;
	}
	
	public Set<Abstraction<Var,Val,State,StmtID>> bindValue(Val addedValue, Var e) {
		int index = e.ordinal();
		if(boundValues[index]==null) {
			final Abstraction<Var,Val,State,StmtID> copy = copy();
			copy.boundValues[index] = addedValue;
			Set<Abstraction<Var,Val,State,StmtID>> res = new HashSet<Abstraction<Var,Val,State,StmtID>>();
			res.add(this);
			res.add(copy);
			return res; 
		}
		return Collections.singleton(this);
	}
	
	public Abstraction<Var,Val,State,StmtID> withStateChangedTo(State s) {
		if(state != null && state.equals(s))
			return this;
		Abstraction<Var,Val,State,StmtID> copy = copy();
		copy.state = s;
		return copy;
	}
	
	public boolean stateIs(State s) {
		return state!=null && state.equals(s);
	}

	private Abstraction<Var,Val,State,StmtID> copy() {
		try {
			@SuppressWarnings("unchecked")
			Abstraction<Var,Val,State,StmtID> clone = (Abstraction<Var,Val,State,StmtID>) super.clone();
			clone.boundValues = boundValues.clone();
			if(stmtTrace!=null) clone.stmtTrace = stmtTrace.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(stmtTrace);
		result = prime * result + Arrays.hashCode(boundValues);
		result = prime * result + ((state == null) ? 0 : state.hashCode());
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
		@SuppressWarnings("rawtypes")
		Abstraction other = (Abstraction) obj;
		if (!Arrays.equals(stmtTrace, other.stmtTrace))
			return false;
		if (!Arrays.equals(boundValues, other.boundValues))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Abstraction [boundValues=" + Arrays.toString(boundValues)
				+ ", stmtTrace=" + Arrays.toString(stmtTrace)
				+ ", state=" + state + "]";
	}

}