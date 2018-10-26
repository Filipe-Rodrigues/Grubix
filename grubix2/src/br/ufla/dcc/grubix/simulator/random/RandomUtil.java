/********************************************************************************
This file is part of ShoX and was created by the Orcos Team.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net
and the Orcos developers as defined under http://orcos.cs.upb.de
********************************************************************************/
package br.ufla.dcc.grubix.simulator.random;

import java.util.List;
import java.util.ListIterator;


/**
 * Utility methods for randomization.
 * 
 * @author dmeister
 *
 */
public class RandomUtil {
	
	/**
	 * Private constructor to prevent instances.
	 *
	 */
	private RandomUtil() {
		// prevent instances
	}
	
	/**
	 * shuffles the list in-place using the random generator.
	 * 
	 * @param list list to shuffle
	 * @param random random source
	 */
	@SuppressWarnings("unchecked")
	public static void shuffle(List<?> list, RandomGenerator random) {
		Object[] array = list.toArray();
		int size = array.length;
		
        // Shuffle array
        for (int i = size; i > 1; i--) {
        	swap(array, i - 1, random.nextInt(i));        	
        }

        // Dump array back into list
        ListIterator it = list.listIterator();
        for (int i = 0; i < array.length; i++) {
            it.next();
            it.set(array[i]);
        }
	}
	
	/**
	 * swaps the elements using an temporary variable.
	 * 
	 * @param array array
	 * @param source swap element 1
	 * @param destination swap element 2
	 */
	private static void swap(Object[] array, int source, int destination) {
		Object tmp = array[source];
		array[source] = array[destination];
		array[destination] = tmp;
	}
}
