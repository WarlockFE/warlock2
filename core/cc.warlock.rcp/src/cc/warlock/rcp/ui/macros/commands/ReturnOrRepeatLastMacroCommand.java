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
package cc.warlock.rcp.ui.macros.commands;

import cc.warlock.core.client.IMacroCommand;
import cc.warlock.core.client.IWarlockClientViewer;

public class ReturnOrRepeatLastMacroCommand implements IMacroCommand {
	
	public String getIdentifier() {
		return "ReturnOrRepeatLast";
	}
	
	public void execute(IWarlockClientViewer viewer) {
		if(viewer.getEntry().getText().length() > 0)
			viewer.getEntry().submit();
		else
			viewer.getEntry().repeatLastCommand();
	}
	
	public String getDescription() {
		return "Send the current command, or repeat the last command in the command history";
	}
}
