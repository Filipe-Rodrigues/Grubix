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

import static java.util.Collections.sort;
import java.util.ArrayList;

import br.ufla.dcc.grubix.simulator.EventId;
import br.ufla.dcc.grubix.simulator.SimulationFailedException;
import br.ufla.dcc.grubix.simulator.event.Interference;
import br.ufla.dcc.grubix.simulator.event.PhysicalPacket;
import br.ufla.dcc.grubix.simulator.event.Transmission;
import br.ufla.dcc.grubix.simulator.node.metainf.UpwardsPhysicalMetaInfo;




/**
 * Encapsulates the interference queue for each node (AirModule). This queue is used for
 * storing all (interfering) signals that a node receives at the current time.
 * Currently, we assume that all nodes share the same frequency and the data signal can be
 * distinguished from the interfering signals precisely. The transmitted data packets are
 * damaged on a byte level according to the accumulated signal strengths of the interfering signals. 
 * @author dirk_held
 *
 */
public class InterferenceQueue {
	
	/**
	 * contains for simple queues (= results of interference computations) the reachability
	 * information of the to be interfered signal. 
	 */
	private Reachability selfReachability;
	
	/**
	 * if true, no sorting is performed and Intersection and bit mangling is not supported.
	 */
	private boolean simple;
	
	/**
	 * is true, if the ArrayList is sorted.
	 */
    private boolean sorted;
	
    /** contains the longest duration of all added interferences to ease searching. */
    private double currentMaxDuration;

    /** the all time maximum duration. */
	private double maxDuration;
	
	/** contains the end of the latest interference. */
	private double maxTime; 
	
	/**
	 * contains all occurred interferences.
	 */
	private ArrayList<Interference> interferences = new ArrayList<Interference>();

	/**
	 * the used BitManglingModel to mangle Packets, if this not a simple InterferenceQueue.
	 */
	private BitManglingModel bmm;
	
	/**
	 * used, to initialize the internal state.
	 * @param bmm use null for a quick InterferenceQueue without sorting/intersecting/mangling.
	 */
	public InterferenceQueue(BitManglingModel bmm) {
		this.bmm = bmm;
		simple             = (bmm == null);
		sorted             = !simple; 
		currentMaxDuration = 0.0;
		maxDuration        = 0.0;
		maxTime            = 0.0;
	}
	
	/**
	 * called, if a sorted ArrayList is needed and the state is unclear.
	 * The sorting operation is then performed, if necessarry.
	 *
	 */
	@SuppressWarnings("unchecked")
	private void checkSorted() {
		if (!simple && !sorted) {
			sort(interferences);
			sorted = true;
		}
	}
	
	/**
	 * Locates by binary search the position where the interval should be. If no match  
	 * was found in exact mode, or if no interferences are stored, the result is -1.
	 * @param time     starting time of the interval
	 * @param duration duration of the interval.
	 * @param exact    is a exact match required? 
	 * @return an index into interferences, or -1.
	 */
	private int locate(double time, double duration, boolean exact) {
		int low = 0, high = interferences.size() - 1, mid = 0, cmp;
		
		if (simple) {
			if (exact || (high < 0)) {
				return -1;
			} else {
				return 0;
			}
		} else {
			// if method public: checkSorted();
			
			while (low <= high) {
				mid = (low + high) >> 1;
				cmp = interferences.get(mid).compareTo(time, duration);
			
				if (cmp < 0) {        // interferences.get(mid) < [time,duration]
					low = mid + 1;
				} else if (cmp > 0) { // interferences.get(mid) > [time,duration]
					high = mid - 1;
				} else {
					return mid;
				}
			}
			
			if (exact) {
				return -1;
			} else {
				return mid;
			}
			// return -(mid+1);
		}
	}

