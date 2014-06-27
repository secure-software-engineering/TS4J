package de.fraunhofer.sit.codescan.sootbridge.typestate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.Stmt;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
import de.fraunhofer.sit.codescan.sootbridge.ErrorMarker;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCollection;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.CallContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.EqualsContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.IfCheckContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.ReportError;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.ValueContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.VarContext;
import de.fraunhofer.sit.codescan.sootbridge.util.MethodWithAnnotatedParameters;

public class Config<Var extends Enum<Var>, State extends Enum<State>, StmtID extends Enum<StmtID>>
		implements AtCallToReturn<Var, State, StmtID>,AtCollection<Var, State, StmtID>,
		CallContext<Var, State, StmtID>, ValueContext<Var, State, StmtID>,
		VarContext<Var, State, StmtID>, Done<Var, State, StmtID>,
		EqualsContext<Var, State, StmtID>, IfCheckContext<Var, State, StmtID>,
		AtReturn<Var, State, StmtID>, ReportError<Var, State, StmtID>,
		AtNormalEdge<Var, State, StmtID> {

	private static final String ANY_METHOD = "*";
	private final static int THIS = -1;
	private final static int RETURN = -2;
	private final static int CONSTANT = -3;

	SootMethod method;
	Stmt invokeStmt;
	int currSlot = -1;
	Var eqCheckVar;
	Var asArray;
	Set<Abstraction<Var, State, StmtID>> originalAbstractions;
	Set<Abstraction<Var, State, StmtID>> abstractions;
	Set<Abstraction<Var, State, StmtID>> stillToProve;
	boolean filteredOut = false;
	boolean not = false;
	Config<Var, State, StmtID> next;
	private final SootMethod calleeAtReturnFlow;
	private final IIFDSAnalysisContext context;
	private String errorMessage;
	private ArrayList<Integer> currSlotArray = null;
	private Var replaceInArray;

	public Config(final Abstraction<Var, State, StmtID> abstraction,
			Stmt invokeStmt, IIFDSAnalysisContext context) {
		this(abstraction, invokeStmt, context, null);
	}

	@SuppressWarnings("serial")
	public Config(final Abstraction<Var, State, StmtID> abstraction,
			Stmt invokeStmt, IIFDSAnalysisContext context, SootMethod callee) {
		this(new HashSet<Abstraction<Var, State, StmtID>>() {
			{
				add(abstraction);
			}
		}, invokeStmt, context, callee);
	}

	public Config(Set<Abstraction<Var, State, StmtID>> abstractions,
			Stmt invokeStmt, IIFDSAnalysisContext context, SootMethod callee) {
		this.context = context;
		this.calleeAtReturnFlow = callee;
		this.abstractions = new HashSet<Abstraction<Var, State, StmtID>>(
				abstractions);
		this.originalAbstractions = Collections.unmodifiableSet(abstractions);
		this.invokeStmt = invokeStmt;
		this.stillToProve = new HashSet<Abstraction<Var, State, StmtID>>();
	}

	public IfCheckContext<Var, State, StmtID> atAnyCallToClass(String className) {
		if (abstractions.isEmpty())
			return this;

		if (invokeStmt != null) {
			SootMethod calledMethod = invokeStmt.getInvokeExpr().getMethod();
			if (Scene
					.v()
					.getActiveHierarchy()
					.isClassSubclassOfIncluding(
							calledMethod.getDeclaringClass(),
							Scene.v().getSootClass(className))) {
				method = calledMethod;
				return this;
			}
		}

		// the filter did not match
		noMatch();
		return this;
	}

	private void noMatch() {
		filteredOut = true;
		abstractions.clear();
	}

	public IfCheckContext<Var, State, StmtID> atCallTo(String signature) {
		return atCallToHelper(signature, false);
	}

	public IfCheckContext<Var, State, StmtID> atCallToWithRegex(String regex) {
		return atCallToHelper(regex, true);
	}
	private IfCheckContext<Var,State,StmtID> atCallToHelper(String signature, boolean regex){
		if (abstractions.isEmpty())
			return this;

		if (invokeStmt != null) {
			SootMethod calledMethod = invokeStmt.getInvokeExpr().getMethod();
			// TODO should allow for fuzzy matching of declaring type
			if ((!regex && calledMethod.getSignature().equals(signature)) || (regex && calledMethod.getSignature().matches(signature))) {
				method = calledMethod;
				return this;
			}
		}

		// the filter did not match
		noMatch();
		return this;
	}
	public IfCheckContext<Var, State, StmtID> atReturnFromMethodOfStmt(
			StmtID sid) {
		if (abstractions.isEmpty())
			return this;

		if (calleeAtReturnFlow != null) {
			for (Iterator<Abstraction<Var, State, StmtID>> i = abstractions
					.iterator(); i.hasNext();) {
				Abstraction<Var, State, StmtID> abs = i.next();
				Unit stmt = abs.getStatement(sid);
				if (stmt == null
						|| !context.getICFG().getMethodOf(stmt)
								.equals(calleeAtReturnFlow)) {
					i.remove();
				}
			}
		}

		if (abstractions.isEmpty())
			// the filter did not match
			noMatch();
		return this;
	}

	public ValueContext<Var, State, StmtID> trackThis() {
		if (abstractions.isEmpty())
			return this;
		currSlot = THIS;
		return this;
	}

	public ValueContext<Var, State, StmtID> trackReturnValue() {
		if (abstractions.isEmpty())
			return this;
		currSlot = RETURN;
		return this;
	}

	public ValueContext<Var, State, StmtID> trackParameter(int i) {
		if (abstractions.isEmpty())
			return this;
		if (i < 0 || i > method.getParameterCount() - 1) {
			throw new IllegalArgumentException("Invalid parameter index");
		}
		currSlot = i;
		return this;
	}

	public CallContext<Var, State, StmtID> as(Var var) {
		if (abstractions.isEmpty())
			return this;
		Set<Abstraction<Var, State, StmtID>> newAbstractions = new HashSet<Abstraction<Var, State, StmtID>>();
		
		if(currSlotArray != null){
			for (int a : currSlotArray){
				InvokeExpr ie = invokeStmt.getInvokeExpr();
				Value addedValue = ie.getArg(a);
				if(addedValue == null){
					continue;
				}
				for (Abstraction<Var, State, StmtID> abs : abstractions) {
					newAbstractions.addAll(abs.bindValue(addedValue,var));
				}
			}
			currSlotArray= null;
		}else{
			Value addedValue = extractValue();
			if (addedValue == null)
				return this;
	
			for (Abstraction<Var, State, StmtID> abs : abstractions) {
				newAbstractions.addAll(abs.bindValue(addedValue,var));
			}
		}
		abstractions = newAbstractions;
		
		return this;
	}

	private Value extractValue() {
		if (!invokeStmt.containsInvokeExpr()) {
			if (currSlot == CONSTANT && invokeStmt instanceof DefinitionStmt) {
				DefinitionStmt defnStmt = (DefinitionStmt) invokeStmt;
				return defnStmt.getRightOp();
			}

		}
		Value val;
		InvokeExpr ie = invokeStmt.getInvokeExpr();
		switch (currSlot) {
		case THIS:
			val = ((InstanceInvokeExpr) ie).getBase();
			break;
		case RETURN:
			if (invokeStmt instanceof DefinitionStmt) {
				DefinitionStmt defnStmt = (DefinitionStmt) invokeStmt;
				val = defnStmt.getLeftOp();
			} else {
				return null;
			}
			break;
		case CONSTANT:
			return null;
		default:
			val = ie.getArg(currSlot);
			break;
		}
		return val;
	}

	public Done<Var, State, StmtID> toState(State s) {
		if (abstractions.isEmpty())
			return this;

		Set<Abstraction<Var, State, StmtID>> newAbstractions = new HashSet<Abstraction<Var, State, StmtID>>();
		for (Abstraction<Var, State, StmtID> abs : abstractions) {
			newAbstractions.add(abs.withStateChangedTo(s));
		}
		abstractions = newAbstractions;

		return this;
	}

	public Set<Abstraction<Var, State, StmtID>> getAbstractions() {
		// TODO is it really ok to store a global filteredOut flag?
		// or does this need to be done per abstraction?
		Set<Abstraction<Var, State, StmtID>> res = new HashSet<Abstraction<Var, State, StmtID>>();
		if (fillAbstractions(res)) {
			res.addAll(originalAbstractions);
		}
		res.addAll(stillToProve);
		return res;
	}

	private boolean fillAbstractions(
			Set<Abstraction<Var, State, StmtID>> returnValue) {
		boolean nextFilteredOut = true;
		if (next != null)
			nextFilteredOut = next.fillAbstractions(returnValue);
		if (!filteredOut)
			returnValue.addAll(abstractions);
		return filteredOut && nextFilteredOut;
	}

	public AtCollection<Var, State, StmtID> orElse() {
		assert (next == null);
		next = new Config<Var, State, StmtID>(originalAbstractions, invokeStmt,
				context, calleeAtReturnFlow);
		return next;
	}

	public CallContext<Var, State, StmtID> always() {
		return this;
	}

	public EqualsContext<Var, State, StmtID> ifValueBoundTo(Var var) {
		
		if (abstractions.isEmpty())
			return this;
		assert var != null;
		eqCheckVar = var;
		return this;
	}

	public CallContext<Var, State, StmtID> equalsThis() {
		if (abstractions.isEmpty())
			return this;
		currSlot = THIS;
		return computeEquals();
	}

	public CallContext<Var, State, StmtID> equalsReturnValue() {
		if (abstractions.isEmpty())
			return this;
		currSlot = RETURN;
		return computeEquals();
	}

	public CallContext<Var, State, StmtID> equalsParameter(int paramIndex) {
		if (abstractions.isEmpty())
			return this;
		currSlot = paramIndex;
		return computeEquals();
	}
	public CallContext<Var, State, StmtID> eachEqualsInstance(Class<?> instance) {
		if (abstractions.isEmpty())
			return this;

		if(invokeStmt instanceof AssignStmt){
			AssignStmt as = (AssignStmt) invokeStmt;
			Value lOp = as.getLeftOp();
			Value rOp = as.getRightOp();
			for (Iterator<Abstraction<Var, State, StmtID>> i = abstractions
					.iterator(); i.hasNext();) {
				Abstraction<Var, State, StmtID> abs = i.next();
					if(abs.lastReplacedValue != null && abs.lastReplacedValue.equals(lOp)){
						if(instance.isInstance(rOp)){
							boolean removed = abs.removeFromBoundArrrayValues(rOp,eqCheckVar);
							@SuppressWarnings("unchecked")
							HashSet<Value> arrayValues = (HashSet<Value>)abs.getArrayValues(eqCheckVar);
							if(arrayValues != null && arrayValues.size() == 0 && removed){
								return this;
							}
						}
					}
					if(!instance.isInstance(rOp)){
						if(lOp instanceof ArrayRef){
							abs.pushArrayValue(as.getRightOp(),((ArrayRef) lOp).getBase());
						} 
					} 
					@SuppressWarnings("unchecked")
					HashSet<Value> arrayValues = (HashSet<Value>)abs.getArrayValues(rOp);
					if(arrayValues != null && arrayValues.size() > 0){
						stillToProve.add(abs);
					}else if(rOp instanceof NewArrayExpr){
						return this;
					}
				i.remove();
			}
		}
		if (abstractions.isEmpty())
			noMatch();
		return this;
	}
	public CallContext<Var, State, StmtID> equalsConstant(Class<?> instance) {
		if (abstractions.isEmpty())
			return this;
	
		for (Iterator<Abstraction<Var, State, StmtID>> i = abstractions
				.iterator(); i.hasNext();) {
			Abstraction<Var, State, StmtID> abs = i.next();
		
			Value v = abs.getValue(eqCheckVar);
			if(v == null){
				i.remove();
			} else{
				boolean filter = !instance.isInstance(v);
				if(not){
					filter = !filter;
				}
				if (filter) {
					i.remove();
				}
			}
		}
		not = false;
		
		if (abstractions.isEmpty())
			noMatch();
		return this;
	}
	private CallContext<Var, State, StmtID> computeEquals() {
		Value v = extractValue();
		if (v == null)
			return this;
		// TODO should we do non-destructive updates here?
		for (Iterator<Abstraction<Var, State, StmtID>> i = abstractions
				.iterator(); i.hasNext();) {
			Abstraction<Var, State, StmtID> abs = i.next();

			Value checkValue = abs.getValue(eqCheckVar);
			boolean filter = (checkValue == null || !checkValue.equals(v));
			if(not){
				filter = !filter;
			}
			if (filter) {
				i.remove();
			}
		}
		not = false;
		if (abstractions.isEmpty())
			noMatch();
		return this;
	}

	public IfCheckContext<Var, State, StmtID> and() {
		return this;
	}

	public Done<Var, State, StmtID> storeStmtAs(StmtID sid) {
		if (abstractions.isEmpty())
			return this;
		if (invokeStmt == null)
			throw new IllegalArgumentException(
					"Atempting to store call statement at return");
		Set<Abstraction<Var, State, StmtID>> res = new HashSet<Abstraction<Var, State, StmtID>>(
				abstractions.size());
		for (Abstraction<Var, State, StmtID> abs : abstractions) {
			res.add(abs.storeStmt(invokeStmt, sid));
		}
		this.abstractions = res;
		return this;
	}

	public IfCheckContext<Var, State, StmtID> atAnyReturn() {
		return atReturnFrom(ANY_METHOD);
	}

	public IfCheckContext<Var, State, StmtID> atReturnFrom(String signature) {
		if (abstractions.isEmpty())
			return this;
		if (calleeAtReturnFlow != null) {
			if (signature.equals(ANY_METHOD))
				return this;
			if (calleeAtReturnFlow.getSignature().equals(signature)) {
				method = calleeAtReturnFlow;
				return this;
			}
		}

		// filter did not match
		noMatch();
		return this;
	}

	public CallContext<Var, State, StmtID> ifInState(State s) {
		if (abstractions.isEmpty())
			return this;
		if (s == null)
			throw new IllegalArgumentException("State must not be null");
		// TODO should we do non-destructive updates here?
		for (Iterator<Abstraction<Var, State, StmtID>> i = abstractions
				.iterator(); i.hasNext();) {
			Abstraction<Var, State, StmtID> abs = i.next();
			if (!abs.stateIs(s)) {
				i.remove();
			}
		}
		if (abstractions.isEmpty())
			noMatch();
		return this;
	}

	public ReportError<Var, State, StmtID> reportError(String errorMessage) {
		if (abstractions.isEmpty())
			return this;
		this.errorMessage = errorMessage;
		return this;
	}

	public Done<Var, State, StmtID> atStmt(StmtID sid) {
		if (abstractions.isEmpty())
			return this;
		for (Abstraction<Var, State, StmtID> abs : abstractions) {
			Unit stmt = abs.getStatement(sid);
			// TODO what if stmt==null? do we allow this to happen?
			if (stmt != null) {
				String methodSignature = context.getICFG().getMethodOf(stmt).getSignature();
				String className = context.getICFG().getMethodOf(stmt)
						.getDeclaringClass().getName();
				context.reportError(new ErrorMarker(errorMessage, className, methodSignature, 
						stmt.getJavaSourceStartLineNumber()));
			}
		}
		return this;
	}

	@Override
	public Done<Var, State, StmtID> here() {
		if (abstractions.isEmpty())
			return this;
		Unit stmt = invokeStmt;

		String methodSignature = context.getICFG().getMethodOf(stmt).getSignature();
		String className = context.getICFG().getMethodOf(stmt)
				.getDeclaringClass().getName();
		context.reportError(new ErrorMarker(errorMessage, className, methodSignature, stmt
				.getJavaSourceStartLineNumber()));
		return this;
	}

	@Override
	public Done<Var, State, StmtID> doNothing() {
		return this;
	}

	@Override
	public IfCheckContext<Var, State, StmtID> atAssignTo(Var var) {

		if (abstractions.isEmpty())
			return this;
		if(invokeStmt instanceof AssignStmt){
			AssignStmt as = (AssignStmt) invokeStmt;
			for (Iterator<Abstraction<Var, State, StmtID>> i = abstractions
					.iterator(); i.hasNext();) {
				Abstraction<Var, State, StmtID> abs = i.next();
				Value boundValue = abs.getBoundValue(var);

				Value lOp = as.getLeftOp();
				Value rOp = as.getRightOp();
				if(lOp instanceof ArrayRef){
					rOp =((ArrayRef)lOp).getBase();
				}
				if(abs.lastReplacedValue != null && abs.lastReplacedValue.equals(lOp)){
					continue;
				}
				if ((boundValue == null || !boundValue.equals(rOp))) {
					i.remove();
				}
			}
			
		}

		if (abstractions.isEmpty())
			noMatch();

		return this;
	}
	
	@Override
	public EqualsContext<Var, State, StmtID> not() {
		not = true;
		return this;
	}
	@Override
	public IfCheckContext<Var, State, StmtID> atCallToMethodWithAnnotation(
			String annotation) {	


		if(calleeAtReturnFlow != null){
			if (calleeAtReturnFlow.hasTag("VisibilityAnnotationTag")) {
				VisibilityAnnotationTag tag = (VisibilityAnnotationTag) calleeAtReturnFlow
						.getTag("VisibilityAnnotationTag");
				for (AnnotationTag annTag : tag.getAnnotations()) {
					if(annTag.getType().equals(annotation)){
						method = calleeAtReturnFlow;
						return this;
					}

				}
			}
		}
		noMatch();
		return this;
	}

	@Override
	public IfCheckContext<Var, State, StmtID> atMethodFromList(Set<String> methodSet) { 
		if(calleeAtReturnFlow != null){
			if (methodSet.contains(calleeAtReturnFlow.getSignature())) {
				method = calleeAtReturnFlow;
				
				return this;	
			}
		}
		noMatch();
		return this;
	}

	@Override
	public ValueContext<Var, State, StmtID> atMethodFromListWithParameter(Set<MethodWithAnnotatedParameters> sources){
		if(calleeAtReturnFlow != null){
			for(MethodWithAnnotatedParameters m : sources) {
				ArrayList<Integer> sinkIndices = m.getAnnotatedParameterIndices();
				if(sinkIndices.size() > 0){
					if(m.getMethodSignature().equals(calleeAtReturnFlow.getSignature())){
						method = calleeAtReturnFlow;
						if(sinkIndices.size() == 1){
							return trackParameter(sinkIndices.get(0));	
						}
						currSlotArray = sinkIndices;
						return this;
					}	
				}
			}
		}
		noMatch();
		return this;
	}

	@Override
	public CallContext<Var, State, StmtID> asArray(Var var) {
		if (abstractions.isEmpty())
			return this;
		as(var);
		
		for (Abstraction<Var, State, StmtID> abs : abstractions) {
			abs.initializeArrayValue(var);
			
		}
		asArray = var;
		return this;
	}

}

enum ValueId {
	BASE(0), RETURN(-1), PARAM1(1), PARAM2(2), PARAM3(3), PARAM4(4), PARAM5(5);
	protected final int paramNum;

	ValueId(int i) {
		this.paramNum = i;
	}
}