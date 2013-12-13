package example2;

public class Usage {

    public void main() {
        /*
         * Build the sequence
         */
        final CallSequence sequence = new CallSequence();
        sequence.append(new FluxCompensatorCall() {
            @Override
            public void execute() {
                getFluxCompensator().turnOn();
                finished(); // <-- often forgotten, the call will timeout
            }

        });
        sequence.append(new FluxCompensatorCall() {
            @Override
            public void execute() {
                getFluxCompensator().increaseFlux(10);
                // not finished here, because we have to wait for the callback
            }

            @Override
            public void fluxIncreased(int flux) {
                if (flux < 50) {
                    getFluxCompensator().increaseFlux(50 - flux);
                    // not even finished here because we don't have the desired value yet
                } else {
                    finished();
                }
            }
        });

        /*
         * and execute it
         */
        sequence.execute();

        /*
         * The challenge here is, that it depends on the invoked remote calls in which of callbacks 'finished' or
         * 'aborted' must be called.
         */
    }

}
