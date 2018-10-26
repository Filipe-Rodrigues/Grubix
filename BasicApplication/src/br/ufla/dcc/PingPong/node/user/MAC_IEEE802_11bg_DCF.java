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

package br.ufla.dcc.PingPong.node.user;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.node.AirState;
import br.ufla.dcc.grubix.simulator.node.BitrateAdaptationPolicy;
import br.ufla.dcc.grubix.simulator.node.Link;
import br.ufla.dcc.grubix.simulator.node.MACLayer;
import br.ufla.dcc.grubix.simulator.node.MACState;
import br.ufla.dcc.grubix.simulator.node.RadioState;
import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.Interval;
import br.ufla.dcc.grubix.simulator.LayerException;
import br.ufla.dcc.grubix.simulator.LayerType;
import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.SimulationFailedException;
import br.ufla.dcc.grubix.simulator.event.CrossLayerEvent;
import br.ufla.dcc.grubix.simulator.event.Finalize;
import br.ufla.dcc.grubix.simulator.event.Initialize;
import br.ufla.dcc.grubix.simulator.event.LayerState;
import br.ufla.dcc.grubix.simulator.event.LogLinkPacket;
import br.ufla.dcc.grubix.simulator.event.MACCarrierSensing;
import br.ufla.dcc.grubix.simulator.event.MACEvent;
import br.ufla.dcc.grubix.simulator.event.MACProcessAckTimeout;
import br.ufla.dcc.grubix.simulator.event.MACSendACK;
import br.ufla.dcc.grubix.simulator.event.Packet;
import br.ufla.dcc.grubix.simulator.event.PhysicalLayerState;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.ReceiverInformationEvent;
import br.ufla.dcc.grubix.simulator.event.SendingTerminated;
import br.ufla.dcc.grubix.simulator.event.StartSimulation;
import br.ufla.dcc.grubix.simulator.event.StatisticLogRequest;
import br.ufla.dcc.grubix.simulator.event.TransmissionFailedEvent;
import br.ufla.dcc.grubix.simulator.event.WakeUpCall;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.event.user.DoublePacketEvent;
import br.ufla.dcc.grubix.simulator.event.user.LostPacketEvent;
import br.ufla.dcc.grubix.simulator.event.user.RejectedPacketEvent;
import br.ufla.dcc.grubix.simulator.event.user.WlanFramePacket;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;
import br.ufla.dcc.grubix.simulator.node.user.AARFRateAdaptation;
import br.ufla.dcc.grubix.simulator.node.user.CarrierSenseInterruptedEvent;
import br.ufla.dcc.grubix.simulator.node.user.IEEE_802_11_TimingParameters;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/** 
 * Implements the IEEE 802.11b MAC in AdHoc aka DCF mode.
 * 
 * @author Dirk Held
 */
public class MAC_IEEE802_11bg_DCF extends MACLayer {

	/** Logger of this class. */
	protected static final Logger LOGGER = Logger.getLogger(MAC_IEEE802_11bg_DCF.class.getName());

	/** Index for the statistic type "drops/s". */
	private static final int DROPS_PER_SECOND = 0;
	
	/** object to hold all needed timing parameters, generated from the 802.11 Phy. */
	private static IEEE_802_11_TimingParameters globalTimings;
	
	/** define the length of contention window. */
	private static final int[] CW = {7, 15, 31, 63, 127, 255};
	/** the maximum possible CW-Array Index. */
	private static final int MAX_CW_IDX = 5;
	/** the maximum number of retries performed. */
	private int maxRetryCount;
	/** the current index into the CW-Size-Array. */
	private int currentCW;
	
	/** the randomized time to do wait for a free medium after a collision. */
	private double backoffTime;
	
	/** the time, when the last packet was started to be sent. */
	private double lastTrySent;
	
	/** the position index of the last trySend call. */
	private int lastTrySendPos;
	
	/** the time, when the last packet was started to be sent. */
	private double lastSent;
	
	/** is true, if a send was issued and the MAC waits for the medium to get access. */
	private boolean pendingCS;
	
	/** if true, an ACK for the last sent packet ist still missing. */
	private boolean pendingACK;
	
	/** if true, an ACK will be send soon, so no other packets can be send. */
	protected boolean willSendACK;
	
	/** timeout for a pending ACK. */
	private double[] ackDelay;
	
