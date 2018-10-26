package br.ufla.dcc.grubix.debug.compactlogging;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.xml.sax.InputSource;

/**
 * Class used to specify the location of a compact log file. It is possible to read history or statistics log content. 
 * 
 * @author mika
 *
 */
public class CompactLogInputSource extends InputSource {

	/**
	 * Type of contant which can be found in the compact format log file specified by this InputSource.
	 */
	private int logContentType;
	/**
	 * ID of the ShoX simulator used to create the specified compact format log file. 
	 */
	private String simulatorId;
	
	/**
	 * Constant representing a ShoX history compact format log file.
	 */
	public static final int HISTORY = 0;
	/**
	 * Constant representing a ShoX statistic compact format log file.
	 */
	public static final int STATISTICS = 1;
	
	/**
	 * Constructor to define the location and type of compact log format log file.
	 * 
	 * @param systemId prefix of the filename, missing simulator Id and filename extension
	 * @param logContentType {@link #HISTORY} or {@link #STATISTICS} constant
	 * @param simulatorId simulator ID which was used to create the log file
	 */
	public CompactLogInputSource(String systemId, int logContentType, String simulatorId) {
		
		
		if (simulatorId == null) {
			simulatorId = "";
			if (systemId.endsWith(CompactFileLogger.FILENAME_EXTENSION)) {
				systemId = 
					systemId.substring(0, systemId.lastIndexOf(CompactFileLogger.FILENAME_EXTENSION));
			}
		}
			
			if (logContentType == HISTORY || logContentType == STATISTICS) {
				this.logContentType = logContentType;	
				this.simulatorId = simulatorId;
				this.setSystemId(systemId);
				try {
					this.setCharacterStream(new FileReader(systemId + simulatorId 
															+ CompactFileLogger.FILENAME_EXTENSION));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				throw new IllegalArgumentException("History or statistic log content readback supported only");
			}
		
	}
	
	/**
	 * Returns the filename of the history file with attached {@link #simulatorId}.
	 * @return filename of the history file with attached {@link #simulatorId}
	 */
	public String getHistoryFilenamePrefix() {
		if (logContentType == HISTORY) {
			return  this.getSystemId() + simulatorId;
		} else {
			return null;
		}
	}
	/**
	 * Returns the <code>HISTORY</code> or <code>STATISTICS</code> constant determinating the contant of this log file.
	 * @return <code>HISTORY</code> or <code>STATISTICS</code> int constant
	 */
	public int getLogContentType() {
		return logContentType;
	}
	
	/**
	 * Returns the ID of the simulator which was used to generate the log file specified by this InputSource.
	 * @return String representing the simulator ID used to write this file
	 */
	public String getSimulatorId() {
		return simulatorId;
	}
	
	/**
	 * Returns <code>true</code> if content of the log file specified by this InputSource is simulator history.
	 * @return <code>true</code> if content of the log file specified by this InputSource is simulator history 
	 */
	public boolean isHistory() {
		return logContentType == HISTORY;
	}
	
	/**
	 * Returns <code>true</code> if content of the log file specified by this InputSource is simulator statistics.
	 * @return <code>true</code> if content of the log file specified by this InputSource is simulator statistics
	 */
	public boolean isStatistics() {
		return logContentType == STATISTICS;
	}
}
