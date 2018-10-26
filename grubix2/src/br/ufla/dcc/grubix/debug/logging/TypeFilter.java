package br.ufla.dcc.grubix.debug.logging;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

/**
 * Returns all class names of class files within the br.ufla.dcc.grubix.simulator.event folder. Names are extracted from
 * the filesystem or from a jar file.
 * 
 * @author mika
 *
 */
public final class TypeFilter {
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(TypeFilter.class.getName());
	
	/**
	 * Solely exists to test functionality of this class.
	 * 
	 * @param a unused
	 */
	public static void main(String[] a) {
		String pref = "br.ufla?dcc?grubix?simulator?event?";
		pref = pref.replace('?', File.separatorChar);
		System.out.println("File.separator=" + File.separator + " -> " + pref);
		
		try {
			String[] types = new TypeFilter().getTypes();
			for (String t : types) {
				System.out.println(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * List of all Events in the br.ufla.dcc.grubix.simulator.event package, filled by calling the constructor of this class.
	 */
	private String[] types = null;
	
	/**
	 * Returns all class names of class files within the br.ufla.dcc.grubix.simulator.event package.
	 * @return String[] containing all class names of class files within the br.ufla.dcc.grubix.simulator.event package
	 */
	public String[] getTypes() {
		return types.clone();
	}
	
	/**
	 * Default constructor.  Will create an array containing all members of the br.ufla.dcc.grubix.simulator.event package,
	 * which can be retrieved using {@link #getTypes()}. Works well if code is not executed within the JUnit 
	 * environment.
	 * 
	 * @throws URISyntaxException if ressource loaction fails
	 * @throws IOException if Fileoperations fail
	 */
	public TypeFilter() throws URISyntaxException, IOException {
		
		URL location = getClass().getResource("/br/ufla/dcc/grubix/simulator/event");
		
		LinkedList<String> eventClasses = null;
		
		if (location.toString().startsWith("file:")) {
			// local file
			File e = new File(new URI(location.toString()));
			LOGGER.debug("LOCAL FILE " + e);
			eventClasses = getEventList(e);
		} else if (location.toString().startsWith("jar:file:")) {
			// local file inside a jar
			LOGGER.debug("LOCAL JAR FILE");
			int exl = location.toString().indexOf("!");
			String path = location.toString().substring(0, exl);
			LOGGER.debug("PATH:" + path);
			int lastSlash = path.lastIndexOf("/");
			String jarFilename = path.substring(lastSlash + 1);
			JarFile jar = new JarFile(jarFilename);
			eventClasses = getEventList(jar);
		} else {
			LOGGER.fatal("UNHANDLED LOCATION");
			return;
		}
		 		
		Collections.sort(eventClasses);

		types = new String[eventClasses.size()];
		int counter = 0;
		for (String f : eventClasses) {
			types[counter++] = f;
		}
		
	}

	/**
	 * Called internally by constructor to retrieve package members from the filesystem.  
	 * 
	 * @param root Location of br.ufla.dcc.grubix.simulator.event package in the filesystem
	 * @return List with memebers of br.ufla.dcc.grubix.simulator.event package
	 */
	private LinkedList<String> getEventList(File root) {
		LinkedList<String> result = new LinkedList<String>();
		
		File[] files = root.listFiles();
		
		for (File f : files) {
			if (f.isFile()) {
				if (f.toString().endsWith(".class")) {
					String name = f.toString();
					int pref = name.indexOf("br"+ File.separator +"ufla" + File.separator + "dcc" + File.separator + "grubix" + File.separator 
													+ "simulator" + File.separator + "event" + File.separator);
					name = name.substring(pref);
					name = name.substring(0, name.length() - ".class".length());
					name = name.replace(File.separatorChar, '.');
					result.add(name);
				}
			} else if (f.isDirectory()) {
				result.addAll(getEventList(f));
			}				
		}
		
		return result;
	}
	
	/**
	 * Called internally by constructor to retrieve package members from within a jar.
	 * 
	 * @param jar Location of br.ufla.dcc.grubix.simulator.event package in the jar
	 * @return List with memebers of br.ufla.dcc.grubix.simulator.event package
	 */
	private LinkedList<String> getEventList(JarFile jar) {
		LinkedList<String> result = new LinkedList<String>();
		
		Enumeration<JarEntry> e = jar.entries();
		while (e.hasMoreElements()) {
			JarEntry je = e.nextElement();
			if (je.toString().startsWith("br/ufla/dcc/grubix/simulator/event")) {
				if (je.toString().endsWith(".class")) {
					if (je.toString().indexOf('$') == -1) {
						String name = je.toString();
						name = name.substring(0, name.length() - ".class".length());
						name = name.replace('/', '.');
						result.add(name);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Checks if the class represented by it's passed name is present in the local classpath.
	 * 
	 * @param className name of the class to search for
	 * @return <code>true</code> if present
	 */
	public static boolean isPresent(String className) {
		try {
			Class c = Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
