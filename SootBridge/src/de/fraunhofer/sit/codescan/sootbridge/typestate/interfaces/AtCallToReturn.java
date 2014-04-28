package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

import java.util.Set;

import de.fraunhofer.sit.codescan.sootbridge.util.MethodWithAnnotatedParameters;

public interface AtCallToReturn<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public IfCheckContext<Var,State,StmtID> atCallTo(String methodSignature);
	public IfCheckContext<Var,State,StmtID> atAnyCallToClass(String className);
	public IfCheckContext<Var,State,StmtID> atCallToMethodWithAnnotation(String annotation);
	public IfCheckContext<Var,State,StmtID> atMethodFromList(Set<String> list);
	public ValueContext<Var,State,StmtID> atMethodFromListWithParameter(Set<MethodWithAnnotatedParameters> list);
}