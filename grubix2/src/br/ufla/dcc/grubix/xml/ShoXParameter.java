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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to tag fields in configurable classes that
 * can (and should) be configured in the XML configuration file.
 * ShoxParameter fields can also be configured in the GUI.
 *
 * @author dmeister
 *
 */
@Retention(value = RUNTIME)
@Target(FIELD)
public @interface ShoXParameter {
	/**
	 * Human readable description of the field.
	 * If the description isn't set, the field is configurable through the GUI.
	 */
	String description() default "";

	/**
	 * Flag that indicated if the field is required.
	 * Default is "false".
	 */
	boolean required() default false;

	/**
	 * value that should be bind when no value is configured.
	 * Can be null.
	 * Only defaultValue or defaultClass can be used. If both are used, this
	 * results in an ConfigurationException.
	 */
	String defaultValue() default "";

	/**
	 * Class that should be bind when no value is configured.
	 * Can be null, but it must be a Configurable with no required, not preconfigured value.
	 * Only defaultValue or defaultClass can be used. If both are used, this
	 * results in an ConfigurationException.
	 */
	Class<? extends Configurable> defaultClass() default Configurable.class;

	/**
	 * optional name of the parameter.
	 * Used in the GUI. If not set, the field name is used.
	 * @return
	 */
	String name() default "";
}
