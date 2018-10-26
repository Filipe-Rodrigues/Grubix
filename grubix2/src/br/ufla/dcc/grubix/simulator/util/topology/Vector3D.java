package br.ufla.dcc.grubix.simulator.util.topology;

public class Vector3D {
	private double x;
	private double y;
	private double z;
	
	public Vector3D(double x, double y, double z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}
	
	public void add (Vector3D toAdd) {
		this.x += toAdd.x;
		this.y += toAdd.y;
		this.z += toAdd.z;
	}
	
	
	
}
