package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

public interface AtReturn<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public IfCheckContext<Var, State, StmtID> atReturnFromMethodOfStmt(StmtID sid);
	public IfCheckContext<Var,State,StmtID> atReturnFrom(String methodSignature);
	public IfCheckContext<Var, State, StmtID> atAnyReturn();
	public Done<Var, State, StmtID> doNothing();
}