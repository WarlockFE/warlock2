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
package cc.warlock.core.stormfront.settings.skin;

import java.util.HashMap;

import cc.warlock.core.client.IWarlockHighlight;
import cc.warlock.core.client.IWarlockSkin;
import cc.warlock.core.client.WarlockColor;
import cc.warlock.core.client.settings.WindowConfigurationProvider;

/**
 * The default skin handles any attributes who's values are "skin"
 * @author marshall
 */
public class DefaultSkin /*implements IWarlockSkin*/ {

	public final static DefaultSkin instance = new DefaultSkin();
	
	//public static final int DEFAULT_FONT_SIZE = 12;
	//public static final WarlockColor MAIN_COLOR = new WarlockColor(-1, -1, -1);
	
	// TODO give these proper size/load since we know their final sizes.
	protected HashMap<String, WarlockColor> fgColors = new HashMap<String, WarlockColor>();
	protected HashMap<String, WarlockColor> bgColors = new HashMap<String, WarlockColor>();
	protected WarlockColor commandLineBarColor;
	
	protected WarlockColor defaultWindowBackground, defaultWindowForeground;
	
	private DefaultSkin ()
	{
	/*	fgColors.put("bold", new WarlockColor("#FFFF00"));
		fgColors.put("roomName", new WarlockColor("#FFFFFF"));
		fgColors.put("speech", new WarlockColor("#80FF80"));
		fgColors.put("thought", new WarlockColor("#FF8000"));
		fgColors.put("cmdline", new WarlockColor("#FFFFFF"));
		fgColors.put("whisper", new WarlockColor("#80FFFF"));
		fgColors.put("watching", new WarlockColor("#FFFF00"));
		fgColors.put("link", new WarlockColor("#62B0FF"));
		fgColors.put("selectedLink", new WarlockColor("#000000"));
		fgColors.put("command", new WarlockColor("#FFFFFF"));
		
		fgColors.put(WindowConfigurationProvider.WINDOW_MAIN, MAIN_COLOR);
		bgColors.put(WindowConfigurationProvider.WINDOW_MAIN, MAIN_COLOR);
		
		bgColors.put("roomName", new WarlockColor("#0000FF"));
		bgColors.put("bold", MAIN_COLOR);
		bgColors.put("speech", MAIN_COLOR);
		bgColors.put("whisper", MAIN_COLOR);
		bgColors.put("thought", MAIN_COLOR);
		bgColors.put("watching", MAIN_COLOR);
		bgColors.put("link", MAIN_COLOR);
		bgColors.put("cmdline", new WarlockColor("#000000"));
		bgColors.put("selectedLink", new WarlockColor("#62B0FF"));
		bgColors.put("command", new WarlockColor("#404040"));
		
		commandLineBarColor = new WarlockColor("#FFFFFF");
		
		defaultWindowForeground = new WarlockColor("#F0F0FF");
		defaultWindowBackground = new WarlockColor("191932");
	}
	
	public static DefaultSkin getInstance() {
		return instance;
	}
	
	public WarlockColor getColor(ColorType type) {
		if (type == ColorType.MainWindow_Background)
			return getDefaultBackgroundColor(WindowConfigurationProvider.WINDOW_MAIN);
		else if (type == ColorType.MainWindow_Foreground)
			return getDefaultForegroundColor(WindowConfigurationProvider.WINDOW_MAIN);
		else if (type == ColorType.CommandLine_Background)
			return getDefaultBackgroundColor("command");
		else if (type == ColorType.CommandLine_Foreground)
			return getDefaultForegroundColor("command");
		else if (type == ColorType.CommandLine_BarColor)
			return commandLineBarColor;
		
		return new WarlockColor(WarlockColor.DEFAULT_COLOR);
	}

	public String getFontFace(FontFaceType type) {
		if (System.getProperties().getProperty("os.name").contains("Windows"))
		{
			return "Verdana";
		}
		return "Sans";
	}
	
	// These are hard coded for now, we should either have our own "skin" defined in a configuration somewhere,
	// or try to pull from stormfront's binary "skn" file somehow?
	// At any rate -- these look to be the right "default" settings for stormfront..
	public WarlockColor getDefaultForegroundColor (String styleName)
	{
		WarlockColor color = new WarlockColor(WarlockColor.DEFAULT_COLOR);
		
		if (fgColors.containsKey(styleName))
		{
			color = fgColors.get(styleName);
		}
		
		if (color == MAIN_COLOR)
		{
			color = getDefaultWindowForeground();
		}
		
		return color;
	}
	
	public WarlockColor getDefaultBackgroundColor (String styleName)
	{
		WarlockColor color = new WarlockColor(WarlockColor.DEFAULT_COLOR);
		
		if (bgColors.containsKey(styleName))
		{
			color = bgColors.get(styleName);
		}
		
		if (color == MAIN_COLOR)
		{
			color = getDefaultWindowBackground();
		}
		
		return color;
	}
	
	public WarlockColor getDefaultWindowBackground() {
		return defaultWindowBackground;
	}

	public WarlockColor getDefaultWindowForeground() {
		return defaultWindowForeground;
	}
	
	public WarlockColor getBackgroundColor(IWarlockHighlight string) {
		WarlockColor background = string.getStyle().getBackgroundColor();
		if (background.isDefault()) {
			background = getDefaultWindowBackground();
		}
		return background;
	}
	
	public WarlockColor getForegroundColor(IWarlockHighlight string) {
		WarlockColor foreground = string.getStyle().getForegroundColor();
		if (foreground.isDefault()) {
			foreground = getDefaultWindowForeground();
		}
		return foreground;*/
	}
}
