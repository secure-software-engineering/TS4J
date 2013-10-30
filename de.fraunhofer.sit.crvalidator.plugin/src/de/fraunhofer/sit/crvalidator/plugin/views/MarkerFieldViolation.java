package de.fraunhofer.sit.crvalidator.plugin.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * Violation field (column) for custom marker view
 * @author triller
 *
 */
public class MarkerFieldViolation extends MarkerField {

	public MarkerFieldViolation() {
	}

	@Override
	public String getValue(MarkerItem item) {
		
		String val = "";
		
		IMarker marker = item.getMarker(); 
		
		if (marker != null)
		{
			val = marker.getAttribute("violation", "");
		}
		
		return val;
	}

}
