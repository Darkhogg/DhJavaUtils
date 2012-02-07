/*
 * This file is part of DhJavaUtils.
 * 
 * DhJavaUtils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DhJavaUtils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DhJavaUtils. If not, see <http://www.gnu.org/licenses/>.
 */
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
	private OperatingSystem ( final String name, final String regex ) {
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
	public static OperatingSystem forName ( final String osname ) {
		final String lowosname = osname.toLowerCase();
		
		for ( final OperatingSystem os : values() ) {
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
