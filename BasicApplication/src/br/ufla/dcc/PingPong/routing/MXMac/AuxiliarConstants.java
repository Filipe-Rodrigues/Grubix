package br.ufla.dcc.PingPong.routing.MXMac;

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;

public class AuxiliarConstants {

	public static final Position LEFT = new Position(-1, 0);
	public static final Position RIGHT = new Position(1, 0);
	public static final Position UP = new Position(0, -1);
	public static final Position DOWN = new Position(0, 1);
	public static final Position ORIGIN = new Position(0, 0);
	public static final double MAX_X = 500;
	public static final double MAX_Y = 500;
	public static final double MEAN_HOP_DISTANCE = 35.64d;
	public static final double MEAN_PREAMBLE_COUNT_NORMAL = 24;
	public static final double MEAN_PREAMBLE_COUNT_BACKBONE = 5;

}
