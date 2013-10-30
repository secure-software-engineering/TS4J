package de.fraunhofer.sit.crvalidator.plugin.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

public class InitHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
		// Auto-generated method stub
	}

	@Override
	public void dispose() {
		// Auto-generated method stub
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
//		System.out.println("hello init crv");
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true; //enable plugin
	}

	@Override
	public boolean isHandled() {
		return true; //enable plugin
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
		// Auto-generated method stub
	}

}
