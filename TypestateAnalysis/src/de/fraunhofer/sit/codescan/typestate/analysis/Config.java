package de.fraunhofer.sit.codescan.typestate.analysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.fraunhofer.sit.codescan.sootbridge.ErrorMarker;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;


public class Config<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>>
implements AtCallToReturn<Var,State,StmtID>, CallContext<Var,State,StmtID>, ValueContext<Var,State,StmtID>, VarContext<Var,State,StmtID>, Done<Var,State,StmtID>,
EqualsContext<Var,State,StmtID>, IfCheckContext<Var,State,StmtID>, AtReturn<Var,State,StmtID>, ReportError<Var,State,StmtID> {
	
	private static final String ANY_METHOD = "*";
	private final static int THIS = -1;
	private final static int RETURN = -2;
	
	SootMethod method;
	Stmt invokeStmt;
	int currSlot = -1;
	Var eqCheckVar;
	Set<Abstraction<Var, Value, State, StmtID>> originalAbstractions;
	Set<Abstraction<Var, Value, State, StmtID>> abstractions;
	boolean filteredOut = false;
	Config<Var,State,StmtID> next;
	private final SootMethod calleeAtReturnFlow;
	private final IIFDSAnalysisContext context;
	private String errorMessage;

	public Config(final Abstraction<Var, Value, State, StmtID> abstraction, Stmt invokeStmt, IIFDSAnalysisContext context) {
		this(abstraction, invokeStmt, context, null);
	}
	
	@SuppressWarnings("serial")
	public Config(final Abstraction<Var, Value, State, StmtID> abstraction, Stmt invokeStmt, IIFDSAnalysisContext context, SootMethod callee) {
		this(new HashSet<Abstraction<Var, Value, State, StmtID>>(){{
			add(abstraction);
		}}, invokeStmt, context, callee);
	}
	
	public Config(Set<Abstraction<Var, Value, State, StmtID>> abstractions, Stmt invokeStmt, IIFDSAnalysisContext context, SootMethod callee) {
		this.context = context;
		this.calleeAtReturnFlow = callee;
		this.abstractions = new HashSet<Abstraction<Var, Value, State, StmtID>>(abstractions);
		this.originalAbstractions = Collections.unmodifiableSet(abstractions);
		this.invokeStmt = invokeStmt;		
	}


	public IfCheckContext<Var, State, StmtID> atAnyCallToClass(String className) {
		if(abstractions.isEmpty()) return this;

		if(invokeStmt!=null) {
			SootMethod calledMethod = invokeStmt.getInvokeExpr().getMethod();
			if(Scene.v().getActiveHierarchy().isClassSubclassOfIncluding(
					calledMethod.getDeclaringClass(), Scene.v().getSootClass(className))) {
				method = calledMethod;
				return this;
			}
		} 
		
		//the filter did not match
		noMatch();
		return this;
	}

	private void noMatch() {
		filteredOut = true;
		abstractions.clear();
	}

	public IfCheckContext<Var, State, StmtID> atCallTo(String signature) {
		if(abstractions.isEmpty()) return this;

		if(invokeStmt!=null) {
			SootMethod calledMethod = invokeStmt.getInvokeExpr().getMethod();
			if(calledMethod.getSignature().equals(signature)) {
				method = calledMethod;
				return this;
			}
		}
		
		//the filter did not match
		noMatch();
		return this;
	}

	public ValueContext<Var,State,StmtID> trackThis() {
		if(abstractions.isEmpty()) return this;
		currSlot = THIS;
		return this;
	}

	public ValueContext<Var,State,StmtID> trackReturnValue() {
		if(abstractions.isEmpty()) return this;
		currSlot = RETURN;
		return this;
	}

	public ValueContext<Var,State,StmtID> trackParameter(int i) {
		if(abstractions.isEmpty()) return this;
		if(i<0||i>method.getParameterCount()-1) {
			throw new IllegalArgumentException("Invalid parameter index");
		}
		currSlot = i;
		return this;
	}

	public VarContext<Var,State,StmtID> as(Var var) {
		if(abstractions.isEmpty()) return this;
		
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
		if(ie==null)
			throw new IllegalArgumentException("Cannot extract values at return.");
		
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
		if(abstractions.isEmpty()) return this;

		Set<Abstraction<Var,Value,State,StmtID>> newAbstractions = new HashSet<Abstraction<Var,Value,State,StmtID>>(); 
		for(Abstraction<Var,Value,State,StmtID>  abs: abstractions) {
			newAbstractions.add(abs.withStateChangedTo(s));
		}
		abstractions = newAbstractions;

		return this;
	}
	
	public Set<Abstraction<Var, Value, State, StmtID>> getAbstractions() {
		Set<Abstraction<Var, Value, State, StmtID>> res = new HashSet<Abstraction<Var,Value,State,StmtID>>();
		if(fillAbstractions(res)) {
			res.addAll(originalAbstractions);
		}
		return res;
	}
	
	private boolean fillAbstractions(Set<Abstraction<Var, Value, State, StmtID>> returnValue) {
		boolean nextFilteredOut = true;
		if(next!=null)
			nextFilteredOut = next.fillAbstractions(returnValue);
		if(!filteredOut) returnValue.addAll(abstractions);
		return filteredOut && nextFilteredOut;
	}

	public AtCallToReturn<Var,State,StmtID> orElse() {
		assert(next==null);
		next = new Config<Var,State,StmtID>(originalAbstractions, invokeStmt, context, calleeAtReturnFlow);
		return next;
	}

	public CallContext<Var, State, StmtID> always() {
		return this;
	}

	public EqualsContext<Var, State, StmtID> ifValueBoundTo(Var var) {
		if(abstractions.isEmpty()) return this;
		assert var!=null;
		eqCheckVar = var;
		return this;
	}

	public CallContext<Var, State, StmtID> equalsThis() {
		if(abstractions.isEmpty()) return this;
		currSlot = THIS;
		return computeEquals();
	}

	public CallContext<Var, State, StmtID> equalsReturnValue() {
		if(abstractions.isEmpty()) return this;
		currSlot = RETURN;
		return computeEquals();
	}

	public CallContext<Var, State, StmtID> equalsParameter(int paramIndex) {
		if(abstractions.isEmpty()) return this;
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
		if(abstractions.isEmpty()) noMatch();
		return this;
	}

	public IfCheckContext<Var, State, StmtID> and() {
		return this;
	}

	public Done<Var, State, StmtID> storeStmtAs(StmtID sid) {
		if(abstractions.isEmpty()) return this;
		if(invokeStmt==null)
			throw new IllegalArgumentException("Atempting to store call statement at return");
		Set<Abstraction<Var, Value, State, StmtID>> res = new HashSet<Abstraction<Var,Value,State,StmtID>>(abstractions.size());
		for(Abstraction<Var, Value, State, StmtID> abs: abstractions) {
			res.add(abs.storeStmt(invokeStmt, sid));
		}
		this.abstractions = res;
		return this;
	}

	public IfCheckContext<Var, State, StmtID> atAnyReturn() {
		return atReturnFrom(ANY_METHOD);
	}
	
	public IfCheckContext<Var, State, StmtID> atReturnFrom(String signature) {
		if(abstractions.isEmpty()) return this;
		if(calleeAtReturnFlow!=null) {
			if(signature.equals(ANY_METHOD)) return this;
			if(calleeAtReturnFlow.getSignature().equals(signature)) {
				method = calleeAtReturnFlow;
				return this;
			}
		}
		
		//filter did not match
		noMatch();
		return this;
	}

	public CallContext<Var, State, StmtID> ifInState(State s) {
		if(abstractions.isEmpty()) return this;
		if(s==null) throw new IllegalArgumentException("State must not be null");
		//TODO should we do non-destructive updates here?
		for (Iterator<Abstraction<Var,Value,State,StmtID>> i = abstractions.iterator(); i.hasNext();) {
			Abstraction<Var,Value,State,StmtID> abs = i.next();
			if(!abs.stateIs(s)) {
				i.remove();
			}			
		}
		if(abstractions.isEmpty()) noMatch();
		return this;
	}
	
	public ReportError<Var,State,StmtID> reportError(String errorMessage) {
		if(abstractions.isEmpty()) return this;
		this.errorMessage = errorMessage;
		return this;
	}

	public Done<Var, State, StmtID> atStmt(StmtID sid) {
		if(abstractions.isEmpty()) return this;
		for(Abstraction<Var,Value,State,StmtID> abs: abstractions) {
			Unit stmt = abs.getStatement(sid);
			//TODO what if stmt==null? do we allow this to happen?
			if(stmt!=null) {
				String className = context.getICFG().getMethodOf(stmt).getDeclaringClass().getName();
				context.reportError(new ErrorMarker(errorMessage,className,stmt.getJavaSourceStartLineNumber()));
			}
		}
		return null;
	}
}

