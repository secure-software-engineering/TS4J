package example2;

public class FluxCompensator {

	public void turnOn() {
		// no callback, fire-and-forget
	}
	
	public void turnOff() {
		// no callback, fire-and-forget
	}
	
	public void increaseFlux(final int delta) {
		// the fluxIncreased callback will be called asynchronously sometime later (through a magic dispatching mechanism TM)
	}
	
	public void decreaseFlux(final int delta) {
		// the fluxDecreased callback will be called asynchronously sometime later (through a magic dispatching mechanism TM)
	}
}
