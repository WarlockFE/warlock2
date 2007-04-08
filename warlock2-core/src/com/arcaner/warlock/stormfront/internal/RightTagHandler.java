package com.arcaner.warlock.stormfront.internal;

import com.arcaner.warlock.stormfront.IStormFrontProtocolHandler;

public class RightTagHandler extends DefaultTagHandler {

	public RightTagHandler(IStormFrontProtocolHandler handler) {
		super(handler);
	}
	
	public String[] getTagNames() {
		return new String[] { "right" };
	}

	public boolean handleCharacters(char[] ch, int start, int length) {
		return true;
	}
}
