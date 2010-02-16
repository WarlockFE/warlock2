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
package cc.warlock.core.client.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import cc.warlock.core.client.IWarlockClient;
import cc.warlock.core.client.WarlockString;
import cc.warlock.core.util.ConfigurationUtil;

/**
 * A simple text-based logger that rotates daily
 * 
 * @author marshall
 *
 */
public class SimpleLogger implements IClientLogger {

	protected IWarlockClient client;
	protected StringBuffer buffer = new StringBuffer();
	protected int maxBufferSize = 2000;
	protected boolean nextlineStamp = false;
	
	protected static final DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
	protected static final DateFormat timeFormat = new SimpleDateFormat("[H:m:s] ");
	
	public SimpleLogger (IWarlockClient client)
	{
		this.client = client;
	}
	
	public void logEcho(String command) {
		appendBuffer(command);
		appendBuffer("\n");
		
		checkAndDumpBuffer();
	}

	public void logPrompt(String prompt) {
		appendBuffer(prompt);
		
		checkAndDumpBuffer();
	}

	public void logText(WarlockString text) {
		appendBuffer(text.toString());
		
		checkAndDumpBuffer();
	}
	
	protected void appendBuffer(String str) {
		if (nextlineStamp) {
			nextlineStamp = false;
			str = timeFormat.format(Calendar.getInstance().getTime()) + str;
		}
		str = str.replaceAll("\n(.)", "\n" + timeFormat.format(Calendar.getInstance().getTime()) + "$1");
		if (str.endsWith("\n")) {
			nextlineStamp = true;
		}
		buffer.append(str);
	}

	protected String characterName = null;
	protected File getLogFile ()
	{
		if (characterName == null) {
			characterName = client.getCharacterName().get();
		}
		
		return new File(ConfigurationUtil.getConfigurationDirectory("logs", true),
			characterName + "-" + dateFormat.format(Calendar.getInstance().getTime()) + ".txt");
	}
	
	protected void checkAndDumpBuffer ()
	{
		if (buffer.length() >= maxBufferSize)
		{
			dumpBuffer();
		}
	}
	
	public void flush() {
		dumpBuffer();
	}
	
	protected void dumpBuffer ()
	{
		try {
			FileOutputStream stream = new FileOutputStream(getLogFile(), true);
			stream.write(buffer.toString().getBytes());
			stream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		buffer.setLength(0);
	}
}
