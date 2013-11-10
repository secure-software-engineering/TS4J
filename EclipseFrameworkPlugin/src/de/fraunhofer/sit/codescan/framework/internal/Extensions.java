package de.fraunhofer.sit.codescan.framework.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import de.fraunhofer.sit.codescan.framework.IFDSAnalysisPlugin;

public class Extensions {

	/**
	 * Returns the set of all registered analysis plugins. 
	 */
	public static IConfigurationElement[] getContributorsToExtensionPoint() {
	    IExtensionRegistry reg = Platform.getExtensionRegistry();
	    IConfigurationElement[] elements = reg.getConfigurationElementsFor(Constants.EXTENSION_POINT_ID);
	    return elements;
	}
	
	public static IFDSAnalysisPlugin createPluginObject(IConfigurationElement extension) {
		try {
			return (IFDSAnalysisPlugin) extension.createExecutableExtension("class");
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

}
