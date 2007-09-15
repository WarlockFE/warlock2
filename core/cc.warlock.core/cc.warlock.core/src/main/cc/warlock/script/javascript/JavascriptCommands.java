package cc.warlock.script.javascript;

import java.io.Serializable;
import java.util.Arrays;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import cc.warlock.client.stormfront.IStormFrontClient;
import cc.warlock.script.IScript;
import cc.warlock.script.IScriptCommands;
import cc.warlock.script.Match;

public class JavascriptCommands {

	private IScriptCommands commands;
	private JavascriptScript script;
	
	private static final String CALLBACK = "callback";
	private static final String USER_OBJECT = "userobject";
	
	public class JavascriptStopException extends Exception implements Serializable {
		private static final long serialVersionUID = 7226391328268718796L;
	}
	
	public JavascriptCommands(IScriptCommands commands, JavascriptScript script) {
		this.commands = commands;
		this.script = script;
	}

	public void echo(String text) {
		commands.echo(script, text);
	}

	public IStormFrontClient getClient() {
		return commands.getClient();
	}

	public Match matchWait(Match[] matches) {
		Match match = commands.matchWait(Arrays.asList(matches));
		Function function = (Function)match.getAttribute(CALLBACK);
		try {
			function.call(script.getContext(), script.getScope(), null, new Object[] {match.getAttribute(USER_OBJECT)});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return match;
	}

	public void move(String direction) {
		commands.move(direction);
	}

	public void pause(int seconds) {
		commands.pause(seconds);
	}

	public void put(String text) {
		commands.put(script, text);
	}

	public void waitFor(Match match) {
		commands.waitFor(match);
	}

	public void waitForPrompt() {
		commands.waitForPrompt();
	}

	public void waitForRoundtime() {
		commands.waitForRoundtime();
	}

	public void exit() throws JavascriptStopException {
		script.stop();
		
		throw new JavascriptStopException();
	}
	
	public Match match(String text, Function function, Scriptable object) {
		Match m = new Match();
		m.setMatchText(text);
		m.setAttribute(CALLBACK, function);
		m.setAttribute(USER_OBJECT, object);
		
		return m;
	}
	
	public void stop() {
		commands.stop();
	}
}