	/** time to wait for signal propagation. */
	private double propagationDelay;
	
	/** is true, if sending should be issued after a free carrier for the duration DIFS. */
	private boolean immediateSend;
	
	/** holds the current to be sent packet, until it has passed the air. */
	private WlanFramePacket currentOutPacket;
	
	/** holds the not yet processed transmissions. */
	protected final LinkedList<WlanFramePacket> outQueue;
	
	/** Counting the number of dropped packets (retryLimit exceeded) since last reset. */
	private int droppedPackets;
	
	/** If true, nodes will always do arandom backofo,after an broadcast was received. */
	@ShoXParameter(description = "If true, nodes will always do arandom backofo,after an broadcast was received.", defaultValue = "false")
	private boolean backoffAfterReceivedBroadcast;
	
	/** true if MAC is in promiscuous and delivers ALL received data packet to upperLayer. */
	@ShoXParameter(description = "If true, nodes can overhear messages destined for neighbors.", defaultValue = "false")
	private boolean promiscuous;
	
	/**
	 * The maximum number of retries, the MAC should do until giving up.
	 */
	@ShoXParameter(description = "The maximum number of retries, the MAC should do until giving up.", defaultValue = "10")
	private int retryLimit;
	
	/**
	 * method to adapt the bitrate to changing environment.
	 */
	@ShoXParameter(description = "method to adapt the bitrate to changing environment, chose from ARF, AARF, NO_ARF.", defaultValue = "NO_ARF")
	private String rateAdaption;

	/**
	 * the required number of consecutive successful transmissions before rising the bitrate.
	 */
	@ShoXParameter(description = "the required number of consecutive successfull transmissions before "
			+ "rising the bitrate.", defaultValue = "10")
	private int raUpLevel;
	
	/**
	 * the tolerated number of consecutive failed transmissions before lowering the bitrate.
	 */
	@ShoXParameter(description = "the tolerated number of consecutive failed transmissions before "
			+ "lowering the bitrate.", defaultValue = "2")
	private int raDownLevel;
	
	/**
	 * For AARF the multiplier to apply on erroneous bitrate increases after premature increases.
	 */
	@ShoXParameter(description = "For AARF the multiplier to aply on erroneous bitrate increases " 
			+ "after premature increases.", defaultValue = "2")
	private int raUpMult;
	
	/**
	 * the bitrate is rised if raUpLevel packets were received successfully or this time has passed 
	 * since the last retried packet.
	 */
	@ShoXParameter(description = "the bitrate is rised if raUpLevel packets were received successfully or " 
			+ "this time has passed since the last retried packet.", defaultValue = "10")
	private double raTimeout;
	
	/** set to true, if the mac should include the current queue into the state. */
	@ShoXParameter(description = "set to true, if the mac should include the current queue into the state.", defaultValue = "false")
	private boolean includeQueueInState;
	
	/** constructor. */
	public MAC_IEEE802_11bg_DCF() {
		outQueue = new LinkedList<WlanFramePacket>();
		this.droppedPackets = 0;
		this.promiscuous = false;
	}

	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#lowerSAP(br.ufla.dcc.PingPong.event.Packet)
	 * 
	 * @param packet to process coming from lower layer.
	 */
	@Override
	public final void lowerSAP(Packet packet) {
		boolean isTerminal = packet.isTerminal();

		boolean forThisNode = packet.getReceiver().equals(id);
		boolean forAllNodes = (packet.getReceiver() == NodeId.ALLNODES);
		WlanFramePacket frame = (WlanFramePacket) packet, ack;

		/*
		 * On any modification of this method, please ensure, that trySend() will be called after the
		 * reception of a packet, since this might have blocked a new outgoing packet from beeing sent.
		 */
		NodeId senderId = frame.getSender().getId();
		boolean doDropPacket = !forThisNode && !forAllNodes;
		
		if (doDropPacket && promiscuous && !isTerminal && !senderId.equals(id)) {
			doDropPacket = false;
		}
		
		if (doDropPacket) {
			if (LOGGER.isDebugEnabled()) { // check if enabled to reduce performance impact
				LOGGER.debug("Packet not for this node. Throwing away " + packet); 
			}
			// advance queue after receiving neither a broadcast nor a packet for this node.
			trySend(0); // #0#
		} else { 
			if (forThisNode && isTerminal && (frame.isControl())) {
				switch (frame.getType()) {
					case ACK: pendingACK = false;
							  immediateSend = true;
							  break;
					case RTS: break;
					case CTS: break;
					default: break;
				}
				// TODO handle MAC control packets, like [rts, cts]
				
				// no trySend() needeed here, since the wakeupcall processing MACProcessAckTimeout will handle this. 
			} else {
				sendPacket(packet.getEnclosedPacket());
				
				if (forThisNode) {
					willSendACK = true; 
	
					ack = new WlanFramePacket(sender, packet.getSender().getId(),
							PacketType.ACK, frame.getSignalStrength());
					
					applyBitrate(ack, frame.getBitrateIdx());
	
					WakeUpCall wuc = new MACSendACK(sender, globalTimings.getSifs(), ack);
					sendEventSelf(wuc);
					
					// no trySend() here, since any send prior the send of the ack makes no sense.
				} else {
					if (backoffAfterReceivedBroadcast && forAllNodes) {
						setBackoffTime();
					}
					
					// advance queue after receiving a broadcast.
					trySend(1); // #1#
				}
			}
		}
	}

