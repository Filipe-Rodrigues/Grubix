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

import java.util.ArrayList;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.TimedObject;




/** 
 * Abstract superclass for all types of packets.
 * A packet can be terminal (no enclosed packet) or non-terminal.
 * Communication between two nodes is only possible between the same LAYERS.
 * Therefore LAYERS of same height can communicate because addressing in the goal
 * node is done via decapsulating the enclosed packets.
 * 
 * The size of any packet is automatically calculated by the {@link PacketSizeCalculator}.
 * If you intend to influence its calculation or if you want to set the size of your own packets
 * manually then use one of the annotations ({@link StaticPacketSize},{@link StaticHeaderDataSize},
 * {@link NoHeaderData}) as described in the {@link PacketSizeCalculator} class.
 * It is also possible to use the {@link this#setHeaderLength(int)} method, however this
 * method should be marked as deprecated in the future.
 * The time needed to transmit a packet depends on its (automatically) calculated
 * size and the BPS (in terms of ShoX: Bits per Simulationstep) value. The BPS needs to be
 * set by the {@link MACLayer} or {@link PhysicalLayer} implementation.  
 * 
 * @author Andreas Kumlehn, Dirk Held
 */
public abstract class Packet extends ToLayer implements TimedObject, Cloneable {
	
	/** If true, the AirModule sends a receiver information event back. 
	 * @see br.ufla.dcc.grubix.simulator.event.ReceiverInformationEvent */
	@NoHeaderData
	private boolean requestReceiverInformation;
	
	/** If true, the headerlength and (sync-)Duration is changeable. */
	@NoHeaderData
	protected boolean changeable;
	
	/** defines the bit error rate. for example 10^-6 */
	@NoHeaderData
	private double ber;
	
	/** is true, if the packet is after a transmission still intact. */
	@NoHeaderData
	private boolean valid;
	
	/** 
	 * Layers may set this flag and check, where important packets are 
	 *  dropped otherwhise. This flag is inherited from the inner packet.
	 */
	@NoHeaderData
	private boolean important;
	
	/*
	 * These two lists are generated within the air and put into the physical packet.
	 * Then, these lists are forwarded into the inner packets.
	 */
	/** this list is set in the air module to the list of nodes receiving the packet valid. */
	@NoHeaderData
	private ArrayList<NodeId> receivers;
	/** this list is set in the air module to the list of nodes receiving the packet invalid. */
	@NoHeaderData
	private ArrayList<NodeId> invalidReceivers;
	/** this list can be set from the creator for broadcasts to contain nodes
	 *  to be expected to receive this packet. If an important received packet  
	 *  gets dropped in the Air or the 802.11 Phy, a log message is printed. */
	@NoHeaderData
	private ArrayList<NodeId> importantReceivers;
	
	/** all meta informations are stored here. This link is forwarded down to lower layers. */
	@NoHeaderData
	private MetaInformationContainer metaInfos = null;
	
	/** the header of this layer. */
	private String header;
	
	/** if a header is not defined as String, at least the length must be set. */
	@NoHeaderData
	private int headerLength = 0;
	
	/**
	 * 
	 * Direction of the packet inside a node. via radio.
	 */
	@NoHeaderData
	private Direction direction = Direction.DOWNWARDS;

	/** The simulation time when the packet was created (i.e. initially sent). */
	@NoHeaderData
	private double timeStamp;
	
	/**
	 * The enclosed packet inside a packet.
	 * Is null if no packet enclosed.
	 */
	private Packet enclosedPacket;

	/** flag that indicates whether or not the header length was set manually. */
	@NoHeaderData
	private boolean isManuallySetHeaderLength = false;
	
	/**
	 * Default constructor of class Packet to create a terminal packet
	 * with no enclosed packet.
	 * @param sender Senderaddress of the packet
	 * @param receiver ReceiverId of the packet
	 */
	public Packet(Address sender, NodeId receiver) {
		super(sender, receiver);
		init(null);
	}
	