interface AtCallToReturn<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public IfCheckContext<Var,State,StmtID> atCallTo(String methodSignature);
	public IfCheckContext<Var,State,StmtID> atAnyCallToClass(String className);
}

interface AtReturn<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public IfCheckContext<Var,State,StmtID> atReturnFrom(String methodSignature);
	public IfCheckContext<Var, State, StmtID> atAnyReturn();
}

interface EqualsContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public CallContext<Var,State,StmtID> equalsThis();
	public CallContext<Var,State,StmtID> equalsReturnValue();
	public CallContext<Var,State,StmtID> equalsParameter(int paramIndex);
}

interface CallContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends VarContext<Var,State,StmtID> {
	public IfCheckContext<Var,State,StmtID> and();
	public ValueContext<Var,State,StmtID> trackThis();
	public ValueContext<Var,State,StmtID> trackReturnValue();
	public ValueContext<Var,State,StmtID> trackParameter(int paramIndex);
	public ReportError<Var,State,StmtID> reportError(String errorMessage);
}

interface IfCheckContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends VarContext<Var,State,StmtID> {
	public EqualsContext<Var,State,StmtID> ifValueBoundTo(Var var);
	public CallContext<Var,State,StmtID> always();
	public CallContext<Var,State,StmtID> ifInState(State s);
}

interface ValueContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public VarContext<Var,State,StmtID> as(Var var);
}

interface VarContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends Done<Var,State,StmtID> {
	public Done<Var,State,StmtID> toState(State s);
}

interface Done<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {	
	public AtCallToReturn<Var,State,StmtID> orElse();
	public Done<Var,State,StmtID> storeStmtAs(StmtID sid);
}

interface ReportError<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {	
	public Done<Var,State,StmtID> atStmt(StmtID sid);
}


enum ValueId {
	BASE(0), RETURN(-1), PARAM1(1), PARAM2(2), PARAM3(3), PARAM4(4), PARAM5(5);
	protected final int paramNum;
	ValueId(int i) { this.paramNum = i; }
} 