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
package es.darkhogg.util.concurrent;

/**
 * An exception thrown by {@link Pipe} when the pipe is closed.
 * 
 * @author Darkhogg
 * @version 1.0
 */
public final class PipeClosedException extends IllegalStateException {
	
	/** Serial version ID */
	private static final long serialVersionUID = 233807269069750949L;
	
	/**
	 * @param message
	 *            Message for this exception
	 */
	public PipeClosedException ( final String message ) {
		super( message );
	}
}