	/**
	 * Overloaded constructor to create non-terminal packets by
	 * specifying a packet to enclose.
	 * ReceiverID of the new packet taken from enclosedPacket.
	 * 
	 * @param sender Senderaddress of the packet
	 * @param packet The packet to enclose inside the new packet.
	 */
	public Packet(Address sender, Packet packet) {
		super(sender, packet.getReceiver());
		init(packet);
	}
	
	/**
	 * Overloaded constructor to create non-terminal packets by
	 * specifying a packet to enclose.
	 * ReceiverID of the new packet may not be the same as stated in the enclosedPacket.
	 * 
	 * @param sender Senderaddress of the packet
	 * @param receiver ReceiverId of the packet
	 * @param packet The packet to enclose inside the new packet.
	 */
	public Packet(Address sender, NodeId receiver, Packet packet) {
		super(sender, receiver);
		init(packet);
	}
	
	/**2
	 * the initilisator for both constructors.
	 * @param packet the optional enclosed packet.
	 */
	private void init(Packet packet) {
		important      = false;
		changeable     = true;
		enclosedPacket = packet;
		importantReceivers = null;
		requestReceiverInformation = false;
		
		if (packet != null) {
			setImportant(packet.isImportant());
			
			// get the meta information from the packet that is enclosed
			metaInfos          = packet.getMetaInfos();

			importantReceivers = packet.getImportantReceivers();
			
			packet.setWriteProtect();
		} else {
			metaInfos = new MetaInformationContainer();
		}
		
		reset();
	}
	
	/** This method is to be called in AirModule transmit only. */
	public void createReceiverLists() {
		receivers        = new ArrayList<NodeId>();
		invalidReceivers = new ArrayList<NodeId>();
		
		Packet p = enclosedPacket;
			
		while (p != null) {
			p.receivers        = receivers;
			p.invalidReceivers = invalidReceivers;
			p = p.enclosedPacket;
		}
	}

	/** 
	 * This method is used in the air layer to add a receiver of
	 * this packet to the approbiate list in the original packet.
	 * The value of valid is used to determine the correct list.
	 * @param isValid the result of the mangling process.
	 * @param other   the NodeId of the receiver.
	 */
	public void addReceiver(boolean isValid, NodeId other) {
		if (isValid) {
			receivers.add(other);
		} else {
			invalidReceivers.add(other);
		}
	}
	
