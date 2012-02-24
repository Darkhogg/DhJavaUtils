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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * A simple blocking queue that can be closed.
 * 
 * @author Daniel Escoz
 * @version 1.0
 * @param <E>
 *            Type of the elements of this pipe
 */
public final class Pipe<E> implements Iterable<E> {
	
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
		
		// Create the new node
		final Node<E> node = new Node<E>( elem );
		
		// Add the new node
		if ( first == null ) {
			first = node;
			last = node;
		} else {
			last.next = node;
			last = node;
		}
		
		// Notify one of the waiting threads, if any
		notify();
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
		for ( final E elem : iter ) {
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
		return privTake( -1, null, true );
	}
	
	/**
	 * Retrieves the next element on this pipe, or waits indefinitely until one is added. If this pipe is {@link #close
	 * closed} while waiting or this method is called on an empty and closed pipe, the <tt>hardFail</tt> method decides
	 * what to do. If <tt>true</tt>, a {@link ClosedPipeException} is thrown. If <tt>false</tt>, <tt>null</tt> is
	 * returned.
	 * 
	 * @param hardFail
	 *            <tt>true</tt> for this method to throw an exception when the pipe is closed, <tt>false</tt> to return
	 *            <tt>null</tt>
	 * @return The next element on the pipe
	 * @throws InterruptedException
	 *             If the current thread is interrupted while waiting on this method
	 * @throws PipeClosedException
	 *             If this pipe is closed and there are no more elements to retrieve
	 */
	public synchronized E take ( final boolean hardFail ) throws InterruptedException {
		return privTake( -1, null, hardFail );
	}
	
	public synchronized E take ( final long time, final TimeUnit unit ) throws InterruptedException {
		if ( time < 0 ) {
			throw new IllegalArgumentException( "Negative time (" + time + ")" );
		}
		
		return privTake( time, unit, true );
	}
	
	public synchronized E take ( final long time, final TimeUnit unit, final boolean hardFail )
		throws InterruptedException
	{
		if ( time < 0 ) {
			throw new IllegalArgumentException( "Negative time (" + time + ")" );
		}
		
		return privTake( time, unit, hardFail );
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
	
	@Override
	public synchronized Iterator<E> iterator () {
		return new NodeIterator<>( first );
	}
	
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder( "Pipe[" );
		
		Node<E> node = first;
		while ( node != null ) {
			sb.append( node.element );
			node = node.next;
			
			if ( node != null ) {
				sb.append( ", " );
			}
		}
		
		return sb.append( "]" ).toString();
	}
	
	/**
	 * @param time
	 *            Total time to wait. Negative for unlimited waiting.
	 * @param unit
	 *            Time unit of the previous parameter
	 * @param exception
	 *            Whether to throw an exception if the pipe is closed.
	 * @return The next element on the pipe, or <tt>null</tt> if none.
	 * @throws InterruptedException
	 *             If the current thread is interrupted while waiting
	 * @throws PipeClosedException
	 *             If the pipe is closed, there's no more elements on the pipe and <tt>exception</tt> is <tt>true</tt>
	 */
	private synchronized E privTake ( final long time, final TimeUnit unit, final boolean exception )
		throws InterruptedException
	{
		try {
			// Wait for elements to arrive
			if ( first == null && !closed ) {
				if ( time < 0 ) {
					// Unbounded waiting
					this.wait();
				} else {
					// Bounded waiting
					unit.timedWait( this, time );
				}
			}
			
			// If there are no more elements...
			if ( first == null ) {
				if ( closed ) {
					// If the pipe is closed...
					if ( exception ) {
						throw new PipeClosedException( "Pipe closed" );
					} else {
						return null;
					}
				} else {
					// If the thread finished waitings...
					if ( exception ) {
						throw new PipeTimeoutException( "Timeout" );
					} else {
						return null;
					}
				}
			}
			
			// Take the first element
			final E elem = first.element;
			first = first.next;
			
			if ( first == null ) {
				last = null;
			}
			
			// Return it
			return elem;
			
		} catch ( final InterruptedException ex ) {
			close();
			throw ex;
		}
	}
	
	/**
	 * A node of a linked list
	 * 
	 * @author Daniel Escoz
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
	
	/**
	 * An iterator over nodes of a linked list
	 * 
	 * @author Daniel Escoz
	 * @param <E>
	 *            Type of the elements
	 */
	private static final class NodeIterator<E> implements Iterator<E> {
		
		/** The next node */
		private Node<E> node;
		
		/**
		 * @param node
		 *            Initial node of the iteration
		 */
		public NodeIterator ( final Node<E> node ) {
			this.node = node;
		}
		
		@Override
		public boolean hasNext () {
			return node != null;
		}
		
		@Override
		public E next () {
			if ( !hasNext() ) {
				throw new NoSuchElementException();
			}
			
			final E elem = node.element;
			node = node.next;
			return elem;
		}
		
		@Override
		public void remove () {
			throw new UnsupportedOperationException();
		}
		
	}
}
