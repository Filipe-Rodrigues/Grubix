/**
 * 
 */
package br.ufla.dcc.grubix.simulator.kernel;

/**
 * This exception should be thrown, when the SimulationManager
 * detects that an envelope which it pulls from the envelope queue
 * would turn back the simulation time of Shox.
 * 
 * @author Florian Rittmeier
 */
@SuppressWarnings("serial")
class InvalidEnvelopeTimeException extends RuntimeException {

	/**
	 * Default constructor. 
	 */
	public InvalidEnvelopeTimeException() {
	}

	/**
	 * @param arg0 details for the exception
	 */
	public InvalidEnvelopeTimeException(String arg0) {
		super(arg0);
	}
}
