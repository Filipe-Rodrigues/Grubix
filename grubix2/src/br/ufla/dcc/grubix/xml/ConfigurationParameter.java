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

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class used the contruct and configure object of the type
 * {@link Configurable}.
 * Objects of this type are created using the {@link ConfigurationParameter#extractParameter(Class)} method.
 *
 * @author dmeister
 *
 */
public class ConfigurationParameter {

	/**
	 * name of the parameter.
	 */
	private final String name;

	/**
	 * class of the parameter type.
	 */
	@SuppressWarnings("unchecked")
	private final Class typeClass;

	/**
	 * description of the parameter.
	 */
	private final String description;

	/**
	 * flag if the parameter is required.
	 */
	private final boolean required;
	
	/**
	 * default value.
	 */
	private final Object defaultValue;
	
	/**
	 * default class.
	 */
	@SuppressWarnings("unchecked")
	private final Class defaultClass;

	/**
	 * Private constructor.
	 *  Please construct objects using the factory method {@link ConfigurationParameter#extractParameter(Class)}.
	 * @param name name of the parameter aka name of the field
	 * @param typeClass type of the parameter
	 * @param description human readable description of the parameter
	 * @param required flag if the parameter is required
	 * @param defaultValue default value for the parameter (if it is the empty string, null is used)
	 * @param defaultClass default class for the paraemter (if it is the Configurable interface, null is used)
	 */
	@SuppressWarnings("unchecked")
	private ConfigurationParameter(String name, Class typeClass,
			String description, boolean required, Object defaultValue, Class defaultClass) {
		super();
		Class revisedDefaultClass = defaultClass;
		Object revisedDefaultValue = defaultValue;
		if (revisedDefaultClass.equals(Configurable.class)) {
			revisedDefaultClass = null;
		}
		if (revisedDefaultValue == null || revisedDefaultValue.equals("")) {
			revisedDefaultValue = null;
		}
		this.name = name;
		this.typeClass = typeClass;
		this.description = description;
		this.required = required;
		this.defaultClass = revisedDefaultClass;
		this.defaultValue = revisedDefaultValue;
	}

	/**
	 * returns a human readable description.
	 * May be null if the parameter should not be displayed in the Configuration user interface.
	 *
	 * @see ShoXParameter#description()
	 * @return human readable description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * returns the name of the parameter.
	 * Implementation note: The name is equal the the name of the field. This may change in the future to allow
	 * a more flexibel configuration user interface.
	 *
	 * @return the name of the parameter (not null)
	 */
	public String getName() {
		return name;
	}

	/**
	 * returns the declared class of the type.
	 * @return class of the type (not null)
	 */
	@SuppressWarnings("unchecked")
	public Class getType() {
		return typeClass;
	}

	/**
	 * return the Java type name of the parameter.
	 * Implemetnation note: At the moment all Configurable object types are displayed as java.lang.Object to
	 * be compatibel with the old implemntation of the paramter system. This may be changes later to
	 * allow a more describtive user interface.
	 *
	 * @return one of the values (java.lang.String,java.lang.Double,java.lang.Integer,
	 * java.lang.Object)
	 */
	public String getTypeName() {
		return getTypeName(typeClass);
	}

	/**
	 * returns a flag indicating if the parameter must be set during.
	 * the configuration
	 * @return true if the parameter is required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Helper method that try to find a class with the given name.
	 * @param obj name of the class to search for
	 * @return a class or null
	 */
	@SuppressWarnings("unchecked")
	public static Class getClassByName(Object obj) {
		String className = obj.toString();
		try {
			Class cls = Class.forName(className);
			return cls;
		} catch (ClassNotFoundException e) {
			return null; // returning null is a good reaction here
		}
	}

	/**
	 * returns a suitable factory for the class with the given class name.
	 * If the class represents an {@link ConfigurableFactory} the factory a instance of the
	 * class is returned.
	 * If the class represents a {@link Configurable} a {@link DefaultConfigurableFactory} for the class
	 * is returned.
	 *
	 * @param className name of a class (not null)
	 * @return a factory (not null)
	 * @throws ConfigurationException throws if the class is neither a Configurable nor
	 * a ConfigurableFactory, if the class doesn't exits
	 */
	@SuppressWarnings("unchecked")
	public static ConfigurableFactory<? extends Configurable> getFactory(
			String className) throws ConfigurationException {
		try {
			Class cls;
			cls = Class.forName(className);
			return getFactory(cls);
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException("config error: class not found "
					+ className);
		}
	}

	/**
	 * returns a suitable factory for the class with the given class.
	 * If the class represents an {@link ConfigurableFactory} the factory a instance of the
	 * class is returned.
	 * If the class represents a {@link Configurable} a {@link DefaultConfigurableFactory} for the class
	 * is returned.

	 * @param cls class of a Configurable or a factory (not null)
	 * @return a factory (not null)
	 * @throws ConfigurationException throws if the class is neither a Configurable nor
	 * a ConfigurableFactory, if the class doesn't exits
	 */
	@SuppressWarnings("unchecked")
	public static ConfigurableFactory<? extends Configurable> getFactory(
			Class cls) throws ConfigurationException {
		try {
			if (ConfigurableFactory.class.isAssignableFrom(cls)) { // is factory
				Class<ConfigurableFactory> factoryCls = cls;
				return factoryCls.newInstance();
			} else if (Configurable.class.isAssignableFrom(cls)) { // is configurable
				return new DefaultConfigurableFactory(cls);
			} else {
				throw new ConfigurationException(
						"config error: No Configurable " + cls.getName());
			}
		} catch (InstantiationException e) {
			throw new ConfigurationException("error while creating factory for " + cls, e);
		} catch (IllegalAccessException e) {
			throw new ConfigurationException("security error while creating factory for " + cls, e);
		}
	}

	/**
	 * Extracts all shox parameter from a class.
	 *
	 * @param cls class that implements Configurable
	 * @return a list of all parameters in the class. Returns an empty list there are none.
	 */
	public static List<ConfigurationParameter> extractParameter(
			Class<? extends Configurable> cls) {
		List<ConfigurationParameter> paramList = new LinkedList<ConfigurationParameter>();

		List<Field> fields = getFields(cls);
		for (Field field : fields) {
			ShoXParameter parameterAnnotation = field
			.getAnnotation(ShoXParameter.class);

			ConfigurationParameter parameter = new ConfigurationParameter(
					getFieldName(parameterAnnotation, field), field.getType(), parameterAnnotation
					.description(), parameterAnnotation.required(), parameterAnnotation.defaultValue(), 
					parameterAnnotation.defaultClass());
			paramList.add(parameter);
		}
		return paramList;
	}

	/**
	 * Private method to get the name of a field.
	 * Implementation note: Currently it takes the name of the field. May use the name property of the
	 * parameter in later versions.
	 *
	 * @param parameter shox parameter
	 * @param field field of the shox parameter
	 * @return name of the field (not null)
	 */
	private static String getFieldName(ShoXParameter parameter, Field field) {
		return field.getName();
	}

	/**
	 * Private method to transform the type name so that
	 * the type names become compatible with the shox system.
	 *
	 * Implemetnation note: At the moment all Configurable object types are displayed as java.lang.Object to
	 * be compatibel with the old implemntation of the paramter system. This may be changes later to
	 * allow a more describtive user interface.
	 *
	 * @param cls class to get the name from.
	 * @return one of the values (java.lang.String,java.lang.Double,java.lang.Integer,
	 * java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	private static String getTypeName(Class cls) {
		if (cls.equals(int.class)) {
			return Integer.class.getName();
		}
		if (cls.equals(double.class)) {
			return Double.class.getName();
		}
		if (cls.equals(String.class)) {
			return String.class.getName();
		}
		return Object.class.getName();
	}

	/**
	 * get the Field object if the field with fieldName has the
	 * ShoxParamter annotation.
	 * The field can be private and in any superclass of the given class.
	 *
	 * @param cls class with the file
	 * @param fieldName name of the field
	 * @return a Field object
	 * @throws ConfigurationException thrown when the field with the name
	 * has not the annotation or if there is no such field
	 */
	@SuppressWarnings("unchecked")
	public static Field getField(Class cls, String fieldName)
	throws ConfigurationException {
		try {
			Field field = cls.getDeclaredField(fieldName);
			ShoXParameter parameter = field.getAnnotation(ShoXParameter.class);
			if (parameter == null) {
				throw new ConfigurationException("field " + fieldName);
			}
			return field;
		} catch (NoSuchFieldException e) {
			Class superClass = cls.getSuperclass();
			if (superClass == null) {
				throw new ConfigurationException("no such field " + fieldName);
			}
			return getField(superClass, fieldName);
		}
	}

	/**
	 * returns a list of Field objects with all field with the ShoxParameter.
	 * annotation in the given class and its superclasses.
	 *
	 * @param cls Class to search for fields
	 * @return a list of Field with ShoxParameter annotation
	 */
	@SuppressWarnings("unchecked")
	public static List<Field> getFields(Class cls) {
		List<Field> fields = new LinkedList<Field>();
		Class currentClass = cls;
		while (currentClass != null) {
			for (Field field : currentClass.getDeclaredFields()) {
				ShoXParameter parameter = field
				.getAnnotation(ShoXParameter.class);
				if (parameter == null) {
					continue;
				}
				fields.add(field);
			}
			currentClass = currentClass.getSuperclass();
		}
		return fields;
	}

	/**
	 * gets defaultClass.
	 * @return current defaultClass
	 */
	@SuppressWarnings("unchecked")
	public Class getDefaultClass() {
		return defaultClass;
	}

	/**
	 * gets defaultValue.
	 * @return current defaultValue
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

}
