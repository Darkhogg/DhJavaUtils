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
package es.darkhogg.util.swing;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

/**
 * An <tt>ExecutorService</tt> that executes all tasks in the <i>Swing EDT</i>.
 * 
 * @author Daniel Escoz Solana
 * @version 1.0
 */
public final class SwingExecutorService extends AbstractExecutorService {
	
	/** Whether this executor is shutdown */
	private volatile boolean shutdown = false;
	
	@Override
	public void shutdown () {
		shutdown = true;
	}
	
	@Override
	public List<Runnable> shutdownNow () {
		return Collections.emptyList();
	}
	
	@Override
	public boolean isShutdown () {
		return shutdown;
	}
	
	@Override
	public boolean isTerminated () {
		return shutdown;
	}
	
	@Override
	public boolean awaitTermination ( final long timeout, final TimeUnit unit ) {
		return false;
	}
	
	@Override
	public void execute ( final Runnable command ) {
		SwingUtilities.invokeLater( command );
	}
	
}