	/**
	 * internal method, to process the outQueue, fetch the next packet and send it.
	 * @return true if a carrier sense was started.
	 */
	protected final boolean trySend(int posIdx) {
		lastTrySendPos = posIdx;
		/*
		 * There is no periodic event issued (polling), where it is checked, if a packet is in the
		 * out queue and can be sent. It has to be ensured, that for every to be sent packet, at least
		 * once trySend() is called and is leading to a carrier sense, which uppon  completion will 
		 * actually send a packet or issue another trySend() with a changed backoff time. Thus trySend()
		 * is to be called after entering a new packet to the out queue, after the reception of a packet
		 * (which may have blocked sending another packet) and of course after the reception of a packet.
		 * If this is not done immediately, it has to be ensured, that it will definitely done later.
		 * Otherwhise, the out queue will contain some to be send packets at the end of the simulation.
		 */
		
		boolean ok =   ((currentOutPacket == null) && (outQueue.size() > 0)) 
		            || ((currentOutPacket != null) && currentOutPacket.isReadyForTransmission()); 
		
		if (ok && !willSendACK && !pendingCS && !pendingACK) {
			AirState           as       = (AirState)           getNode().getLayerState(LayerType.AIR);
			PhysicalLayerState phyState = (PhysicalLayerState) getNode().getLayerState(LayerType.PHYSICAL);
			RadioState radioState = phyState.getRadioState();
			boolean radioFree = radioState == RadioState.LISTENING;
			
			if (radioFree) {
				pendingCS = true;
				
				double difs = globalTimings.getDifs();

				if (immediateSend) {
					// as.getLastInterference().getEnd(); this would include own transmissions too 
					if ((as == null) || (as.getInterQueue().getMaxTime() + difs > getNode().getCurrentTime())) {
						calcBackoffTime(0.0);
					}
				}
				
				if (immediateSend) {
					startCarrierSense(difs, 0.0);
				} else {
					startCarrierSense(difs, backoffTime);
				}
				return true;
			} else {
				if (as != null) {
					Interval lastIncoming = as.getLastIncoming();
					
					if (lastIncoming != null) {
						double end = lastIncoming.getEnd();
						double now = node.getCurrentTime();
						
						if (end > now) {
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("initiate a delayed trySend()");
							}
							sendEventSelf(new MACEvent(sender, end - now));
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * internal method to apply the current used bitrate.
	 * Afterwards getSendingTime is valid.
	 * @param f the frame, where the bitrate is applied to.
	 * @param forcedBitrateIdx use a value >= 0 to set a specific bitrate index,
	 * 						   (needs to be obtained from a rate adaptation object). 
	 */
	private void applyBitrate(WlanFramePacket f, int forcedBitrateIdx) {
		if (forcedBitrateIdx >= 0) {
			/* 
			 * This is only used for ACK or other control packets,
			 * which are not stored and processed in the normal 
			 * queue of to be sent packets.
			 */
			f.setBPS(globalTimings, forcedBitrateIdx);
		} else {
			BitrateAdaptationPolicy raLocal = raDefaultPolicy;
			Link link = getMetaDataLink(f);
			
			if (link != null) {
				if (link.getBitrateAdaptationPolicy() != null) {
					raLocal = link.getBitrateAdaptationPolicy();
				}
			}
			
			f.setBPS(globalTimings, raLocal);
		}
	}
	
	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#upperSAP(br.ufla.dcc.PingPong.event.Packet)
	 * @param packet to process coming from higher layer.
	 */
	@Override
	public final void upperSAP(Packet packet) {
		boolean ownPacket = packet.getReceiver() == id;
		if (ownPacket) {
			packet.flipDirection();
			sendPacket(packet);
			return;
		}
		
		double now = getNode().getCurrentTime();
		LogLinkPacket lp = (LogLinkPacket) packet;
		
		WlanFramePacket nextPacket = new WlanFramePacket(sender, lp);
		applyBitrate(nextPacket, -1);
		
		outQueue.addLast(nextPacket);
		
		/*
		 * If multiple packets were sent at the same time, the first one triggers trySend(),
		 * since this means, now > lastSent. On receiving of MACSendingTerminated, the next
		 * trySend() is issued, to further process the queue. Thus, for every to be send 
		 * packet at least once trySend()is called.
		 */
		
		if (now > lastTrySent) {
			lastTrySent = now;
			trySend(2); // #2#
		
			if (!pendingCS && !willSendACK) {
				// something is currently received, thus backoff is needed.
				calcBackoffTime(0.0);
			}
		}
	}

	/**
	 * @see br.ufla.dcc.PingPong.node.Layer#processEvent(br.ufla.dcc.PingPong.event.Initialize)
	 * @param init Initialize event to start up the layer.
	 */
	@Override
	public final void processEvent(Initialize init) {
		int n = globalTimings.getMaxBitrateIDX();
		ackDelay = new double[n + 1];
		
		for (int i = 0; i <= n; i++) {
			WlanFramePacket f = new WlanFramePacket(sender, NodeId.ALLNODES, PacketType.ACK, -1.0);
			raDefaultPolicy.setBitrateIdx(i);
			applyBitrate(f, -1);
			ackDelay[i] = globalTimings.getSifs() + f.getDuration() + 2.0 * globalTimings.getSyncDuration() 
				+ 2.5 * propagationDelay; 
		}
		
		maxRetryCount = Math.max(retryLimit, MAX_CW_IDX + 1);
		raDefaultPolicy.setBitrateIdx(n);
	}
	
	/**
	 * Method to start this layer.
	 * @param start StartSimulation event to start the layer.
	 */
	@Override
	protected void processEvent(StartSimulation start) {
		Address thisAddress = new Address(id, LayerType.MAC);
		StatisticLogRequest slr = new StatisticLogRequest(thisAddress, 
				getConfig().getSimulationSteps(1), 
				DROPS_PER_SECOND);
		sendEventSelf(slr);
	}

	/**
	 * If a packet was rejected by the Air, this method tries to avoid the loss of this packet.
	 * 
	 * @param rpe   the rejected packet event.
	 * @param frame the dropped frame.
	 */
	protected void tryHandleDroppedPacket(RejectedPacketEvent rpe, WlanFramePacket frame) {
		// here the packet is just dropped. override this, to change the behavior (see DBG variant).
	}
	
	/**
	 * this method is to botch/debug an error of this class, where a packet is sent too early.
	 * 
	 * @param rpe the event containing the too early sent packet.
	 */
	private void handleRejectedPacketEvent(RejectedPacketEvent rpe) {
		double now = node.getCurrentTime(), lts = now - lastTrySent, ls = now - lastSent;
		
		String msg = "";
		
		if (pendingCS)				  { msg = "pCS,"; }
		if (pendingACK)   			  { msg += "pACK,"; }
		if (willSendACK)			  { msg += "wsACK,"; }
		if (immediateSend)            { msg += "imS,"; }
		if (currentOutPacket != null) { msg += "cOut, "; }
		
		msg = "state: " + msg + "tsPos:" + lastTrySendPos + " now: " + now + "lts: " + lts + " ls: " + ls;
		
		Packet p = rpe.getCurrentPacket();
		
		if (p != null) {
			msg += "curr. p: " + p.getHighestEnclosedPacket().getClass().toString();
		}
		
		Packet packet = rpe.getPacket();
		
		if (packet != null) {
			LOGGER.warn(msg);
			
			if (packet instanceof WlanFramePacket) {
				tryHandleDroppedPacket(rpe, (WlanFramePacket) packet);
			}
		} else {
			if (rpe instanceof CarrierSenseInterruptedEvent) {
				LOGGER.info(msg);
				
				CarrierSenseInterruptedEvent csi = (CarrierSenseInterruptedEvent) rpe;
				LOGGER.info(id + " cs was interrupted and restarted. No Data is lost. (d=" 
							+ csi.getRest() + " p=" + csi.getPos() + ")");
			}
		}
	}
	
	/**
	 * process the incoming events MACCarrierSensing and MACprocessAckTimeout.
	 * 
	 * @param wuc contains the to be processed wakeup-call.
	 * @throws LayerException if the wakeup call could not be processed.
	 */
	@Override
	public void processWakeUpCall(WakeUpCall wuc) throws LayerException {
		if (wuc instanceof SendingTerminated) {
			willSendACK = false;
			trySend(4); // #4#
		} else if (wuc instanceof RejectedPacketEvent) {
			handleRejectedPacketEvent((RejectedPacketEvent) wuc);
		} else if (wuc instanceof CrossLayerEvent) {
			((CrossLayerEvent) wuc).forwardUp(this);
		} else if (wuc instanceof MACCarrierSensing) {
			processMACCarrierSensing((MACCarrierSensing) wuc);
		} else if (wuc instanceof MACProcessAckTimeout) {
			processMACprocessAckTimeout((MACProcessAckTimeout) wuc);
		} else if (wuc instanceof MACSendACK) {
			processMACsendACK((MACSendACK) wuc);
		} else if (wuc instanceof ReceiverInformationEvent) {
			processReceiverInformationEvent((ReceiverInformationEvent) wuc);
		} else if (wuc instanceof MACEvent) {
			processMACEvent((MACEvent) wuc);
		} else if (wuc instanceof StatisticLogRequest) {
			logStatistic((StatisticLogRequest) wuc);
			sendEventSelf(wuc);
		} else {
			throw new LayerException("MacModule of Node " + id + " received wakeup call " + wuc.getClass().getName());
		}
	}

	/** notes the id of the last sent and checked physical packet. */
	private int     lastSeenId      = -1;
	/** set to true, if the packet was received at least once. */
	private boolean currentWasReceivedOnce = false;
	
	/**
	 * method to process the receiver information event.
	 * @param wuc the to be processed event.
	 */
	private void processReceiverInformationEvent(ReceiverInformationEvent wuc) {
		Packet packet = wuc.getPacket();
		
		if ((packet != null) && (packet instanceof PhysicalPacket)) {
			PhysicalPacket pPacket  = (PhysicalPacket) packet;
			WlanFramePacket mPacket = (WlanFramePacket) pPacket.getEnclosedPacket();
			
			if (!packet.getReceiver().equals(NodeId.ALLNODES) && !mPacket.isTerminal()) {
				Packet innerP = mPacket.getHighestEnclosedPacket();
				int packetId = innerP.getId().asInt() * 10 + innerP.getLayer().ordinal();
				
				if (lastSeenId != packetId) {
					currentWasReceivedOnce = false;
				}
				
				if (pPacket.hasReceived(pPacket.getReceiver())) {
					if (lastSeenId == packetId) {
						sendEventUp(new DoublePacketEvent(sender, mPacket.getEnclosedPacket()));
					}
					
					currentWasReceivedOnce = true;
				} else {
					if ((mPacket.getRetryCount() >= maxRetryCount) && !currentWasReceivedOnce) {
						sendEventUp(new LostPacketEvent(sender, mPacket.getEnclosedPacket()));
					}
				}
				
				lastSeenId = packetId;
			}
		}
	}
	
	/**
	 * Method to process the generic MACEvent. This is only used for a delayed trySend().
	 * @param wuc the to be processed wakeup call.
	 */
	private void processMACEvent(MACEvent wuc) {
		boolean res = this.trySend(5); // #5#
		
		if (res) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("successfully initiate the delayed trySend()");
			}
		}
	}
	
	/**
	 * Method to log a statistic event of a certain type.
	 * @param slr A wakeup call with information on what to log
	 */
	private void logStatistic(StatisticLogRequest slr) {
		if (slr.getStatisticType() == DROPS_PER_SECOND) {
			SimulationManager.logStatistic(id, LayerType.MAC, "time", "Dropped Packets / s", 
							Double.toString(SimulationManager.getInstance().getCurrentTime()), 
							Integer.toString(droppedPackets));
			droppedPackets = 0;
		}
	}
	
	/** Internal method to set a random backoff delay. */
	private void setBackoffTime() {
		immediateSend = false;
		
		int i = getRandom().nextInt(CW[currentCW]);
		
		backoffTime = globalTimings.getSlotTime() * i;
		
		if (currentCW < MAX_CW_IDX) {
			currentCW++;  
		}
	}
	
	/**
	 * internal method to modify/set the backoff delay.
	 * @param csStart time from which the carrier sensing started.
	 */
	private void calcBackoffTime(double csStart) {
		if (immediateSend) {
			setBackoffTime();
		} else {
			long passedBackoffSlots = (long) Math.floor(
					(getNode().getCurrentTime() - csStart)
					/ globalTimings.getSlotTime());
			backoffTime -= passedBackoffSlots * globalTimings.getSlotTime();
			
			if (backoffTime <= 0.0) {
				immediateSend = true;
				backoffTime = 0.0;
			}
		}
	}
	
	/**
	 * internal method to handle MACCarrierSensing.
	 * @param macCS the to be handled event.
	 */
	private void processMACCarrierSensing(MACCarrierSensing macCS) {
		pendingCS = false;

		/*
		 * like in lowerSAP, any change to this method requires, 
		 * that trySend() will be called, after sending a packet,
		 * or after the decision not to do so.
		 */
		
		if (macCS.isNoCarrier()) {
			if ((currentOutPacket == null) && (outQueue.size() > 0)) {
				currentOutPacket = outQueue.removeFirst();
			}
			
			if (currentOutPacket == null) {
				LOGGER.warn("carrier sensing returned but no out packet present !");
			} else {
				currentOutPacket.getRaPolicy().reset(getNode().getCurrentTime());
					
				//immediateSend = true;
					
				PacketType pType = currentOutPacket.getType();
				boolean toAllNodes = currentOutPacket.getReceiver() == NodeId.ALLNODES;
				
				pendingACK = !toAllNodes && ((pType == PacketType.DATA) || (pType == PacketType.RTS));
					
				currentOutPacket.setAckRequested(pendingACK);
				currentOutPacket.setReadyForTransmission(false);
				
				lastSent = getNode().getCurrentTime();
				sendPacket(currentOutPacket);
				
				if (pendingACK) {
					// 2nd syncDuration already included in ackDelay !!!
					double delay = currentOutPacket.getDuration() + ackDelay[currentOutPacket.getBitrateIdx()];
				
					WakeUpCall wuc = new MACProcessAckTimeout(sender, delay);
					sendEventSelf(wuc);
				} else {
				// since no ack expected, this is shoot and forget
					currentOutPacket = null; 
				}
				/*
				 * If the current packet is sent, a wakeup call will 
				 * inform us and another trySend() will be issued.
				 */
			}
		} else {
			calcBackoffTime(macCS.getCsStart());
			trySend(6); // #6#
		}
	}
	
	/**
	 * internal method to handle MACprocessAckTimeout events.
	 * @param macAckTimeout the to be handled event.
	 */
	private void processMACprocessAckTimeout(MACProcessAckTimeout macAckTimeout) {
		
		// see above in processMACCarrierSensing, in case of changes of this method.
		
		if (pendingACK) {
			pendingACK = false;

			currentOutPacket.getRaPolicy().processFailure(getNode().getCurrentTime());
			
			int retryCount = currentOutPacket.getRetryCount() + 1;
			
			if (retryCount <= maxRetryCount) { 
				currentOutPacket.setRetryCount(retryCount);
				calcBackoffTime(0.0);
				currentOutPacket.setReadyForTransmission(true);
				currentOutPacket.reset(); // prepare the packet for resending ##
				applyBitrate(currentOutPacket, -1);
				
				// enforce recalculation of a random backoff, since a collision may have happened. 
				
				if (immediateSend || (backoffTime <= 0.0)) {
					setBackoffTime();
				}

				trySend(7); // #7#
			} else {
				Packet llP = currentOutPacket.getEnclosedPacket();
				
				if (llP != null) {
					sendEventUp(new TransmissionFailedEvent(sender, llP));
				}
				currentOutPacket = null;
				this.droppedPackets++;
				trySend(8); // #8#
			}
		} else { 
			if (currentOutPacket != null) {
				if (currentOutPacket.getReceiver() != NodeId.ALLNODES) {
					currentOutPacket.getRaPolicy().processSuccess();
					currentCW = 0;
				}
				currentOutPacket = null;
			}
			trySend(9); // #9#
		}
	}

	/**
	 * internal method to immediately send an ACK.
	 * @param sendACK the to be processed event.
	 */
	private void processMACsendACK(MACSendACK sendACK) {
		sendPacket(sendACK.getAck());
		/*
		 * If this ack has left the building, an event will inform us
		 * and uppon handling of this event, trySend() will be issued.
		 */
	}
	
	/**
	 * process the finalize event.
	 * @param end the to be processed event.
	 */
	@Override
	protected void processEvent(Finalize end) {
		double now = SimulationManager.getInstance().getCurrentTime();
		
		now -= lastTrySent;
		now  = getConfig().getSeconds(now);
		
		int i = outQueue.size();
		
		if (i > 0) {
			if (pendingCS || (now < 0.05)) {
				LOGGER.info(id + " has still " + i + " outstanding packets left (busy).");
			} else {
				LOGGER.warn(id + " has still " + i + " outstanding packets (idle).");
			}
		}
	}
	
	/**
	 * currently no states needed, thus no statechanges possible.
	 * @return the current MAC-state.
	 */
	@Override
	public MACState getState() {
		int size = outQueue.size();
		
		ArrayList<WlanFramePacket> queue = null;
		
		if (includeQueueInState) {
			queue = new ArrayList<WlanFramePacket>();
			
			if (currentOutPacket != null) {
				queue.add(currentOutPacket);
			}
			
			queue.addAll(outQueue);
		}
		
		if (currentOutPacket != null) {
			size++;
		}
		
		MACState state = new MACState(raDefaultPolicy, 16.0, size);
		
		if (queue != null) {
			state.setQueue(queue);
		}
		
		return state;
	}

	/**
	 * Currently only supports the change of the Bitrate via the default policy.
	 * @param state everything is ignored except promiscuous.
	 * @return false always.
	 */
	@Override
	public boolean setState(LayerState state) {
		//TODO the other attribute of state should be used
		promiscuous = ((MACState) state).getPromiscuous();
		return false;
	}

	/**
	 * method, to read the configuration parameters and to configure the MAC accordingly. 
	 * @param configuration the configuration to use for this node.
	 * @throws ConfigurationException is thrown, if an illegal rate adaptation method is chosen.
	 */
	@Override
	public void initConfiguration(Configuration configuration) throws ConfigurationException {
		super.initConfiguration(configuration);
		int i = 1, mode = AARFRateAdaptation.NO_ARF, maxBitrateIDX;
		double x;
		
		if (globalTimings == null) {
			PhysicalLayerState phyState = (PhysicalLayerState) getNode().getLayerState(LayerType.PHYSICAL);
			globalTimings = (IEEE_802_11_TimingParameters) phyState.getTimings();
		}
		timings = globalTimings;		
		maxBitrateIDX = globalTimings.getMaxBitrateIDX();
		
		if (rateAdaption.equals("ARF")) {
			mode = AARFRateAdaptation.ARF;
		} else if (rateAdaption.equals("AARF")) {
			mode = AARFRateAdaptation.AARF;
			i = raUpMult;
		} else if (rateAdaption.equals("NO_ARF")) {
			LOGGER.info("no rate adaptation used.");
		} else {
			throw new ConfigurationException("MacModule of Node " + id 
					                         + " illegal rate adaption policy " + rateAdaption);
		}
		
		x = getConfig().getSimulationSteps(raTimeout);
		raDefaultPolicy = new AARFRateAdaptation(this, maxBitrateIDX, maxBitrateIDX, mode, 
				                                -raDownLevel, raUpLevel, i, x);
		currentCW        = 0;
		pendingCS        = false;
		pendingACK       = false;
		willSendACK      = false;
		immediateSend    = true;
		currentOutPacket = null;
		
		propagationDelay = getConfig().getPropagationDelay();
		if (propagationDelay != getConfig().getSimulationSteps(1.0e-7)) {
			throw new SimulationFailedException(
					"propagation delay is invalid for "
					+ MAC_IEEE802_11bg_DCF.class.getName());
		}
	}
}