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

Copyright 2008 The ShoX developers as defined under http://shox.sourceforge.net
********************************************************************************/

package br.ufla.dcc.grubix.simulator.node;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.Direction;
import br.ufla.dcc.grubix.simulator.EventId;
import br.ufla.dcc.grubix.simulator.Interval;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.AirLogTransmission;
import br.ufla.dcc.grubix.simulator.event.AirPerformCarrierSense;
import br.ufla.dcc.grubix.simulator.event.Finalize;
import br.ufla.dcc.grubix.simulator.event.Initialize;
import br.ufla.dcc.grubix.simulator.event.Interference;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.MACCarrierSensing;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalLayerState;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.ReceiverInformationEvent;
import br.ufla.dcc.grubix.simulator.event.SendingTerminated;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.ToLayer;
import br.ufla.dcc.grubix.simulator.event.Transmission;
import br.ufla.dcc.grubix.simulator.event.TransmissionBeginIncoming;
import br.ufla.dcc.grubix.simulator.event.TransmissionEndIncoming;
import br.ufla.dcc.grubix.simulator.event.TransmissionEndOutgoing;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.event.genericcarriersense.CarrierSenseCarrierFreeCheck;
import br.ufla.dcc.grubix.simulator.event.genericcarriersense.CarrierSenseDurationEnd;
import br.ufla.dcc.grubix.simulator.event.genericcarriersense.CarrierSenseFailed;
import br.ufla.dcc.grubix.simulator.event.genericcarriersense.CarrierSenseIncomingInterference;
import br.ufla.dcc.grubix.simulator.event.genericcarriersense.CarrierSenseRegistrationRequest;
import br.ufla.dcc.grubix.simulator.event.genericcarriersense.CarrierSenseResult;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.genericcarriersense.CarrierSenseInformation;
import br.ufla.dcc.grubix.simulator.physical.BitManglingModel;
import br.ufla.dcc.grubix.simulator.physical.InterferenceQueue;



/** 
 * Class representing a artificial layer below the physical layer of a node.
 * This class is fixed for all nodes.
 * The AirModule stores outgoing packets as long as they are "on air" and
 * not completely send to avoid multiple sending.
 * Incoming packets are stored in sets to calculate the interference
 * and calculate bit errors.
 * 
 * @author Andreas Kumlehn, Dirk Held
 */
public class AirModule extends Layer implements StateIO {
	
	/** the used bit mangling model. */
	private final BitManglingModel bmm;
	
	/** Logger of this class. */
	protected static Logger LOGGER = Logger.getLogger(AirModule.class.getName()); 
	
	/** used to format the values logged in processAirLogTransmission(). */
	private static DecimalFormat decimalFormat = new DecimalFormat("0.00000");
	/** used in processAirLogTransmission to build the messages. */
	private static StringBuilder logTransmissionMessage = new StringBuilder();
	
	/** used for internal repeating of the cs. */
	protected Address senderMAC;
	
	/** the interval, when the AirModule has to perform carrier sense. */
	protected Interval csInterval;
	
	/** this flag indicates an interference during the variable cs interval. */
	protected boolean interferenceInVarCS;
	
	/** the interval, when the last interference occurred. */
	private Interval lastInterference;
	
	/** the queue to collect current interferences. */
	private InterferenceQueue interQueue;
	
	/** the state of the physical layer upon an event.*/
	private PhysicalLayerState phyState;
	
	/**MAC_IEEE802_11bg_AdHoc The currently transmitted packet. Null if nothing on air. */
	protected Transmission outgoing;
	/** occupied interval of the current outgoing transmission. */
	private Interval outgoingI;
	/** the last time, where a transmission reached this node, regardless of interferences. */
	private Interval lastIncomingI;
	
	/** count the number of dropped incoming packets. */
	private int droppedPacket;
	/** count the number of discarded outgoing packets. */
	private int discardedPackets;
	
	/** Map of current incoming transmissions. */
	private Map<EventId, Transmission> currentIncomingTransmissions;
	
	/**
	 * Used by the generic carrier sense mechanism to store unfinished
	 * carrier senses.<br><br>
	 * 
	 * INVARIANT:
	 * 			  For regular carrier senses: 
	 * 			  Define y as first simulation time the carrier was
	 * 			  detected after registration.
	 *			  <br> 
	 *  		  For negative carrier sense:
	 *  		  Define y as first simulation time the carrier got
	 *  		  free after registration.
	 *			  <br>
	 *  		  For carrier sense with virtual carrier sense mode
	 *  		  disabled:
	 *  		  y = min(y, time node starts sending)
	 *			  <br> 
	 * 			  Define x as the simulation time the carrier sense
	 * 			  was registered + duration of the carrier sense.
	 *			  <br>
	 *			  Then at simulation time x the CarrierSenseInformation
	 * 			  for all carrier senses with y less than x is already
	 * 			  removed from the list.
	 */
	private List<CarrierSenseInformation> unfinishedCarrierSenses;
	
	/** used for debugging purposes. */
	protected Interval lastReceivedPacketI;
	/** used for debugging purposes. */
	protected Interval lastSentPacketI;
	
	
	/** the packets for the former counters. */
	protected Packet lastReceivedPacket, lastSentPacket;

	/** Counts the number of transmissions while the air module is busy. */
	private static int airModuleBusyCount = 0;
	
	/**
	 * This is used to be able to check the channel for activity
	 * without receiving the incoming packets.
	 * Therefore if this is activated, the AirModule will not switch to
	 * RadioState.RECEIVING and will stay in RadioState.LISTENING, when
	 * an incoming transmission is detected.
	 */
	private boolean isOnlyCarrierSensing;
	
	/**
	 * The time (in simulation steps) when isOnlyCarrierSensing was
	 * last time switched from on to off, as this was the last time
	 * packets weren`t decoded.
	 */
	private double lastTimeOnlyCarrierSensing;
	
