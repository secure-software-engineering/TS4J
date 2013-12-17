package example1;

import java.util.ArrayList;
import java.util.List;

/**
 * Groups a set of values which should be updates simultaneously in the UI. 
 */
public class ValueGroup {

	final List<ModelValue> values = new ArrayList<ModelValue>();
	
	public void add(final ModelValue model) {
		values.add(model);
	}
	
	public void flush() {
//		for (final ModelValue value : values) {
//			// update the UI widget connected to the model
//		}
	}
	
}
