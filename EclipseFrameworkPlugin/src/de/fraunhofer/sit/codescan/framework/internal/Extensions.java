package de.fraunhofer.sit.codescan.framework.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import de.fraunhofer.sit.codescan.framework.AnalysisPlugin;

public class Extensions {

//	/**
//	 * Returns the set of all registered analysis plugins. 
//	 */
//	public static Set<AnalysisPlugin> getContributorsToExtensionPoint() {
//		try {
//			IExtensionRegistry reg = Platform.getExtensionRegistry();
//			IExtensionPoint ep = reg.getExtensionPoint(Constants.EXTENSION_POINT_ID);
//			IExtension[] extensions = ep.getExtensions();
//			Set<AnalysisPlugin> contributors = new HashSet<AnalysisPlugin>();
//			for (int i = 0; i < extensions.length; i++) {
//				IExtension ext = extensions[i];
//				IConfigurationElement[] ce = ext.getConfigurationElements();
//				for (int j = 0; j < ce.length; j++) {
//						contributors.add((AnalysisPlugin) ce[j].createExecutableExtension("class"));
//				}
//			}
//			return contributors;
//		} catch (CoreException e) {
//			e.printStackTrace();
//			return Collections.emptySet();
//		}
//	}
//	
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
			e.printStackTrace();
			return null;
		}
	}

}
