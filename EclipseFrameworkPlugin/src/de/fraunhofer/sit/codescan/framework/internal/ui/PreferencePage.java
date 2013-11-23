package de.fraunhofer.sit.codescan.framework.internal.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.fraunhofer.sit.codescan.framework.internal.Activator;
import de.fraunhofer.sit.codescan.framework.internal.Constants;

public class PreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	protected String previousMarkerStyle;

	public PreferencePage() {
		super(GRID);

	}

	public void createFieldEditors() {
		addField(new RadioGroupFieldEditor(Messages.PreferencePage_MarkerStyle,
				Messages.PreferencePage_RadioGroupLabel, 1,
				new String[][] { { Messages.PreferencePage_MnemonicWarning, Messages.PreferencePage_IDWarning },
						{ Messages.PreferencePage_MnemonicError, Messages.PreferencePage_IDError } }, getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
	    setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.PreferencePage_PageDescription);
		previousMarkerStyle = getMarkerStyle();
	}
	
	@Override
	public boolean performOk() {	
		boolean ok = super.performOk();
		
		if(ok) {
			//if the marker style was changed...
			String markerStyle = getMarkerStyle();
			if(!markerStyle.equals(previousMarkerStyle)) {
				//update the severity of all markers
				int severity = markerStyle.equals(Messages.PreferencePage_IDError) ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING;
				try {
					IMarker[] markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(Constants.MARKER_TYPE, false, IResource.DEPTH_INFINITE);
					for (IMarker m: markers) {
						m.setAttribute(IMarker.SEVERITY,severity);
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}	
			}
		}	
				
		return ok;
	}
	
	@Override
	protected void performApply() {
		performOk();
		previousMarkerStyle = getMarkerStyle();
	}

	public static String getMarkerStyle() {
		return Activator.getDefault().getPreferenceStore().getString(Messages.PreferencePage_MarkerStyle);
	}
	
}