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

        BooleanValue someLonelyBooleanValue = new BooleanValue();

        /*
         * Usage patterns
         */

        // either all values change
        someBooleanValue.setChecked(true);
        someIntValue.increment(2);
        someTextValue.setText("Hello World");
        modelGroup.flush(); // <-- make the changes actually visible. This one is often missing.

        // or only part of it
        someBooleanValue.setChecked(false);
        modelGroup.flush(); // <-- call flush also if only a part of the group changes. This one is also often missing.

        // But there can also exist values that are not part of a model group. Their associated UI widget is updated
        // immediately on change.
        someLonelyBooleanValue.setChecked(false);

        /*
         * The challenge is to find places where a value, which is part of a model group is changed and
         * modelGroup.flush() is not called.
         */
    }
}
