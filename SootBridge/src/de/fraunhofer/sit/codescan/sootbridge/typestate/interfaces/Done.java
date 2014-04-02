package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

public interface Done<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {	
	public AtCollection<Var,State,StmtID> orElse();
	public Done<Var,State,StmtID> storeStmtAs(StmtID sid);
}