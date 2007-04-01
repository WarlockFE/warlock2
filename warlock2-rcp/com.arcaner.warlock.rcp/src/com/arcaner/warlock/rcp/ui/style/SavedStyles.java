package com.arcaner.warlock.rcp.ui.style;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Properties;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import com.arcaner.warlock.configuration.WarlockConfiguration;

public class SavedStyles {

	private static Hashtable<String,Style> styles = new Hashtable<String,Style>();
	private static Properties props = new Properties();
	
	private static final String STYLE_PREFIX = "style.";
	private static final String FOREGROUND = "foreground";
	private static final String BACKGROUND = "background";
	private static final String RED = "red";
	private static final String GREEN = "green";
	private static final String BLUE = "blue";
	private static final String FONT_SIZE = "fontSize";
	private static final String FONT_NAME = "fontName";
	private static final String BOLD = "bold";
	private static final String ITALIC = "italic";
	private static final String UNDERLINE = "underline";
	
	static {
		try {
			FileInputStream stream = new FileInputStream(WarlockConfiguration.getConfigurationFile("styles.properties"));
			props.load(stream);
			stream.close();
			
			for (Object obj : props.keySet())
			{
				String property = (String) obj;
				if (property.startsWith(STYLE_PREFIX))
				{
					String elements[] = property.split("\\.");
					String styleName = elements[1];
					String propertyName = elements[2];
					
					if (!styles.containsKey(styleName)) {
						styles.put(styleName, new Style());
						styles.get(styleName).setStyleName(styleName);
					}
					
					Style style = styles.get(styleName);
					if (FOREGROUND.equals(propertyName) && style.getForeground() == null)
						style.setForeground(getColorFromStyle(styleName, FOREGROUND));
					else if (BACKGROUND.equals(propertyName) && style.getBackground() == null)
						style.setBackground(getColorFromStyle(styleName, BACKGROUND));
					else if (FONT_SIZE.equals(propertyName))
						style.setFontSize(Integer.parseInt(props.getProperty(property)));
					else if (FONT_NAME.equals(propertyName))
						style.setFontName(props.getProperty(property));
					else if (BOLD.equals(propertyName))
						style.setBold(Boolean.parseBoolean(props.getProperty(property)));
					else if (ITALIC.equals(propertyName))
						style.setItalic(Boolean.parseBoolean(props.getProperty(property)));
					else if (UNDERLINE.equals(propertyName))
						style.setUnderline(Boolean.parseBoolean(props.getProperty(property)));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void save ()
	{
		props.clear();
		
		for (Style style : styles.values())
		{
			setColorFromStyle(style.getStyleName(), FOREGROUND, style.getForeground());
			setColorFromStyle(style.getStyleName(), BACKGROUND, style.getBackground());
			setStyleProperty(style.getStyleName(), FONT_SIZE, style.getFontSize() + "");
			setStyleProperty(style.getStyleName(), FONT_NAME, style.getFontName());
			setStyleProperty(style.getStyleName(), BOLD, style.isBold()+"");
			setStyleProperty(style.getStyleName(), ITALIC, style.isItalic()+"");
			setStyleProperty(style.getStyleName(), UNDERLINE, style.isUnderline()+"");
		}
	}
	
	public static Collection<Style> getAllStyles ()
	{
		return styles.values();
	}
	
	public static Style getStyleFromName (String styleName)
	{
		return styles.get(styleName);
	}
	
	public static void addStyle (Style style)
	{
		styles.put(style.getStyleName(), style);
		save();
	}
	
	public static void removeStyle (String styleName)
	{
		styles.remove(styleName);
		save();
	}
	
	private static Color getColorFromStyle (String styleName, String prefix)
	{
		int red = Integer.parseInt(getStyleProperty(styleName, prefix + "." + RED));
		int green = Integer.parseInt(getStyleProperty(styleName, prefix + "." + GREEN));
		int blue = Integer.parseInt(getStyleProperty(styleName, prefix + "." + BLUE));
		
		return new Color(Display.getDefault(), red, green, blue);
	}
	
	private static void setColorFromStyle (String styleName, String prefix, Color color)
	{
		setStyleProperty(styleName, prefix + "." + RED, color.getRed() + "");
		setStyleProperty(styleName, prefix + "." + GREEN, color.getGreen() + "");
		setStyleProperty(styleName, prefix + "." + BLUE, color.getBlue() + "");
	}

	private static String getStyleProperty (String styleName, String propertyName)
	{
		return props.getProperty(STYLE_PREFIX + styleName + "." + propertyName);
	}
	
	private static void setStyleProperty (String styleName, String propertyName, String value)
	{
		props.setProperty(STYLE_PREFIX + styleName + "." + propertyName, value);
	}
}
