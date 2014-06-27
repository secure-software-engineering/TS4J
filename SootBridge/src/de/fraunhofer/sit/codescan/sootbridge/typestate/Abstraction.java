package de.fraunhofer.sit.codescan.sootbridge.typestate;

import static heros.TwoElementSet.twoElementSet;
import static java.util.Collections.singleton;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.InstanceFieldRef;

/**
 * A generic typestate abstraction that can be used to track multiple correlated objects whose pointer values
 * are bound to a finite set of variables. In addition, the abstraction stores an internal state and
 * can store a finite set of statement references (e.g. for error reporting), also indexed by variables. 
 *
 * @param <Var> The set of variables used to index over bound values.
 * @param <Value>	The type of values that can be bound. In Soot, this will typically be Value.
 * @param <State> The finite set of possible internal states.
 * @param <StmtID> The set of variables used to index over bound statements. 
 */
public class Abstraction<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> implements Cloneable {
	
	/** The values bound to the abstraction, indexed by Var-type variables. */
	protected Value[] boundValues;
	/** The Statement-Trace bound to the abstraction, indexed by StmtID-type variables. */
	protected Unit[] stmtTrace;
	/** The internal state. */
	protected State state;
	protected Object[] boundArrayValues;
	protected Value lastReplacedValue;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected final static Abstraction ZERO = new Abstraction() {
		{
			boundValues =  new Value[0];
			stmtTrace = new Unit[0];
		}
		public String toString() { return "<ZERO>"; };
		public Value getValue(Enum e) { return null; };
		public boolean stateIs(Enum s) { return false; }
		public boolean equals(Object obj) { return obj==this; }
		public int hashCode() { return 23; };
		public Abstraction replaceValue(Value fromVal, Value toVal) { return this; };
		protected Abstraction copy() { return new Abstraction(); };
		protected Value getBoundValue(int index) { return null; };
		protected Value getBoundValue(Enum var) { return null; };
		protected void setBoundVal(Value val, Enum var) { throw new UnsupportedOperationException(); };
		protected void setBoundVal(Value val, int index) { throw new UnsupportedOperationException(); };
		protected void setStatement(Unit u, Enum sid) { throw new UnsupportedOperationException(); };
		public Abstraction withStateChangedTo(Enum s) { return this; }
		public Unit getStatement(Enum sid) { return null; };
	};

	@SuppressWarnings("unchecked")
	public final static <Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> Abstraction<Var,State,StmtID> zero() {
		return ZERO;
	}
	
	private Abstraction() {
	}

	@SuppressWarnings("unchecked")
	public Abstraction(Var e, Value v, State s) {
		int size = e.getClass().getEnumConstants().length;		
		boundValues = (Value[]) new Object[size];
		boundValues[e.ordinal()] = v;
		boundArrayValues = (List<Value>[]) new Object[size];
		state = s;
	}
	
