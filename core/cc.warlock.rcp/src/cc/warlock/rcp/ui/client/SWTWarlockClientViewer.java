/**
 * Warlock, the open-source cross-platform game client
 *  
 * Copyright 2008, Warlock LLC, and individual contributors as indicated
 * by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
/*
 * Created on Mar 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cc.warlock.rcp.ui.client;

import java.io.InputStream;

import org.eclipse.swt.widgets.Display;

import cc.warlock.core.client.IWarlockClientViewer;

/**
 * @author Marshall
 *
 * A convenience super class for viewers who need SWT thread access
 */
public class SWTWarlockClientViewer implements IWarlockClientViewer  {

	private IWarlockClientViewer viewer;
	
	public SWTWarlockClientViewer (IWarlockClientViewer viewer)
	{
		this.viewer = viewer;
	}
	
	private class PlaySoundWrapper implements Runnable {
		public InputStream soundStream;
		
		public PlaySoundWrapper(InputStream soundStream) {
			this.soundStream = soundStream;
		}
		
		public void run () {
			viewer.playSound(soundStream);
		}
	}
	
	protected void run(Runnable runnable)
	{
		Display.getDefault().asyncExec(new CatchingRunnable(runnable));
	}
	
	public void playSound(InputStream file) {
		run(new PlaySoundWrapper(file));
	}
	
	public boolean isStreamOpen(String streamName) {
		// This method is not allowed to use any SWT methods
		return viewer.isStreamOpen(streamName);
	}
}
