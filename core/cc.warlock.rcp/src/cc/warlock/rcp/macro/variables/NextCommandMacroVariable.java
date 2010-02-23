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
 * Created on Mar 27, 2005
 */
package cc.warlock.rcp.macro.variables;

import cc.warlock.core.client.ICommand;
import cc.warlock.core.client.IWarlockClient;
import cc.warlock.rcp.macro.IMacroVariable;
import cc.warlock.rcp.views.GameView;


/**
 * @author Marshall
 */
public class NextCommandMacroVariable implements IMacroVariable {

	public String getIdentifier() {
		return "$nextCommand";
	}

	public String getValue(GameView gameView) {
		IWarlockClient client = gameView.getWarlockClient();
		ICommand command = client.getCommandHistory().next();
		
		if(command != null) {
			return command.getCommand();
		}
		
		return null;
	}
}
