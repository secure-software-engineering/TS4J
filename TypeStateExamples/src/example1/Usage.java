package example1;

public class Usage {

    public void main() {
        /*
         * Initialization
         */
        BooleanValue someBooleanValue = new BooleanValue();
        IntValue someIntValue = new IntValue();
        TextValue someTextValue = new TextValue();

        ValueGroup modelGroup = new ValueGroup();
        modelGroup.add(someBooleanValue);
        modelGroup.add(someIntValue);
        modelGroup.add(someTextValue);

        someBooleanValue.setChecked(false);
        someIntValue.increment(3);
        
        extracted(someTextValue);
        modelGroup.flush();
    }

	private void extracted(TextValue someTextValue) {
		someTextValue.setText("foo");
	}

}
