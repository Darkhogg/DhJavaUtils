package es.darkhogg.util;

import java.util.regex.Pattern;

/**
 * Represents an operating system family.
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public enum OperatingSystem {
	/** FreeBSD family of OS's */
	FREEBSD( "FreeBSD", ".*freebsd.*" ),
	
	/** GNU/Linux family of OS's */
	LINUX( "Linux", ".*linux.*" ),
	
	/** Mac OS and Mac OS X operating systems */
	MAC( "Mac", "mac.*" ),
	
	/** Solaris operating system */
	SOLARIS( "Solaris", "sunos.*|solaris.*" ),
	
	/** Windows family of OS's */
	WINDOWS( "Windows", ".*windows.*" );
	
	/** Name of this operating system family */
	private final String name;
	
	/** Regular expression used to recognize this operating system by name */
	private final String regex;
	
	/**
	 * @param name
	 *            The name of this operating system family
	 * @param regex
	 *            The regular expresion for recognizing this operating system
	 */
	private OperatingSystem ( String name, String regex ) {
		if ( name == null | regex == null ) {
			throw new NullPointerException();
		}
		
		this.name = name;
		this.regex = regex;
	}
	
	/** @return The name of this operating system */
	public String getName () {
		return name;
	}
	
	/**
	 * Returns an operating system family that is recognized for the given name
	 * 
	 * @param osname
	 *            Name of the operating system
	 * @return The operating system family of the given <tt>name</tt>, or <tt>null</tt> if none is available.
	 */
	public static OperatingSystem forName ( String osname ) {
		String lowosname = osname.toLowerCase();
		
		for ( OperatingSystem os : values() ) {
			if ( Pattern.matches( os.regex, lowosname ) ) {
				return os;
			}
		}
		
		return null;
	}
	
	/**
	 * @return The operating system family in which the current VM is running, as reported by the <tt>os.name</tt>
	 *         system property
	 */
	public static OperatingSystem getCurrent () {
		return forName( System.getProperty( "os.name" ) );
	}
}