	/** Constructor of the class AirModule. */
	public AirModule() {
		super(LayerType.AIR);
		this.bmm = Configuration.getInstance().getBitManglingModel();
		this.currentIncomingTransmissions = new HashMap<EventId, Transmission>();
		this.interQueue = new InterferenceQueue(bmm);
		this.isOnlyCarrierSensing = false;
		this.lastTimeOnlyCarrierSensing = 0.0;
		this.unfinishedCarrierSenses = new ArrayList<CarrierSenseInformation>();
	}
	
	/**
	 * Constructor of the class AirModule.
	 * @param bmm bit mangling model
	 */
	public AirModule(BitManglingModel bmm) {
		super(LayerType.AIR);
		this.bmm = bmm;
		this.currentIncomingTransmissions = new HashMap<EventId, Transmission>();
		this.interQueue = new InterferenceQueue(bmm);
		this.isOnlyCarrierSensing = false;
		this.lastTimeOnlyCarrierSensing = 0.0;
		this.unfinishedCarrierSenses = new ArrayList<CarrierSenseInformation>();
	}

	/**
	 * internal method to update the last interference to the latest one.
	 * @param trans the interferring transmission.
	 * @param setOutgoing if true, this is an outgoing transmission stored as interference.
	 */
	private void updateLastInterference(Transmission trans, boolean setOutgoing) {
		double transStart = trans.getPacket().getTime();
		Interval newInter = new Interval(transStart, trans.getSendingTime());
		
		if (setOutgoing) {
			outgoingI = newInter;
		}
		
		if (lastInterference == null) {
			lastInterference = newInter;
		} else if (lastInterference.getEnd() <= transStart) {
			lastInterference = newInter;
		} else if (newInter.getEnd() > lastInterference.getEnd()) {
			lastInterference.join(newInter);
		}
	}
	
	/**
	 * Method to process events for the AirModule.
	 * Will catch Transmission, TransmissionEnd*, Interference  
	 * objects, all other objects cause a LayerException.
	 * 
	 * @param event event to process.
	 * @throws LayerException If type of given event is not type of any handled event.
	 */
	public void processEvent(ToLayer event) throws LayerException {

		if (event instanceof Packet) {
			Packet packet = (Packet) event;
			if (packet.getDirection() == Direction.UPWARDS) {
				//direction is upward, packet coming from lower layer
				lastReceivedPacket = packet;
			}
		}
		
		phyState = (PhysicalLayerState) node.getLayerState(LayerType.PHYSICAL);
			
		if (event instanceof Interference) {
			processInterference((Interference) event);
		} else if (event instanceof Transmission) {
			processTransmission((Transmission) event);
		} else if (event instanceof TransmissionBeginIncoming) {
			processTransmissionBeginIncoming((TransmissionBeginIncoming) event);
		} else if (event instanceof TransmissionEndIncoming) {
			processTransmissionEndIncoming((TransmissionEndIncoming) event);
		} else if (event instanceof TransmissionEndOutgoing) {
			processTransmissionEndOutgoing((TransmissionEndOutgoing) event);
		} else if (event instanceof AirLogTransmission) { 
			processAirLogTransmission((AirLogTransmission) event); 
		} else if (event instanceof AirPerformCarrierSense) {
			processAirPerformCarrierSense((AirPerformCarrierSense) event);
		} else if (event instanceof CarrierSenseRegistrationRequest) {
			processCarrierSenseRegistrationRequest((CarrierSenseRegistrationRequest) event);
		} else if (event instanceof CarrierSenseDurationEnd) {
			processCarrierSenseDurationEnd((CarrierSenseDurationEnd) event);
		} else if (event instanceof CarrierSenseIncomingInterference) {
			processCarrierSenseIncomingInterference((CarrierSenseIncomingInterference) event);
		} else if (event instanceof CarrierSenseCarrierFreeCheck) {
			processCarrierSenseCarrierFreeCheck((CarrierSenseCarrierFreeCheck) event);
		} else {
			throw new LayerException("AirModule of Node " + id + " received event " + event.getClass().getName());
		}
		// node.setLayerState(LayerType.PHYSICAL, phyState);###
	}
	
	/**
	 * Part of processEvent, to process incoming transmissions.
	 * @param trans the to be processed event.
	 */
	private void processTransmission(Transmission trans) {
		/*
		 * The Transmission is delivered instantaneous at the time the
		 * sender starts the transmission. The starting time of the
		 * enclosed packet is set in the future, when the transmission
		 * actually will take effect. The following 
		 * TransmissionBeginIncoming event has a built in delay, to reflect
		 * the propagation delay. Thus the receiving starts, when the 
		 * packet "really" reaches the destination.   
		 */
		
		updateLastInterference(trans, false);
		
		WakeUpCall wuc = new TransmissionBeginIncoming(sender, trans);
		sendEventSelf(wuc);
	}	
	
