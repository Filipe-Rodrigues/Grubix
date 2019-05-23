package br.ufla.dcc.PingPong.routing.EXMac;

public class Pair<L extends Comparable<L>, R> implements Comparable<Pair<L, R>> {

	private L left;
	private R right;
	
	public Pair(L left, R right) {
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
	public int compareTo(Pair<L, R> other) {
		return this.left.compareTo(other.left);
	}

}
