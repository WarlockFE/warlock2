/*
 * Created on Jan 15, 2005
 */
package cc.warlock.stormfront.internal;

import org.xml.sax.Attributes;

import cc.warlock.stormfront.IStormFrontProtocolHandler;


/**
 * @author Marshall
 */
public class AppTagHandler extends DefaultTagHandler {
	
	public AppTagHandler(IStormFrontProtocolHandler handler) {
		super(handler);
	}
	
	public String[] getTagNames() {
		return new String[] { "app" };
	}
	
	public void handleStart(Attributes atts) {
		String characterName = atts.getValue("char");
		String gameName = atts.getValue("game");
		
		handler.getClient().getDefaultStream().getTitle().set("[" + gameName + "] " + characterName);
		handler.getClient().getCharacterName().set(new String(characterName));
	}
}