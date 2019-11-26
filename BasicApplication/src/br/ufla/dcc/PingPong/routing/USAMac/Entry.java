package br.ufla.dcc.PingPong.routing.USAMac;

public class Entry<L extends Comparable<L>, R> implements Comparable<Entry<L, R>> {

	private L left;
	private R right;
	
	public Entry(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public void setLeft(L left) {
		this.left = left;
	}

	public R getRight() {
		return right;
	}

	public void setRight(R right) {
		this.right = right;
	}

	@Override
	public String toString() {
		return "Pair [left=" + left + ", right=" + right + "]";
	}

	@Override
	public int compareTo(Entry<L, R> other) {
		return this.left.compareTo(other.left);
	}

}
