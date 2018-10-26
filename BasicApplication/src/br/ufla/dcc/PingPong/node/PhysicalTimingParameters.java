package br.ufla.dcc.PingPong.node;

public class PhysicalTimingParameters {
	/** the maximum possible bitrate. */
	protected int maxBitrateIDX;
	/** contains the used BPS for the payload. */
	protected double[] bps;

	protected PhysicalTimingParameters() {
	}
	
	/**
	 * Constructor.
	 * @param bps bits per simulation step array
	 * @param maxBitrateIndex index with the maximal bitrate
	 */
	public PhysicalTimingParameters(double... bps) {
		this.bps = bps.clone();
		this.maxBitrateIDX = bps.length - 1;
	}
	
	/** @return the maxBitrateIDX. */
	public final int getMaxBitrateIDX() {
		return maxBitrateIDX;
	}

	/** @return the payloadBPS. */
	public final double getBPS(int i) {
		return bps[i];
	}
}
