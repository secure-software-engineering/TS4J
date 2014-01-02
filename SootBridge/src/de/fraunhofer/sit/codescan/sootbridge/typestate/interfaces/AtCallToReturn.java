package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

public interface AtCallToReturn<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public IfCheckContext<Var,State,StmtID> atCallTo(String methodSignature);
	public IfCheckContext<Var,State,StmtID> atAnyCallToClass(String className);
}