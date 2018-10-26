/**
 * 
 */
package br.ufla.dcc.grubix.simulator.event;
import br.ufla.dcc.grubix.simulator.DeviceType;
import br.ufla.dcc.grubix.simulator.NodeId;

/**
 * This event is send by the EnergyManager to inform itself
 * to check for raising callbacks, powering down a node or
 * remove one time consumptions which are over.
 * 
 * @author Florian Rittmeier
 */
public class EnergyManagerWakeUpCallEvent extends ToDevice {

	/**
	 * The constructor.
	 * @param node the node whos energy manager has to be informed
	 * @param delay the delay when the event has to be delivered
	 */
	public EnergyManagerWakeUpCallEvent(NodeId node, double delay) {
		super(node, DeviceType.POWERMANAGEMENT, node, DeviceType.POWERMANAGEMENT, delay);
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "EnergyManagerWakeUpCallEvent";
	}
	
}
