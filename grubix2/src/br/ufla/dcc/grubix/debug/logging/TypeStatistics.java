package br.ufla.dcc.grubix.debug.logging;

import java.util.HashMap;
import java.util.Locale;

/**
 * The purpose of this class is to save a hit counter for each Element stored. The type of the elements stored is 
 * determinated by <T>. The counter itself is a long value.
 * 
 * @author mika
 *
 * @param <T> Type of the key for each element stored
 */
public class TypeStatistics<T> {
	/**
	 * Saves the statistic counters for each T Element.
	 */
	private HashMap<T, Long> hits;
	
	/**
	 * Standard constructor.
	 */
	public TypeStatistics() {
		reset();
	}
	
	/**
	 * Resets all counters, also looses all known elements.
	 */
	public void reset() {
		hits = new HashMap<T, Long>();
	}
	
	/**
	 * Increases the counter by 1 for the specified element by the key T. If the element is new, it is added and 
	 * it's counter is set to 1.
	 * 
	 * @param key The key to determinate the element which counter is to increase
	 */
	public void hit(T key) {
		hit(key, 1L);
	}
	
	/**
	 * Increases the counter by weight for the specified element by the key T. If the element is new, it is added and
	 * it's counter is set to weight.
	 * 
	 * @param key The key to determinate the element which counter is to increase
	 * @param weight The value to add to the element which counter is to increase
	 */
	public void hit(T key, long weight) {
		if (hits.containsKey(key)) {
			long oldValue = hits.get(key);
			hits.put(key, oldValue + weight);
		} else {
			hits.put(key, weight);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("TypeStatistics :");
		for (T key : hits.keySet()) {
			buffer.append(String.format(Locale.US, "%10dx %s\n", hits.get(key), key.toString()));
		}
		return buffer.toString();
	}
}
