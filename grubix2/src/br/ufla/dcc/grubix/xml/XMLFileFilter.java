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

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * This class represents a file filter which lets pass only ".*" and ".xml" files.
 * @author jlsx
 */
public final class XMLFileFilter extends FileFilter {
	
	/** Allowed file name extensions. */
	private String[] extensions;
	
	/** Description which is displayed in the file chooser's file type box. */
	private String description;
	
	/**
	 * Constructs a file filter with a default description.
	 * @param ext The allowed file extensions for this filter.
	 */
	public XMLFileFilter(String ext) {
		this(new String[] {ext}, "");
	}
	
	/**
	 * Constructs a file filter with allowed extensions and a description for the
	 * file chooser's file type combo box.
	 * @param ext List of allowed file extensions for this filter
	 * @param des The description which is displayed in the file chooser's file type box.
	 */
	public XMLFileFilter(String[] ext, String des) {
		extensions = new String[ext.length];
		for (int i = 0; i < ext.length; i++)	{
			extensions[i] = ext[i].toLowerCase();
		}
		
		if (des.equals("")) {
			description = "." + ext[0] + " Files";
		} else {
			description = des;
		}
	}
	
	/**
	 * Filters files by file extensions.
	 * @param f File which is tested for applicability
	 * @return True, if extension of f is in this filter's list of extensions,
	 * false otherwise
	 */
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		
		String name = f.getName().toLowerCase();
		for (int i = 0; i < extensions.length; i++) {
			if (name.endsWith(extensions[i])) {
				return true;
			}
		}
		return false;
	}
	
	/** @return Description which is displayed in the file chooser's file type box. */
	public String getDescription() {
		return description;
	}
}
