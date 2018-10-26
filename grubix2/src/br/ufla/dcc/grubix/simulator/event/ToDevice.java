/**
 * 
 */
package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.DeviceType;
import br.ufla.dcc.grubix.simulator.NodeId;

/**
 * This class can be used to send events/messages to specific devices of a node.
 * This should allow a easier extension of communication between devices/components
 * which are not a layer.
 * 
 * @author Florian Rittmeier
 *
 */
public class ToDevice extends ToNode {

	/**
	 * The delay time for the event.
	 */
	protected double delay;
	
	/**
	 * The device for which the event is for.
	 */
	protected DeviceType device;
	
	/**
	 * The node whos device send this event.
	 */
	protected NodeId sender;
	/**
	 * The device which send this event.
	 */
	protected DeviceType senderDevice;
	
	/**
	 * Constructor.
	 * @param receiver The node whos device will receive the event
	 * @param device The device which will receive the event
	 * @param sender The node whos device send this event
	 * @param senderDevice The device which send the event
	 * @param delay delay before event is send to receiver
	 */
	public ToDevice(NodeId receiver, DeviceType device, NodeId sender, DeviceType senderDevice, double delay) {
		super(receiver);
		
		this.delay = delay;

		this.device = device;
		this.sender = sender;
		this.senderDevice = senderDevice;
	}

	/**
	 * Constructor. Its assumed that no delay is required.
	 * @param receiver The node whos device will receive the event
	 * @param device The device which will receive the event
	 * @param sender The node whos device send this event
	 * @param senderDevice The device which send the event
	 */
	public ToDevice(NodeId receiver, DeviceType device,  NodeId sender, DeviceType senderDevice) {
		super(receiver);

		this.delay = 0.0;

		this.device = device;
		this.sender = sender;
		this.senderDevice = senderDevice;
	}
	
	/* (non-Javadoc)
	 * @see br.ufla.dcc.grubix.simulator.event.Event#toString()
	 */
	@Override
	public String toString() {
		return "ToDevice";
	}
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.event.Event#getDelay()
	 * @return Delay of the WUC as Double.
	 */
	public final double getDelay() {
		return this.delay;
	}
	
	
	/** @param delay the new delay for an reused event. */
	public final void setDelay(double delay) {
		// XXX I'm not sure if we need a setter
		if (delay < 0.0) {
			delay = 0.0;
		}
		this.delay = delay;
	}

	/**
	 * @return the sender
	 */
	public final NodeId getSender() {
		return sender;
	}

	/**
	 * @return the senderDevice
	 */
	public final DeviceType getSenderDevice() {
		return senderDevice;
	}
	

}
