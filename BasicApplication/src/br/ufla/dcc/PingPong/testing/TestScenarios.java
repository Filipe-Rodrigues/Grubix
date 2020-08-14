package br.ufla.dcc.PingPong.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.ufla.dcc.grubix.simulator.Position;

@SuppressWarnings("unused")
public class TestScenarios {

	private static TestScenarios instance = null;
	private List<Integer> scene1upper = null;
	private List<Integer> scene1lower = null;
	private List<Integer> repeated = null;
	private double maxX;
	private double maxY;
	private int upperCounter = 0;
	private int lowerCounter = 0;
	private boolean upperTime = false;
	
	private TestScenarios() {
		double SQUARE_SIDE = 300d;
		scene1upper = new ArrayList<Integer>();
		scene1lower = new ArrayList<Integer>();
		repeated = new ArrayList<Integer>();
		maxX = SQUARE_SIDE;
		maxY = SQUARE_SIDE;
	}
	
	public static TestScenarios getInstance() {
		if (instance == null) {
			instance = new TestScenarios();
		}
		return instance;
	}
	
	public void evaluateTarget(int id, Position position) {
		if (id == 1) return;
		double propBound = maxY * 0.2d;
		if (position.getYCoord() < propBound) {
			scene1upper.add(id);
		} else if (position.getYCoord() > maxY - propBound) {
			scene1lower.add(id);
		}
	}
	
	private int getOrdered(boolean upper) {
		if (upper) {
			return (upperCounter < scene1upper.size()) 
					? (scene1upper.get(upperCounter++))
					: (-1);
		}
		return (lowerCounter < scene1lower.size()) 
				? (scene1lower.get(lowerCounter++))
				: (-1);
	}
	
	private int getRandom(boolean upper) {
		Random rnd = new Random();
		int idx, repCount = -1;
		List<Integer> list = (upper) ? (scene1upper) : (scene1lower);
		do {
			repCount++;
			idx = rnd.nextInt(list.size());
		} while (repeated.contains(list.get(idx)) && repCount <= 5);
		repeated.add(list.get(idx));
		return list.get(idx);
		
	}
	
	private int nextUpper() {
		upperTime = false;
		return getRandom(true);
		//return getOrdered(true);
	}
	
	private int nextLower() {
		upperTime = true;
		return getRandom(false);
		//return getOrdered(false);
	}
	
	public int next() {
		if (upperTime) {
			return nextUpper();
		}
		return nextLower();
	}
}
