package de.fraunhofer.sit.crvalidator.plugin;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;

import de.fraunhofer.sit.crvalidator.plugin.listeners.AfterBuildListener;

public class StartUp implements IStartup {
	
	@Override
	public void earlyStartup() {
		
		System.out.println("Early startup");
		//register build listener to catch POST_CHANGE events (after a build was run by eclipse)		
		IResourceChangeListener listener = AfterBuildListener.getInstance();
		
		//ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);

	}

}
