package de.fraunhofer.sit.codescan.framework.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import de.fraunhofer.sit.codescan.framework.IFDSAnalysisPlugin;
import de.fraunhofer.sit.codescan.framework.MethodBasedAnalysisPlugin;

public class Extensions {

	/**
	 * Returns the set of all registered analysis plugins. 
	 */
	public static IConfigurationElement[] getContributorsToExtensionPoint() {
	    IExtensionRegistry reg = Platform.getExtensionRegistry();
	    IConfigurationElement[] elements = reg.getConfigurationElementsFor(Constants.EXTENSION_POINT_ID);
	    return elements;
	}
	
	public static IFDSAnalysisPlugin[] createIFDSAnalysisPluginObjects(IConfigurationElement extension) {
		try {
			IConfigurationElement[] children = extension.getChildren("ifdsAnalysis");
			IFDSAnalysisPlugin[] res = new IFDSAnalysisPlugin[children.length];
			int i=0;
			for (IConfigurationElement analysisConfig : children) {
				res[i++] = (IFDSAnalysisPlugin) analysisConfig.createExecutableExtension("class"); 
			}
			return res;
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static MethodBasedAnalysisPlugin[] createMethodBasedAnalysisPluginObjects(IConfigurationElement extension) {
		try {
			IConfigurationElement[] children = extension.getChildren("methodBasedAnalysis");
			MethodBasedAnalysisPlugin[] res = new MethodBasedAnalysisPlugin[children.length];
			int i=0;
			for (IConfigurationElement analysisConfig : children) {
				res[i++] = (MethodBasedAnalysisPlugin) analysisConfig.createExecutableExtension("class"); 
			}
			return res;
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

}
