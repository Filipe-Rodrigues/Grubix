/**
 * 
 */
package br.ufla.dcc.grubix.simulator.node.energy;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.node.energy.units.Watt;

/**
 * @author Florian Rittmeier
 * 
 * This interface provides PowerConsumers with the ability of
 * communication with the EnergyManager e.g. for announcing
 * a one time consumption of energy.
 *
 */
public interface PowerConsumerFeedback {
	
	/**
	 * Used to announce a one/short time consumption of a PowerConsumer e.g.
	 * when a device requires some additional energy for initalization
	 * or changing between internal states.
	 * If a one/short time consumption is announced this consumption is considered
	 * in addition to the regular average consumption.
	 * 
	 * @param consumption amount of Energy which is required by the PowerConsumer
	 * @param duration how long this additional consumption should be considered
	 */
	void announceOneTimeConsumption(Watt consumption, double duration);
	
	/**
	 * PowerConsumers have to call this method
	 * before updating one of their attributes,
	 * e.g. before updating their average power
	 * consumption per second.
	 */
	void beforeUpdate();

	/**
	 * PowerConsumers have to call this method
	 * after updating one of their attributes,
	 * e.g. before updating their average power
	 * consumption per second.
	 */
	void afterUpdate();

	/**
	 * Registers a callback function
	 * {@see br.ufla.dcc.shxo.simulator.energy.PowerLevelCallback#performed()}
	 * which is called when the level of power is equal to or below that level.
	 * Each registered callback is only called once.
	 * 
	 * This description is copied from
	 * {@link br.ufla.dcc.grubix.simulator.node.energy.EnergyManager##registerPowerLevelCallback(long, PowerLevelCallback)}
	 * 
	 * @param level a value between 0 and 100
	 * @param clb the callback function
	 * 
	 * @see br.ufla.dcc.grubix.simulator.node.energy.EnergyManager##registerPowerLevelCallback(long, PowerLevelCallback)
	 */
	void registerPowerLevelCallback(long level, PowerLevelCallback clb);
	
	/**
	 * @return the node to which the PowerConsumer instance belongs to
	 */
	NodeId getNode();

}
