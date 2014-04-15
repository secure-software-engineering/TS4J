package de.fraunhofer.sit.codescan.sootbridge;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import soot.SceneTransformer;

public class PluggableTransformer {
	protected IConfigurationElement transformerConfigElement;

	public PluggableTransformer(IConfigurationElement config) {
		transformerConfigElement = config;
	}

	public String getPack() {
		String name = transformerConfigElement.getAttribute("packagename");
		try{
			name = name.substring(0, name.indexOf("."));
		} catch (StringIndexOutOfBoundsException ex){
			throw new RuntimeException("The packagename of the SootTransformer must contain a dot(.)!");
		}
		return name;
	}

	public String getPackageName() {
		return transformerConfigElement.getAttribute("packagename");
	}

	public SceneTransformer getInstance() {
		try {
			return (SceneTransformer) transformerConfigElement
					.createExecutableExtension("class");
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	public boolean executeBeforeAnalysis(){
		String executionTime = transformerConfigElement.getAttribute("executionTime");
		return executionTime.equals("beforeAnalysis");
	}
}
