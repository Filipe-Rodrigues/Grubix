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

package br.ufla.dcc.grubix.simulator.physical;

import br.ufla.dcc.grubix.simulator.Position;

/**
 * helper-class to encapsulate the calculated information of reachability
 * and resulting signal strength after applying a physical model.
 * @author Dirk Held
 */
public class Reachability {

	/** position of the receiver for the reachability calculation. */
	private Position receiver;
	
	/** position of the sender for the reachability calculation. */
	private Position sender;
	
	/** the resulting squared euclidean distance. */
	private double   squaredDistance;
	
	/** the accumulated attenuation on this line of sight. */
	private double   attenuation;
	
	/** the resulting signal strength for a given signal strength of the sender. */
	private double   ssAtReceiver;

	/**
	 * result of the reachability calculation, using the PhysicalModel
	 * unitDisc uses only the distance to compute this, but all others
	 * require the resulting signal strength to be above a certain value.
	 * reachable is true, if the node is within reach of the sender.
	 */
	private boolean reachable;
	
	/**
	 * like above, but true, when the sending node interferes with the
	 * receiving node node. Thus reachable = true => interfering = true.  
	 */
	private boolean interfering;
	
	/**
	 * the remaining attenuated signal strength at the reciver, where
	 * unitDisc model doesn't attenuate up to a certain distance and all
	 * others yield a certain attenuation, which was already calculated 
	 * for isReachable. 
	 */
	private double  signalStrength;

	/**
	 * returns, if the receiver is reachable by the sender.
	 * @return reachable
	 */
	public final boolean isReachable() {
		return reachable;
	}

	/**
	 * store the result of the reachability calculation of a physical model.
	 * @param reachable indicates, whether the receiver is reachable or not.
	 */       
	public final void setReachable(boolean reachable) {
		this.reachable = reachable;
	}

	/** @return the interfering status */
	public final boolean isInterfering() {
		return interfering;
	}

	/** @param interfering the interfering status to be set */
	public final void setInterfering(boolean interfering) {
		this.interfering = interfering;
	}

	/**
	 * return the resulting signal strength after the occured attenuation.
	 * @return signalStrength
	 */
	public final double getSignalStrength() {
		return signalStrength;
	}

	/**
	 * store the attenuated signal strength due to the used physical model.
	 * @param signalStrength the signal strength of the sender
	 */
	public final void setSignalStrength(double signalStrength) {
		this.signalStrength = signalStrength;
	}

	/**
	 * returns the attenuation, calculated by the used physical model. 
	 * @return attenuation
	 */
	public final double getAttenuation() {
		return attenuation;
	}

	/**
	 * store the attenuation given from the physical model.
	 * @param attenuation calculated by the physical model
	 */
	public final void setAttenuation(double attenuation) {
		this.attenuation = attenuation;
	}

	/** 
	 * return the resulting signal strength at the receiver calculated by the phys. model.
	 * @return signal strength at the receiver
	 */
	public final double getSsAtReceiver() {
		return ssAtReceiver;
	}

	/**
	 * store the resulting signal strength at the receiver calculated by the phys. model.
	 * @param ssAtReceiver the signal strength at the receiver calculated by the phys. model
	 */
	public final void setSsAtReceiver(double ssAtReceiver) {
		this.ssAtReceiver = ssAtReceiver;
	}

	/**
	 * returns the euclidean distance after the positions of the receiver and the sender where given.
	 * @return distance of sender by the euclidean metric.
	 */
	public final double getDistance() {
		return Math.sqrt(squaredDistance);
	}
	
	/**
	 * returns the squared euclidean distance after the positions of the receiver and the sender where given.
	 * @return distance of sender by the euclidean metric.
	 */
	public final double getSquaredDistance() {
		return squaredDistance;
	}

	/**
	 * return the current position of the receiver.
	 * @return receiver position
	 */
	public final Position getReceiver() {
		return receiver;
	}

	/**
	 * return the current position of the sender.
	 * @return sender position
	 */
	public final Position getSender() {
		return sender;
	}
	
	/**
	 * stores the positions of the sender and the receiver and calculates the euclidean distance.
	 * @param receiver         the position of the receiver
	 * @param sender           the position of the sender
	 */
	public final void setPositions(Position receiver, Position sender) {
		this.receiver = receiver;
		this.sender   = sender;
		
		if ((receiver != null) && (sender != null)) {
			squaredDistance = receiver.getSquaredDistance(sender);
		} else  {
			throw new NullPointerException("sender or receiver position missing"); 
		}
	}
}
