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
package cc.warlock.core.client;

import java.util.Collection;

/**
 * This interface represents a "style" that applies to a string of text. 
 * @author Marshall
 *
 */
public interface IWarlockStyle {

	public enum StyleType {
		BOLD, ITALIC, UNDERLINE, LINK, MONOSPACE
	};
	
	public Collection<StyleType> getStyleTypes();
	public Runnable getAction();
	
	public WarlockColor getForegroundColor();
	public WarlockColor getBackgroundColor();
	public WarlockFont getFont();
	public boolean isFullLine();
	public String getName();
	
	public void addStyleType (StyleType styleType);

	public void setAction(Runnable action);
	public void setForegroundColor(WarlockColor color);
	public void setBackgroundColor(WarlockColor color);
	public void setFont(WarlockFont font);
	public void setFullLine(boolean fullLine);
	public void setName(String name);
	public String getSound();
	public void setSound(String sound);

}
