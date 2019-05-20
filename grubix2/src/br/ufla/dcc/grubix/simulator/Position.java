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

package br.ufla.dcc.grubix.simulator;

import java.io.Serializable;

/** 
 * Class to store POSITION of a node.
 * 
 * This class should be immutable since it transmitted inside packets without cloning.
 * 
 * @author Andreas Kumlehn
 */
public class Position implements Serializable {
	
	/** XCoordinate of the POSITION. */
	private final double xCoord;
	
	/** YCoordinate of the POSITION. */
	private final double yCoord;
	
	/**
	 * Constructor of the class Position.
	 * 
	 * @param xCoord X coordinate of the POSITION
	 * @param yCoord Y coordinate of the POSITION
	 */
	public Position(double xCoord, double yCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
	}

	/** @return Returns the xCoord. */
	public final double getXCoord() {
		return xCoord;
	}

	/** @return Returns the yCoord. */
	public final double getYCoord() {
		return yCoord;
	}
	
	/** @return Returns the sum of the distance vectors*/
	public final Position sum(Position pos) {
		return new Position(xCoord + pos.xCoord, yCoord + pos.yCoord);
	}
	
	/** @return Returns the multiplication between the coordinates of the distance vectors*/
	public final Position mult(Position pos) {
		return new Position(xCoord * pos.xCoord, yCoord * pos.yCoord);
	}
	
	/**
	 * Method to calculate the euclidean distance between an instance
	 * and another given position.
	 * 
	 * @param pos The other position.
	 * @return Euclidean distance between the two positions.
	 */
	public final double getDistance(Position pos) {
		return Position.getDistance(this, pos);
	}
	
	/**
	 * Method to calculate the sqaured euclidean distance between an instance
	 * and another given position.
	 * 
	 * @param pos The other position.
	 * @return Squared Euclidean distance between the two positions.
	 */
	public final double getSquaredDistance(Position pos) {
		return Position.getSquaredDistance(this, pos);
	}
	
	/**
	 * Method to calculate the position half the direct way between two positions.
	 * 
	 * @param pos The (other) Position.
	 * @return Position half the way between the two given positions.
	 */
	public Position getHalfWayPosition(Position pos) {		
		return Position.getHalfWayPosition(this, pos);
	}
	
	/**
	 * Static method to calculate the euclidean distance between two positions.
	 * 
	 * @param pos1 First POSITION.
	 * @param pos2 Second POSITION.
	 * @return Euclidean distance between the two given positions.
	 */
	public static double getDistance(Position pos1, Position pos2) {
		double deltaXsquared = Math.pow(pos1.xCoord - pos2.xCoord, 2);
		double deltaYsquared = Math.pow(pos1.yCoord - pos2.yCoord, 2);
		return Math.sqrt(deltaXsquared + deltaYsquared);
	}
	
	/**
	 * Static method to calculate the squared euclidean distance between two positions.
	 * 
	 * @param pos1 First POSITION.
	 * @param pos2 Second POSITION.
	 * @return Squared Euclidean distance between the two given positions.
	 */
	public static double getSquaredDistance(Position pos1, Position pos2) {
		double deltaXsquared = (pos1.xCoord - pos2.xCoord) * (pos1.xCoord - pos2.xCoord);
		double deltaYsquared = (pos1.yCoord - pos2.yCoord) * (pos1.yCoord - pos2.yCoord);
		return deltaXsquared + deltaYsquared;
	}
	
	/**
	 * Static method to calculate the position half the direct way between two positions.
	 * 
	 * @param pos1 First Position.
	 * @param pos2 Second Position.
	 * @return Position half the way between the two given positions.
	 */
	public static Position getHalfWayPosition(Position pos1, Position pos2) {
		double x1, y1, x2, y2, hwX, hwY;
		
		x1 = pos1.getXCoord();
		y1 = pos1.getYCoord();
		x2 = pos2.getXCoord();
		y2 = pos2.getYCoord();
		
		hwX = (x1 + x2) / 2;
		hwY = (y1 + y2) / 2;
		
		return new Position(hwX, hwY);
	}
	
	/**
	 * This method computes the angle between the two vectors u and v represented by the positions
	 * (this, positionTarget) and (this, positionTest).
	 * The angle is computed counter-clockwise to u.
	 * 
	 * @param positionTarget The other end position of u.
	 * @param positionTest The other end position of v.
	 * @return The angle alpha between u and v as 0 <= alpha < 2*PI
	 */
	public double computeAngle(Position positionTarget, Position positionTest) {
		return Position.computeAngle(this, positionTarget, positionTest);
	}
	
	/**
	 * This method computes the angle between the two vectors u and v represented by the positions
	 * (positionSource, positionTarget) and (positionSource, positionTest).
	 * The angle is computed counter-clockwise to u.
	 * 
	 * @param positionSource The common end position of u and v.
	 * @param positionTarget The other end position of u.
	 * @param positionTest The other end position of v.
	 * @return the angle alpha between u and v as 0 <= alpha < 2*PI
	 */
	public static double computeAngle(Position positionSource, Position positionTarget, Position positionTest) {
		double targetNodeX, targetNodeY, testNodeX, testNodeY;
		double myX, myY, uX, uY, vX, vY;
		double alpha, cosinusAlpha, cosinusAlphaNumerator, cosinusAlphaDenominator;
		
		targetNodeX = positionTarget.getXCoord();
		targetNodeY = positionTarget.getYCoord();
		testNodeX = positionTest.getXCoord();
		testNodeY = positionTest.getYCoord();
		myX = positionSource.getXCoord();
		myY = positionSource.getYCoord();
		
		uX = targetNodeX - myX;
		uY = targetNodeY - myY;
		
		vX = testNodeX - myX;
		vY = testNodeY - myY;
		
		cosinusAlphaNumerator = ((uX * vX) + (uY * vY));
		cosinusAlphaDenominator = Math.sqrt((((uX * uX) + (uY * uY))) * (((vX * vX) + (vY * vY))));
		
		cosinusAlpha = cosinusAlphaNumerator / cosinusAlphaDenominator;
		
		alpha = Math.acos(cosinusAlpha);
		
		if (((uX * vY) - (vX * uY)) < 0) {
			alpha = (2 * Math.PI) - alpha;
		}
		
		return alpha;
	}
	
	/**
	 * Compares to Position objects.
	 * @param obj The position to be compared to this position
	 * @return True, iff <code>obj</code> is of type Position and both 
	 * x- and y- coordinate are identical
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof Position)) {
			return false;
		}
		
		Position pos = (Position) obj;
		if ((this.xCoord == pos.xCoord) 
				&& (this.yCoord == pos.yCoord)) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return A String representation of this position in the form (x, y).
	 */
	public String toString() {
		return "(" + xCoord + ", " + yCoord + ")";
	}

	/**
	 * implements the hashcode method.
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(xCoord);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yCoord);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
}
