package de.fraunhofer.sit.codescan.sootbridge;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import soot.SceneTransformer;
import soot.Transformer;

public class PluggableTransformer {
	protected IConfigurationElement transformerConfigElement;

	public PluggableTransformer(IConfigurationElement config) {
		transformerConfigElement = config;
	}
	public String getPack(){
		return transformerConfigElement.getAttribute("pack");
	}
	public String getPackageName() {
		return transformerConfigElement.getAttribute("packagename");
	}

	public SceneTransformer getInstance() {
		try {
			return (SceneTransformer) transformerConfigElement.createExecutableExtension("class");
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
}
