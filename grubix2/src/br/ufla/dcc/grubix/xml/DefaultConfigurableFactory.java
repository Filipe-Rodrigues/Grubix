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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;




import org.apache.log4j.Logger;
import org.jdom.Element;

import br.ufla.dcc.grubix.simulator.kernel.Simulator;


/**
 * Creates and configure classes of the type {@link Configurable} using the meta
 * informations provided by the {@link ShoXParameter} annotation.
 * 
 * @author dmeister
 * @see ConfigurableFactory
 * @see ShoXParameter
 * @param <T>
 *            type of the class to create and configure
 */
public final class DefaultConfigurableFactory<T extends Configurable>
		implements ConfigurableFactory<T> {

	/**
	 * Logger of the class.
	 */
	private static final Logger LOGGER = Logger.getLogger(Simulator.class.getName());

	/**
	 * Holds the configuration with the field name as the key.
	 */
	private final Map<String, Object> configuration;

	/**
	 * Class object of the class to create.
	 */
	private final Class<? extends T> instanceClass;

	/**
	 * Public constructor. No configuration parameter are set in a newly created
	 * factory.
	 * 
	 * @param instanceClass
	 *            class object of the class to create (not null)
	 */
	public DefaultConfigurableFactory(Class<? extends T> instanceClass) {
		this.configuration = new LinkedHashMap<String, Object>();
		this.instanceClass = instanceClass;
	}

	/**
	 * Returns the class of the constructed instance.
	 * 
	 * @return the class object of the type constructed by this factory
	 * @see ConfigurableFactory#getInstanceClass()
	 */
	public Class<? extends T> getInstanceClass() {
		return instanceClass;
	}

	/**
	 * Constucts a new instance of T. All required parameter must be set. There
	 * must be a default constructor which can also be private.
	 * 
	 * A new instance is created by a default constructor (that may be private)
	 * 
	 * For each field that has a {@link ShoXParameter} annotation, the
	 * following steps are performed. If no value is configured the default
	 * value of the field is used. If the field class is an array, the
	 * configuration value will be an element in a new array. If the configured
	 * value is a factory, a new object will be created and bind to the field.
	 * The configured value is bind to the field.
	 * 
	 * @see DefaultConfigurableFactory#configureField(Configurable, Field)
	 * @return a new instance which all configured fields set.
	 * @throws ConfigurationException
	 *             exception thrown when the configuration failed
	 * @see ConfigurableFactory#newInstance()
	 */
	@SuppressWarnings("unchecked")
	public T newInstance() throws ConfigurationException {
		T instance;
		try {
			// create a new instance using every parameter-less constructor
			Constructor<? extends T> ctor = instanceClass
					.getDeclaredConstructor();
			ctor.setAccessible(true);
			instance = ctor.newInstance();

			for (Field field : ConfigurationParameter.getFields(instance
					.getClass())) {

				configureField(instance, field);
			}
			instance.init();
			return instance;
		} catch (InstantiationException e) {
			throw new ConfigurationException("config error", e);
		} catch (InvocationTargetException e) {
			throw new ConfigurationException("config error", e);
		} catch (NoSuchMethodException e) {
			throw new ConfigurationException("config error", e);
		} catch (IllegalAccessException e) {
			// illegal access should not happen since we set accessible to true
			throw new AssertionError("illegal access should not happen");
		}
	}

	/**
	 * configured a field of the instance. If no value is configured the default
	 * value of the field is used. If the field class is an array, the
	 * configuration value will be an element in a new array. If the configured
	 * value is a factory, a new object will be created and bind to the field.
	 * 
	 * The configured value is set to the field using reflection even if the
	 * field is private and/or a matching set method exists.
	 * 
	 * @param instance
	 *            instance to be configured
	 * @param field
	 *            field to be set
	 * @throws ConfigurationException
	 *             is thrown in case an error occurs
	 */
	@SuppressWarnings("unchecked")
	private void configureField(T instance, Field field)
			throws ConfigurationException {
		ShoXParameter parameter = field.getAnnotation(ShoXParameter.class);
		Object value = this.configuration.get(field.getName());

		if (value == null) {
			value = processDefaultValue(field, parameter);
		}
		if (value == null && parameter.required()) {
			throw new ConfigurationException("required field " + field
					+ " not set");
		}
		if (field.getType() == File.class && value instanceof String) {
			value = new File(value.toString());
		}
		// handle array
		if (field.getType().isArray()) {
			List<Object> list = (List<Object>) value;
			value = processArrayField(field, list);
		}
		// handle factory for a value
		if (value instanceof ConfigurableFactory && !isFactoryField(field)) {
			ConfigurableFactory factory = (ConfigurableFactory) value;
			value = factory.newInstance();
		}
		if (value != null) {
			field.setAccessible(true);
			try {

				field.set(instance, value);
			} catch (IllegalAccessException e) {
				// illegal access should not happen since we set accessible to
				// true
				throw new AssertionError("illegal access should not happen");
			}
		}
	}

	/**
	 * processes an field that is an Array type.
	 * 
	 * @param field
	 *            field to be configured
	 * @param list
	 *            list of configured values (may be null) If a element of the
	 *            list is a factory, a new object is created using the factory.
	 * @return an array that should be bind to the field
	 * @throws ConfigurationException
	 *             thrown if a configuration is invalid.
	 */
	@SuppressWarnings("unchecked")
	private Object processArrayField(Field field, List<Object> list)
			throws ConfigurationException {
		List<Object> values = new LinkedList<Object>();
		if (list != null) {
			for (Object item : list) {
				if (item instanceof ConfigurableFactory 
					&& !field.getType().getComponentType().equals(ConfigurableFactory.class)) {
					ConfigurableFactory factory = (ConfigurableFactory) item;
					values.add(factory.newInstance());
				} else {
					values.add(item);
				}
			}
		}
		Object[] array = (Object[]) createArray(field.getType(), values.size());
		values.toArray(array);
		return array;
	}

	/**
	 * creates an array of the given (array) type.
	 * 
	 * @param <AT>
	 *            Array type
	 * @param type
	 *            Array type class object
	 * @param size
	 *            size of the array to create
	 * @return a new array of the given type
	 */
	@SuppressWarnings("unchecked")
	private static <AT> AT createArray(Class<AT> type, int size) {
		AT array = (AT) Array.newInstance(type.getComponentType(), size);
		return array;
	}

	/**
	 * processes the default value of the parameter. It checks that one (and
	 * only one) of the annotation parameters
	 * {@link ShoXParameter#defaultClass()} and
	 * {@link ShoXParameter#defaultValue()} is set. If
	 * {@link ShoXParameter#defaultValue()} is set the value is converted and
	 * returned. If the field is a factory field and the
	 * {@link ShoXParameter#defaultClass()} is a factory is factory is returned.
	 * If the {@link ShoXParameter#defaultClass()} is a factory and the field
	 * isn't a factory, a new object is created using the factory.
	 * 
	 * @see DefaultConfigurableFactory#convertValue(Object, Class)
	 * @param field
	 *            a field (not null)
	 * @param parameter
	 *            a parameter (not null)
	 * @return null if there is no default value, the value itself if
	 *         defaultValue() is set, a new instance of a class created by a
	 *         factory if defaultClass is set.
	 * @throws ConfigurationException
	 *             exception
	 */
	private Object processDefaultValue(Field field, ShoXParameter parameter)
			throws ConfigurationException {
		if (!hasDefault(parameter)) {
			return null;
		}
		if (!parameter.defaultValue().equals("")) {
			return convertValue(parameter.defaultValue(), field.getType());
		}
		// defaultClass is set
		ConfigurableFactory<? extends Configurable> factory = ConfigurationParameter
				.getFactory(parameter.defaultClass());

		if (isFactoryField(field)) {
			return factory;
		}
		return factory.newInstance();
	}

	/**
	 * checks if the field is a factory.
	 * 
	 * @param field
	 *            field
	 * @return true if the field is a factory, otherwise false
	 */
	private boolean isFactoryField(Field field) {
		return field.getType().isAssignableFrom(ConfigurableFactory.class);
	}

	/**
	 * checks if the parameter has a default configuration.
	 * 
	 * @param parameter
	 *            a parameter
	 * @return true if it has a default configuration, false otherwise
	 * @throws ConfigurationException
	 *             thrown if both default attributes are set.
	 */
	private boolean hasDefault(ShoXParameter parameter)
			throws ConfigurationException {
		if (parameter.defaultClass().equals(Configurable.class)
				&& parameter.defaultValue().equals("")) {
			return false;
		}
		if (!parameter.defaultClass().equals(Configurable.class)
				&& !parameter.defaultValue().equals("")) {
			throw new ConfigurationException(
					"Parameter must not set both default types");
		}
		return true;
	}

	/**
	 * Adds a new Shox parameter configuration value.<br/> 1. the value is
	 * converted to a assignment compatible type if this is possible e.g. the
	 * value is a string containing a number and the field class is int.<br/>
	 * 2. if the field is an array, multiply binding to the field are allowed.
	 * 
	 * If the field is (even after the converting) not assignment compatible
	 * with the field, a {@link ConfigurationException} is thrown.
	 * 
	 * @param key
	 *            field to configure
	 * @param value
	 *            value to bind
	 * @throws ConfigurationException
	 *             thrown if key and value are no valid configuration
	 */
	
	//@Override
	public void putConfiguration(String key, Object value, int index)
			throws ConfigurationException {
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unchecked")
	public void putConfiguration(String key, Object value)
			throws ConfigurationException {
		Field field = ConfigurationParameter.getField(instanceClass, key);
		Class fieldClass = field.getType();
		Object obj = convertValue(value, fieldClass);

		if (fieldClass.isArray()) {
			List<Object> list = (List<Object>) configuration.get(key);
			if (list == null) {
				list = new LinkedList<Object>();
				configuration.put(key, list);
			}
			if (!isAssignable(obj, fieldClass.getComponentType())) {
				throw new ConfigurationException(
						"config error: not assignable (dest: " + fieldClass
								+ ",config: " + obj + ")");
			}
			list.add(obj);
		} else {
			if (!isAssignable(obj, fieldClass)) {
				throw new ConfigurationException(
						"config error: not assignable (dest: " + fieldClass
								+ ",config: " + obj + ")");
			}
			configuration.put(key, obj);
		}
	}

	/**
	 * Converts an object to a proper value. If the value is a String and the
	 * field is a number, a matching valueOf method is called. If the value if a
	 * Class, a new factory for the class is created.
	 * 
	 * @param value
	 *            value
	 * @param fieldClass
	 *            destination class
	 * @return a converted value
	 * @throws ConfigurationException
	 *             thrown when Configuration is invalid
	 */
	@SuppressWarnings("unchecked")
	private Object convertValue(Object value, Class fieldClass)
			throws ConfigurationException {
		Object obj = value;
		if (value instanceof String) {
			try {
				String valueString = (String) value;
				if (fieldClass == int.class || fieldClass == Integer.class) {
					obj = Integer.valueOf(valueString);
				}
				if (fieldClass == long.class || fieldClass == Long.class) {
					obj = Long.valueOf(valueString);
				}
				if (fieldClass == double.class || fieldClass == Double.class) {
					obj = Double.valueOf(valueString);
				}
				if (fieldClass == boolean.class || fieldClass == Boolean.class) {
					obj = Boolean.valueOf(valueString);
				}
				if (fieldClass == File.class) {
					File tmpFile = new File(value.toString());
					if (tmpFile.exists()) {
						obj = tmpFile;
					} else {
						tmpFile = new File(System.getProperty("user.dir")
								+ File.separator + value.toString());
						if (tmpFile.exists()) {
							obj = tmpFile;
						} else {
							try {
								tmpFile.createNewFile();
								obj = tmpFile;
							} catch (IOException e) {
								LOGGER.error("exception catched", e);
							}
						}
					}
				}
			} catch (NumberFormatException e) {
				throw new ConfigurationException("invalid number format", e);
			}
			if (ConfigurationParameter.getClassByName(obj) != null
					&& isAssignable(ConfigurationParameter.getClassByName(obj),
							fieldClass)) {
				return convertValue(ConfigurationParameter.getClassByName(obj),
						fieldClass);
			}
		}
		if (value instanceof Class) {
			obj = ConfigurationParameter.getFactory((Class) value);
		}

		return obj;
	}

	/**
	 * tests if an object can be assign to a class.
	 * 
	 * @param obj
	 *            object to test
	 * @param fieldClass
	 *            class
	 * @return true or false
	 */
	@SuppressWarnings("unchecked")
	private boolean isAssignable(Object obj, Class<?> fieldClass) {
		return fieldClass.isAssignableFrom(obj.getClass())
				|| obj instanceof Class && fieldClass.isAssignableFrom((Class) obj)
				|| obj instanceof Integer && fieldClass == int.class
				|| obj instanceof Double && fieldClass == double.class
				|| obj instanceof Long && fieldClass == long.class
				|| obj instanceof Boolean && fieldClass == boolean.class
				|| obj instanceof String && ConfigurationParameter.getClassByName(obj) != null
				|| obj instanceof List
				&& isAssignable(ConfigurationParameter.getClassByName(obj),
						fieldClass)
				|| obj instanceof ConfigurableFactory
				&& fieldClass.isAssignableFrom(((ConfigurableFactory) obj)
						.getInstanceClass())
				|| fieldClass == File.class
				&& obj instanceof String;
	}

	/**
	 * saves the configuration.
	 * 
	 * @param xmlTag
	 *            The XML-tag, which will surround the data, stored by this
	 *            factory.
	 * @param internal
	 *            True, if the tag configures a nested class. False, if the tag
	 *            configures a normal, not nested class.
	 * @return Element JDOM Element representing the configuration.
	 * @throws ConfigurationException
	 *             exception thrown when the configuration failed
	 * @see br.ufla.dcc.grubix.xml.ConfigurableFactory#saveConfigToXML()
	 */
	@SuppressWarnings("unchecked")
	public Element saveConfigToXML(String xmlTag, boolean internal)
			throws ConfigurationException {

		Element root = null;
		if (internal) {
			root = new Element("classparam");
		} else {
			root = new Element(xmlTag);

		}
		Element classElement = new Element("class");
		classElement.setText(instanceClass.getName());
		if (!internal) {
			root.addContent(classElement);
		}
		Element params = new Element("params");

		if (internal) {
			root.setAttribute("name", xmlTag);
			root.addContent(classElement);
			for (Entry<String, Object> entry : configuration.entrySet()) {
				if (entry.getValue() instanceof ConfigurableFactory) {

					Element newData = ((ConfigurableFactory) entry.getValue())
							.saveConfigToXML(entry.getKey(), true);
					params.addContent(newData);
				} else {
					if (entry.getValue() instanceof List) {
						LOGGER.debug("write array.");
						List tmpArray = (List) entry.getValue();
						
						for (int i = 0; i < tmpArray.size(); i++) {
							ConfigurableFactory[] cFactories;
							ConfigurableFactory cfa;
							try {
								cFactories = (ConfigurableFactory[]) tmpArray.get(i);
								for (int j = 0; j < cFactories.length; j++) {
									ConfigurableFactory cf = cFactories[j];
									Element newData = cf.saveConfigToXML(entry.getKey(), true);
									params.addContent(newData);
								}
							} catch (ClassCastException e) {
								try {
									cfa = (ConfigurableFactory) tmpArray.get(i);
									Element newData = cfa.saveConfigToXML(entry.getKey(), true);
									params.addContent(newData);
								} catch (ClassCastException e1) {
									// if it is no ConfigurableFactory nor a ConfigurableFactory[] ignore it.
									LOGGER.debug("exception catched " + e1.getMessage());
								}
							}
						}

					} else {
						Element param = new Element("param");
						param.setAttribute("name", entry.getKey());
						param.setText(entry.getValue().toString());
						params.addContent(param);
					}
				}
			}
		} else {
			for (Entry<String, Object> entry : configuration.entrySet()) {
				if (entry.getValue() instanceof ConfigurableFactory) {

					// internal configuration.
					Element newData = ((ConfigurableFactory) entry.getValue())
							.saveConfigToXML(entry.getKey(), true);
					params.addContent(newData);

				} else {

					if (entry.getValue() instanceof List) {
						LOGGER.debug("write array.");
						List tmpArray = (List) entry.getValue();
						
						for (int i = 0; i < tmpArray.size(); i++) {
							ConfigurableFactory cf = (ConfigurableFactory) tmpArray.get(i);
							Element newData = cf.saveConfigToXML(entry.getKey(), true);
							params.addContent(newData);
						}

					} else {
						Element param = new Element("param");
						param.setAttribute("name", entry.getKey());
						param.setText(entry.getValue().toString());
						params.addContent(param);
					}
				}
			}
		}
		if (params.getChildren().size() > 0) {
			root.addContent(params);
		}

		return root;
	}

	/**
	 * Gets the value stored with the given key.
	 * 
	 * @param key
	 *            the key
	 * @throws ConfigurationException
	 *             exception thrown when the configuration failed
	 * @return Object the object stored to the corresponding key.
	 * 
	 * @see br.ufla.dcc.grubix.xml.ConfigurableFactory#getValue(java.lang.String)
	 */
	public Object getValue(String key) throws ConfigurationException {
		Object obj = configuration.get(key);
		if (obj != null) {
			return configuration.get(key);
		}
		throw new ConfigurationException("no such value " + key + "!");
	}

	/**
	 * Gets the value stored with the given key.
	 * 
	 * @param key the key
	 * @param defaultValue value returned if there is no value for the given key
	 * @return Object the object stored to the corresponding key.
	 * 
	 * @see br.ufla.dcc.grubix.xml.ConfigurableFactory#getValue(java.lang.String)
	 */
	public Object getValue(String key, Object defaultValue) {
		Object obj = configuration.get(key);
		if (obj != null) {
			return configuration.get(key);
		}
		return defaultValue;
	}
	
	/**
	 * Checks if the configuration is correct and all required fields are set.
	 * 
	 * @return boolean true, if the configuration is valid and false otherwise
	 */
	public boolean isValid() {

		boolean isValid = true;

		try {
			Class<? extends Configurable> actualClass = this.getInstanceClass();
			List<ConfigurationParameter> parameter = ConfigurationParameter
					.extractParameter(actualClass);

			for (ConfigurationParameter para : parameter) {
				// if field is valid, everything is ok.
				// if field is invalid isFieldValid throws
				// ConfigurationException, the configuration is invalid
				if (isValid) {
					isValid = isFieldValid(actualClass, para.getName());
				}
			}
		} catch (ConfigurationException e) {
			isValid = false;
		}
		return isValid;
	}

	/**
	 * Checks if the configuration is valid for the given field.
	 * 
	 * @param classname
	 *            the class, which contains the field, that will be checked.
	 * @param fieldname
	 *            the name of the field to be checked.
	 * @return boolean true, if the field configuration is valid false,
	 *         otherwise
	 * @throws ConfigurationException
	 *             ConfigurationException will be thrown in case an error
	 *             occurs.
	 */
	@SuppressWarnings("unchecked")
	private boolean isFieldValid(Class classname, String fieldname)
			throws ConfigurationException {
		boolean isValid = true;
		Field field;
		field = ConfigurationParameter.getField(classname, fieldname);
		ShoXParameter parameter = field.getAnnotation(ShoXParameter.class);
		Object value = this.configuration.get(field.getName());
		String defaultValue = parameter.defaultValue();

		if (value == null && defaultValue.equals("") && parameter.required()) {
			isValid = false;
		}
		if (value != null && value instanceof String && parameter.required()
				&& ((String) value).length() == 0) {
			isValid = false;
		}
		if (value instanceof ConfigurableFactory) {
			isValid = ((ConfigurableFactory) value).isValid();
		}

		if (!isValid) {
			LOGGER.warn(field.toGenericString().substring(
					field.toGenericString().lastIndexOf(".") + 1)
					+ " required: "
					+ parameter.required()
					+ "  valid: "
					+ isValid);
			throw new ConfigurationException(field.toGenericString().substring(
					field.toGenericString().lastIndexOf(".") + 1)
					+ " required: "
					+ parameter.required()
					+ "  valid: "
					+ isValid);
		}

		return isValid;
	}
	
	/**
	 * {@inheritDoc}
	 * @see br.ufla.dcc.grubix.xml.ConfigurableFactory#removeArrayConfiguration(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public void removeArrayConfiguration(String key) throws ConfigurationException {
		Field field = ConfigurationParameter.getField(instanceClass, key);
		Class fieldClass = field.getType();
		if (fieldClass.isArray()) {
			configuration.remove(key);
		} else {
			throw new ConfigurationException("field is no array!");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "Factory [class=" + instanceClass + "]";
	}

	
}
