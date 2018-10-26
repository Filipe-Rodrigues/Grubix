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

package br.ufla.dcc.grubix.simulator.node.user;

import br.ufla.dcc.grubix.simulator.NodeId;
import br.ufla.dcc.grubix.simulator.event.MACPacket;
import br.ufla.dcc.grubix.simulator.event.MACPacket.PacketType;
import br.ufla.dcc.grubix.simulator.event.user.RejectedPacketEvent;
import br.ufla.dcc.grubix.simulator.event.user.WlanFramePacket;

/** 
 * Implements the IEEE 802.11b MAC in AdHoc aka DCF mode.
 * 
 * @author Dirk Held
 */
public class MAC_IEEE802_11bg_DCF_DBG extends MAC_IEEE802_11bg_DCF {

	/**
	 * If a packet was rejected by the Air, this method tries to avoid the loss of this packet.
	 * 
	 * @param rpe   the rejected packet event.
	 * @param frame the dropped frame.
	 */
	@Override
	protected void tryHandleDroppedPacket(RejectedPacketEvent rpe, WlanFramePacket frame) {
		if (frame.getReceiver().equals(NodeId.ALLNODES)) {
			LOGGER.warn(id + " added a pushed back broadcast to the queue. ("
						+ rpe.getOriginator().getShortName() + ")");
			outQueue.addFirst(frame);
		} else if (frame.getType() == PacketType.ACK) {
			willSendACK = false;
			/*
			 * Here, no own data is lost. The sender may think of an lost packet
			 * and eventually send it again and may send a transmission failed event
			 * after a number of retries. 
			 */
			LOGGER.warn(id + " failed to send an ack for an incoming packet.("
						+ rpe.getOriginator().getShortName() + ")");
		} else {
			int count = frame.getRetryCount();
			
			if (count > 0) {
				frame.setRetryCount(count - 1);
				LOGGER.warn(id + " reduced the retry counter for a pushed back packet. ("
							+ rpe.getOriginator().getShortName() + ")");
			} else {
				LOGGER.warn(id + " got a pushed back packet, which will be resend soon. ("
							+ rpe.getOriginator().getShortName() + ")");
			}
		}
		trySend(3); // #3#
		
		super.tryHandleDroppedPacket(rpe, frame);
	}
}