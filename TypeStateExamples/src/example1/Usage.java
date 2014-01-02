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

        modelGroup.flush();
        someBooleanValue.setChecked(false);
        someIntValue.increment(3);
    } 

}
