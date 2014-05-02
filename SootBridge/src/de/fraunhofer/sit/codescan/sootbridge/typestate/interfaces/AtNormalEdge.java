package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

public interface AtNormalEdge<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {
	public IfCheckContext<Var,State,StmtID> atAssignTo(Var var);
}

