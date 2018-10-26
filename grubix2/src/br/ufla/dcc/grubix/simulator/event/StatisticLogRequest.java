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

package br.ufla.dcc.grubix.simulator.event;

import br.ufla.dcc.grubix.simulator.Address;

/**
 * This class represents a request to log a statistic. User-made classes can use this type of
 * wakeup call to get conveniently notified that they have to log a certain statistic. The
 * particular statistic to log when receiving this request is indicated by a type index. 
 * @author jlsx
 */
public class StatisticLogRequest extends WakeUpCall {

	/** The particular statistic that is to be logged. */
	private int statisticType;
	
	/**
	 * Constructs a wakeup call for a particular type of statistics.
	 * @param sender Sender of the WakeUpCall.
	 * @param delay Wanted delay until callback.
	 * @param statisticType The particular statistic that is to be logged
	 */
	public StatisticLogRequest(Address sender, double delay, int statisticType) {
		super(sender, delay);
		this.statisticType = statisticType;
	}

	/**
	 * @return The particular statistic that is to be logged.
	 */
	public int getStatisticType() {
		return this.statisticType;
	}
}
