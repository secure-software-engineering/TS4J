package de.fraunhofer.sit.codescan.androidssl.handlers;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

import de.fraunhofer.sit.codescan.androidssl.listeners.AfterBuildListener;

public class StartupHandler implements IStartup {

	public void earlyStartup() {
		//register build listener to catch POST_CHANGE events (after a build was run by eclipse)		
		IResourceChangeListener listener = AfterBuildListener.getInstance();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_BUILD);
	}

}
