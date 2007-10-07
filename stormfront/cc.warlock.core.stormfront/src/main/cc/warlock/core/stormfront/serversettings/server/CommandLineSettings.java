package cc.warlock.core.stormfront.serversettings.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cc.warlock.core.client.IWarlockSkin;
import cc.warlock.core.stormfront.client.StormFrontColor;
import cc.warlock.core.stormfront.xml.StormFrontAttribute;
import cc.warlock.core.stormfront.xml.StormFrontElement;

public class CommandLineSettings extends ColorSetting {

	protected String fontFace;
	protected int fontSize = -1;
	protected String barColor;
	
	protected HashMap<String,String> extraAttributes = new HashMap<String,String>();
	
	protected static final String KEY_FACE = "face";
	protected static final String KEY_SIZE = "size";
	protected static final String KEY_FGCOLOR = "fgcolor";
	protected static final String KEY_BARCOLOR = "barcolor";
	
	protected static ArrayList<String> skipAttributes = new ArrayList<String>();
	static {
		Collections.addAll(skipAttributes, KEY_FACE, KEY_SIZE, KEY_FGCOLOR, KEY_BGCOLOR, KEY_BARCOLOR);
	}
	
	public CommandLineSettings (ServerSettings settings, StormFrontElement element, Palette palette)
	{
		super(settings, element, palette);
		this.foregroundKey = KEY_FGCOLOR;
		
		if (element != null)
		{
			fontFace = element.attributeValue(KEY_FACE);
			String size = element.attributeValue(KEY_SIZE);
			if (size != null)
			{
				fontSize = getPixelSizeInPoints(Integer.parseInt(size));
			}
			
			this.barColor = element.attributeValue(KEY_BARCOLOR);
			
			for (StormFrontAttribute attribute : element.attributes())
			{
				if (!skipAttributes.contains(attribute.getName()))
				{
					extraAttributes.put(attribute.getName(), attribute.getValue());
				}
			}
		}
	}
	
	@Override
	public String getId() {
		return "cmdline";
	}

	@Override
	protected void saveToDOM() {
		setAttribute(KEY_FGCOLOR, foregroundColor);
		setAttribute(KEY_BGCOLOR, backgroundColor);
		
		if (fontFace != null)
			setAttribute(KEY_FACE, fontFace);
		
		if (fontSize != -1)
			setAttribute(KEY_SIZE, ""+fontSize);
		
		if (barColor != null)
			setAttribute(KEY_BARCOLOR, barColor);
	}

	@Override
	protected String toStormfrontMarkup() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<cmdline fgcolor=\"" + foregroundColor + "\" bgcolor=\"" + backgroundColor + "\" ");
		
		for (Map.Entry<String,String> entry : extraAttributes.entrySet())
		{
			buffer.append(entry.getKey() + "=\"" + entry.getValue() + "\" ");
		}
		buffer.append("/>");
		
		return buffer.toString();
	}

	public String getFontFace() {
		return fontFace;
	}

	public void setFontFace(String fontFace) {
		if (this.fontFace != fontFace)
			needsUpdate = true;
		
		this.fontFace = fontFace;
	}

	public int getFontSizeInPixels() {
		return fontSize;
	}
	
	public int getFontSizeInPoints() {
		return getPixelSizeInPoints(fontSize);
	}

	public void setFontSizeInPixels(int fontSize) {
		if (this.fontSize != fontSize)
			needsUpdate = true;
		
		this.fontSize = fontSize;
	}
	
	public void setFontSizeInPoints (int fontSize) {
		setFontSizeInPixels(getPointSizeInPixels(fontSize));
	}

	public StormFrontColor getBarColor ()
	{
		if (this.barColor != null && "skin".equals(this.barColor))
		{
			return (StormFrontColor) serverSettings.getDefaultSkin().getColor(IWarlockSkin.ColorType.CommandLine_BarColor);
		}
		else return getColorFromString(KEY_BARCOLOR, barColor, true);
	}
	
	public void setBarColor (StormFrontColor barColor)
	{
		this.barColor = assignColor(barColor, this.barColor);
	}
}