package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

public interface ValueContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public CallContext<Var,State,StmtID> as(Var var);
	public CallContext<Var, State, StmtID> asArray(Var var);
}