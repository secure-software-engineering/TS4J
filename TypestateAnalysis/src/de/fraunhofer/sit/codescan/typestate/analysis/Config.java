package de.fraunhofer.sit.codescan.typestate.analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;


public class Config<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>>
implements Do<Var,State,StmtID>, CallContext<Var,State,StmtID>, ValueContext<Var,State,StmtID>, VarContext<Var,State,StmtID>, Done<Var,State,StmtID>,
EqualsContext<Var,State,StmtID>, IfCheckContext<Var,State,StmtID> {
	
	private final static int THIS = 0;
	private final static int RETURN = 1;
	
	SootMethod method;
	Stmt invokeStmt;
	Set<Abstraction<Var, Value, State, StmtID>> abstractions;
	int currSlot = -1;
	Var eqCheckVar;
	boolean done;
	
	@SuppressWarnings("serial")
	public Config(final Abstraction<Var, Value, State, StmtID> abstraction, Stmt invokeStmt) {
		this(new HashSet<Abstraction<Var, Value, State, StmtID>>(){{
			add(abstraction);
		}}, invokeStmt);
	}
	
	public Config(Set<Abstraction<Var, Value, State, StmtID>> abstractions, Stmt invokeStmt) {
		this.abstractions = abstractions;
		this.invokeStmt = invokeStmt;
		this.done = false;
	}

	public IfCheckContext<Var, State, StmtID> atAnyCallToClass(String className) {
		if(done) return this;
		SootMethod calledMethod = invokeStmt.getInvokeExpr().getMethod();
		if(Scene.v().getActiveHierarchy().isClassSubclassOfIncluding(
				calledMethod.getDeclaringClass(), Scene.v().getSootClass(className))) {
			method = calledMethod;
		} else {
			done = true;
		}
		return this;
	}

	public IfCheckContext<Var, State, StmtID> atCallTo(String signature) {
		if(done) return this;
		SootMethod calledMethod = invokeStmt.getInvokeExpr().getMethod();
		if(calledMethod.getSignature().equals(signature)) {
			method = calledMethod;
		} else {
			done = true;
		}
		return this;
	}

	public ValueContext<Var,State,StmtID> trackThis() {
		if(done) return this;
		currSlot = THIS;
		return this;
	}

	public ValueContext<Var,State,StmtID> trackReturnValue() {
		if(done) return this;
		currSlot = RETURN;
		return this;
	}

	public ValueContext<Var,State,StmtID> trackParameter(int i) {
		if(done) return this;
		if(i<0||i>method.getParameterCount()-1) {
			throw new IllegalArgumentException("Invalid parameter index");
		}
		currSlot = i+2;
		return this;
	}

	public VarContext<Var,State,StmtID> as(Var var) {
		if(done) return this;
		
		Value addedValue = extractValue();
		if(addedValue==null) return this;

		Set<Abstraction<Var,Value,State,StmtID>> newAbstractions = new HashSet<Abstraction<Var,Value,State,StmtID>>(); 
		for(Abstraction<Var,Value,State,StmtID>  abs: abstractions) {
			newAbstractions.addAll(abs.bindValue(addedValue, var));
		}
		abstractions = newAbstractions;
		return this;
	}

	private Value extractValue() {
		InvokeExpr ie = invokeStmt.getInvokeExpr();
		Value val;
		switch(currSlot) {
		case THIS:
			val = ((InstanceInvokeExpr)ie).getBase();
			break;
		case RETURN:
			if(invokeStmt instanceof DefinitionStmt) {
				DefinitionStmt defnStmt = (DefinitionStmt) invokeStmt;
				val = defnStmt.getLeftOp();
			} else {
				return null;
			}
		default:
			val = ie.getArg(currSlot);
			break;
		}
		return val;
	}

	public Done<Var,State,StmtID> toState(State s) {
		if(done) return this;

		Set<Abstraction<Var,Value,State,StmtID>> newAbstractions = new HashSet<Abstraction<Var,Value,State,StmtID>>(); 
		for(Abstraction<Var,Value,State,StmtID>  abs: abstractions) {
			newAbstractions.add(abs.withStateChangedTo(s));
		}
		abstractions = newAbstractions;

		return this;
	}
	
	public Set<Abstraction<Var, Value, State, StmtID>> getAbstractions() {
		return abstractions;
	}

	public Do<Var,State,StmtID> andAlso() {
		//TODO should we instead implement an "or" semantics here, applying this to the old abstractions?
		return new Config<Var,State,StmtID>(abstractions, invokeStmt);
	}

	public CallContext<Var, State, StmtID> always() {
		return this;
	}

	public EqualsContext<Var, State, StmtID> ifValueBoundTo(Var var) {
		if(done) return this;
		assert var!=null;
		eqCheckVar = var;
		return this;
	}

	public CallContext<Var, State, StmtID> equalsThis() {
		if(done) return this;
		currSlot = THIS;
		return computeEquals();
	}

	public CallContext<Var, State, StmtID> equalsReturnValue() {
		if(done) return this;
		currSlot = RETURN;
		return computeEquals();
	}

	public CallContext<Var, State, StmtID> equalsParameter(int paramIndex) {
		if(done) return this;
		currSlot = paramIndex;
		return computeEquals();
	}

	private CallContext<Var, State, StmtID> computeEquals() {
		Value v = extractValue();
		if(v==null) return this;
		//TODO should we do non-destructive updates here?
		for (Iterator<Abstraction<Var,Value,State,StmtID>> i = abstractions.iterator(); i.hasNext();) {
			Abstraction<Var,Value,State,StmtID> abs = i.next();
			if(abs.getValue(eqCheckVar)==null || !abs.getValue(eqCheckVar).equals(extractValue())) {
				i.remove();
			}			
		}
		if(abstractions.isEmpty())
			done = true;
		return this;
	}
}



interface Do<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public IfCheckContext<Var,State,StmtID> atCallTo(String methodSignature);
	public IfCheckContext<Var,State,StmtID> atAnyCallToClass(String className);
}

interface EqualsContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public CallContext<Var,State,StmtID> equalsThis();
	public CallContext<Var,State,StmtID> equalsReturnValue();
	public CallContext<Var,State,StmtID> equalsParameter(int paramIndex);
}

interface CallContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends VarContext<Var,State,StmtID> {
	public ValueContext<Var,State,StmtID> trackThis();
	public ValueContext<Var,State,StmtID> trackReturnValue();
	public ValueContext<Var,State,StmtID> trackParameter(int paramIndex);
}

interface IfCheckContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends VarContext<Var,State,StmtID> {
	public EqualsContext<Var,State,StmtID> ifValueBoundTo(Var var);
	public CallContext<Var,State,StmtID> always();
}

interface ValueContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public VarContext<Var,State,StmtID> as(Var var);
}

interface VarContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends Done<Var,State,StmtID> {
	public Done<Var,State,StmtID> toState(State s);
}

interface Done<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {	
	public Do<Var,State,StmtID> andAlso();
}


enum ValueId {
	BASE(0), RETURN(-1), PARAM1(1), PARAM2(2), PARAM3(3), PARAM4(4), PARAM5(5);
	protected final int paramNum;
	ValueId(int i) { this.paramNum = i; }
} 