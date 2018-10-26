package br.ufla.dcc.grubix.simulator;

/**
 * Implemented by all classes that require to know the precise current simulation time. Since
 * it would be bad programming style (spaghetti code) to give each such class a reference to
 * the simulation manager (and also, most classes are not allowed to access simulation manager
 * methods), a class that needs time information can invoke 
 * {@link br.ufla.dcc.grubix.simulator.kernel.SimulationManager#setCurrentTime(TimedObject)} to have
 * its time information set correctly.
 * @author jlsx
 */
public interface TimedObject {

	/**
	 * Passes the current simulation time obtained from the simulation manager.
	 * @param currentTime The current simulation time
	 */
	void setTime(double currentTime);
}