	/**
	 * This method finds an interference, which intersects with the given interval and
	 * returns it's position, or -1 if no interference intersects with the interval.
	 * @param time     the starting time of the interval
	 * @param duration the length of the interval
	 * @return a intersecting interval index or -1
	 */
	private int findAIntersection(double time, double duration) {
		int pos, pos2, pos3, size = interferences.size();
		
		// TODO comparing of floats is dangerous, improve this
		
//		 if method public: checkSorted();
		if (simple || (size == 0) || (maxTime < time) 
				|| ((time + duration) < interferences.get(0).getSimStartTime())) { 
			return -1;
		} else {
			pos = locate(time, duration, false);
		
		   /*
	 		* catch not intersecting results and check left and right neighbor 
	 		* for an intersection. return -1, if no intersection was found.
	 		*/
			
			if (!interferences.get(pos).intersects(time, duration)) {
				pos2 = pos + 1;
											
				if (pos2 == size || !interferences.get(pos2).intersects(time, duration)) {
					pos2 = -1; // no intersection at the right neighbor
				} else {
					pos3 = pos - 1;
					
					while ((pos3 >= 0) && (interferences.get(pos3).getSimStartTime() + currentMaxDuration > time)) {
						if (interferences.get(pos3).intersects(time, duration)) { 
							pos2 = pos3; // found a intersecting left neighbor
							pos3 = 0;
						}
						pos3--;
					}
				}
				pos = pos2;
			}
			return pos;
		}
	}
	
	/**
	 * returns for a given interval the subset of the affected interferences.
	 * @param time of the requested interference interval.
	 * @param trans if != null, the affected transmission.
	 * @return the interfering intervals.
	 */
	private InterferenceQueue getIntersections(double time, Transmission trans) {
		int left, pos;
		double rightT;
		double duration; 
		EventId selfPacketID;
		
		InterferenceQueue res = new InterferenceQueue(null);
		
		if (trans == null) {
			duration = 0.0;
			selfPacketID = null;
		} else {
			duration = trans.getSendingTime();
			selfPacketID = trans.getPacket().getId();
		}

		rightT = time + duration;
		
		if (interferences.size() > 0) {
			checkSorted();
		
			pos = findAIntersection(time, duration);
		
			if (pos >= 0) {
				res.addInterferingSignal(interferences.get(pos), selfPacketID);
				left = pos - 1;
				pos++;
				
				while ((left >= 0) && (interferences.get(left).getSimStartTime() + currentMaxDuration > time)) {
					if (interferences.get(left).intersects(time, duration)) {
						res.addInterferingSignal(interferences.get(left), selfPacketID);
					}
					left--;
				}
				
				while ((pos < interferences.size()) && (interferences.get(pos).getSimStartTime() < rightT)) {
					if (interferences.get(pos).intersects(time, duration)) { // maybe obsolete
						res.addInterferingSignal(interferences.get(pos), selfPacketID);
					}
					pos++;
				}
			}
		}
		return res;
	}
	
	/**
	 * @param currentTime the current simulation time.
	 * @return the current interference.
	 */
	public final double getCurrentInterference(double currentTime) {
		double res = 0.0;
		
		InterferenceQueue inter = getIntersections(currentTime, null);
		
		for (Interference i : inter.getInterferences()) {
			res += i.getSignalStrength();
		}
		return res;
	}
	
	/**
	 * method, to update maxDuration and maxTime, if necessary.
	 * @param inter the to be checked interference.
	 */
	private void checkMax(Interference inter) {
		double t = inter.getSendingTime();
		
		if (t > maxDuration) {
			maxDuration = t;
		}
		
		if (t > currentMaxDuration) {
			currentMaxDuration = t;
		}
		
		t += inter.getSimStartTime();
			
		if (t > maxTime) {
			maxTime = t;
		}
	}
	
	/**
	 * Adds a new incoming interfering signal to the interference queue.
	 * @param inter interfering signal
	 * @param selfPacketID if != null, stopps this interference from beeing added.
	 */
	public final void addInterferingSignal(Interference inter, EventId selfPacketID) {
		if (simple) {
			if ((selfPacketID == null) || (inter.getPacket().getId() != selfPacketID)) { 
				this.interferences.add(inter);
			} else {
				selfReachability = inter.getReachability();
			}
		} else {
			this.interferences.add(inter);
			checkMax(inter);
		}
		sorted = false;
	}

