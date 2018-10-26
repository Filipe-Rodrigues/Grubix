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

package br.ufla.dcc.grubix.simulator;

import java.util.ArrayList;

/**
 * this class stores time intervals, allows a cleanup up to a specific time and
 * the lookup for the next free time spot after a specific point in time.
 * @author dirk
 *
 */
public class TimeLine {
	/**
	 * stores the current set of disjoint time intervals.
	 */
	private ArrayList<Interval> timeline = new ArrayList<Interval>();
	
	/**
	 * Locates by binary search the position where the interval should be. 
	 * @param start    starting time of the interval
	 * @param duration duration of the interval.
	 * @return an index into interferences.
	 */
	private int locate(double start, double duration) {
		int low = 0, high = timeline.size() - 1, mid, cmp;
		
		mid = (low + high) >> 1;

		while (low <= high) {
			cmp = timeline.get(mid).compareTo(start, duration);
			
			if (cmp < 0) {        // timeline.get(mid) < [time,duration]
				low = mid + 1;
			} else if (cmp > 0) { // timeline.get(mid) > [time,duration]
				high = mid - 1;
			} else {
				return mid;
			}
		}
		return mid;
	}
	
	/**
	 * check, whether the intervals left to pos are
	 * still disjoint and correct this, if necessary.
	 * @param pos the position to start from.
	 * @return the position of the interval after the correction.
	 */
	private int resolveLeft(int pos) {
		int pos2;
		Interval i, i2;
		
		pos2 = pos - 1;
		i = timeline.get(pos);
		
		while ((pos2 >= 0) && (i.intersects(timeline.get(pos2)))) {
			i2 = i;
			i = timeline.get(pos2);
			i.join(i2);
			timeline.remove(pos);
			pos = pos2;
		}
		return pos;
	}
	
	/**
	 * check, whether the intervals right to pos are
	 * still disjoint and correct this, if necessary.
	 * @param pos the position to start from.
	 */
	private void resolveRight(int pos) {
		int pos2;
		Interval i;
		
		pos2 = pos + 1;
		i = timeline.get(pos);
		
		while ((pos2 < timeline.size()) && (i.intersects(timeline.get(pos2)))) {
			i.join(timeline.get(pos2));
			timeline.remove(pos2);
		}
	}
	
	/**
	 * method to add a new interval and intersect it with already
	 * stored intervals.
	 * @param start    the start of the interval.
	 * @param duration the duration of the interval.
	 */
	public final void addInterval(double start, double duration) {
		int pos, cmp;
		
		Interval i, iNew = new Interval(start, duration);
		
		if (timeline.size() == 0) {  // no current intervals, just add
			timeline.add(iNew);
		} else {
			pos = locate(start, 0);
			i = timeline.get(pos);

			if (i.intersects(start, duration)) {
				i.join(start, duration);
				
				pos = resolveLeft(pos);
				resolveRight(pos);
			} else {
				cmp = i.compareTo(iNew);
				
				if (cmp < 0) {
					pos++;
					
					if (pos == timeline.size()) {
						timeline.add(iNew);
					} else {
						timeline.add(pos, iNew);
						resolveRight(pos);
					}
				} else if (cmp > 0) {
					timeline.add(pos, iNew);
					resolveLeft(pos);
				}
			}
		}
	}
	
	/** 
	 * method to cleanup the timeline upto a point in time.
	 * @param time the point in time upto which the timeline is to be cleared.
	 */
	public final void cleanupUpto(double time) {
		while ((timeline.size() > 0) && (time < timeline.get(0).getStart())) {
			timeline.remove(0);
		}
	}
	
	/**
	 * does a lookup within the timeline and returns the next point in time after
	 * the given time, where the timeline has a gap and returns it as interval.
	 * @param time start the lookup within the timeline at this time.
	 * @return the next gap after time as Interval. Interval.duration < 0 denotes infinity.
	 */
	public Interval nextGap(double time) {
		int pos, cmp;
		Interval i, iNew;
		double duration;
		
		if (timeline.size() == 0) {
			iNew = new Interval(time, -1);
		} else {
			pos = locate(time, 0);
			i   = timeline.get(pos);
			cmp = i.compareTo(time, 0);
			
			if (cmp > 0) {
				iNew = new Interval(time, i.getStart() - time);
			} else {
				pos++;
				time = i.getStart() + i.getDuration();
				
				if (pos == timeline.size()) {
					duration = -1;
				} else {
					i   = timeline.get(pos);
					duration = i.getStart() - time;
				}
				iNew = new Interval(time, duration);
			}
		}
		return iNew;
	}
}
