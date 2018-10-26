/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.energy;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.energy.units.Watt;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/**
 * This is the base class for all power scavenging suppliers.
 * 
 * A power scavenging supplier has a fixed amount of energy it can provide
 * each second. The next second the same amount of energy is avaliable again.
 * Examples are power sockets, solar cells and things like that. 
 * 
 * @author Florian Rittmeier
 */
public abstract class PowerScavengingSupplier implements PowerSupplier {

	/**
	 * The production (energy per second) of this power scavenging supplier.
	 */
	protected Watt maximumOutput;

	/**
	 * @return the production (energy per second) of this power scavenging supplier
	 */
	public Watt currentOutput() {
		return (Watt) this.maximumOutput.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	public void initConfiguration(Configuration configuration) {
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void init() throws ConfigurationException {
	}
	
}
