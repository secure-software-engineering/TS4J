package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

public interface EqualsContext<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public CallContext<Var,State,StmtID> equalsThis();
	public CallContext<Var,State,StmtID> equalsReturnValue();
	public CallContext<Var,State,StmtID> equalsParameter(int paramIndex);
}