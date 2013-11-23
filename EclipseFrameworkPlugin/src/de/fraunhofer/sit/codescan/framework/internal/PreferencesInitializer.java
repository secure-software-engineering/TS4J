package de.fraunhofer.sit.codescan.framework.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import de.fraunhofer.sit.codescan.framework.internal.ui.Messages;

public class PreferencesInitializer extends AbstractPreferenceInitializer {

	public PreferencesInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(Activator.getDefault().getBundle().getSymbolicName());
		node.put(Messages.PreferencePage_MarkerStyle,Messages.PreferencePage_IDError);
	}

}
