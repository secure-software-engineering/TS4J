package de.fraunhofer.sit.crvalidator.plugin.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * Relation field (column) for custom marker view
 * @author triller
 *
 */
public class MarkerFieldRelation extends MarkerField {

	public MarkerFieldRelation() {
	}

	@Override
	public String getValue(MarkerItem item) {
		String val = "";
		
		IMarker marker = item.getMarker(); 
		
		if (marker != null)
		{
			val = marker.getAttribute("relation", "");
		}
		
		return val;
	}

}
