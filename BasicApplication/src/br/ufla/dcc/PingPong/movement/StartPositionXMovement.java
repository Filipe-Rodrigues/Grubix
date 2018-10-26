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

package br.ufla.dcc.PingPong.movement;


import java.util.Iterator;
import java.util.Vector;

import br.ufla.dcc.grubix.simulator.Position;
import br.ufla.dcc.grubix.simulator.kernel.Configuration;
import br.ufla.dcc.grubix.simulator.node.Node;
import br.ufla.dcc.grubix.xml.ConfigurationException;





/**
 * This class generates start positions to the nodes at the bottom or at the upper part of the 
 * scene.
 * @author Edison Pignaton de Freitas, Tales Heimfarth
 */
public class StartPositionXMovement extends StartPositionGenerator {
	
	/**
	 * xSize (from the Configuration).
	 */
	private double xSize;
	
	/**
	 * ySize (from the Configuration).
	 */
	private double ySize;
	
	/**
	 * startSide defines from which side the node will start.
	 * true = right
	 * false = left
	 * default = true
	 */
	private boolean startSide = true; 
	
	private Vector<Position> posicoes = new Vector<Position>();
	
   public void set_startSide(boolean b){
	   startSide = b;
   }
	
	/**
	 * {@inheritDoc}
	 */
	public void initConfiguration(Configuration config) throws ConfigurationException {
		super.initConfiguration(config);
		xSize = config.getXSize();
		ySize = config.getYSize();

	}
	
	/**
	 * @see br.ufla.dcc.grubix.simulator.movement.StartPositionGenerator.
	 * @param node The node for which the new position should be generated.
	 * @return A new position for a node
	 */
	@Override
	public final Position newPosition(Node node) {
		double x = 0;
		double y = 0;
	//	System.out.print("passou por aqui");
	   		if(node.getId().asInt()==0){
				if (startSide == true){
					x = xSize; 
					y = Math.random()*ySize; 
				}
				else{
					x = 0;
					y = Math.random()*ySize;
				}
	   		}else{
	   			do{
		   			if (startSide == true){
						x = xSize; 
						y = Math.random()*ySize; 
					}
					else{
						x = 0;
						y = Math.random()*ySize;
					}
	   			}while(distanceToNearst(x,y)<((Configuration.getInstance().getXSize()/Configuration.getInstance().getNodeCount()))*0.8);
	   		}
	   			
		 Position realPos = new Position(x, y);
		 posicoes.add(realPos);
		return realPos;
	}
	
	private double distanceToNearst(double x, double y){
		double dis = 0;
		Position pnew = new Position(x,y);
		Iterator<Position> iter = null;
        iter = posicoes.iterator();
		
        double menor = 10000.0;
		while (iter.hasNext()){
			Position p = iter.next();
			if (Position.getDistance(p, pnew)< menor){
				menor = Position.getDistance(p, pnew);
			}
		}
		//System.out.print("passou por aqui2");
		return menor;
	}

	public double getXSize() {
		return xSize;
	}

	public void setXSize(double size) {
		xSize = size;
	}

	public double getYSize() {
		return ySize;
	}

	public void setYSize(double size) {
		ySize = size;
	}
}


