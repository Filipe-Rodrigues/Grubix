package br.ufla.dcc.PingPong.routing.MXMac;

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.kernel.SimulationManager;

public class AuxiliarConstants {

	public static final Position LEFT = new Position(-1, 0);
	public static final Position RIGHT = new Position(1, 0);
	public static final Position UP = new Position(0, -1);
	public static final Position DOWN = new Position(0, 1);
	public static final Position ORIGIN = new Position(0, 0);
	private static double MAX_X = -1;
	private static double MAX_Y = -1;
	public static final double MEAN_HOP_DISTANCE = 35.64d;
	public static final double MEAN_PREAMBLE_COUNT_NORMAL = 24;
	public static final double MEAN_PREAMBLE_COUNT_BACKBONE = 5;

	public static double MAX_X() {
		if (MAX_X == -1) {
			MAX_X = Configuration.getInstance().getXSize();
		}
		return MAX_X;
	}
	
	public static double MAX_Y() {
		if (MAX_Y == -1) {
			MAX_Y = Configuration.getInstance().getYSize();
		}
		return MAX_Y;
	}
}
