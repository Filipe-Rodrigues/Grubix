/********************************************************************************
This file is part of ShoX.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with 
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2006 The ShoX developers as defined under http://shox.sourceforge.net
********************************************************************************/

package br.ufla.dcc.grubix.simulator.node;

import java.io.Serializable;

import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.Finalize;
import br.ufla.dcc.grubix.simulator.event.Initialize;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.Moved;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.SimulationState;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.ToLayer;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationKernel;
import br.ufla.dcc.grubix.simulator.random.RandomGenerator;
import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;



/** 
 * Abstract superclass for all types of Layers in the network stack.
 * 
 * A layer has a general service accesspoint (SAP) processPacket. This SAP is very
 * important in any direct subclass of this one (for example LogLinkLayer).
 * The SAP has to be overloaded there and check whether the packet comes from the
 * layer one step higher respectively lower. Then delegate the packet to the
 * more specific SAP for higher / lower packets.
 * 
 * If the specific layer wants to place WakeUpCalls, it should overload the method
 * processWakeUpCall().
 * If the specific layer wants to receive Moved events with new positions of the node,
 * it has to register to the moved observer with Node.addToMovedObserver(Layer)
 * and overload processEvent(Moved moved).
 * 
 * @author Andreas Kumlehn 
 */
public abstract class Layer implements Configurable, StateIO, Serializable {
	
	/** Logger of the class Node. */
	private static final Logger LOGGER = Logger.getLogger(Layer.class);
	
	/**
	 * type of the layer.
	 */
	private final LayerType layerType;
	
	/** 
	 * The node to which this layer belongs.
	 * The attribute is "protected" to avoid breaking existing code.
	 * However, the getNode() method is preferred. 
	 */
	protected Node node;
	
	/**
	 * Id of the node.
	 * The attribute is "protected" to avoid breaking existing code.
	 * However, the getId() method is preferred.
	 */
	protected NodeId id;
	
	/**
	 * sender address of this layer.
	 * The attribute is "protected" to avoid breaking existing code.
	 * However, the getSender() method is preferred.
	 */
	protected Address sender;
	
	/** shortcut to access Configuration.getInstance(). */
	private Configuration config;

	/**
	 * flag is the layer is suspended.
	 * This disabled the sending of events
	 */
	private boolean isSuspended;

	/** the message to print, if an event is to be processed on a suspended node. */
	private String suspendMsgP, suspendMsgE;
	
	/**
	 * Constructor.
	 * 
	 * @param layerType type of the layer.
	 */
	public Layer(LayerType layerType) {
		if (layerType == null) {
			throw new IllegalArgumentException("layerType");
		}
		this.layerType = layerType;
	}
		