	/**
	 * Part of processEvent, to process the begin of incoming transmissions.
	 * @param transBegin the to be processed event.
	 */
	private void processTransmissionBeginIncoming(TransmissionBeginIncoming transBegin) {
		Transmission trans = transBegin.getTransmission();
		Packet packet = trans.getPacket();
		NodeId originator = packet.getSender().getId();
		
		// to allow interferences
		if    ((phyState.getRadioState() == RadioState.LISTENING) 
			|| (phyState.getRadioState() == RadioState.RECEIVING)) {
			
			/*
			 * If we only check for channel activity and not also want
			 * to receive packets, we do not switch to
			 * RadioState.RECEIVING as in reality switching to
			 * RadioState.RECEIVING would imply that we activate the
			 * decoding logic, which consumes more power.
			 */
			if (this.isOnlyCarrierSensing) {
				/* Do not switch RadioState, we are only sensing
				 * for a carrier wave.
				 */
			}
			else {
				phyState.setRadioState(RadioState.RECEIVING);
				node.setLayerState(LayerType.PHYSICAL, phyState);
			}

			//---- notice the last incoming transmission as soon as it reaches this node for cs ----
			
			double now = node.getCurrentTime();
			double duration = trans.getSendingTime();
			
			Interval lastIncomingI2 = new Interval(now, duration);
			
			if (lastIncomingI == null) {
				lastIncomingI = lastIncomingI2;
			} else {
				double end = lastIncomingI.getEnd();
				
				if (lastIncomingI2.getEnd() > end) {
					if (now < end) {
						lastIncomingI.join(lastIncomingI2);
					} else {
						lastIncomingI = lastIncomingI2;
					}
				}
			}
			//---------------------------------------------------------------------------------------
			
			currentIncomingTransmissions.put(trans.getId(), trans);
			//sendingTime of the sender = hearingTime of the receiver
			EventId packetID  = packet.getId();
			WakeUpCall wuc = new TransmissionEndIncoming(sender, trans.getSendingTime(), 
														trans.getId(), originator, packetID);
			sendEventSelf(wuc);
		} else {
			if (packet.isImportant() && packet.getReceiver().equals(id)) {
				LOGGER.warn(id + " dropped important packet from " + originator);
			}
			droppedPacket++;
		}
	}
	
	/**
	 * internal method to process the request signaling the end of a sending packet request.
	 * @param transEnd the to be processed event.
	 */
	private void processTransmissionEndOutgoing(TransmissionEndOutgoing transEnd) {
		phyState.setRadioState(RadioState.LISTENING);
		node.setLayerState(LayerType.PHYSICAL, phyState);

		PhysicalPacket p = outgoing.getPacket();
		
		/**
		 * For negative carrier sense the time a node stops transmitting
		 * a packet might be an interesting event if it is a carrier sense
		 * with virtual carrier sense mode activated.
		 * Without virtual carrier sensing this is an invalid state as the
		 * carrier sense would have been cancel already or not have been
		 * registered successfully.
		 */
		for (CarrierSenseInformation carrierSenseInfo : this.unfinishedCarrierSenses) {
			if (carrierSenseInfo.isNegativeCarrierSense()) {
				if (carrierSenseInfo.isVirtualCarrierSense()) {
					CarrierSenseCarrierFreeCheck carrierFreeCheck;
					carrierFreeCheck = new CarrierSenseCarrierFreeCheck(
							sender, 0.0, carrierSenseInfo);
					sendEventSelf(carrierFreeCheck);
				} else {
					throw new IllegalStateException("negative carrier sense "
							+ "without virtual carrier sense should not have "
							+ "been registered or already removed");
				}
			}
		}
		
		sendEventUp(new SendingTerminated(sender, p));
		
		if (LOGGER.isInfoEnabled()) {
			sendEventSelf(new AirLogTransmission(sender, p, outgoingI));
		}
		
		if (p.isRequestReceiverInformation()) {
			ReceiverInformationEvent wuc = new ReceiverInformationEvent(sender, p, node.getCurrentTime());
			sendEventTo(wuc, LayerType.MAC);
		}
		
		outgoing  = null; //transmission ended, reset outgoing packet
		outgoingI = null;
	}
	
	/**
	 * add a node id with at least 2 chars to a string builder.
	 * @param sb     the string builder to add to.
	 * @param other  the node id to add.
	 */
	private void addNodeId(StringBuilder sb, NodeId other) {
		if ((other.asInt() >= 0) && (other.asInt() < 10)) {
			sb.append(" ");
		}
		sb.append(other.asInt());
	}
	
	/**
	 * internal method to process the event to log a transmission.
	 * @param logTrans the to be processed event.
	 */
	private void processAirLogTransmission(AirLogTransmission logTrans) {
		PhysicalPacket p = logTrans.getPacket();
		
		if (LOGGER.isInfoEnabled()) {
			addNodeId(logTransmissionMessage, id);
			logTransmissionMessage.append("->"); 
			addNodeId(logTransmissionMessage, p.getReceiver());	
			
			LayerType type = p.getHighestLayerType();
			
			logTransmissionMessage.append(" ");
			logTransmissionMessage.append(type.getShortName());
			
			if ((type.ordinal() == 3) || (type.ordinal() == 5)) {
				logTransmissionMessage.append("  [");
			} else {
				logTransmissionMessage.append(" [");
			}
			
			Interval out = logTrans.getOut();
			
			logTransmissionMessage.append(decimalFormat.format(out.getStart()));
			logTransmissionMessage.append(" - "); 
			logTransmissionMessage.append(decimalFormat.format(out.getEnd()));
			logTransmissionMessage.append("] ");
			logTransmissionMessage.append(decimalFormat.format(p.getDuration()));
	
			Packet p2 = p.getEnclosedPacket(LayerType.APPLICATION);
			
			if (p2 != null) {
				logTransmissionMessage.append(" ");
				logTransmissionMessage.append(p2.getId());
			}
			
			if (p.getReceiver() == NodeId.ALLNODES) {
				LOGGER.info(logTransmissionMessage.toString());
				
				StringBuilder sb  = new StringBuilder("-> ");
				
				NodeId.getNodeIdListSB(sb, p.getReceivers());
				
				if (p.getInvalidReceivers().size() > 0) {
					sb.append(" invalid: ");
					NodeId.getNodeIdListSB(sb, p.getInvalidReceivers());
				}
				
				LOGGER.info(sb.toString());
			} else {
				if (p.getReceivers().size() == 0 && p.getInvalidReceivers().size() == 0) {
					logTransmissionMessage.append(" NOBODY.");
				} else {
					if (p.hasReceivedDamaged(p.getReceiver())) {
						logTransmissionMessage.append(" invalid.");
					}
				}
				LOGGER.info(logTransmissionMessage.toString());
			}
			logTransmissionMessage.setLength(0);
		}
	}
	