	/**
	 * used to cleanup outdated interferences.
	 * @param currentTime flush old inteferring signals
	 */
	public final void garbageCollect2(double currentTime) {
		int i;
		boolean init = true;
		Interference inter;
		
		i = interferences.size() - 1;
		
		if (i >= 0) {
			checkSorted();
		
			if (maxTime < currentTime) {
				interferences.clear();
				currentMaxDuration = 0.0;
			} else {
				// TODO implement faster version, by using maxDuration;
		
				while (i >= 0) {
					inter = interferences.get(i);
			
					if (inter.getSimStartTime() + inter.getSendingTime() < currentTime) {
						interferences.remove(i);
					} else {
						if (init) {
							init = false;
							currentMaxDuration = 0.0;
						}
						checkMax(inter);
					}
					i--;
				}
			}
		}
	}
	

	/**
	 * used to cleanup outdated interferences.
	 * @param currentTime flush old inteferring signals
	 */
	public final void garbageCollect(double currentTime) {
		int i,count,pos;
		Interference inter;
		
		count = interferences.size();
		
		if (count >= 0) {
			checkSorted();
			
			pos = 0;
			
			for (i = 0; i < count; i++) {
				inter = interferences.get(pos);
				
				if (inter.getSimStartTime() + inter.getSendingTime() < currentTime) {
					interferences.remove(pos);
				} else {
					pos++;
				}
			}
			
			currentMaxDuration = 0.0;
			//maxTime = 0.0; we need to know, when the last interference has occurred.
			
			for (i = 0; i < interferences.size(); i++) {
				inter = interferences.get(i);
				checkMax(inter);
			}
		}
	}
	

	/**
	 * Accumulates interfering signals during transmission interval of the transmission and 
	 * damages the encapsulated data packet accordingly. The particular strategy which decides
	 * what and how exactly to damage the data packet is delegated to the associated
	 * BitManglingModel. 
	 * @param trans The transmission object which contains the current data packet
	 * @return The resulting data packet which is damaged if interference occurred during transmission
	 */
	public final PhysicalPacket getResultingDataPacket(Transmission trans) {
		if (simple || (bmm == null)) {
			return null;
		} else {
			double startTime = trans.getSimStartTime();
			InterferenceQueue inter = getIntersections(startTime, trans);
		
			PhysicalPacket phyPacket = bmm.getResultingDataPacket(startTime, trans, inter);
			
			Reachability reachability = inter.getSelfReachability();
			
			if (reachability != null) {
				UpwardsPhysicalMetaInfo upPhyMetaInfo = new UpwardsPhysicalMetaInfo(reachability.getSsAtReceiver());
				
				/* use physical model VariableDisc and 
				 * "phyPacket.getMetaInfos().getUpwardsPhysicalMetaInfo().geSignalStrength();" to retrieve the RSSI
				 */
				
				phyPacket.getMetaInfos().addMetaInfo(upPhyMetaInfo);
			} else {
				throw new SimulationFailedException("no Reachability-Object stored for this packet.");
			}
			
			return phyPacket;
		}
	}

	/** @return the interferences. */
	public final ArrayList<Interference> getInterferences() {
		return interferences;
	}

	/**
	 * @return the sorted
	 */
	public final boolean isSorted() {
		return sorted;
	}

	/**
	 * @return the simple state of the InterferenceQueue.
	 */
	public final boolean isSimple() {
		return simple;
	}

	/**
	 * @return the selfReachability
	 */
	public final Reachability getSelfReachability() {
		if (simple) {
			return selfReachability;
		} else {
			return null;
		}
	}

	/** @return the maxTime */
	public final double getMaxTime() {
		return maxTime;
	}
	
	/** @return the maxDuration */
	public final double getMaxDuration() {
		return maxDuration;
	}
	
	/** @return the currentMaxDuration */
	public final double getCurrentMaxDuration() {
		return currentMaxDuration;
	}

	/** @return the size of the interference queue. */
	public final int getSize() {
		return interferences.size();
	}
}