	public Value getValue(Var e) {
		if(boundValues==null) return null;
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
	public Abstraction<Var,State,StmtID> replaceValue(Value fromVal, Value toVal) {
		if(boundValues==null) return zero();
		Abstraction<Var,State,StmtID> copy = null;
		for (int i = 0; i < boundValues.length; i++) {
			Value val = getBoundValue(i);
			if(val != null){
			if(val instanceof Value){
				Value v =(Value) val;
				if(v.equivTo(fromVal)) {
				if(copy==null) copy = copy();
				copy.setBoundVal(toVal, i);
			}
			}else{
			if(val.equals(fromVal)) {
				if(copy==null) copy = copy();
				copy.setBoundVal(toVal, i);
			}
			if(val != null && val instanceof InstanceFieldRef){
				InstanceFieldRef ifr = (InstanceFieldRef) val;
				if(ifr.getBase().equals(fromVal) && ifr.getBaseBox().canContainValue(toVal)){
					ifr.setBase(toVal);
					copy.setBoundVal(ifr, i);	
				}
			}}
			replaceInBoundArrrayValues(fromVal, toVal, i);
		}}
		if(copy==null) return this;
		else return copy;
	}
	
	public Abstraction<Var,State,StmtID> storeStmt(Unit u, StmtID sid) {
		Unit storedStmt = getStatement(sid);
		if(storedStmt==null || !storedStmt.equals(u)) {
			Abstraction<Var,State,StmtID> copy = copy();
			copy.setStatement(u, sid);
			return copy;
		} else {
			return this;
		}
	}
	
	protected void setStatement(Unit u, StmtID sid) {
		if(stmtTrace==null) stmtTrace = new Unit[sid.getClass().getEnumConstants().length];
		stmtTrace[sid.ordinal()] = u;
	}
	
	/**
	 * This method is usually called for computing the abstractions to be passed
	 * at call and return flow functions. This method returns one copy of this abstraction
	 * for every value in from that is contained in this abstraction. Within that copy,
	 * the from-value has been replaced by the corresponding to-value from the other list.
	 * If an entry is <code>null</code> in the from or to list, then this entry is not processed.   
	 */
	public Set<Abstraction<Var,State,StmtID>> replaceValuesAndCopy(List<Value> from, List<Value> to) {
		assert(from.size()==to.size());
		
		Set<Abstraction<Var,State,StmtID>> res = new HashSet<Abstraction<Var,State,StmtID>>();
		if(this.equals(ZERO)){
			 res.add(this);
			 return res;
		}
		for(int i=0; i<from.size(); i++) {
			Value fromVal = from.get(i);
			Value toVal = to.get(i);
			if(fromVal!=null && toVal!=null) {
				Abstraction<Var,State,StmtID> derived = replaceValue(fromVal,toVal);
				if(derived!=this) {
					res.add(derived);
				}
			}
		}
		if(res.isEmpty()){
			res.add(this);
		}
		return res;
	}
	
	/**
	 * This method is called to replace all values in the from List to the appropriate values in the to List.
	 * But in contrast to the <code>Method replaceValuesAndCopy</code> it will do the replace on the object itself.
	 * @param from
	 * @param to
	 * @return
	 */
	public Abstraction<Var,  State, StmtID> replaceValues(
			List<Value> from, List<Value> to) {
		assert (from.size() == to.size());
		boolean didReplace = false;
		Abstraction<Var,  State, StmtID> copy = copy();

		if (boundValues == null)
			return zero();
		for (int l = 0; l < from.size(); l++) {
			Value fromVal = from.get(l);
			Value toVal = to.get(l);

			for (int i = 0; i < boundValues.length; i++) {
				Value val = getBoundValue(i);
				if (val != null && val.equivTo(fromVal)) {
					didReplace = true;
					copy.setBoundVal(toVal, i);					
				}
				if(val != null && val instanceof InstanceFieldRef){
					InstanceFieldRef ifr = (InstanceFieldRef) val;
					if(ifr.getBase().equivTo(fromVal) && ifr.getBaseBox().canContainValue(toVal)){
						ifr.setBase( toVal);
						copy.setBoundVal(ifr, i);	
						didReplace = true;
					}
				}
				replaceInBoundArrrayValues(fromVal, toVal, i);
			}
		}
		if (didReplace) {
			return copy;
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public void replaceInBoundArrrayValues(Value fromVal, Value toVal, int i) {
		if(boundArrayValues != null && boundArrayValues[i] != null){
			HashSet<Value> array = (HashSet<Value>) boundArrayValues[i];
			if(array.remove(fromVal)){
				lastReplacedValue = fromVal;
				if(toVal != null)
					array.add(toVal);
			}
		}
	}
	public boolean removeFromBoundArrrayValues(Value remVal, Var var) {
		if(boundArrayValues != null && boundArrayValues[var.ordinal()] != null){
			@SuppressWarnings("unchecked")
			HashSet<Value> set = (HashSet<Value>) boundArrayValues[var.ordinal()];
			return set.remove(remVal);				
		}
		return false;
	}
	public Set<? extends Abstraction<Var,State,StmtID>> bindValue(Value addedValue, Var var) {
		if(getBoundValue(var)==null) {
			final Abstraction<Var,State,StmtID> copy = copy();
			copy.setBoundVal(addedValue, var);
			return twoElementSet(this, copy); 
		}
		this.setBoundVal(addedValue, var);
		return singleton(this);
	}

	protected Value getBoundValue(Var var) {
		return getBoundValue(var.ordinal());
	}

	protected Value getBoundValue(int index) {
		if(boundValues == null){
			return null;
		}
		return boundValues[index];
	}

	protected void setBoundVal(Value val, Var var) {
		if(boundValues==null) {
			boundValues = new Value[var.getClass().getEnumConstants().length];
		}
		setBoundVal(val, var.ordinal());
	}

	protected void setBoundVal(Value val, int index) {
		boundValues[index] = val;
	}
	
	public Abstraction<Var,State,StmtID> withStateChangedTo(State s) {
		if(state != null && state.equals(s))
			return this;
		Abstraction<Var,State,StmtID> copy = copy();
		copy.state = s;
		return copy;
	}
	public  Abstraction<Var,State,StmtID> initializeArrayValue(Var var){
		Abstraction<Var,  State, StmtID> res;
		if(this.equals(ZERO)){
			res = copy();
		} else {
			res = this;
		}
		if(res.boundArrayValues==null) {
			int size = var.getClass().getEnumConstants().length;
			res.boundArrayValues = new HashSet[size];
			res.boundArrayValues[var.ordinal()] = new HashSet<Value>();
		}
		return res;
	}

	
	@SuppressWarnings("unchecked")
	protected void pushToArray(Value op,int index){
		if(boundArrayValues == null){
			return;
			//throw new RuntimeException("initializeArrayValue has to be called first!");
		}
		HashSet<Value> list = (HashSet<Value>) boundArrayValues[index];
		if(list == null){
			throw new RuntimeException("Variable is not initialized!");
		}
		if(!list.contains(op)){
			list.add(op);
		}
		boundArrayValues[index] = list;
	} 
	public Object getArrayValues(Var var){
		if(var == null){
			return null;
		}
		return getArrayValues(var.ordinal());
	}
	

	public Object getArrayValues(int i){
		if(boundArrayValues == null){
			return null;
		}
		return boundArrayValues[i];
	}
	public Object getArrayValues(Value val){
		for(int i = 0; i < boundValues.length; i++){
			if(val.equals(boundValues[i])){
				return getArrayValues(i);
			}
		}
		return null;
	}
	public  Abstraction<Var,State,StmtID> pushArrayValue(Value rOp, Value arrayBase){

		for (int i = 0; i < boundValues.length; i++) {
			Value val = getBoundValue(i);
			if (val != null && val.equals(arrayBase)) {
				pushToArray(rOp, i);
			}
		}
		return this;
	}
	public boolean stateIs(State s) {
		return state!=null && state.equals(s);
	}

	protected Abstraction<Var,State,StmtID> copy() {
		try {
			@SuppressWarnings("unchecked")
			Abstraction<Var,State,StmtID> clone = (Abstraction<Var,State,StmtID>) super.clone();
			if(boundValues!=null) clone.boundValues = boundValues.clone();
			if(stmtTrace!=null) clone.stmtTrace = stmtTrace.clone();
			if(boundArrayValues!=null) clone.boundArrayValues = boundArrayValues.clone();
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
		if (!Arrays.equals(boundArrayValues, other.boundArrayValues))
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
		String boundArrays = "";
		if(boundArrayValues != null){
			boundArrays +="[";
			for(Object o : boundArrayValues){
				if(o != null)
					boundArrays += o.toString();
			}
			boundArrays +="]";
		}
		return "Abstraction [boundValues=" + Arrays.toString(boundValues)
				+ ", stmtTrace=" + Arrays.toString(stmtTrace)
				+ (!boundArrays.equals("") ? ", boundArrayValues=" + boundArrays :"")
				+ ", state=" + state + "]";
	}

	public Abstraction<Var, State, StmtID> destroyLocals(
			Collection<Local> destroyLocals) {

		if (boundValues == null)
			return null;
		Iterator<Local> it = destroyLocals.iterator();
		while(it.hasNext()){
			Local destroyVal = it.next();

			for (int i = 0; i < boundValues.length; i++) {
				Value val = getBoundValue(i);
				if (val != null && val.equivTo(destroyVal)) {
					setBoundVal(null, i);					
				}
				replaceInBoundArrrayValues(destroyVal, null, i);
			}
		}
		for (int i = 0; i < boundValues.length; i++) {
			Value val = getBoundValue(i);
			if (val != null) {
				return this;					
			}
		}
		return null;
	}
}