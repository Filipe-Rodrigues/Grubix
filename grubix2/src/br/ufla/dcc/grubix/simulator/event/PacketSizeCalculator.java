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

Copyright 2007 The ShoX developers as defined under http://shox.sourceforge.net and
the Orcos developers as defined under http://orcos.cs.upb.de
********************************************************************************/


package br.ufla.dcc.grubix.simulator.event;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.Address;
import br.ufla.dcc.grubix.simulator.NodeId;


/**
 * This class is used to calculate the size (unit: bit) of any object.
 * You might use {@link StaticPacketSize} in order to define a static packet size,
 * {@link StaticHeaderDataSize} to assume a static size for a single field or
 * {@link NoHeaderData} to mark a variable as unimportant for the size calculation.
 * Please be aware that static fields are also neglected when calculating the objects size.
 * All other variables that are not marked with {@link NoHeaderData} are used to calculate
 * the objects size.  
 *
 * @author Thomas Kemmerich
 */
public final class PacketSizeCalculator {
	
	/** Logger of the class. */
	private static final Logger LOGGER = Logger.getLogger(PacketSizeCalculator.class.getName());
	
	/**
	 * Flag used for determining an unknown field size.
	 */
	private static final int UNKOWNFIELDSIZE = -1;

	/**
	 * number of bits actually used in NodeId
	 */
	private static final int NODEID_SIZE_IN_BIT = 12;
	
	/**
	 * A cache for the fields of the classes.
	 */
	@SuppressWarnings("unchecked")
	private static HashMap<Class, List<Field>> fieldCache = new HashMap<Class, List<Field>>();
	
	
	/**
	 * Constructor.
	 */
	private PacketSizeCalculator() {
	}
	
	
	/**
	 * Calculates the size of a given packet in bits.
	 * The size is defined by all variables that are not marked using the {@link NoHeaderData} 
	 * annotation.
	 * @param packet The packet to calculate the size for.
	 * @return The calculated size in bits.
	 */
	public static int calculatePacketSize(Packet packet) {	
		int size = 0;
		if (packet != null) {
			List<Field> fields = getFields(packet.getClass());
			size = calculateSize(fields, packet);
		}
		return size;
	}
	
	/**
	 * Calculates the size of a given object in bits.
	 * The size is defined by all variables that are not marked using the {@link NoHeaderData} 
	 * annotation.
	 * @param object The object to calculate the size for.
	 * @return The calculated size in bits.
	 */
	public static int calculateObjectSize(Object object) {	
		List<Object> collection = new ArrayList<Object>();
		collection.add(object);
		return calculateCollectionSize(collection);

	}
	
	/**
	 * Returns a list of Field objects with all non-static fields that are not marked
	 * with {@link NoHeaderData}.
	 *
	 * @param cls Class to search for fields. 
	 * @return a list of non-static fields that are not marked with {@link NoHeaderData}.
	 */
	@SuppressWarnings("unchecked")
	private static List<Field> getFields(Class cls) {
		List<Field> fields = fieldCache.get(cls);
		if (fields != null) { // use the cached values
			return fields;
		}
		fields = new LinkedList<Field>();
		Class currentClass = cls;
		while (currentClass != null && currentClass != Event.class) {
			for (Field field : currentClass.getDeclaredFields()) {
				
				// we need to ignore the enclosed packet
				if (field.getName().equals("enclosedPacket")) {
					continue;
				}
				
				// keep static fields
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				
				// if the field is marked using  the NoHeaderData annotation then continue with the
				// next field.
				NoHeaderData noHeaderData = field.getAnnotation(NoHeaderData.class);
				if (noHeaderData != null) {
					continue;
				}
				
				fields.add(field);
			}
			currentClass = currentClass.getSuperclass();
		}
		fieldCache.put(cls, fields);
		return fields;
	}
	
	
	
	
	/**
	 * Calculates the size of any object in bits.
	 * If a variable is marked with an empty {@link NoHeaderData} then
	 * a standard size is returned.
	 * @param fieldList The list of fields of the object o to process.
	 * @param o The object to calculate the size for.
	 * @return the calculated size in bits.
	 */
	private static int calculateSize(List<Field> fieldList, Object o) {
		int size = 0;
		Object fieldObject = null;
		
		// if the object has a static size then use it and determine the size of the enclosed packet if not null.
		StaticPacketSize staticPacketSize = o.getClass().getAnnotation(StaticPacketSize.class);
		if (staticPacketSize != null) {
			return staticPacketSize.value();
		}
		
		// calculate the sum of the header data w.r.t. the annotations
		for (Field field : fieldList) {
			
			// if the user set a static size for this field then use it
			StaticHeaderDataSize staticHeaderDataSize = field.getAnnotation(StaticHeaderDataSize.class);
			if (staticHeaderDataSize != null) {
				size += staticHeaderDataSize.value();
				continue;
			}
			
			// the fields size is to be calculated now
			try {
				field.setAccessible(true); // disable Java language access checks.
				fieldObject = field.get(o);
				field.setAccessible(false); // re-enforce Java language access checks.
			} catch (IllegalAccessException e) {
				LOGGER.error("Failed to calculate size of a String object (" + field.toString() + ")" + e);
			}
			if (fieldObject == null) { //don't add size if object is null
				continue;
			}
			int fieldSize = getDefaultFieldSize(fieldObject); 

			if (fieldSize == UNKOWNFIELDSIZE) {
				List<Field> fields = getFields(fieldObject.getClass());
				fieldSize = calculateSize(fields, fieldObject);
			}

			size += fieldSize;
		}
		
		return size;
	}
	
