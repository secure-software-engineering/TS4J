package de.fraunhofer.sit.codescan.typestate.analysis;

import soot.Scene;
import soot.SootMethod;


public class Config<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>>
implements Do<Var,State,StmtID>, CallContext<Var,State,StmtID>, ValueContext<Var,State,StmtID>, VarContext<Var,State,StmtID>, Done<Var,State,StmtID> {
	
	private final static int THIS = 0;
	private final static int RETURN = 1;
	
	SootMethod method;
	Var[] trackedVars;
	int currSlot = -1;
	State state;
	
	@SuppressWarnings("unchecked")
	public CallContext<Var,State,StmtID> atCallTo(String signature) {
		method = Scene.v().getMethod(signature);		
		//two extra slots for this and return value
		trackedVars = (Var[]) new Object[method.getParameterCount()+2]; 
		return this;
	}

	public ValueContext<Var,State,StmtID> trackThis() {
		currSlot = THIS;
		return this;
	}

	public ValueContext<Var,State,StmtID> trackReturn() {
		currSlot = RETURN;
		return this;
	}

	public ValueContext<Var,State,StmtID> trackParameter(int i) {
		if(i<0||i>method.getParameterCount()-1) {
			throw new IllegalArgumentException("Invalid parameter index");
		}
		currSlot = i+2;
		return this;
	}

	public VarContext<Var,State,StmtID> as(Var var) {
		trackedVars[currSlot] = var;
		return this;
	}

	public Done<Var,State,StmtID> inState(State s) {
		state = s;
		return this;
	}
	
//	
//	enum V { VALUE_GROUP, MODEL_VALUE };
//	
//	enum S { FLUSHED, TAINTED };
//	
//	enum U { VALUE_ADD_CALL, MODEL_VALUE_UPDATE };
//	
//	public static void main(String[] args) {
//		Do<V,S,U> c = new Config<V,S,U>();
//		Done<V, S, U> inState = c.atCallTo("void foo()").trackThis().as(V.VALUE_GROUP).inState(S.FLUSHED);
//	}
	
}



interface Do<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public CallContext<Var,State,StmtID> atCallTo(String signature);
}

interface CallContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public ValueContext<Var,State,StmtID> trackThis();
	public ValueContext<Var,State,StmtID> trackReturn();
	public ValueContext<Var,State,StmtID> trackParameter(int paramIndex);
}

interface ValueContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public VarContext<Var,State,StmtID> as(Var var);
}

interface VarContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public Done<Var,State,StmtID> inState(State s);
}

interface Done<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {	
}


enum ValueId {
	BASE(0), RETURN(-1), PARAM1(1), PARAM2(2), PARAM3(3), PARAM4(4), PARAM5(5);
	protected final int paramNum;
	ValueId(int i) { this.paramNum = i; }
} 