	/**
	 * Part of processEvent, to process TransmissionEndIncoming events.
	 * @param transEnd the to be processed event
	 */
	private void processTransmissionEndIncoming(TransmissionEndIncoming transEnd) {
		EventId transId = transEnd.getTransmissionId();
		Transmission trans = this.currentIncomingTransmissions.remove(transId);
		Packet packet = trans.getPacket();
		
		PhysicalLayerState physicalState = (PhysicalLayerState) node.getLayerState(LayerType.PHYSICAL);
		boolean radioOff = physicalState.getRadioState() == RadioState.OFF;
		
		boolean noIntersectionWithOutgoingTransmission = (outgoingI == null)
			|| !outgoingI.intersects(trans.getSimStartTime(), trans.getSendingTime());

		if (!radioOff && noIntersectionWithOutgoingTransmission) {
			PhysicalPacket resPacket = interQueue.getResultingDataPacket(trans);
			
			/*
			 * If we are currently not decoding incoming packets (we
			 * are only checking for a carrier wave)
			 * or
			 * if the packet was partly received when
			 * isOnlyCarrierSensing was activated, then do not forward it upwards, only do what
			 * is required to clean up the transmission.
			 */			
			// the time the node started receiving this transmission
			double receiveStartTime = trans.getSimStartTime();
			double epsilon = Configuration.getInstance().getPropagationDelay() * 0.01;
			if (this.isOnlyCarrierSensing
				|| receiveStartTime < this.lastTimeOnlyCarrierSensing + epsilon) {
				
				if (resPacket != null && resPacket.isValid()) {
					/*
					 * see below.
					 */
					interQueue.garbageCollect(getKernel().getCurrentTime());
				}
				
				getNode().incPacketCount(true, LayerType.AIR);
				return;
			}
			
			
			phyState.setRadioState(RadioState.LISTENING);
		
			if (resPacket != null) {
				if (LOGGER.isInfoEnabled() || packet.isRequestReceiverInformation()) {
					/* TODO Should in future be done using observer pattern as proposed
					 * 		by the Orcos PG on shox-devel March 4, 2008. 
					 */					
					trans.getSenderPacket().addReceiver(resPacket.isValid(), id);
				}
				
				if (resPacket.isValid()) {
					boolean forThisNode = (resPacket.getReceiver().asInt() == id.asInt());
					
					if (forThisNode && resPacket.isTransitToWillSend()) {
						// this blocks other incoming packets until one is sent.
						phyState.setRadioState(RadioState.WILL_SEND);
					}
					
					/*
					 * It would be desirable, to do the garbage collection for every packet
					 * and not just for the valid ones. This would require it, to determine 
					 * the time up to which the interference queue can be cleared. If the 
					 * packet is valid, it is obvious, that no interference had interfered 
					 * with this packet, thus all interferences up to the current simulation
					 * time can be removed. Otherwise 
					 * t := packet.getTime() - Max(allPackets.getDuration()) - eps
					 * would be a good guess. 
					 */
					interQueue.garbageCollect(getKernel().getCurrentTime());
				}

				node.setLayerState(LayerType.PHYSICAL, phyState);
				resPacket.flipDirection();
				sendPacket(resPacket);
				getNode().incPacketCount(true, LayerType.AIR);
			} else {
				node.setLayerState(LayerType.PHYSICAL, phyState);
				LOGGER.error("Packet " + packet + " returned by the BitManglingModel is null.");
			}
		} else {
			/* TODO Should in future be done using observer pattern as proposed
			 * 		by the Orcos PG on shox-devel March 4, 2008. 
			 */					
			if (packet.isExpectedReceiver(id)) {
				LOGGER.warn(id + "dropped important packet from " + packet.getSender().getId());
			}
			LOGGER.info("Packet " + trans.getSenderPacket() + " was discarded.");
			discardedPackets++;
		}
	}
	
	/**
	 * Part of processEvent, to process Interference events.
	 * @param inter the to be processed event
	 */
	private void processInterference(Interference inter) {
		/*
		 * The Interference is delivered instantaneous at the time the
		 * sender starts the transmission. This is done, to deliver all
		 * Interference to all nodes *before* they are used. But the 
		 * starting time of the enclosed packet is set in the future,
		 * when the Interference actually will take effect. This has to
		 * be taken into account, when starting new events upon this 
		 * interference.   
		 */
		
		interQueue.addInterferingSignal(inter, null);

		updateLastInterference(inter, false);
		
		double transStart = inter.getPacket().getTime();
		double transDelay = transStart - getKernel().getCurrentTime();
		
		// rollover carrier sense
		if ((csInterval != null) && (csInterval.intersects(transStart, 0.0))) {
			MACCarrierSensing wuc = new MACCarrierSensing(sender, transDelay, false);
			
			wuc.setCsStart(csInterval.getStart());
			
			sendEventTo(wuc, LayerType.MAC);
			interferenceInVarCS = true;
			csInterval = null;
		}
		
		// handle generic carrier sense mechanism case
		CarrierSenseIncomingInterference interferenceBeacon;
		interferenceBeacon = new CarrierSenseIncomingInterference(
				sender, transDelay, inter);
		
		sendEventSelf(interferenceBeacon);
	}
	
	/**
	 * internal method, to inform the MAC of a free Carrier. Now goes it loose (maybe).
	 * @param cs   the current active cs (only used in the DBG variant).
	 * @param pos  the index of the caller (-"-).
	 */
	protected void csFreeEvent(AirPerformCarrierSense cs, int pos) {
		// time and varTime has passed, without any carrier -> inform the men, err MAC.
			
		csInterval = null;
		
		WakeUpCall wuc = new MACCarrierSensing(sender, 0.0, true);
		sendEventTo(wuc, LayerType.MAC);
	}
	
