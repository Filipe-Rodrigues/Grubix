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

import br.ufla.dcc.grubix.simulator.Interval;
import br.ufla.dcc.grubix.simulator.event.Interference;




/**
 * This class is used to construct a vector of non-intersecting single noise events
 * from multiple interferences. This vector can afterwards be used, to mangle a packet.
 * 
 * This class computes from a list of Interferences, which disturb a transmission, a 
 * vector, which represents the noise, which affects parts of the transmission. If
 * multiple interferences do overlap, they are flattened out. The result is a vector
 * of distinct disturbances with it's accumulated intensity. To shorten the evaluation,
 * a minimal noise intensity may be used, such that the evaluation will be stopped, if
 * the noise exceeds this value somewhere. After The evaluation, all (so far) processed
 * noises and the (so far) most significant noise can be accessed.
 *  
 * @author Dirk Held
 */
public class NoiseVector {

	/** The flattened vector of all disturbances, affecting a transmission. */
	private ArrayList<Noise> noise;
	
	/** Contains the most significant (found) disturbance. */ 
	private Noise mostSignificantNoise;
	
	/**
	 * Method to add a new noise event to the noise vector.
	 * 
	 * @param n         The to be added noise event.
	 * @param pos       The position, where the event is inserted.
	 * @param noiseLevel The noise intensity, where true is returned, or 0.0, when 
	 * 					no premature stop of the evaluation should be performed. 
	 * @return true, if a significant noise was found (the most 
	 *   intense disturbance may be more intense than this one).
	 */
	private boolean checkNoise(Noise n, double noiseLevel) {
		if ((mostSignificantNoise == null) || (mostSignificantNoise.getRssi() < n.getRssi())) {
			mostSignificantNoise = n;
		}
		
		if ((noiseLevel > 0.0) && (mostSignificantNoise.getRssi() >= noiseLevel)) {
			return true;
		}
		
		return false;
	}
	
	private void flattenInterferences(Interval t, InterferenceQueue inter, double noiseLevel) {
		if (inter.getSize() == 0) return;
			
		for (Interference i : inter.getInterferences()) {
			Noise n = new Noise(i);
			
			if (n.getStart() < t.getStart()) {
				n.trimStart(t.getStart());
			}
			
			if (n.getEnd() > t.getEnd()) {
				n.trimEnd(t.getEnd());
			}
			
			noise.add(n);
			
			if (checkNoise(n, noiseLevel)) return;
		}
		
		ArrayList<Noise> tNoise = noise;
		
		noise = new ArrayList<Noise>();

		while (tNoise.size() > 1) {
			sort(tNoise);
	
			Noise n  = tNoise.remove(0);
			Noise n2 = tNoise.get(0);
			
			if (n.intersects(n2)) {
				tNoise.remove(0);
				
				Noise n3 = n.intersect(n2);
				
				if (n2.getDuration() > 0) {
					tNoise.add(0, n2);
					
					if (checkNoise(n2, noiseLevel)) return;
				}
				
				if (n3 != null) {
					tNoise.add(0, n3);
					
					if (checkNoise(n3, noiseLevel)) return;
				}
				
				if (n.getDuration() > 0) {
					tNoise.add(0, n);
					
					if (checkNoise(n, noiseLevel)) return;
				}
			} else {
				noise.add(n);
			}
		}
		
		if (tNoise.size() > 0) noise.add(tNoise.get(0));
		
		if ((noiseLevel == 0.0) && (noise.size() > 1)) {
			int i = 1;
			
			while (i < noise.size()) {
				Noise n1 = noise.get(i - 1);
				Noise n2 = noise.get(i);
				
				if ((n1.getEnd() == n2.getStart()) && (n1.getRssi() == n2.getRssi())) {
					noise.remove(i);
				} else {
					i++;
				}
			}
		}
	}
	
	/**
	 * Default constructor of this class.
	 *  
	 * @param startTime  The starting time to trim all processed interferences to.
	 * @param duration   The disturbed transmission, to get the duration from, to
	 * 					 also trim all processed interferences.
	 * @param inter      The queue, containing all to be processed interferences.
	 * @param noiseLevel If a value > 0.0 is given, stop the flattening, when noise  
	 * 					 with this intensity is found. Thus not all interferences 
	 * 					 from the queue will be put into the NoiseVector.
	 */
	public NoiseVector(double startTime, double duration, InterferenceQueue inter, double noiseLevel) {
		mostSignificantNoise = null;
		noise = new ArrayList<Noise>();

		flattenInterferences(new Interval(startTime, duration), inter, noiseLevel);
	}
	
	/** @return the noise. */
	public final ArrayList<Noise> getNoise() {
		return noise;
	}

	/** @return the mostSignificantNoise. */
	public final Noise getMostSignificantNoise() {
		return mostSignificantNoise;
	}
}
