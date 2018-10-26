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

import br.ufla.dcc.grubix.xml.Configurable;
import br.ufla.dcc.grubix.xml.ConfigurationException;
import br.ufla.dcc.grubix.xml.ShoXParameter;

/**
 * File logger to log data used for the evaluation of simulation runs.
 * The logger writes a comma-separated file with strings enclosed in "-characters. 
 * The xml logger or other types of ShoxLogger, nor log4j are really suitable for this purpose.
 * 
 * It is assumed that this logger is essential for the simulation run. When an IO error,
 * occurs a {@link SimulationFailedException} will be thrown.
 * 
 * @author dmeister
 *
 */
public abstract class EvaluationLogger implements Configurable {

	@ShoXParameter(description = "The separator used to separate each column.", defaultValue = ",")
	private String separator;
	
	/** @return the separator. */
	public final String getSeparator() {
		return separator;
	}

	/** @param separator the separator to set. */
	public final void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * headers.
	 */
	@ShoXParameter(description = "column header")
	private String[] header;

	/**
	 * flag is the header was written.
	 */
	private boolean headerWritten;

	/**
	 * Constructor.
	 * 
	 * @param header zero or more column headers. If there are not header titles, no header
	 * line will be written.
	 */
	public EvaluationLogger(String... header) {
		this.header = header.clone();
		this.separator = ",";
	}

	/**
	 * Constructor.
	 * 
	 * @param separator the used separator of each column.
	 * @param header    zero or more column headers. If there are not header titles, no header
	 * line will be written.
	 */
	public EvaluationLogger(char separator, String... header) {
		this.header    = header.clone();
		this.separator = Character.toString(separator);
	}

	/**
	 * writes the header to the file.
	 * @param header
	 * @throws IOException
	 */
	protected void writeHeader() {
		if (header != null && header.length > 0) {
			StringBuilder builder = new StringBuilder();
			for (Object headerColumn : header) {
				builder.append(headerColumn);
				builder.append(separator);
			}
			builder.setCharAt(builder.length() - 1, '\n');
			write(builder.toString());
		}
	}

	protected abstract void write(String message);

	/**
	 * May be overwritten.
	 *
	 */
	public void close() {
		// empty implementation
	}

	/**
	 * write the actual statistical values to the file.
	 * 
	 * @param values data values
	 */
	public void write(Object...values) {
		if (!headerWritten) {
			writeHeader();
			headerWritten = true;
		}
		StringBuilder builder = new StringBuilder();
		for (Object value : values) {
			if (value == null) {
				value = "";
			} else if (!(value instanceof Number)) {
				value = "\"" + value + "\"";
			}
			builder.append(value);
			builder.append(separator);
		}
		builder.setCharAt(builder.length() - 1, '\n');

		write(builder.toString());

	}
	
	/**
	 * {@inheritDoc}
	 * @see br.ufla.dcc.grubix.xml.Configurable#init()
	 */
	public void init() throws ConfigurationException {
		// nothing to do
	}

	/**
	 * @return the headerWritten
	 */
	protected boolean isHeaderWritten() {
		return headerWritten;
	}

	/**
	 * @return the header
	 */
	protected String[] getHeader() {
		return header.clone();
	}

}