	/**
	 * private method to schedule the variable part of the carrier sense.
	 *  
	 * @param start the simulation time, when the variable carrier sense should start.
	 * @param cs    the carrier sense event, to get the needed parameters from.
	 */
	private void scheduleVarCS(double start, AirPerformCarrierSense cs) {
		double v = cs.getVarTime();
		
		csInterval = new Interval(start, v);
		
		sendEventSelf(new AirPerformCarrierSense(sender, v, cs.getMinTime(), v, true));
		interferenceInVarCS = false;
	}
	
	/**
	 * internal method to check for a carrier during a past interval and reissue
	 * a carrier sensing, if a carrier was detected, after the interference.
	 *   
	 * @param now the current time, specifing the end of the free carrier period.
	 * @param cs  the event containing the requested sensing interval information.
	 * @return true if the carrier was free all the specified time.
	 */
	private boolean checkCarrierMinFree(double now, AirPerformCarrierSense cs) {
		double csDuration = cs.getDelay();
		
		if ((lastInterference != null) && (lastInterference.intersects(now - csDuration, csDuration))) {
			// the min. waiting time has passed, but the carrier wasn't free.
			// let the MAC try it again after interference.end + min. waiting time.
			
			// this should the the time to wait, until the medium is free again.
			double mediumFreeDelay = lastInterference.getEnd() + cs.getMinTime() - now;
			
			if (mediumFreeDelay < 0.0) {
				// for some strange reason, the node waited too long for the free carrier.
				// TODO: check, if a negative value can be avoided here.
				double start = now + mediumFreeDelay;
				
				mediumFreeDelay = -mediumFreeDelay;
				
				// handle two different cases, when a node waited too long for a free medium.
				
				if (mediumFreeDelay < cs.getVarTime()) { 
					// min. free has passed, but not the var time => schedule var carrier sensing
					scheduleVarCS(start, cs);
					return false;
				} else {                   
					// min. and var time have passed => schedule slightly delayed send packet.
					// this is slightly subobtimal, since the node may have started earlier, to send a packet.
					csFreeEvent(cs, 0);
					return false;
				}
			} else {
				sendEventSelf(new AirPerformCarrierSense(senderMAC, mediumFreeDelay, 
						      cs.getMinTime(), cs.getVarTime(), true));
				return false;
			}
		} else {
			return true;
		}
	}
		
	/**
	 * Part of processEvent, to process AirPerformCarrierSense events.
	 * @param cs the to be processed event.
	 */
	private void processAirPerformCarrierSense(AirPerformCarrierSense cs) {
		double now = getNode().getCurrentTime()/*, t = cs.getDelay()*/;
		
		if (interferenceInVarCS && cs.isRetry()) {
			interferenceInVarCS = false;
		} else {
			if (checkCarrierMinFree(now, cs)) {
				//the min. waiting time has passed with no carrier sensed.
				if (cs.getSender().getFromLayer().equals(LayerType.MAC)) {
					if (cs.getVarTime() == 0.0) {
					// no additional carrier sensing requested, thus go ahead.
						csFreeEvent(cs, 1);
					} else {
						// schedule the additional carrier sensing here.
						scheduleVarCS(now, cs);
					}
				} else {
					if (csInterval != null) {
						csFreeEvent(cs, 2);
					} // else: variable carrier sensing time was interrupted by an interference and
			  		//       the CSfreeEvent was already issued, thus prohibit double event.
				}
			}
		}
	}

	/**
	 * Part of processEvent, to process CarrierSenseRegistrationRequest,
	 * which is used by other layers to register a carrier sense.
	 * 
	 * @param registrationRequest the to be processed
	 * 									  registration request
	 */
	private void processCarrierSenseRegistrationRequest(
			CarrierSenseRegistrationRequest registrationRequest) {
				
		CarrierSenseInformation theCarrierSenseInfo =
			registrationRequest.getGenericCarrierSenseInformation();
		
		/**
		 * Carrier senses not having virtual carrier sense mode
		 * can't be handled, when the node is transmitting a
		 * packet as a physical carrier sense cannot be performed
		 * while sending.
		 */
		if (!registrationRequest.isVirtualCarrierSense()) {
			PhysicalLayerState thePhyState;
			thePhyState = (PhysicalLayerState) node.getLayerState(LayerType.PHYSICAL);
			
			if (thePhyState.getRadioState() == RadioState.SENDING) {
				CarrierSenseFailed failedEvent;
				failedEvent = new CarrierSenseFailed(sender,
						theCarrierSenseInfo.getRegistrant().getId(),
						theCarrierSenseInfo);
				sendEventTo(failedEvent, theCarrierSenseInfo
						.getRegistrant().getFromLayer());
				return;
			}
		}
		
		if (registrationRequest.isNegativeCarrierSense()) {
			initNegativeCarrierSense(theCarrierSenseInfo);
		} else {
			initRegularCarrierSense(theCarrierSenseInfo);
		}
	}

	/**
	 * Used by the generic carrier sense mechanism to initialize
	 * a negative carrier sense (where we want to detect of a carrier
	 * is/gets free).
	 * <br><br>
	 * WARN: Should only be called by
	 * 		 {@link processCarrierSenseRegistrationRequest}
	 * 
	 * @param carrierSenseInfo information of the to be started carrier sense
	 *
	 */
	private void initNegativeCarrierSense(
			CarrierSenseInformation carrierSenseInfo) {
		/* 
		 * Init first check if the carrier is busy.
		 * If it is busy the check itself will initiate further
		 * (=later) checks.
		 */
		CarrierSenseCarrierFreeCheck freeCarrierCheck;
		freeCarrierCheck = new CarrierSenseCarrierFreeCheck(
				sender, 0.0, carrierSenseInfo);
		sendEventSelf(freeCarrierCheck);

		/* 
		 * Save the new carrier sense in the list of
		 * unfinished carrier senses as this used to check if an event for
		 * a particular carrier sense still has to be processed.
		 */
		this.unfinishedCarrierSenses.add(carrierSenseInfo);
		
		/* 
		 * Create beacon that informing us about the end of this
		 * carrier sense period.
		 */
		CarrierSenseDurationEnd checkDurationEnd = new CarrierSenseDurationEnd(
				sender, carrierSenseInfo.getDuration(),
				carrierSenseInfo);
		sendEventSelf(checkDurationEnd);
	} 
	