	/**
	 * Returns a default size in bits depending on the passed object instance.
	 * @param fieldObject the object.
	 * @return the default size in bits.
	 */
	@SuppressWarnings("unchecked")
	private static int getDefaultFieldSize(Object fieldObject) {
		int fieldSize = 0;
		if (fieldObject instanceof Byte) {
			fieldSize = 8;
		} else if (fieldObject instanceof Integer) {
			fieldSize = 32;
		} else if(fieldObject instanceof NodeId) {
			fieldSize = NODEID_SIZE_IN_BIT;			
		} else if (fieldObject instanceof Address) {
			fieldSize = NODEID_SIZE_IN_BIT + 8; //NodeId + LayerType
		} else if (fieldObject instanceof Double) {
			fieldSize = 64;
		} else if (fieldObject instanceof Short) {
			fieldSize = 16;
		} else if (fieldObject instanceof Long) {
			fieldSize = 64;
		} else if (fieldObject instanceof Float) {
			fieldSize = 32;
		} else if (fieldObject instanceof Boolean) {
			fieldSize = 1;
		} else if (fieldObject instanceof String) {
			fieldSize = (((String) fieldObject).length() * 2 + 1) 	// Assuming UTF-16 and a terminating \0 
						* 8; // to convert from byte to bit
		} else if (fieldObject instanceof Character) {
			fieldSize = 16; // Assuming UTF-16
		} else if (fieldObject instanceof Collection) {
			fieldSize = calculateCollectionSize((Collection) fieldObject);
		} else if (fieldObject instanceof Map) {
			fieldSize = calculateMapSize((Map) fieldObject);
		} else {
			//fieldSize = calculateObjectSize(fieldObject);
			fieldSize = UNKOWNFIELDSIZE;
		}
		return fieldSize;
	}

	/**
	 * Calculates the size of a Collection object in bits.
	 * @param c The collection.
	 * @return The collections size in bits.
	 */
	@SuppressWarnings("unchecked")
	private static int calculateCollectionSize(Collection c) {
		int size = 0;

		for (Object o : c) {
			if (o == null) {
				continue;
			}
			int fieldSize = getDefaultFieldSize(o); 

			if (fieldSize == UNKOWNFIELDSIZE) {
				List<Field> fields = getFields(o.getClass());
				fieldSize = calculateSize(fields, o);
			}
			
			size += fieldSize;
		}
		return size;
	}
	
	/**
	 * Calculates the size of a Map object in bits.
	 * @param m The map.
	 * @return The maps size in bits.
	 */
	@SuppressWarnings("unchecked")
	private static int calculateMapSize(Map m) {
		return calculateCollectionSize(m.values()) + calculateCollectionSize(m.keySet());
	}
	
	
}