	/**
	 * use this method for example where you process the MACSendingTerminated,
	 * to check, whether a node has received this packet, or not.
	 * 
	 * @param other the expected receiver of this packet.
	 * @return true, if the packet has reached the receiver undamaged.
	 */
	public boolean hasReceived(NodeId other) {
		for (NodeId other2 : receivers) {
			if (other.equals(other2)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * use this method for example where you process the MACSendingTerminated,
	 * to check, whether a node has received this packet invalid, or not.
	 * 
	 * @param other the expected receiver of this packet.
	 * @return true, if the packet has reached the receiver damaged.
	 */
	public boolean hasReceivedDamaged(NodeId other) {
		for (NodeId other2 : invalidReceivers) {
			if (other.equals(other2)) {
				return true;
			}
		}
		return false;
	}
	
	/** this method resets the packet to prepare the packet for resending. */
	public void reset() {
		if (direction == Direction.UPWARDS) {
			direction = Direction.DOWNWARDS;
		}
		
		changeable = true;
		valid = true;
		ber = -1.0;
		
		if (enclosedPacket != null) {
			enclosedPacket.reset();
		}
	}

	/**
	 * implements the clone method to duplicate packets.
	 * @return a clone of the object.
	 */
	@Override
	public Object clone() {
		Packet obj;
		try {
			obj = (Packet) super.clone();
			
			if (enclosedPacket == null) {
				obj.metaInfos = new MetaInformationContainer();
			} else {
				obj.enclosedPacket = (Packet) enclosedPacket.clone();
				obj.metaInfos      = obj.enclosedPacket.metaInfos;
			}
			
			return obj;
		} catch (CloneNotSupportedException e) {
			throw new Error("Base Packet of Packet forbids cloneing");
		}
	}
	
	/** after a packet is added as inner packet to another packet the headerlength is locked. */
	public void setWriteProtect() {
		changeable = false;
		
		if (enclosedPacket != null) {
			enclosedPacket.setWriteProtect();
		}
	}
	
	/**
	 * Method to test if a packet has no enclosed packet.
	 * @return  Boolean TRUE if no packet enclosed.
	 */
	public final boolean isTerminal() {
		return enclosedPacket == null;
	}

	/** method to prepare the packet for the next hop. */
	public final void flipDirection() {
		if (direction == Direction.UPWARDS) {
			direction = Direction.DOWNWARDS;
		} else {
			direction = Direction.UPWARDS;
		}
		
		if (enclosedPacket != null) {
			enclosedPacket.flipDirection();
		}
	}
	
	/**
	 * @return Returns the direction.
	 */
	public final Direction getDirection() {
		return direction;
	}
	
	/**	@return Returns the enclosedPacket. */
	public Packet getEnclosedPacket() {
		return enclosedPacket;
	}
	
	/**
	 * sets the enclosed packet.
	 * 
	 * @param packet a new enclosed packet
	 */
	public void setEnclosedPacket(Packet packet) {
		if (!changeable) {
			throw new IllegalStateException("packet not changeable");
		}
		this.enclosedPacket = packet;
	}
	
	/** @return the layer type of the innermost enclosed packet.*/
	public final LayerType getHighestLayerType() {
		if (enclosedPacket == null) {
			return getLayer();
		} else {
			return enclosedPacket.getHighestLayerType();
		}
	}
	
	/**
	 * method to extract a special enclosed layer of a specified type.
	 * 
	 * @param layer the requested layer.
	 * @return the requested packet or null.
	 */
	public final Packet getEnclosedPacket(@SuppressWarnings("hiding") LayerType layer) {
		Packet packet = enclosedPacket;
		
		if ((packet == null) || (packet.getLayer().equals(layer))) {
			return packet;
		} else {
			return packet.getEnclosedPacket(layer);
		}
	}

	/**
	 * method to extract a layer of a specified type. If the packet itself is of the
	 * requested type, it is returned. Otherwise getEnclosedPacket(layer) is called.
	 * 
	 * @param layer the requested layer.
	 * @return the requested packet or null.
	 */
	public final Packet getPacket(@SuppressWarnings("hiding") LayerType layer) {
		if (getLayer().equals(layer)) {
			return this;
		} else {
			return getEnclosedPacket(layer);
		}
	}
	
	/** @return the header. */
	public final String getHeader() {
		return header;
	}

	/** @param header the header to set. */
	public final void setHeader(String header) {
		if (changeable) {
			this.header = header;
			setHeaderLength(header.length());
		}
	}
	
	/** 
	 * Returns the pure headerLength of this packet (disregarding the enclosed packets!).
	 * If the headerLength is set using the {@link this#setHeaderLength(int)} method then
	 * this value is returned. If the value has not been set then the 
	 * {@link PacketSizeCalculator#calculatePacketSize(Packet)} method is used to determine
	 * the header length.
	 * @return the calculated or manually set headerLength.
	 */
	public final int getHeaderLength() {
		int retVal = headerLength;
		if (!isManuallySetHeaderLength) {
			retVal = PacketSizeCalculator.calculatePacketSize(this);
		} 
		return retVal;
	}

	/** 
	 * Sets the size of this packet.
	 * The size set also needs to include the size of all enclosed packets!
	 * @param headerLength the headerLength to set in bit.
	 */
	public final void setHeaderLength(int headerLength) {
		if (changeable) {
			this.headerLength = headerLength;
			this.isManuallySetHeaderLength = true;
		}
	}
	
	/** 
	 * Returns the total packet size in bits.
	 * @return the total packet size in bits.
	 */
	public final int getTotalPacketSizeInBit() {
		int totalSize = 0;
		Packet currentPacket = this;
		while (currentPacket != null) {
			totalSize += currentPacket.getHeaderLength();
			currentPacket = currentPacket.getEnclosedPacket();
		}
		return totalSize; 
	}

	/**
	 * Sets the current simulation time obtained from the simulation manager.
	 * @param currentTime The current simulation time
	 */
	public final void setTime(double currentTime) {
		timeStamp = currentTime;
	}
	
	/**
	 *  method to take the propagation delay into account.
	 * @param delta the time value, to change the timestamp by.
	 */
	public final void adjustTime(double delta) {
		timeStamp += delta;
		
		if (enclosedPacket != null) {
			enclosedPacket.adjustTime(delta);
		}
	}
	
	/** @return The simulation time when the packet was created (i.e. initially sent). */
	public final double getTime() {
		return timeStamp;
	}

	/** @return the BER. */
	public final double getBER() {
		return ber;
	}

	/** @param ber the ber to set. */
	public final void setBER(double ber) {
		this.ber = ber;
	}

	/** @return the valid flag. */
	public final boolean isValid() {
		return valid;
	}

	/**
	 * Sets the valid flag for this packet. If this packet has enclosed packets, they
	 * are flagged recursively, as well. 
	 * @param valid the valid to set. 
	 */
	public final void setValid(boolean valid) {
		this.valid = valid;
		if (this.getEnclosedPacket() != null) {
			this.getEnclosedPacket().setValid(valid);
		}
	}

	/** @return the layer which this packet belongs to. */
	public final LayerType getLayer() {
		return getSender().getFromLayer();
	}
	
	/**
	 * searches recursively through the packet and all enclosed packets for the first packet
	 * from the given class.
	 * @param <T> a packet class
	 * @param packetClass a class object for a given packet type
	 * @return the first enclosed packet with the given type or null
	 */
	@SuppressWarnings("unchecked")
	public <T extends Packet> T containsPacketType(Class<T> packetClass) {
		 if (this.getClass().equals(packetClass)) {
			 return (T) this; // cast never fails
		 }
		 if (enclosedPacket != null) {
			 return enclosedPacket.containsPacketType(packetClass);
		 }
		 return null;
	 }

	/**
	 * searches the highest enclosed packet. This means
	 * the enclosed packet with no other enclosed packets.
	 * In most cases this will be an application layer packet, but
	 * it may also be an operating system packet.
	 * This method assumes a correct created packet chain. If there is a loop
	 * in the packet chain, this method will cause an infinite loop.
	 * 
	 * @return the highest enclosed packet.
	 */
	public Packet getHighestEnclosedPacket() {
		if (enclosedPacket == null) {
			return this;
		}
		return enclosedPacket.getHighestEnclosedPacket();
	}
	
	/**
	 * searches through the packet and all enclosed packets for the last packet
	 * from the given class.
	 * @param <T> a packet class
	 * @param packetClass a class object for a given packet type
	 * @return the last enclosed packet with the given type or null
	 */
	@SuppressWarnings("unchecked")
	public <T extends Packet> T getInnermostPacketOfType(Class<T> packetClass) {
		Packet currentlyInspectedPacket;
		T lastMatchingPacket;
		
		lastMatchingPacket = null;
		currentlyInspectedPacket = this;

		while (currentlyInspectedPacket != null) {
			if (currentlyInspectedPacket.getClass().equals(packetClass)) {
				lastMatchingPacket = (T) currentlyInspectedPacket;
			}
			currentlyInspectedPacket = currentlyInspectedPacket.getEnclosedPacket();
		}
		return lastMatchingPacket;
	}

	/** @return true, if this packet was set to be important. */
	public final boolean isImportant() {
		return important;
	}

	/**
	 * method to declare this packet to be important.
	 * @param important use true, to set this packet to be important.
	 */
	public final void setImportant(boolean important) {
		this.important = important;
	}

	/** @return the list of nodes which received this packet valid. */
	public final ArrayList<NodeId> getReceivers() {
		return receivers;
	}

	/**
	 * method to get the receiver list from a higher layer.
	 * @param receivers the list of receivers to set.
	 */
	public final void setReceivers(ArrayList<NodeId> receivers) {
		this.receivers = receivers;
	}

	/** @return the list of nodes which received this packet invalid. */
	public final ArrayList<NodeId> getInvalidReceivers() {
		return invalidReceivers;
	}

	/**
	 * method to get the invalid receiver list from a higher layer.
	 * @param invalidReceivers the list of invalid receivers to set.
	 */
	public final void setInvalidReceivers(ArrayList<NodeId> invalidReceivers) {
		this.invalidReceivers = invalidReceivers;
	}

	/** @return the list of nodes which are expected to receive this packet. */
	public final ArrayList<NodeId> getImportantReceivers() {
		return importantReceivers;
	}
	
	/**
	 * method to add a node id to the important receivers list. This only makes sense
	 * for broadcasts, since all important unicast packets are handled like the receiver 
	 * is the only entry in the list. The packet is then set to be important.
	 * 
	 * @param other the expected receiver of this packet.
	 */
	public final void addExpectedReceiver(NodeId other) {
		if (!other.equals(NodeId.ALLNODES)) {
			if (importantReceivers == null) {
				importantReceivers = new ArrayList<NodeId>();
			}
			
			for (NodeId other2 : importantReceivers) {
				if (other2.equals(other)) {
					return;
				}
			}
			
			importantReceivers.add(other);
			important = true;
		}
	}
	
	/**
	 * This method is used, to query, if a given node id was considered to be an important 
	 * receiver. This is checked in the Air and the 802.11 Phy prior dropping the packet.
	 * The packet will still be dropped.
	 * 
	 * @param other the id of the receiver to check prior dropping the packet.
	 * @return true if the packet should have reached this node.
	 */
	public final boolean isExpectedReceiver(NodeId other) {
		if (important) {
			if (getReceiver().equals(NodeId.ALLNODES)) {
				if (importantReceivers != null) {
					for (NodeId expReceiver : importantReceivers) {
						if (expReceiver.equals(other)) {
							return true;
						}
					}
				}
				return false;
			} else {
				return getReceiver().equals(other);
			}
		} else {
			return false;
		}
	}
	
	/** This method clears the list of expected receivers. */
	public final void clearExpectedReceivers() {
		importantReceivers = null;
		
		if (enclosedPacket != null) {
			enclosedPacket.clearExpectedReceivers();
		}
	}

	/** 
	 * Gets the meta information of the packet.
	 * @return the meta information stored in this packet (might be null). 
	 */
	public MetaInformationContainer getMetaInfos() {		
		return metaInfos;
	}

	/** @return true if the originator of this packets wants to get the receiver information event. */
	public boolean isRequestReceiverInformation() {
		boolean res = requestReceiverInformation;
		
		if (enclosedPacket != null) {
			res |= enclosedPacket.isRequestReceiverInformation();
		}
		
		return res;
	}

	/** @param requestReceiverInformation use true to request to get the receiver information event. */
	public void setRequestReceiverInformation(boolean requestReceiverInformation) {
		this.requestReceiverInformation = requestReceiverInformation;
	}
	
	/**
	 * @return String for logging.
	 */
	@Override
	public String toString() {
		return "Packet[from=" + getSender().getId() + ",to=" + getReceiver() + "]";
	}
}
