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

import java.util.concurrent.TimeUnit;

/**
 * A simple blocking queue that can be closed.
 * 
 * @author Daniel Escoz
 * @version 1.0
 * @param <E>
 *            Type of the elements of this pipe
 */
public final class Pipe<E> {
	
	/** Whether this pipe is closed */
	private volatile boolean closed = false;
	
	/** First node of the pipe */
	private volatile Node<E> first = null;
	
	/** Last node of the pipe */
	private volatile Node<E> last = null;
	
	/**
	 * Adds an element to this pipe. If the pipe is closed, the element is discarded and this method throws a
	 * {@link PipeClosedException}
	 * 
	 * @param elem
	 *            Element to add
	 * @throws PipeClosedException
	 *             If this pipe is closed
	 */
	public synchronized void add ( final E elem ) {
		if ( closed ) {
			throw new PipeClosedException( "Pipe closed" );
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
	
	/**
	 * Adds all elements retrieved from <tt>iter</tt> to this pipe. If the pipe is closed, all remaining elements are
	 * discarded and this method throws a {@link PipeClosedException}.
	 * <p>
	 * Note that if the iteration is interrupted in any way, some elements might be added while others are not. This
	 * only happens if the iterator obtained from <tt>iter</tt> throws an exception, such as
	 * <tt>ConcurrentModificationException</tt>, in the middle of the iteration.
	 * 
	 * @param iter
	 *            Iterator to retrieve elements from
	 * @throws PipeClosedException
	 *             If this pipe is closed
	 */
	public synchronized void addAll ( final Iterable<? extends E> iter ) {
		for ( E elem : iter ) {
			add( elem );
		}
	}
	
	/**
	 * Retrieves the next element on this pipe, or waits indefinitely until one is added. If this pipe is {@link #close
	 * closed} while waiting or this method is called on an empty and closed pipe, a {@link PipeClosedException} is
	 * thrown.
	 * 
	 * @return The next element on the pipe
	 * @throws InterruptedException
	 *             If the current thread is interrupted while waiting on this method
	 * @throws PipeClosedException
	 *             If this pipe is closed and there are no more elements to retrieve
	 */
	public synchronized E take () throws InterruptedException {
		return take( -1, null );
	}
	
	/**
	 * Retrieves the next element on this pipe, or waits the maximum specified amount of time until one is added. If
	 * this pipe is {@link #close closed} while waiting or this method is called on an empty and closed pipe, a
	 * {@link PipeClosedException} is thrown.
	 * 
	 * @param time
	 *            Maximum time to wait, or a negative number for an unlimited waiting time
	 * @param unit
	 *            Unit in which <tt>time</tt> is expressed
	 * @return The next element on the pipe
	 * @throws InterruptedException
	 *             If the current thread is interrupted while waiting on this method
	 * @throws PipeClosedException
	 *             If this pipe is closed and there are no more elements to retrieve
	 */
	public synchronized E take ( final long time, final TimeUnit unit ) throws InterruptedException {
		try {
			while ( first == null && !closed ) {
				if ( time < 0 ) {
					this.wait();
				} else {
					this.wait( unit.toMillis( time ), (int) unit.toNanos( time ) % 1000000 );
				}
			}
			
			if ( first == null && closed ) {
				throw new PipeClosedException( "Pipe closed" );
			}
			
			final E elem = first.element;
			first = first.next;
			
			if ( first == null ) {
				last = null;
			}
			
			return elem;
		} catch ( final InterruptedException ex ) {
			close();
			throw ex;
		}
	}
	
	/** @return Whether this pipe is empty */
	public synchronized boolean isEmpty () {
		return ( first == null );
	}
	
	/** @return Whether this pipe is closed */
	public synchronized boolean isClosed () {
		return closed;
	}
	
	/** Closes this pipe. */
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
