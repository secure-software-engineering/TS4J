package de.fraunhofer.sit.codescan.typestate.tlschannelcloseanalysis;

import static de.fraunhofer.sit.codescan.typestate.tlschannelcloseanalysis.TLSChannelCloseAnalysisProblem.State.CLOSE_SOCKET;
import static de.fraunhofer.sit.codescan.typestate.tlschannelcloseanalysis.TLSChannelCloseAnalysisProblem.State.INIT_SOCKET;
import static de.fraunhofer.sit.codescan.typestate.tlschannelcloseanalysis.TLSChannelCloseAnalysisProblem.Var.SOCKET;

import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateBackwardsAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateForwardAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtNormalEdge;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;
import de.fraunhofer.sit.codescan.typestate.tlschannelcloseanalysis.TLSChannelCloseAnalysisProblem.State;
import de.fraunhofer.sit.codescan.typestate.tlschannelcloseanalysis.TLSChannelCloseAnalysisProblem.StmtID;
import de.fraunhofer.sit.codescan.typestate.tlschannelcloseanalysis.TLSChannelCloseAnalysisProblem.Var;


public class TLSChannelCloseAnalysisProblem extends AbstractJimpleTypestateForwardAnalysisProblem<Var, State, StmtID>
{

	public TLSChannelCloseAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
	}
	
	private static final String ERROR_MESSAGE_1 = "Is working";
	private static final String INVOKE_CREATESOCKET = "<javax.net.ssl.SSLSocketFactory: java.net.Socket createSocket(java.lang.String,int)>";
	private static final String INVOKE_CLOSESOCKET = "<javax.net.ssl.SSLSocket: void close()>";	
	private static final String INVOKE_TEMPLATEUSAGE = "<Crypto.Output: void templateUsage(java.lang.String,int)>";
	
	private static final String INVOKE_INIT = "<Crypto.TLSClient: void <init>(java.lang.String,int)>";
	private static final String INVOKE_TEMP = "<Crypto.Main: void templateUsage(java.lang.String,int)>";
	private static final String INVOKE_CLOSE = "<Crypto.TLSClient: void closeConnection()>";

	enum Var {SOCKET};
	enum State {INIT_SOCKET,CLOSE_SOCKET};
	enum StmtID {}
	

	@Override
	protected Done<Var, State, StmtID> atCallToReturn(AtCallToReturn<Var, State, StmtID> d) {
		return d.atCallTo(INVOKE_INIT).always().toState(INIT_SOCKET).orElse()
				.atCallTo(INVOKE_CLOSE).always().toState(CLOSE_SOCKET);
		}
	
	
	@Override 
	protected Done<Var, State, StmtID> atReturn(AtReturn<Var, State, StmtID> d) {
		return d.atReturnFrom(INVOKE_TEMPLATEUSAGE).ifInState(INIT_SOCKET).reportError(ERROR_MESSAGE_1).here();
	}
	
	@Override
	protected Done<Var, State, StmtID> atNormalEdge(AtNormalEdge<Var, State, StmtID> d) {
		return null;
	}

}
