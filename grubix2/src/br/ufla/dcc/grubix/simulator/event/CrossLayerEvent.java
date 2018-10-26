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

package br.ufla.dcc.grubix.simulator.event;



import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.node.Layer;


/**
 * common superclass to inform a higher layer about the result of a (failed) transmission.
 * @author Dirk Held
 */
public class CrossLayerEvent extends WakeUpCall {

	/** the logger of this class. */
	public static final Logger LOGGER = Logger.getLogger(CrossLayerEvent.class.getName());
	
	/** the packet, which this event refers to. */
	protected Packet packet;
	
	/** the result of the Transmission. */
	protected CrossLayerResult result;
	
	/** the layer, where the result event was created. */
	protected LayerType originator;
	
	/** if set, this event should not be propagated higher, than this layer. */
	protected LayerType terminalLayer;
	
	/**
	 * default creator of this class. The terminal layer is set to the upper layer of the creator. 
	 * is set to the upper layer of the originator.
	 * 
	 * @param sender the layer, where the event is created.
	 * @param packet the affected packet, may be null.
	 * @param result the error code of the operation.
	 */
	public CrossLayerEvent(Address sender, Packet packet, int result) {
		super(sender);
		init(sender, sender.getFromLayer().getUpperLayer(), packet, new CrossLayerResult(result));
	}

	/**
	 * default creator of this class. The terminal layer is set to the upper layer of the creator.
	 * @param sender the layer, where the transmission failed.
	 * @param packet the failed packet.
	 * @param result the error code of the operation.
	 */
	public CrossLayerEvent(Address sender, Packet packet, CrossLayerResult result) {
		super(sender);
		init(sender, sender.getFromLayer().getUpperLayer(), packet, result);
	}

	/**
	 * default creator of this class.
	 * 
	 * @param sender   the layer, where the event is created.
	 * @param receiver the terminal layer for this event.
	 * @param packet   the affected packet, may be null.
	 * @param result   the error code of the operation.
	 */
	public CrossLayerEvent(Address sender, LayerType receiver, Packet packet, int result) {
		super(sender);
		init(sender, receiver, packet, new CrossLayerResult(result));
	}

	/**
	 * default creator of this class. The terminal layer is set to the upper layer of the creator.
	 * @param sender the layer, where the transmission failed.
	 * @param receiver the terminal layer for this event.
	 * @param packet the failed packet.
	 * @param result the error code of the operation.
	 */
	public CrossLayerEvent(Address sender, LayerType receiver, Packet packet, CrossLayerResult result) {
		super(sender);
		init(sender, receiver, packet, result);
	}

	/**
	 * default creator of this class. The result is set to CrossLayerResult.FAIL.
	 * @param sender the layer, where the transmission failed.
	 * @param receiver the terminal layer for this event.
	 * @param packet the failed packet.
	 */
	public CrossLayerEvent(Address sender, LayerType receiver, Packet packet) {
		super(sender);
		init(sender, receiver, packet, new CrossLayerResult(CrossLayerResult.FAIL));
	}

	/**
	 * default creator of this class. The terminal layer is set to the upper layer of the creator.
	 * The result is set to CrossLayerResult.FAIL.
	 * @param sender the layer, where the transmission failed.
	 * @param packet the failed packet.
	 */
	public CrossLayerEvent(Address sender, Packet packet) {
		super(sender);
		init(sender, sender.getFromLayer().getUpperLayer(), packet, new CrossLayerResult(CrossLayerResult.FAIL));
	}

	/**
	 * method to initialize the event.
	 * 
	 * @param sender        the creator of this event.
	 * @param terminalLayer the wished terminal layer.
	 * @param packet        the affected packet, if any.
	 * @param result        the result to transmit to the higher layers.
	 */
	private void init(Address sender, 
					  @SuppressWarnings("hiding") LayerType terminalLayer,
					  @SuppressWarnings("hiding") Packet packet, 
					  @SuppressWarnings("hiding") CrossLayerResult result) {
		
		originator = sender.getFromLayer();
		setTerminalLayer(terminalLayer);
		this.packet = packet;
		this.result = result;
		
		checkPacketTerminalLayerMsg(terminalLayer);
		checkSuppliedPacket(sender.getFromLayer());
	}
	
