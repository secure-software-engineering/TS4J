package de.fraunhofer.sit.codescan.framework.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import de.fraunhofer.sit.codescan.framework.IIFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.framework.IMethodBasedAnalysisPlugin;

public class Extensions {

	/**
	 * Returns the set of all registered analysis plugins. 
	 */
	public static IConfigurationElement[] getContributorsToExtensionPoint() {
	    IExtensionRegistry reg = Platform.getExtensionRegistry();
	    IConfigurationElement[] elements = reg.getConfigurationElementsFor(Constants.EXTENSION_POINT_ID);
	    return elements;
	}
	
	public static IIFDSAnalysisPlugin[] createIFDSAnalysisPluginObjects(IConfigurationElement extension) {
		try {
			IConfigurationElement[] children = extension.getChildren("ifdsAnalysis");
			IIFDSAnalysisPlugin[] res = new IIFDSAnalysisPlugin[children.length];
			int i=0;
			for (IConfigurationElement analysisConfig : children) {
				res[i++] = (IIFDSAnalysisPlugin) analysisConfig.createExecutableExtension("class"); 
			}
			return res;
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static IMethodBasedAnalysisPlugin[] createMethodBasedAnalysisPluginObjects(IConfigurationElement extension) {
		try {
			IConfigurationElement[] children = extension.getChildren("methodBasedAnalysis");
			IMethodBasedAnalysisPlugin[] res = new IMethodBasedAnalysisPlugin[children.length];
			int i=0;
			for (IConfigurationElement analysisConfig : children) {
				res[i++] = (IMethodBasedAnalysisPlugin) analysisConfig.createExecutableExtension("class"); 
			}
			return res;
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

}
