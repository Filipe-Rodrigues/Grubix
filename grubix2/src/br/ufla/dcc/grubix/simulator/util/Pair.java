package br.ufla.dcc.grubix.simulator.util;

/**
 * This should be used to store a combination of two values.
 * Using an interface like std::pair in STL of C++.
 * @author Florian Rittmeier
 *
 * @param <S> type of first component of the pair
 * @param <T> type of second component of the pair
 */
public class Pair<S, T> {
	/**
	 * first component of the pair.
	 */
	public S first;
	/**
	 * second component of the pair.
	 */
	public T second;
	
	/**
	 * Constructs a pair.
	 * @param first first component
	 * @param second second component
	 */
	public Pair(S first, T second) {
		this.first = first;
		this.second = second; 
	}

	/**
	 * Hashcode implementation using the two values.
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	/**
	 * Equals implementation.
	 * Two objects are equal if the two values are also equal.
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true; 
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Pair<S, T> other = (Pair<S, T>) obj;
		if (first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!first.equals(other.first)) {
			return false; 
		}
		if (second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!second.equals(other.second)) {
			return false;
		}
		return true;
	}	
	
	/**
	 * Returns a string representation of the pair.
	 * @return a string representation of the pair
	 */
	public String toString() {
		return "Pair: " + first + ";" + second;
	}
}
