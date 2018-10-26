package br.ufla.dcc.grubix.simulator.node.energy;

import br.ufla.dcc.grubix.simulator.event.EnergyManagerWakeUpCallEvent;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ConfigurationException;

/**
 * Implementation of the EnergyManager interface that in summary does nothing.
 * It doesn't sum up the energy consumption and there for
 * - calls never the PowerLevelCallback
 * - nodes never get deactivated due to the energy level
 * 
 * @author dmeister
 *
 */
public class NoEnergyManager implements EnergyManager {

	/**
	 * {@inheritDoc}
	 */
	public void checkPowerLevelCallbacks() {
		// nothing to check
	}

	/**
	 * {@inheritDoc}
	 */
	public void initConfiguration(Configuration configuration) {
		// nothing to init
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerPowerLevelCallback(long level, PowerLevelCallback clb) {
		// there is no need to save the callbacks
	}

	/**
	 * {@inheritDoc}
	 */
	public void setNode(Node node) {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public void init() throws ConfigurationException {
		// nothing to init
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void suspend(boolean suspended) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void startup() {
		
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isValidWakeUpCall(EnergyManagerWakeUpCallEvent event) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPowerLevel() {
		return 100;
	}

}
