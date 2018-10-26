package br.ufla.dcc.grubix.simulator.node.energy;

import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.energy.units.Watt;

/**
 * This class is used to handle the information for a single
 * one time consumption. It combines consumption, starttime,
 * endtime and duration and updates its relations.
 * 
 * @author Florian Rittmeier
 */
class OneTimeConsumptionInformation {
	/**
	 * Internal field which helds the consumption for this
	 * one time consumption. 
	 */
	protected Watt consumption;

	/**
	 * Internal field which helds the start of the one time consumption
	 * which is a specific a simulation time.
	 */
	protected double starttime;
	
	/**
	 * Internal field which helds the end of the one time consumption
	 * which is a specific a simulation time.
	 */
	protected double endtime;
	
	/**
	 * Internal field for the duration in simulation steps.
	 */
	protected double duration;
	
	/**
	 * Internal field to access the configuration.
	 */
	protected Configuration config;
	
	/**
	 * The default constructor for OneTimeConsumptionInformation.
	 * @param config the configuration
	 */
	public OneTimeConsumptionInformation(Configuration config) {
		this.config = config;
	}
	
	/**
	 * @return the consumption
	 */
	public final Watt getConsumption() {
		return consumption;
	}
	
	/**
	 * @param consumption the consumption to set
	 */
	public final void setConsumption(Watt consumption) {
		this.consumption = consumption;
	}
	
	/**
	 * @return the duration
	 */
	public final double getDuration() {
		return duration;
	}
	
	/**
	 * @param duration the duration to set
	 */
	public final void setDuration(double duration) {
		this.duration = duration;
		this.endtime = starttime + config.getSimulationSteps(this.duration);
	}
	
	/**
	 * @return the starttime
	 */
	public final double getStarttime() {
		return starttime;
	}
	
	/**
	 * @param starttime the starttime to set
	 */
	public final void setStarttime(double starttime) {
		this.starttime = starttime;
		this.endtime = starttime + config.getSimulationSteps(this.duration);
	}
	
	/**
	 * @return the endtime
	 */
	public final double getEndtime() {
		return endtime;
	}
}


