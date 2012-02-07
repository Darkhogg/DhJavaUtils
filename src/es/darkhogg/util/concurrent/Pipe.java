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

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * A simple blocking queue that can be closed.
 * 
 * @author Daniel Escoz
 * @version 1.0
 * @param <E>
 *            Type of the elements of this pipe
 */
public final class Pipe<E> implements Closeable {
	
	/** Whether this pipe is closed */
	private volatile boolean closed = false;
	
	/** First node of the pipe */
	private volatile Node<E> first = null;
	
	/** Last node of the pipe */
	private volatile Node<E> last = null;
	
	public synchronized void add ( final E elem ) {
		if ( closed ) {
			throw new IllegalStateException( "Pipe closed" );
		}
		
		final Node<E> node = new Node<E>( elem );
		
		if ( first == null ) {
			first = node;
			last = node;
		} else {
			last.next = node;
			last = node;
		}
		
		notifyAll();
	}
	
	public synchronized E take () throws InterruptedException {
		return take( -1, null );
	}
	
	public synchronized E take ( final long time, final TimeUnit unit ) throws InterruptedException {
		while ( first == null && !closed ) {
			if ( time < 0 ) {
				this.wait();
			} else {
				this.wait( unit.toMillis( time ), (int) unit.toNanos( time ) % 1000000 );
			}
		}
		
		if ( first == null && closed ) {
			throw new IllegalStateException( "Pipe closed" );
		}
		
		final E elem = first.element;
		first = first.next;
		
		if ( first == null ) {
			last = null;
		}
		
		return elem;
	}
	
	/** @return Whether this pipe is empty */
	public synchronized boolean isEmpty () {
		return ( first == null );
	}
	
	/** @return Whether this pipe is closed */
	public synchronized boolean isClosed () {
		return closed;
	}
	
	@Override
	public synchronized void close () {
		closed = true;
		notifyAll();
	}
	
	/**
	 * A node of a linked list
	 * 
	 * @author Daniel Escoz
	 * @version 1.0
	 * @param <E>
	 *            Type of the elements
	 */
	private static final class Node<E> {
		
		/** Element on this node */
		private final E element;
		
		/** Pointer to the next node */
		private volatile Node<E> next = null;
		
		/**
		 * Creates a new node with the passed <tt>element</tt>
		 * 
		 * @param element
		 *            Element for this node
		 */
		public Node ( final E element ) {
			this.element = element;
		}
	}
	
}
