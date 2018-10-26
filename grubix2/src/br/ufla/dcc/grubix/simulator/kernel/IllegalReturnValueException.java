/**
 * 
 */
package br.ufla.dcc.grubix.simulator.kernel;

/**
 * This class is used by inner classes of SimulationManager to indicate
 * that a method they called did not return a legal value.
 * 
 * The intended inner classes are:
 * - MoveManaEnvelope
 * - TrafficGenEnvelope
 * 
 * @author Florian Rittmeier
 */
@SuppressWarnings("serial")
class IllegalReturnValueException extends RuntimeException {

	/**
	 * @param description description of the exception situation
	 */
	public IllegalReturnValueException(String description) {
		super(description);
	}


}