	/**
	 * method to check, if the node is suspended and print a message.
	 * @param event the to be processed event/packet.
	 * @return true if the node is not suspended and the event should be processed.
	 */
	private boolean checkNotSuspended(ToLayer event) {
		if (isSuspended()) {
			String s;
			
			if (event instanceof Packet) {
				s = suspendMsgP + (Packet) event + "]";
			} else {
				s = suspendMsgE + event + "]";
			}
			LOGGER.warn(s);
			
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * with this method, a packet is sent along the desired direction to the next layer. 
	 * 
	 * If the layer is suspended, a warning is logged and the event is dropped.
	 * 
	 * @param packet the to be sent packet along the direction. 
	 */
	public void sendPacket(Packet packet) {
		if (checkNotSuspended(packet)) {
			LayerType layer = null;
			
			if (packet.getDirection() ==  Direction.DOWNWARDS) {
				layer = getLayerType().getLowerLayer();
				packet.setTime(getKernel().getCurrentTime());
			} else {
				layer = getLayerType().getUpperLayer();
			}
			
			if (layer == null) {
				throw new IllegalArgumentException("Layer " + this + " cannot send packets " + packet.getDirection());
			}
			
			getKernel().enqueueEvent(this, packet, layer);
		}
	}
	
	/**
	 * method to send an event up to the next higher layer. 
	 * 
	 * If the layer is suspended, a warning is logged and the event is dropped.
	 * 
	 * @param event the to be processed event.
	 */
	public void sendEventUp(ToLayer event) {
		if (checkNotSuspended(event)) {
			getKernel().enqueueEvent(this, event, getLayerType().getUpperLayer());
		}
	}
	
	/**
	 * method to send an event up to the next lower layer. 
	 * 
	 * If the layer is suspended, a warning is logged and the event is dropped.
	 * 
	 * @param event the to be processed event.
	 */
	public void sendEventDown(ToLayer event) {
		if (checkNotSuspended(event)) {
			getKernel().enqueueEvent(this, event, getLayerType().getLowerLayer());
		}
	}
	
	/**
	 * method to send an event to the same layer.
	 * 
	 * If the layer is suspended, a warning is logged and the event is dropped.
	 *  
	 * @param event the to be processed event.
	 */
	public void sendEventSelf(ToLayer event) {
		if (checkNotSuspended(event)) {
			getKernel().enqueueEvent(this, event, getLayerType());
		}
	}
	
	/**
	 * method to send an event to a specific layer.
	 * 
	 * If the layer is suspended, a warning is logged and the event is dropped.
	 *  
	 * @param event the to be processed event.
	 * @param toLayer the layer, to which this event is sent. 
	 */
	public void sendEventTo(ToLayer event, LayerType toLayer) {
		if (checkNotSuspended(event)) {
			getKernel().enqueueEvent(this, event, toLayer);
		}
	}
	
	/**
	 * Final method to delegate the incoming packet onto the according service access point.
	 * 
	 * @param event Event for the layer.
	 * @throws LayerException if something goes wrong.
	 */
	public void processEvent(ToLayer event) throws LayerException {
		if (event instanceof Packet) {
			Packet packet = (Packet) event;
			if (packet.getDirection() == Direction.UPWARDS) {
				//direction is upward, packet coming from lower layer
				this.lowerSAP(packet);
			} else {
				this.upperSAP(packet);
			}
		} else if (event instanceof WakeUpCall) {
			this.processWakeUpCall((WakeUpCall) event);
		} else {
			throw  new LayerException(
					"Layer " + this.getClass().getName()
					+ " did not handle event " + event.getClass().getName()
					+ "in processEvent(ToLayer)!"); 
		}
	}
	
	/** 
	 * @return the layerType of this Layer. 
	 * @deprecated Use getLayerType()
	 */
	@Deprecated
	public final LayerType getThisLayer() {
		return getLayerType();
	}

	/**
	 * Abstract method definining a lower access point on any layer.
	 * 
	 * @param packet Packet to process in the layer coming from a lower layer.
	 * @throws LayerException if something goes wrong (e.g. type not handled properly).
	 */
	public abstract void lowerSAP(Packet packet) throws LayerException;
	
	/**
	 * Abstract method definining a higher access point on any layer.
	 * 
	 * @param packet Packet to process in the layer coming from a higher layer.
	 * @throws LayerException if something goes wrong (e.g. type not handled properly).
	 */
	public abstract void upperSAP(Packet packet) throws LayerException;
	
	/**
	 * Default implementation for all LAYERS. Throw exception when not overloaded.
	 * 
	 * @param wuc The received WakeUpCall.
	 * @throws LayerException when implementation placed WUCs but has no overloaded handling of WUCs.
	 */
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {
		throw  new LayerException(
				"Layer " + this.getClass().getName()
				+ " did not handle event " + wuc.getClass().getName() + "!");
	}
	
	/**
	 * Method to initialize/finalize this layer.
	 * @param simState Initialize/Finalize event to start up / terminate the layer.
	 */
	public void processEvent(SimulationState simState) {
		if (simState instanceof Initialize) {
			this.processEvent((Initialize) simState);
		} else if (simState instanceof Finalize) {
			this.processEvent((Finalize) simState);
		} else if (simState instanceof StartSimulation) {
			this.processEvent((StartSimulation) simState);
		}
	}

	/**
	 * Method to initialize this layer.
	 * 
	 * Subclasses may overwrite it.
	 * 
	 * @param init Initialize event to start up the layer.
	 */	
	protected void processEvent(Initialize init) {
		// nothing to do
	}
	
	/**
	 * Method to finalize this layer.
	 * 
	 * Subclasses may overwrite it.
	 * 
	 * @param end Finalize event to terminate the layer.
	 */
	protected void processEvent(Finalize end) {
		// nothing to do
	}

	/**
	 * Method to start this layer.
	 * 
	 * Subclasses may overwrite it.
	 * 
	 * @param start StartSimulation event to start the layer.
	 */
	protected void processEvent(StartSimulation start) {
		// nothing to do
	}
	
	/**
	 * Method to ensure proper processing of Moved events in the LAYERS.
	 * To receive Moved events a layer has to register in Node.Layerstack.addToMovedObserver
	 * and overload this method for proper implementation.
	 * 
	 * @param moved Moved event with new POSITION.
	 * @throws LayerException if layer registered to MoveObserver and did not overload this method.
	 */
	public void processEvent(Moved moved) throws LayerException {
		throw new LayerException("Node " + getId() + " layer " + this.getClass().getName()
								+ " did not overload processEvent(Moved)!");
	}
	
	/**
	 * Method to retrieve the node of a layer.
	 * Used to retrieve position or energy status.
	 * 
	 * @return Returns the node.
	 */
	public final Node getNode() {
		return node;
	}
	
	/**
	 * Method to update the node for a layer.
	 * Sets a new node only once, further calls
	 * are discarded.
	 * 
	 * Construction needed for creating nodes in NodeGenerator.
	 * 
	 * @param node Node to set.
	 * @return Returns boolean whether update was successful.
	 * 			False when set before.
	 */
	public boolean setNode(Node node) {
		boolean result = this.node == null;
		
		if (result) {
			this.node = node;
			this.id = node.getId();
			this.sender = new Address(node.getId(), getLayerType());
			
			suspendMsgP = "Layer " + id + "." + getLayerType() 
						  +	" suspended. Cannot send packet [";

			suspendMsgE = "Layer " + id + "." + getLayerType()
						  +	" suspended. Cannot send event [";
		}
		return result;
	}
	
	/**
	 * returns the configuration of the current simulation run.
	 * This value is available after the call of initConfiguration().
	 * 
	 * @return the current configuration
	 */
	protected Configuration getConfig() {
		return this.config;
	}
	
	/**
	 * returns a random generator.
	 * In this implementation to "global" random generator of the
	 * Configuration is used.
	 * However, subclasses may overwrite this method to provide
	 * an own random source.
	 * 
	 * @return a random generator
	 */
	public RandomGenerator getRandom() {
		return this.config.getRandomGenerator();
	}
	
	/**
	 * returns the simulation kernel interface.
	 * 
	 * @return simulation kernel interface
	 */
	public SimulationKernel getKernel() {
		return this.config.getKernel();
	}

	/**
	 * initializes the configuration of the object. It is assured by
	 * the runtime system that the <code>Configuration</code> is valid, when
	 * this method is called.
	 * 
	 * This implementation saves a reference to the configuration for later
	 * access. While subclasses should overwrite this method,
	 * we should always call the super implementation.
	 * 
	 * @throws ConfigurationException
	 *             thrown when the object cannot run with the configured values.
	 */
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		this.config = configuration;
		
		// Assertion checks
		if (node == null || id == null || sender == null) {
			throw new ConfigurationException("Invalid implemention of setNode");
		}
		// Call old deprecated init method
		initConfiguration();
	}
	
	/**
	 * initialites the configuration of the object.
	 * To avoid calls of Configuration.getInstance(), please use
	 * initConfiguration(configuration).
	 * 
	 * @deprecated Please use initConfiguration(configuration)
	 * @throws ConfigurationException
	 */
	@Deprecated
	public void initConfiguration() throws ConfigurationException {
		// do nothing
	}

	/**
	 * Called by the ConfigurableFactory after setting the configured parameter.
	 * 
	 * @throws ConfigurationException
	 *             thrown if configuration is invalid.
	 */
	public void init() throws ConfigurationException {
		// nothing to do
	}
	
	/**
	 * returns the node id of the layer.
	 * 
	 * @return node id
	 */
	public final NodeId getId() {
		return node.getId();
	}
	
	/**
	 * returns the sender address of the node.
	 * @return sender address.
	 */
	public Address getSender() {
		return sender;
	}

	/**
	 * returns the layer of the layer.
	 * 
	 * @return the layer type
	 */
	public LayerType getLayerType() {
		return layerType;
	}

	/**
	 * returns true is the layer is suspended.
	 * @return the suspension state
	 */
	public boolean isSuspended() {
		return isSuspended;
	}
	
	/**
	 * suspends the layer.
	 * 
	 * @param suspended new suspension state
	 */
	public void suspend(boolean suspended) {
		if (isSuspended != suspended) {
			isSuspended = suspended;
		}
	}
	
	/** @return the layer state. The default implementation returns "null". */
	public LayerState getState() {
		return null;
	}

	/**
	 * sets the layer state.
	 * @param status the new layer state, which is ignore3d here.
	 * @return false since nothing is done here.
	 */
	public boolean setState(LayerState status) {
		return false;
	}
}
