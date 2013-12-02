package de.fraunhofer.sit.codescan.framework.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.sit.codescan.framework.AnalysisPlugin;

public class Extensions {

	private final static Logger LOGGER = LoggerFactory.getLogger(Extensions.class);

	/**
	 * Returns the set of all registered analysis plugins. 
	 */
	public static IConfigurationElement[] getContributorsToExtensionPoint() {
	    IExtensionRegistry reg = Platform.getExtensionRegistry();
	    IConfigurationElement[] elements = reg.getConfigurationElementsFor(Constants.EXTENSION_POINT_ID);
	    return elements;
	}
	
	public static AnalysisPlugin createPluginObject(IConfigurationElement extension) {
		try {
			return (AnalysisPlugin) extension.createExecutableExtension("class");
		} catch (CoreException e) {
			LOGGER.debug("",e);
			return null;
		}
	}

}
