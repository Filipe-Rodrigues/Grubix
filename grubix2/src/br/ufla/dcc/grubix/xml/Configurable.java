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
package br.ufla.dcc.grubix.xml;

/**
 * All classes that are loaded and configured by the XML configuration file for
 * ShoX must implement this interface. This is particularly the case with all
 * models and layers.
 * 
 * One constraint on the implementations is that they must provide a constructor
 * without parameter (the constructor can be private).
 * 
 * @author dmeister
 */
public interface Configurable {

	/**
	 * Called by the ConfigurableFactory after setting the configured parameter.
	 * 
	 * @exception ConfigurationException thrown if configuration is invalid.
	 */
	void init() throws ConfigurationException;
}
