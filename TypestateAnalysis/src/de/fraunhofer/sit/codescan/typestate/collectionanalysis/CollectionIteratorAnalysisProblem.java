package de.fraunhofer.sit.codescan.typestate.collectionanalysis;

import static de.fraunhofer.sit.codescan.typestate.collectionanalysis.CollectionIteratorAnalysisProblem.State.INIT;
import static de.fraunhofer.sit.codescan.typestate.collectionanalysis.CollectionIteratorAnalysisProblem.State.MODIFIED;
import static de.fraunhofer.sit.codescan.typestate.collectionanalysis.CollectionIteratorAnalysisProblem.Var.COLLECTION;
import static de.fraunhofer.sit.codescan.typestate.collectionanalysis.CollectionIteratorAnalysisProblem.Var.ITERATOR;
import de.fraunhofer.sit.codescan.sootbridge.IIFDSAnalysisContext;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.AbstractJimpleTypestateForwardAnalysisProblem;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtCallToReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.AtReturn;
import de.fraunhofer.sit.codescan.sootbridge.typestate.interfaces.Done;
import de.fraunhofer.sit.codescan.typestate.collectionanalysis.CollectionIteratorAnalysisProblem.State;
import de.fraunhofer.sit.codescan.typestate.collectionanalysis.CollectionIteratorAnalysisProblem.StatementId;
import de.fraunhofer.sit.codescan.typestate.collectionanalysis.CollectionIteratorAnalysisProblem.Var;

public class CollectionIteratorAnalysisProblem extends AbstractJimpleTypestateForwardAnalysisProblem<Var,State,StatementId> {
	
	private static final String COLLECTION_ADD = "<java.util.Collection: boolean add(java.lang.Object)>";
	private static final String NEW_ITERATOR = "<java.util.Collection: java.util.Iterator iterator()>";
	private static final String ITERATOR_NEXT = "<java.util.Iterator: java.lang.Object next()>";

	enum Var { COLLECTION, ITERATOR };
	enum State { INIT, MODIFIED };
	enum StatementId { };

	public CollectionIteratorAnalysisProblem(IIFDSAnalysisContext context) {
		super(context);
	}

	@Override
	protected Done<Var, State, StatementId> atCallToReturn(AtCallToReturn<Var, State, StatementId> d) {		
		return d.atCallTo(NEW_ITERATOR).always().trackThis().as(COLLECTION).trackReturnValue().as(ITERATOR).toState(INIT).orElse().
				//TODO should allow easy matching of multiple methods
			     atCallTo(COLLECTION_ADD).ifValueBoundTo(COLLECTION).equalsThis().toState(MODIFIED).orElse().
			     atCallTo(ITERATOR_NEXT).ifValueBoundTo(ITERATOR).equalsThis().and().ifInState(MODIFIED).reportError("Collection may have been modified!").here();
	}

	@Override
	protected Done<Var, State, StatementId> atReturn(AtReturn<Var, State, StatementId> d) {
		return d.doNothing();
	}

}
