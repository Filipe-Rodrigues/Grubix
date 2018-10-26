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
package br.ufla.dcc.grubix.xml;

import org.jdom.Element;

/**
 * Interface for factory for configurable classes. Instances of the type can be
 * created e.g. by calling {@link ConfigurationParameter#getFactory(Class)}
 * 
 * Usage: <code>
 * ConfigurableFactory<Configurable> factory = ConfigurationParameter.getFactory(cls);
 * factory.putConfiguration("fieldname","configuredvalue");
 * Configurable configurable = factory.newInstance();
 * </code>
 * 
 * Used by the ConfigurationReader to create the
 * {@link br.ufla.dcc.grubix.simulator.kernel.Configuration} object tree.
 * 
 * @author dmeister
 * 
 * @param <T>
 *            Subclass of Configurable to create using this factory
 */
public interface ConfigurableFactory<T extends Configurable> {

	/**
	 * Adds a new Shox parameter configuration value. The key must match to a
	 * field in the {@link ConfigurableFactory#getInstanceClass()} class. The
	 * field can also be private. Implementations may get access to field be
	 * calling {@link java.lang.reflect.Field#setAccessible(boolean)} to true.
	 * 
	 * @param key
	 *            field to configure
	 * @param value
	 *            value to bind. This can be an assignment compatible value, a
	 *            string that can be parsed to an assignment compatible value,
	 *            an factory of an assignment compatible class.
	 * @exception ConfigurationException
	 *                thrown if key and value are no valid configuration
	 */
	void putConfiguration(String key, Object value)
			throws ConfigurationException;
	
	void putConfiguration(String key, Object value, int index)
		throws ConfigurationException;

	/**
	 * Constucts a new instance of T. All required parameter must be set. There
	 * must be a default constructor which can also be private.
	 * 
	 * @return a new instance which all configured fields set.
	 * @exception ConfigurationException
	 *                exception thrown when the configuration failed
	 */
	T newInstance() throws ConfigurationException;

	/**
	 * Returns the class of the constructed instance.
	 * 
	 * @return the class object of the type contructed by this factory
	 */
	Class<? extends T> getInstanceClass();

	/**
	 * Returns the configuration as an JDOM Element.
	 * 
	 * @param xmlTag
	 *            the name of the surrounding xml tag to store the values in.
	 * @param internal
	 *            true, if the factory is nested in another factory, false,
	 *            otherwise
	 * @return the configuration as JDOM Element.
	 * @exception ConfigurationException
	 *                exception thrown when the configuration failed
	 */
	Element saveConfigToXML(String xmlTag, boolean internal) throws ConfigurationException;

	/**
	 * Gets the value stored w.r.t. the given key.
	 * 
	 * @param key
	 *            the key
	 * @throws ConfigurationException
	 *             exception thrown when the configuration failed
	 * @return Object the object stored to the corresponding key.
	 */
	Object getValue(String key) throws ConfigurationException;
	
	/**
	 * Gets the value stored with the given key.
	 * 
	 * @param key the key
	 * @param defaultValue value returned if there is no value for the given key
	 * @return Object the object stored to the corresponding key.
	 * 
	 * @see br.ufla.dcc.grubix.xml.ConfigurableFactory#getValue(java.lang.String)
	 */
	Object getValue(String key, Object defaultValue);
	
	/**
	 * Checks if the configuration is valid and all required fields are set.
	 * @return boolean true, if the configuration is valid and false otherwise
	 */
	boolean isValid(); 
	
	/**
	 * Removes the field identified by the key from the factory.
	 * Needed to remove items from array fields, because items does not get overwritten
	 * when putting a new item with the same key. So once a item would be in the factory 
	 * it would not be possible to delete it again.  
	 * 
	 * It only removes the item if it is a array type.
	 * 
	 * @param key identifies the configuration item which should be deleted.
	 * @throws ConfigurationException in case an error occurs.
	 */
	void removeArrayConfiguration(String key) throws ConfigurationException;
}
