package de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces;

public interface AtCollection<Var extends Enum<Var>,State extends Enum<State>,StmtID extends Enum<StmtID>> extends AtCallToReturn<Var,State, StmtID>,AtNormalEdge<Var,State, StmtID>,AtReturn<Var,State, StmtID>{

}