	/**
	 * Used by the generic carrier sense mechanism to initialize a
	 * regular carrier sense (where we want to detect if the carrier
	 * gets busy).
	 * <br><br>
	 * WARN: Should only be called by
	 * 		 {@link processCarrierSenseRegistrationRequest}
	 * 
	 * @param carrierSenseInfo information of the to be started carrier sense
	 *
	 */
	private void initRegularCarrierSense(
			CarrierSenseInformation carrierSenseInfo) {


		ArrayList<Interference> allInterferences  = interQueue.getInterferences();
		boolean foundMatchingInterference = false;
		double currentTime = SimulationManager.getInstance().getCurrentTime();
		
		/* 
		 * Check if there are interferences which match the current time.
		 */
		
		for (Interference interference : allInterferences) {
			/*
			 * This code assumes that interference.getSimStartTime()
			 * is the time where the transmission of the interference
			 * started leaving the antenna of the sending node.
			 */
			double timeInterferenceReachedThisNode = interference.getSimStartTime();
			double timeInterferencePassedThisNode = timeInterferenceReachedThisNode
					+ interference.getSendingTime();
			
			if (timeInterferenceReachedThisNode <= currentTime
					&& currentTime < timeInterferencePassedThisNode) {
		
				// found an interference matching to the current time 
				foundMatchingInterference = true;

				// inform the layer who registered the carrier sense about the result
				CarrierSenseResult theResult =
					new CarrierSenseResult(sender,
							carrierSenseInfo.getRegistrant().getId(),
							true, carrierSenseInfo);
				
				sendEventTo(theResult, carrierSenseInfo.getRegistrant().getFromLayer());
				
				break;
			}
		}
		
		/* 
		 * Check for outgoing transmission (only with virtual mode).
		 */
		boolean foundOutgoingTransmission = false;

		PhysicalLayerState thePhyState;
		thePhyState = (PhysicalLayerState) node.getLayerState(
				LayerType.PHYSICAL);
		
		if (thePhyState.getRadioState() == RadioState.SENDING) {
			if (carrierSenseInfo.isVirtualCarrierSense()) {
				CarrierSenseResult carrierSenseResult;

				carrierSenseResult = new CarrierSenseResult(sender,
						carrierSenseInfo.getRegistrant().getId(), true,
						carrierSenseInfo);
				sendEventTo(carrierSenseResult, carrierSenseInfo
						.getRegistrant().getFromLayer());
				
				foundOutgoingTransmission = true;				
			} else {
				/* This can never happen has
				 * processCarrierSenseRegistrationRequest() already
				 * checked this situation, informed the registrant about an
				 * error and should not have called this method!
				 */
				throw new IllegalStateException("initRegularCarrierSense "
						+ "called when node was transmitting but carrier "
						+ "sense did not have virtual mode activated");
			}
			
		}
		
		if (!foundMatchingInterference && !foundOutgoingTransmission) {
			/* found no interferences matching to the current time
			 * and no outgoing transmission (if we have virtual mode)
			 */
			
			/* 
			 * We did not find a current interference, so we have to look
			 * for interferences for the rest of the carrier sense duration.
			 * This is automatically be done by handling the
			 * CarrierSenseIncomingInterference event.
			 */ 
				
			/* 
			 * Save the new carrier sense in the list of unfinished
			 * carrier senses as this is used to check if an event for
			 * a particular carrier sense still has to be processed.
			 */
			this.unfinishedCarrierSenses.add(carrierSenseInfo);
				
			/* 
			 * Create beacon that informing us about the end of this
			 * carrier sense period.
			 */
			CarrierSenseDurationEnd checkDurationEnd = new CarrierSenseDurationEnd(
					sender, carrierSenseInfo.getDuration(),
					carrierSenseInfo);
			sendEventSelf(checkDurationEnd);
		}
		
	}

	/**
	 * Part of processEvent, to process
	 * CarrierSenseIncomingInterference, which is used by the
	 * AirModule to inform itself that an interference reaches a node.
	 * 
	 * @param beacon the to be processed beacon
	 */
	private void processCarrierSenseIncomingInterference(CarrierSenseIncomingInterference beacon) {
		/*
		 * All carrier senses in the list of unfinished carrier senses
		 * now get the carrier detected, as at the simulation time we
		 * have an interference reaching the node. 
		 */
		List<CarrierSenseInformation> allNegativeCarrierSenses;
		allNegativeCarrierSenses = new ArrayList<CarrierSenseInformation>(5);

		for (CarrierSenseInformation carrierSenseInfo : this.unfinishedCarrierSenses) {

			if (carrierSenseInfo.isNegativeCarrierSense()) {
				allNegativeCarrierSenses.add(carrierSenseInfo);
				continue;
			}
			
			Address registrant = carrierSenseInfo.getRegistrant();
			CarrierSenseResult theResult = new CarrierSenseResult(sender,
						registrant.getId(), true, carrierSenseInfo);
			sendEventTo(theResult, registrant.getFromLayer());
			
		}
		
		/*
		 * Remove all solved carrier sense by setting the list to
		 * the unsolved carrier sense (=all negative carrier senses).
		 */ 
		this.unfinishedCarrierSenses = allNegativeCarrierSenses;
	}

