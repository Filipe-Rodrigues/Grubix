/********************************************************************************
This file is part of ShoX and was created by the Orcos Team.

ShoX is free software; you can redistribute it and/or modify it under the terms
of the GNU General Public License as published by the Free Software Foundation;
either version 2 of the License, or (at your option) any later version.

ShoX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
ShoX; if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
Fifth Floor, Boston, MA 02110-1301, USA

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net
and the Orcos developers as defined under http://orcos.cs.upb.de
********************************************************************************/
package br.ufla.dcc.grubix.simulator.node;

/**
 * Marker instance for the network layer state.
 * 
 * It has to be assured that a network layer that uses a LayerState which implements this interface 
 * is a backbone routing algorithm. Additionally it has to support the destination NodeId.BACKBONE. 
 * This destination requires the algorithm to send a packet for that destination to any arbitrary 
 * backbone node (usually it should be the nearest one in order to save energy).
 * 
 * @author Thomas Kemmerich
 */
public interface BackboneRoutingSupported {
		
}

