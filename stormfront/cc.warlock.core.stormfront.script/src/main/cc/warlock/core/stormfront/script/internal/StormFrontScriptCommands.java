package cc.warlock.core.stormfront.script.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import cc.warlock.core.client.IProperty;
import cc.warlock.core.client.IPropertyListener;
import cc.warlock.core.script.IScript;
import cc.warlock.core.script.Match;
import cc.warlock.core.script.internal.RegexMatch;
import cc.warlock.core.script.internal.ScriptCommands;
import cc.warlock.core.script.javascript.JavascriptScript;
import cc.warlock.core.stormfront.client.IStormFrontClient;
import cc.warlock.core.stormfront.script.IStormFrontScriptCommand;
import cc.warlock.core.stormfront.script.IStormFrontScriptCommands;
import cc.warlock.core.stormfront.script.wsl.WSLScript;

public class StormFrontScriptCommands extends ScriptCommands implements IStormFrontScriptCommands, IPropertyListener<Integer> {

	protected IStormFrontClient sfClient;
	protected IScript script;
	protected Collection<Match> actions;
	
	public StormFrontScriptCommands (IStormFrontClient client, String name)
	{
		super(client, name);
		this.sfClient = client;
		waitingForRoundtime = false;
		
		client.getRoundtime().addListener(this);
		client.getDeathsStream().addStreamListener(this);
		client.getFamiliarStream().addStreamListener(this);
		client.addRoomListener(this);
	}
	
	public StormFrontScriptCommands (IStormFrontClient client, IScript script)
	{
		this(client, script.getName());
		
		this.script = script;
	}
	
	public IStormFrontClient getStormFrontClient() {
		return sfClient;
	}
	
	protected boolean waitingForRoundtime;
	public void waitForRoundtime ()
	{
		if(!interrupted) {
			assertPrompt();
			try {
				while(sfClient.getRoundtime().get() > 0 && !interrupted) {
					Thread.sleep((sfClient.getRoundtime().get() + 1) * 1000);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void waitNextRoom() {
		super.waitNextRoom();
		waitForRoundtime();
	}
	
	@Override
	public void pause(double seconds) {
		super.pause(seconds);
		waitForRoundtime();
	}
	
	@Override
	public void waitFor(Match match) {
		try {
			super.waitFor(match);
		} finally {
			waitForRoundtime();
		}
	}
	
	@Override
	public void waitForPrompt() {
		try {
			super.waitForPrompt();
		} finally {
			waitForRoundtime();
		}
	}
	
	
	public void propertyActivated(IProperty<Integer> property) {}
	public void propertyChanged(IProperty<Integer> property, Integer oldValue) {
		if (property.getName().equals("roundtime"))
		{
			if (property.get() == 0) waitingForRoundtime = false;
		}
	}
	
	public void propertyCleared(IProperty<Integer> property, Integer oldValue) {}
	
	protected  class ScriptActionThread implements Runnable {
		public void run() {
			LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
			textWaiters.add(queue);
			actionLoop: while(true) {
				String text = null;

				while(text == null) {
					try {
						text = queue.poll(100L, TimeUnit.MILLISECONDS);
						if(actions == null || interrupted) {
							break actionLoop;
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				synchronized(actions) {
					for(Match action : actions) {
						if(action.matches(text)) {
							IStormFrontScriptCommand command = (IStormFrontScriptCommand)action.getAttribute("action");
							String value;
							// FIXME breaks JS scripts
							if (script instanceof WSLScript)
							{
								for(int i = 0; (value = (String)action.getAttribute(String.valueOf(i))) != null; i++) {
									((WSLScript)script).setLocalVariable(String.valueOf(i), value);
								}
							}
							else if (script instanceof JavascriptScript)
							{
								Context context = ((JavascriptScript)script).getContext();
								Scriptable scope = ((JavascriptScript)script).getScope();
								
								ArrayList<String> actionGroups = new ArrayList<String>();
								for(int i = 0; (value = (String)action.getAttribute(String.valueOf(i))) != null; i++) {
									actionGroups.add(value);
								}
								
								Scriptable array = context.newArray(scope, actionGroups.size());
								for (int i = 0; i < actionGroups.size(); i++) {
									array.put(i, scope, actionGroups.get(i));	
								}
								
								scope.put("actionGroups", scope, array);
							}
								
							command.execute();
						}
					}
				}
			}
			textWaiters.remove(queue);
		}
	}
	
	public void addAction(IStormFrontScriptCommand action, String text) {
		if(actions == null) {
			actions = Collections.synchronizedCollection(new ArrayList<Match>());
			new Thread(new ScriptActionThread()).start();
		}
		Match m = new RegexMatch(text);
		m.setAttribute("action", action);
		m.setAttribute("name", text);
		synchronized(actions) {
			actions.add(m);
		}
	}
	
	public void clearActions() {
		actions = null;
	}
	
	public void removeAction(String text) {
		synchronized(actions) {
			Iterator<Match> iter = actions.iterator();
			while(iter.hasNext()) {
				// remove the element with the same name as text
				if(iter.next().getAttribute("name").equals(text)) {
					iter.remove();
				}
			}
			if(actions.size() == 0) {
				actions = null;
			}
		}
	}
	
}