	/**
	 * Part of processEvent, to process
	 * CarrierSenseCarrierFreeCheck, which is used by the
	 * AirModule to inform itself that it has to check if all interferences
	 * have passed the node for a negative carrier sense.
	 * 
	 * @param beacon the to be processed beacon
	 */
	private void processCarrierSenseCarrierFreeCheck(CarrierSenseCarrierFreeCheck beacon) {
		
		if (!this.unfinishedCarrierSenses.contains(beacon.getCarrierSenseInfo())) {
			// this carrier sense was already done.
			return;
		}

		// Check if there are interferences which match the current time
		ArrayList<Interference> allInterferences  = interQueue.getInterferences();
		CarrierSenseInformation theCarrierSenseInfo = beacon.getCarrierSenseInfo();
		boolean foundMatchingInterference = false;
		double currentTime = SimulationManager.getInstance().getCurrentTime();

		/*
		 * The time the last interference has passed the node so that
		 * the node gets free if between now and then no new
		 * interference is started.
		 */ 
		double timeLastInterferencePassed = 0.0;
		
		
		for (Interference interference : allInterferences) {
			/*
			 * This code assumes that interference.getSimStartTime()
			 * is the time where the transmission of the interference
			 * started reaching the antenna of the receiving node.
			 */
			double timeInterferenceReachedThisNode = interference.getSimStartTime();
			double timeInterferencePassedThisNode = timeInterferenceReachedThisNode + interference.getSendingTime();
			
			if (timeInterferenceReachedThisNode <= currentTime
					&& currentTime < timeInterferencePassedThisNode) {
				// found an interference matching to the current time 
				foundMatchingInterference = true;

				// determine the last interference
				if (timeInterferencePassedThisNode > timeLastInterferencePassed) {
					timeLastInterferencePassed = timeInterferencePassedThisNode; 
				}
			}
		}
		
		/**
		 * Check for outgoing transmissions.
		 */
		boolean foundOutgoingTransmission = false;
		PhysicalLayerState thePhyState = 
			(PhysicalLayerState) node.getLayerState(LayerType.PHYSICAL);
		
		if (thePhyState.getRadioState() == RadioState.SENDING) {
			foundOutgoingTransmission = true;
		}
		
		
		if (foundMatchingInterference || foundOutgoingTransmission) {
			if (foundMatchingInterference) {
				double delayToCheck = timeLastInterferencePassed
					- SimulationManager.getInstance().getCurrentTime(); 
				
				CarrierSenseCarrierFreeCheck checkBeacon =
					new CarrierSenseCarrierFreeCheck(
							sender, delayToCheck, theCarrierSenseInfo);
				sendEventSelf(checkBeacon);
			}
			/*
			 * Regarding the else case, a new
			 * CarrierSenseCarrierFreeCheck will be spanned
			 * when processTransmissionEndOutgoing() is called.
			 */
		} else {
			this.unfinishedCarrierSenses.remove(theCarrierSenseInfo);
			
			CarrierSenseResult theResult =
				new CarrierSenseResult(sender,
						theCarrierSenseInfo.getRegistrant().getId(),
						false, theCarrierSenseInfo);
			sendEventTo(theResult, theCarrierSenseInfo.getRegistrant().getFromLayer());					
		}
		
	}
	
	
	/**
	 * Part of processEvent, to process CarrierSenseDurationEnd,
	 * which is used by the AirModule to inform itself that a carrier
	 * sense is finished (if it wasn`t finished before by an
	 * interference).
	 * 
	 * @param beacon the to be processed beacon
	 */
	private void processCarrierSenseDurationEnd(CarrierSenseDurationEnd beacon) {
		
		CarrierSenseInformation theCarrierSenseInfo = 
			beacon.getGenericCarrierSenseInformation();

		// check of carrier sense of the beacon is still unfinished		
		if (this.unfinishedCarrierSenses.remove(theCarrierSenseInfo)) {
			// carrier sense of beacon was unfinished
			boolean carrierDetected;
			
			if (theCarrierSenseInfo.isNegativeCarrierSense()) {
				carrierDetected = true;
			} else {
				carrierDetected = false;
			}
			
			// inform the layer who registered the carrier sense about the result
			Address registrant = theCarrierSenseInfo.getRegistrant();
			CarrierSenseResult theResult = new CarrierSenseResult(sender,
						registrant.getId(), carrierDetected, theCarrierSenseInfo);
			sendEventTo(theResult, registrant.getFromLayer());
		} 
		/* 
		 * Regarding the else case:
		 * The carrier sense of the beacon was already finished and
		 * the registrant was already informed about the result of
		 * the carrier sense.
		 */
	}
	
