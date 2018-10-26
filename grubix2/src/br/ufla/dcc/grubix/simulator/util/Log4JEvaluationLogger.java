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
package br.ufla.dcc.grubix.simulator.util;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import br.ufla.dcc.grubix.simulator.SimulationFailedException;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;



/**
 * logger to log data used for the evaluation of simulation runs.
 * The logger writes a comma-separated file with strings enclosed in "-characters. 
 * The xml logger or other types of ShoxLogger, nor log4j are really suitable for this purpose.
 * 
 * It is assumed that this logger is essential for the simulation run. When an IO error,
 * occurs a {@link SimulationFailedException} will be thrown.
 * 
 * @author dmeister
 *
 */
public class Log4JEvaluationLogger extends EvaluationLogger {

	/**
	 * name of the logger.
	 * Used by the Shox config system.
	 */
	@ShoXParameter(description = "name of the logger", required = true)
	private String loggerName;
	
	/**
	 * name of the priority/log level.
	 * Used by the Shox config system.
	 */
	@ShoXParameter(description = "name of the priority", defaultValue = "ERROR")
	private String priorityName;

	/**
	 * logger instance.
	 */
	private Logger logger;

	/**
	 * priority.
	 */
	private Level prio;

	/**
	 * Constructor.
	 * 
	 * @param logger log4j logger
	 * @param header zero or more column headers. If there are not header titles, no header
	 * line will be written.
	 */
	public Log4JEvaluationLogger(Logger logger, String... header) {
		this(logger, Level.ERROR, header);
	}
	
	/**
	 * Shox-Configuration constructor.
	 */
	protected Log4JEvaluationLogger() {
	}
	
	/**
	 * Package protected Constructor.
	 * 
	 * @param logger log4j logger
	 * @param prio priority
	 * @param header zero or more column headers. If there are not header titles, no header
	 * line will be written.
	 */
	Log4JEvaluationLogger(Logger logger, Level prio, String... header) {
		super(header);
		this.logger = logger;
		this.prio = prio;
	}

	/**
	 * @see br.ufla.dcc.grubix.simulator.util.EvaluationLogger#init()
	 * @throws ConfigurationException thrown if priority name is invalid
	 */
	@Override
	public void init() throws ConfigurationException {
		super.init();
		if (logger == null && loggerName != null) {
			logger = Logger.getLogger(loggerName);
		}
		if (prio == null && priorityName != null) {
			prio = Level.toLevel(priorityName, null);
			if (prio == null) {
				throw new ConfigurationException("invalid priority");
			}
		}
	}

	/**
	 * write the actual statistical values to the file.
	 * 
	 * @param values data values
	 */
	public void write(Object...values) {
		if (!isHeaderWritten()) {
			writeHeader();
		}
		String[] columnHeader = getHeader();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			Object value = values[i];
			if (value == null) {
				value = "";
			}
			builder.append(columnHeader[i]);
			builder.append("=");
			builder.append(value);
			builder.append(",");
		}
		builder.setLength(builder.length() - 1);

		write(builder.toString());

	}
	
	/**
	 * writes the message to the logger.
	 * @param message message to write.
	 */
	protected void write(String message) {
		logger.log(prio, message);
	}
	
	/**
	 * writes the header to the file.
	 * @param header
	 * @throws IOException
	 */
	protected void writeHeader() {
		// do nothing
	}
}
