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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Class to write an XML file. Creates a file and appends to it line by line.
 * 
 * @author Andreas Kumlehn
 */
public class XMLWriter {

	/**
	 * Logger for the class SimulationManager.
	 */
	private static final Logger LOGGER = Logger.getLogger(XMLWriter.class);

	/**
	 * String representing the java encoding used to write the file.
	 */
	private static final String ENCODINGJ = "8859_1";

	/**
	 * String representing the XML encoding used to write the file.
	 */
	private static final String ENCODINGXML = "ISO-8859-1";

	/**
	 * Headline of the logfile as String.
	 */
	private static final String XMLHEAD = "<?xml version=\"1.0\" encoding=\""
			+ XMLWriter.ENCODINGXML + "\"?>\r\n";

	/**
	 * Used carriagereturn/Linefeed combination to write the logfile.
	 */
	private static final String RETURNLINEFEED = "\r\n";

	/**
	 * Character opening an openingtag.
	 */
	private static final String BEGINOPENTAG = "<";

	/**
	 * String opening a closingtag.
	 */
	private static final String BEGINCLOSETAG = "</";

	/**
	 * Character to end a tag.
	 */
	private static final String ENDTAG = ">";

	/**
	 * Character to end an empty tag.
	 */
	private static final String ENDEMPTYTAG = "/>";

	/**
	 * OutputStreamWriter to write onto.
	 */
	private final OutputStreamWriter out;

	/** Stores the name of the file to which this XMLWriter writes. */
	private final String fileName;

	/**
	 * Constructor of the class XMLWriter.
	 * 
	 * @param fileName
	 *            Name of the created logfile.
	 * @throws IOException
	 *             if creating or access to the file not possible.
	 */
	public XMLWriter(String fileName) throws IOException {
		this.fileName = fileName;
		OutputStream fout = new FileOutputStream(fileName);
		BufferedOutputStream bout = new BufferedOutputStream(fout);
		OutputStreamWriter outwriter = new OutputStreamWriter(bout,
				XMLWriter.ENCODINGJ);
		this.out = outwriter;
		this.out.write(XMLWriter.XMLHEAD);
	}

	/**
	 * Closes the XMLWriter stream.
	 */
	public final void close() {
		try {
			this.out.close();
		} catch (IOException e) {
			XMLWriter.LOGGER.error("Closing log file " + this.fileName
					+ " failed! " + e.getMessage());
		}
	}

	/**
	 * writes a string into the logfile.
	 * 
	 * @param buff string to write.
	 */
	private void write(String buff) {
		try {
			out.write(buff);
			out.flush();
		} catch (IOException e) {
			XMLWriter.LOGGER.error("Logging failed! " + e.getMessage());
		}
	}

	/**
	 * Method to write a tag including its value. For example <xcoord> 5
	 * </xcoord>.
	 * 
	 * @param tag tagname to write.
	 * @param value value of the tag as String.
	 */
	public final void writeTag(String tag, String value) {
		StringBuilder buff = new StringBuilder(BEGINOPENTAG);
		buff.append(tag);
		buff.append(XMLWriter.ENDTAG);
		buff.append(value);
		buff.append(XMLWriter.BEGINCLOSETAG);
		buff.append(tag);
		buff.append(XMLWriter.ENDTAG);
		buff.append(XMLWriter.RETURNLINEFEED);
		this.write(buff.toString());
	}

	/**
	 * writes a tag with attributes and content.
	 * 
	 * @param tag tagname to write
	 * @param attributes table containing the tag's attributes as (name, value) pairs
	 * @param value value of the tag as String
	 */
	public final void writeTag(String tag, Map<String, String> attributes,
			String value) {
		this.writeOpenTag(tag, attributes, false, false);
		this.write(value);
		this.closeTag(tag);
	}

	/**
	 * writes only the opening of tag. For example <xcoord>.
	 * 
	 * @param tag tagname to write.
	 */
	public final void openTag(String tag) {
		this.writeOpenTag(tag, null, false, true);
	}

	/**
	 * writes only the opening of tag, but with attributes. For example
	 * <xcoord unit="cm">.
	 * 
	 * @param tag tagname to write.
	 * @param atts table containing the tag's attributes as (name, value) pairs
	 */
	public final void openTag(String tag, Map<String, String> atts) {
		this.writeOpenTag(tag, atts, false, true);
	}

	/**
	 * Helper method for code reuse that writes an open tag with or without
	 * attributes. If empty is true, the tag is closed directly (like
	 * <xcoord/>).
	 * 
	 * @param tag tagname to write.
	 * @param atts table containing the tag's attributes as (name, value)
	 *            pairs, or null
	 * @param empty flag indicating whether or not the tag is supposed to be
	 *            without content
	 * @param endLine if true, a line feed is appended (a new line is opened)
	 */
	private void writeOpenTag(String tag, Map<String, String> atts,
			boolean empty, boolean endLine) {
		StringBuilder buff = new StringBuilder(BEGINOPENTAG);
		buff.append(tag);
		if (atts != null) {
			for (Map.Entry<String, String> entry : atts.entrySet()) {
				buff.append(" ");
				buff.append(entry.getKey());
				buff.append("=\"");
				buff.append(entry.getValue());
				buff.append("\"");
			}
		}
		if (empty) {
			buff.append(XMLWriter.ENDEMPTYTAG);
		} else {
			buff.append(XMLWriter.ENDTAG);
		}
		if (endLine) {
			buff.append(XMLWriter.RETURNLINEFEED);
		}
		this.write(buff.toString());
	}

	/**
	 * Method to write an empty tag, with or without attributes. For example
	 * <xcoord unit="cm"/>.
	 * 
	 * @param tag tagname to write.
	 * @param atts table containing the tag's attributes as (name, value)
	 *            pairs, or null
	 */
	public final void writeEmptyTag(String tag, HashMap<String, String> atts) {
		this.writeOpenTag(tag, atts, true, true);
	}

	/**
	 * Method to write only the closing of tag. For example </xcoord>.
	 * 
	 * @param tag tagname to write.
	 */
	public final void closeTag(String tag) {
		StringBuilder buff = new StringBuilder(BEGINCLOSETAG);
		buff.append(tag);
		buff.append(XMLWriter.ENDTAG);
		buff.append(XMLWriter.RETURNLINEFEED);
		this.write(buff.toString());
	}

	/**
	 * @return A string representation of this class.
	 */
	@Override
	public final String toString() {
		return "XMLWriter for file " + this.fileName + ".";
	}
}
