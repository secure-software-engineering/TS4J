package de.fraunhofer.sit.crvalidator.plugin.views;

import org.eclipse.ui.views.markers.MarkerSupportView;

public class CRVMarkerView extends MarkerSupportView {

	/**
	 * Our custom marker view -> String matches the generator defined in plugin.xml
	 */
	public CRVMarkerView() {
		//TODO: default contentgenerator supports filtering, but ours doesnt? o_o
		super("de.fraunhofer.sit.crvalidator.plugin.markerContentGenerator");
		//super("");
	}


}