	/** method to check, if the supplied packet is for the next higher layer. */
	private void checkSuppliedPacket(LayerType sender) {
		if (packet != null) {
			LayerType upper = sender.getUpperLayer();
			
			if (!packet.getLayer().equals(upper)) {
				LOGGER.error("Node " + this.getSender() + ": supplied packet " + this.packet + " is not from the upper layer " + upper.name()
						+ ", but for layer " + packet.getLayer().name());
			}
		}
	}
	
	/** 
	 * check method to verify if an enclosed packet for the requested 
	 * terminal layer is present. If not, just a message is print. 
	 * @param newReceiver the new wished terminal layer. 
	 */
	private void checkPacketTerminalLayerMsg(LayerType newReceiver) {
		if (!checkPacketLayer(newReceiver)) {
			LOGGER.error("no packet supplied for the requested terminal layer.");
		}
	}
	
	/** 
	 * check method to verify if an enclosed packet for the requested terminal layer is present. 
	 * @param newReceiver the new wished terminal layer. 
	 * @return true if no packet was present or the terminal is the packet or is enclosed.
	 */
	private boolean checkPacketLayer(LayerType newReceiver) {
		if ((packet != null) && (newReceiver != null)) {
			if (!packet.getLayer().equals(newReceiver) && (packet.getEnclosedPacket(newReceiver) == null)) {
				return false;
			}
		} else {
			if ((packet == null) && (terminalLayer != null) && (newReceiver != null)) {
				if (newReceiver.compareTo(terminalLayer) > 0) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * method to check, if this event is to be forwarded up to the next layer.
	 * 
	 * @param thisLayer the layer, which has to decide, if this event is to be forwarded up.
	 * @return true, if this event is to be forwarded up.
	 */
	public final boolean checkForwardUp(Layer thisLayer) {
		boolean forwardUp =    (terminalLayer == null) 
							|| (!thisLayer.getLayerType().equals(terminalLayer));
		if (forwardUp) {
			if (packet != null) {
				if (packet.isTerminal()) {
					forwardUp = false;
				}
			}
		}
		return forwardUp;
	}
	
	/**
	 * method to forward this event up, if necessary.
	 * @param thisLayer the layer, which received but not processes this event.
	 */
	private void forwardUpInternal(Layer thisLayer) {
		if (checkForwardUp(thisLayer)) {
			if (packet != null) {
				packet = packet.getEnclosedPacket();
			}
			setSender(thisLayer.getSender());
			thisLayer.sendEventUp(this);
		}
	}
	
	/**
	 * method to forward this event up (at least to the given layer), if necessary.
	 * @param thisLayer the layer, which received but not processes this event.
	 */
	public final void forwardUp(Layer thisLayer) {
		LayerType receiver = thisLayer.getLayerType().getUpperLayer();
		pushTerminalLayer(receiver);
		forwardUpInternal(thisLayer);
	}
	
	/** @return the packet, which created this event. */
	public final Packet getPacket() {
		return packet;
	}

	/** @return the error code. */
	public final CrossLayerResult getResult() {
		return result;
	}

	/**
	 * method to change the error code.
	 * @param result the new error code.
	 */
	public final void setResult(CrossLayerResult result) {
		this.result = result;
	}

	/** @return the creator of this event. */
	public final LayerType getOriginator() {
		return originator;
	}

	/** @return the terminal layer for this event. */
	public final LayerType getTerminalLayer() {
		return terminalLayer;
	}

	/** 
	 * method to set a terminal layer for this event.
	 * @param receiver the new terminal layer for this event. 
	 */
	private void setTerminalLayer(LayerType receiver) {
		if ((receiver == null) || (originator.compareTo(receiver) < 0)) {
			terminalLayer = receiver;
			//checkPacketLayerMsg();
		} else {
			terminalLayer = originator.getUpperLayer();
			LOGGER.error("terminal layer is not above originating layer.");
		}
	}
	
	/**
	 * method to ensure that the terminal layer is at least the supplied one.
	 * @param receiver the layer, up to which this event has to be forwarded at least.
	 */
	private void pushTerminalLayer(LayerType receiver) {
		if  ((terminalLayer != null)  
		  && (terminalLayer.compareTo(receiver) < 0)
		  && checkPacketLayer(receiver)) {
			setTerminalLayer(receiver);
		}
	}
}