	/**
	 * Method to transmit a packet via the AirModule.
	 * 
	 * @param packet the packet to transmit.
	 * @param signalStrength the strength of the signal to use.
	 * @return true if the packet can be send and radio is not blocked.
	 * 			false otherwise.
	 */
	public boolean transmit(PhysicalPacket packet, double signalStrength) {
		Transmission trans = new Transmission(packet, signalStrength);
		boolean result = outgoing == null;
		
		if (result) {
			packet.setTime(getKernel().getCurrentTime());
			packet.createReceiverLists();
			
			PhysicalLayerState ps = (PhysicalLayerState) getNode().getLayerState(LayerType.PHYSICAL);
			ps.setRadioState(RadioState.SENDING);
			getNode().setLayerState(LayerType.PHYSICAL, ps);

			this.outgoing = trans;
			updateLastInterference(trans, true);
			
			/* 
			 * Check running carrier sense if one of them has problems
			 * with outgoing transmissions or one of them now has to
			 * get the result "carrier detected".
			 */
			List<CarrierSenseInformation> failedCarrierSenses =
				new ArrayList<CarrierSenseInformation>();
			List<CarrierSenseInformation> finishedCarrierSenses =
				new ArrayList<CarrierSenseInformation>();
			
			for (CarrierSenseInformation carrierSenseInfo : this.unfinishedCarrierSenses) {
				if (carrierSenseInfo.isVirtualCarrierSense()) {
					if (!carrierSenseInfo.isNegativeCarrierSense()) {
						/* 
						 * As we just started an outgoing transmission
						 * all regular carrier senses with virtual
						 * carrier sense mode now have the result
						 * "carrier detected".
						 */
						CarrierSenseResult carrierSenseResult;
						
						carrierSenseResult = new CarrierSenseResult(sender,
								carrierSenseInfo.getRegistrant().getId(), true,
								carrierSenseInfo);
						sendEventTo(carrierSenseResult, carrierSenseInfo
								.getRegistrant().getFromLayer());
						
						finishedCarrierSenses.add(carrierSenseInfo);
					}
				} else {
					// not in virtual carrier sense mode, report error
					CarrierSenseFailed failedEvent;

					failedEvent = new CarrierSenseFailed(sender,
							carrierSenseInfo.getRegistrant().getId(),
							carrierSenseInfo);
					sendEventTo(failedEvent, carrierSenseInfo
							.getRegistrant().getFromLayer());
					
					failedCarrierSenses.add(carrierSenseInfo);
				}
			}
			
			this.unfinishedCarrierSenses.removeAll(failedCarrierSenses);
			this.unfinishedCarrierSenses.removeAll(finishedCarrierSenses);
			
			getKernel().transmitPacket(trans);
			WakeUpCall wuc = new TransmissionEndOutgoing(sender, trans.getSendingTime(), trans.getId(), 
					                                     trans.getReceiver(), trans.getPacket().getId());
			sendEventSelf(wuc);
			
			getNode().incPacketCount(false, LayerType.PHYSICAL);
		} else {
			LOGGER.error("transmitting while the airmodule is busy ???");
			airModuleBusyCount++;
			// Count how often the error "transmitting while the airmodule is busy ???" occurs.
			// Needed for evaluation purposes. Could be removed iff the error is fixed.
		}
		return result;
	}
		
	/**
	 * Method to update the node for an airmodule.
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
		boolean result = super.setNode(node);
		senderMAC = new Address(id, LayerType.MAC);
		
		lastIncomingI = null;
		
		if (result) {			
			droppedPacket    = 0;
			discardedPackets = 0;
		}
		return result;
	}

	/** @return the bmm */
	public BitManglingModel getBmm() {
		return bmm;
	}

	/** @return the current state of the air layer. */
	public LayerState getState() {
		return new AirState(outgoingI, csInterval, lastInterference, 
				            interQueue, lastIncomingI, currentIncomingTransmissions, 
				            this.isOnlyCarrierSensing);
	}

	/** 
	 * Sets the state of the AirModule even if we are not "the master of universe".
	 * @param state Only the isOnlyCarrierSensing flag is copied 
	 * @return true if instance of AirState<br>
	 * 		   false if not instance of AirState  
	 */
	public boolean setState(LayerState state) {
		if (state instanceof AirState) {
			AirState airstate = (AirState) state;
			PhysicalLayerState thePhyState = (PhysicalLayerState) node.getLayerState(LayerType.PHYSICAL);			
			
			if (airstate.isOnlyCarrierSensing() != this.isOnlyCarrierSensing) {
				this.isOnlyCarrierSensing = airstate.isOnlyCarrierSensing();
				
				if (this.isOnlyCarrierSensing) {
					// We switched from off to on
					
					/* If we are currently receiving a packet we have
					 * to switch to RadioState.LISTENING as in reality
					 * this would deactivate decoding of the incoming packet.
					 */
				
					if (thePhyState.getRadioState() == RadioState.RECEIVING) {
						thePhyState.setRadioState(RadioState.LISTENING);
						node.setLayerState(LayerType.PHYSICAL, thePhyState);
						
						/*
						 * I assume that a MAC protocol only switches
						 * to isOnlyCarrierSensing when it is not
						 * currently receiving a transmission.
						 * Switching at this scenario means, that the
						 * packet will not be received, even if in
						 * reality parts of would have been decoded. 
						 * But as there might be MAC protocols which
						 * rely on this behavior, I only issue a
						 * warning. 
						 */
						LOGGER.warn("Switched to isOnlyCarrierSensing "
								+ "while receiving/decoding incoming packet!");
					}
				} else if (!this.isOnlyCarrierSensing) {
					// We switched from on to off
					
					/* Update time when isOnlyCarrierSensing was
					 * deactivated as this is required by
					 * processTransmissionEndIncoming to detect
					 * transmissions/packets which were only partly
					 * decoded / partly received while in reality the
					 * decoding circuits would have been active.
					 */					
					this.lastTimeOnlyCarrierSensing = SimulationManager
							.getInstance().getCurrentTime();
					
					/* If we are currently receiving a packet we now
					 * have to switch to RadioState.RECEIVING.
					 */
					if (this.currentIncomingTransmissions.size() > 0) {
						thePhyState.setRadioState(RadioState.RECEIVING);
						node.setLayerState(LayerType.PHYSICAL, thePhyState);
					}
				}
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	/** This call is never used, since layer.processEvent(ToLayer event) is overloaded.
	 * @param packet The packet to be transported 
	 */
	@Override
	public void lowerSAP(Packet packet) {
	}

	/** This call is never used, since layer.processEvent(ToLayer event) is overloaded.
	 * @param packet The packet to be transported 
	 */
	@Override
	public void upperSAP(Packet packet) {
	}

	/**
	 * not used.
	 * @param init the to be processed event.
	 */
	protected void processEvent(Initialize init) {
		// nothing to init
	}

	/**
	 * not used.
	 * @param end the to be processed event.
	 */
	protected void processEvent(Finalize end) {
		// nothing to end
	}

	/**
	 * not used.
	 * @param start the to be processed event.
	 */
	protected void processEvent(StartSimulation start) {
		// nothing to start
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendPacket(Packet packet) {
		if (packet.getDirection() ==  Direction.DOWNWARDS) {
			lastSentPacket = packet;
		}
		super.sendPacket(packet);
	}

	/**
	 * @return the airModuleBusyCount
	 */
	public static int getAirModuleBusyCount() {
		return airModuleBusyCount;
	}
}
