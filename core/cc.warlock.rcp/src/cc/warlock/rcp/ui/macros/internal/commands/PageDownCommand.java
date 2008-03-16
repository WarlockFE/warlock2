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
package cc.warlock.rcp.ui.macros.internal.commands;

import cc.warlock.core.client.IWarlockClientViewer;
import cc.warlock.core.client.settings.macro.IMacroCommand;
import cc.warlock.rcp.views.GameView;

/**
 * @author Will Robertson
 *
 * Handles PageDown Macro (normally assigned to the PageDown Key)
 */
public class PageDownCommand implements IMacroCommand {

	/* (non-Javadoc)
	 * @see cc.warlock.rcp.ui.macros.IMacroCommand#execute(cc.warlock.core.client.IWarlockClientViewer)
	 */
	public void execute(IWarlockClientViewer context) {
		if (GameView.getViewInFocus() != null)
			GameView.getViewInFocus().pageDown();
	}

	/* (non-Javadoc)
	 * @see cc.warlock.rcp.ui.macros.IMacroCommand#getIdentifier()
	 */
	public String getIdentifier() {
		return "PageDown";
	}

	public String getDescription() {
		return "Scroll a page down in the current game view";
	}
}
