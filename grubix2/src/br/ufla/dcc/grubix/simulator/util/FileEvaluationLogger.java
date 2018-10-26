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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import br.ufla.dcc.grubix.simulator.SimulationFailedException;
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
public class FileEvaluationLogger extends EvaluationLogger {

	/**
	 * filename to log to.
	 */
	@ShoXParameter(description = "filename", required = true)
	private String filename;

	/**
	 * Writer instance.
	 */
	private BufferedWriter writer;

	/**
	 * should the logger output on console?
	 */
	private boolean printConsole = false;

	/**
	 * should the logger flush after each write?
	 */
	private boolean flush = false;

	/**
	 * Constructor.
	 * 
	 * @param filename filename to write to
	 * @param header zero or more column headers. If there are not header titles, no header
	 * line will be written.
	 * @throws ConfigurationException 
	 */
	public FileEvaluationLogger(String filename, String... header) {
		super(header);
		
		this.filename = filename;
		init();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param separator the used separator of each column.
	 * @param header    zero or more column headers. If there are not header titles, no header
	 * line will be written.
	 */
	public FileEvaluationLogger(char separator, String filename, String... header) {
		super(separator, header);
		
		this.filename = filename;
		init();
	}
	
	/**
	 * Shox-Configuration constructor.
	 */
	protected FileEvaluationLogger() {
	}

	/**
	 * writes the message to the file.
	 * @param message message to write.
	 */
	@Override
	protected void write(String message) {
		try {
			writer.write(message);
			if (flush) {
				flush();
			}
		} catch (IOException e) {
			throw new SimulationFailedException("faile to write into log", e);
		}
	}


	/**
	 * Flushes the file.
	 */
	public void flush() {
		try {
			writer.flush();
		} catch (IOException e) {
			throw new SimulationFailedException("failed to flush statistic data", e);
		}
	}

	/**
	 * Closes the logger.
	 * This method will not throw an {@link SimulationFailedException}. Any
	 * errors will be catched and ignored.
	 */
	@Override
	public void close() {
		try {
			if (!super.isHeaderWritten()) {
				writeHeader();
			}
			writer.close();
		} catch (IOException e) {
			// ignore
		}
	}


	/**
	 * @return the flush
	 */
	public boolean isFlush() {
		return flush;
	}

	/**
	 * sets the flush flag.
	 * @param flush the flush to set
	 * @return this
	 */
	public FileEvaluationLogger setFlush(boolean flush) {
		this.flush = flush;
		return this;
	}

	/**
	 * @return the printConsole
	 */
	public boolean isPrintConsole() {
		return printConsole;
	}

	/**
	 * sets if the logger should print on the console.
	 * @param printConsole the printConsole to set
	 * @return this.
	 */
	public FileEvaluationLogger setPrintConsole(boolean printConsole) {
		this.printConsole = printConsole;
		return this;
	}

	/**
	 * {@inheritDoc}
	 * @see br.ufla.dcc.grubix.simulator.util.EvaluationLogger#init()
	 */
	@Override
	public void init() {
		try {
			super.init();
			this.writer = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
			throw new SimulationFailedException("failed to open statistics writer", e);
		} catch (ConfigurationException e) {
			throw new SimulationFailedException("failed to init statistics writer", e);
		}
	}
}
