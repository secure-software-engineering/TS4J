package example2;

public abstract class FluxCompensatorCall extends AsyncRemoteCall {

	private final FluxCompensator fluxCompensator = new FluxCompensator();
	
	protected FluxCompensator getFluxCompensator() {
		return fluxCompensator;
	}
	
	public void fluxIncreased(final int flux) {
		// ignore by default, override if interested
	}
	
	public void fluxDecreased(final int flux) {
		// ignore by default, override if interested
	}

}
