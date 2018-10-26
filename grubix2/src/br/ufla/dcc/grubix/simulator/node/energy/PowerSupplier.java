/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.energy;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.xml.Configurable;

/**
 * This is the base class for all PowerSuppliers (reservoirs and scavenging suppliers).
 *  
 * @author Florian Rittmeier
 *
 * TODO May be this interface should specify something like an supplier id.
 */
public interface PowerSupplier extends Configurable {

	/**
	 * Inits the configuration of the power consumer.
	 * 
	 * @param configuration configuration of the system run.
	 */
	void initConfiguration(Configuration configuration);

	/**
	 * Suspends the power supplier.
	 * As the energy manager cares on that a usual power supplier
	 * should not care about this, but there might be complex
	 * power suppliers which need this information.
	 * 
	 * @param suspended new suspension state
	 */
	void suspend(boolean suspended);
	

}
