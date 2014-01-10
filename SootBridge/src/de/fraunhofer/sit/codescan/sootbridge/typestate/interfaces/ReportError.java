package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

public interface ReportError<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> {	
	public Done<Var,State,StmtID> here();
	public Done<Var,State,StmtID> atStmt(StmtID sid);
}