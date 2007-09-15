package cc.warlock.stormfront.internal;

import cc.warlock.script.IScript;
import cc.warlock.stormfront.IStormFrontProtocolHandler;


public class CompassTagHandler extends DefaultTagHandler {

	public CompassTagHandler(IStormFrontProtocolHandler handler) {
		super(handler);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String[] getTagNames() {
		return new String[] { "compass" };
	}
	
	@Override
	public void handleEnd() {
		for (IScript script : handler.getClient().getRunningScripts())
		{
			script.getScriptCommands().movedToRoom();
		}
	}

}
