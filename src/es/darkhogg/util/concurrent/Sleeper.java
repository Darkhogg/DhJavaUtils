/* This file is part of DhJavaUtils.
 *
 * DhJavaUtils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DhJavaUtils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DhJavaUtils.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.darkhogg.util.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * 
 * 
 * @author Daniel Escoz
 * @version 1.0
 */
public final class Sleeper {
	
	/** Object to perform all synchronization */
	private final Object sync = new Object();
	
	/** Minimum value for <tt>System.nanoTime</tt> to actually sleep */
	private volatile long minTime = System.nanoTime();
	
	/** Whether this sleeper is dead */
	private volatile boolean dead = false;
	
	/**
	 * Sleeps the current thread until some other thread calls the
	 * {@link #awake} or {@link kill} methods of this object or the sleeping
	 * thread is interrupted.
	 * 
	 * @throws InterruptedException
	 *             if this thread is interrupted while sleeping
	 */
	public void sleep () throws InterruptedException {
		boolean yield = false;
		long now = System.nanoTime();
		
		synchronized ( sync ) {
			if ( !dead && now >= minTime ) {
				sync.wait();
			} else {
				yield = true;
				minTime = now;
			}
		}
		
		if ( yield ) {
			Thread.yield();
		}
	}
	
	public void sleep ( long time, TimeUnit unit ) throws InterruptedException {
		boolean yield = false;
		long now = System.nanoTime();
		
		synchronized ( sync ) {
			if ( !dead && now >= minTime ) {
				sync.wait( unit.toMillis( time ), (int) unit.toNanos( time ) % 1000000 );
			} else {
				yield = true;
				minTime = now;
			}
		}
		
		if ( yield ) {
			Thread.yield();
		}
	}
	
	public void awake () {
		synchronized ( sync ) {
			sync.notifyAll();
		}
	}
	
	public void awake ( long time, TimeUnit unit ) {
		synchronized ( sync ) {
			minTime = Math.max( minTime, System.currentTimeMillis() + unit.toNanos( time ) );
			sync.notifyAll();
		}
	}
	
	public void kill () {
		synchronized ( sync ) {
			dead = true;
			sync.notifyAll();
		}
	}
}
