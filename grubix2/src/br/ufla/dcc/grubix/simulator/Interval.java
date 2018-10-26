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

/**
 * class to store a time interval.
 * 
 * @author Dirk Held
 *
 */
public class Interval implements Comparable<Interval> {
	
	/** the start of the time interval. */
	private double start;
	
	/** the duration of the interval. */
	private double duration;

	/**
	 * constructor, to initialize the value.
	 * @param start     the start time of the interval.
	 * @param duration  the duration of the interval.
	 */
	public Interval(double start, double duration) {
		this.start = start;
		this.duration = duration;
	}
	
	/** @return the duration */
	public final double getDuration() {
		return duration;
	}

	/** @param duration the duration to set */
	public final void setDuration(double duration) {
		this.duration = duration;
	}

	/** @return the start */
	public final double getStart() {
		return start;
	}

	/** @param start the start to set */
	@Deprecated
	public final void setStart(double start) {
		this.start = start;
	}
	
	/** @return the end. */
	public final double getEnd() {
		return start + duration;
	}

	/** @param end the new end to set. */
	@Deprecated
	public final void setEnd(double end) {
		end -= start;
		
		if (end >= 0) {
			duration = end;
		} else {
			start += end;
			duration = 0;
		}
	}

	/**
	 * Method to set a new start of the interval without changing
	 * the end, if possible. Otherwise the duration is set to zero.
	 * 
	 * @param newStart the new starting time of the interval.
	 */
	public final void trimStart(double newStart) {
		if (newStart <= start) {
			duration += (start - newStart);
		} else {
			double end = start + duration;
			
			if (newStart >= end) {
				duration = 0;
			} else {
				duration = end - newStart;
			}
		}
		
		start = newStart;
	}
	
	/**
	 * Method to set a new end of the interval without changing
	 * the start, if possible. Otherwise the duration is set to zero.
	 * 
	 * @param newEnd the new ending time of the interval.
	 */
	public final void trimEnd(double newEnd) {
		if (newEnd <= start) {
			start    = newEnd;
			duration = 0;
		} else {
			duration = newEnd - start;
		}
	}
	
	/**
	 * Method to intersect two intervals and trim them on request, if they intersect.
	 * If trim is true, after the intersection is done, the same intervals are covered
	 * from the three resulting intervals (this, i, intersect(i)). Also the trim is
	 * performed such that the resulting intervals don't intersect.
	 * 
	 * Example: (0,2).intersect(1,3,true) -> (0,1) + (1,2) + (2,3)
	 * 			(0,3).intersect(1,2,true) -> (0,1) + (1,2) + (2,3) // Thus (1,2) is changed to (2,3) to still cover (0,3)
	 * 			(1,2).intersect(1,2,true) -> (1,1) + (1,2) + (2,2) // both initial intervals are empty afterwards, since they are equal. 
	 * 
	 * @param i    The second interval for the intersection.
	 * @param trim If true, both interval will be trimmed, to not overlap with the returned 
	 * 			   intersection. The resulting length of the intervals may become zero.
	 * 
	 * @return the intersection of this interval, with the given one.
	 */
	public Interval intersect(Interval i, boolean trim) {
		Interval res = null;
		
		switch (compareTo(i)) {
			case -1 : double end = start + duration;
			
					  if (end > i.start) {
						  double end2 = i.getEnd();
						  double end3 = Math.min(end, end2);
						  
						  res = new Interval(i.start, end3 - i.start);
						  
						  if (trim) {
							  trimEnd(res.start);
							  i.trimStart(end3);
							  
							  if (i.duration == 0.0) {
								  // here the second interval is enlarged, to still
								  // cover the union of both intersected intervals.
								  i.duration = end - end2;
							  }
						  }
					  }
					  
					  break;
			
			case  0 : res = new Interval(start, duration);
			
					  if (trim) {
						  i.start   += i.duration;
						  i.duration = 0;
						  duration   = 0;
					  }
					  
					  break;
			
			case  1 : res = i.intersect(this, trim);
		}
		
		return res;
	}
	
	/**
	 * returns true, if the given interval intersects with this interference.
	 * 
	 * @param start    the starting time of the interval
	 * @param duration the length of the interval
	 * @return true, if an intersection occurred.
	 */
	public final boolean intersects(@SuppressWarnings("hiding")	double start, 
									@SuppressWarnings("hiding")	double duration) {
		boolean res = true;
		
		if (start + duration <= this.start) {
			res = false;
		} else if (this.start + this.duration <= start) {
			res = false;
		}
		return res;
	}
	
	/**
	 * check, whether the given interval intersects with this interval.
	 * @param i the interval to check intersection with.
	 * @return true, if both intervals intersect.
	 */
	public final boolean intersects(Interval i) {
		return i.intersects(start, duration);
	}
	
	/**
	 * assuming it intersects already with the given interval, it 
	 * modifies the interval to be the joint set with the given interval.
	 * @param start    the start time of the second interval.
	 * @param duration the duration of the interval.
	 */
	public final void join(@SuppressWarnings("hiding") double start, 
						   @SuppressWarnings("hiding") double duration) {
		double tStart, tEnd;
		
		tStart = this.start + this.duration; // calculate both endpoints of the interval
		tEnd   = start + duration;
		
		if (tStart > tEnd) {  // chose the later one as final endpoint of the interval
			tEnd = tStart;
		}
		
		if (start < this.start) { // determine the start of the resulting interval.
			this.start = start; 
		}
		
		this.duration = tEnd - this.start;
	}
	
	/**
	 * assuming, it intersects already with the given interval, it
	 * modfies the interval, to be the joint set of the two intervals.
	 * @param i the interval to join with.
	 */
	public final void join(Interval i) {
		join(i.getStart(), i.getDuration());
	}
	
	/**
	 * core compare method, to be able to compare versus a time interval
	 * and not just versus an object, as needed by java.
	 * 
	 * @param start    the start time.
	 * @param duration the duration of the interval.
	 * @return the standard compare result out of {-1,0,1}
	 */
	public final int compareTo(@SuppressWarnings("hiding") double start, 
							   @SuppressWarnings("hiding") double duration) {
		if (this.start < start) {
			return -1;  
		}
		
		if (this.start == start) {
			if (this.duration < duration) {
				return -1;
			}
			
			if (this.duration == duration) {
				return 0;
			}
			
			return 1;
		}
		
		return 1;
	}

	/**
	 * implements the comparing method, to include objects of this class into a sorted ArrayList.
	 * The primary key is the start of both intervals. Only if this is the same, the duration is
	 * used as secondary key. 
	 * 
	 * @param i the other object to compare this object to.
	 * @return the standard result of the compare operation one of {-1,0,1}
	 */
	public int compareTo(Interval i) {
		return compareTo(i.getStart(), i.getDuration());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + start + "," + (start + duration) + "]";
	}
}
