/*
 * Created on Mar 26, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package cc.warlock.core.stormfront.client.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cc.warlock.core.client.ICharacterStatus;
import cc.warlock.core.client.ICompass;
import cc.warlock.core.client.IProperty;
import cc.warlock.core.client.IWarlockSkin;
import cc.warlock.core.client.IWarlockStyle;
import cc.warlock.core.client.WarlockClientRegistry;
import cc.warlock.core.client.internal.CharacterStatus;
import cc.warlock.core.client.internal.ClientProperty;
import cc.warlock.core.client.internal.Compass;
import cc.warlock.core.client.internal.WarlockClient;
import cc.warlock.core.client.internal.WarlockStyle;
import cc.warlock.core.script.IScript;
import cc.warlock.core.script.IScriptListener;
import cc.warlock.core.script.ScriptEngineRegistry;
import cc.warlock.core.stormfront.IStormFrontProtocolHandler;
import cc.warlock.core.stormfront.client.IStormFrontClient;
import cc.warlock.core.stormfront.network.StormFrontConnection;
import cc.warlock.core.stormfront.serversettings.server.ServerSettings;
import cc.warlock.core.stormfront.serversettings.skin.DefaultSkin;
import cc.warlock.core.stormfront.serversettings.skin.IStormFrontSkin;

import com.martiansoftware.jsap.CommandLineTokenizer;

/**
 * @author Marshall
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StormFrontClient extends WarlockClient implements IStormFrontClient, IScriptListener {

	protected ICompass compass;
	protected ICharacterStatus status;
	protected int lastPrompt;
	protected ClientProperty<Integer> roundtime, health, mana, fatigue, spirit;
	protected ClientProperty<String> leftHand, rightHand, currentSpell;
	protected boolean isPrompting = false;
	protected StringBuffer buffer = new StringBuffer();
	protected IStormFrontProtocolHandler handler;
	protected boolean isBold;
	protected ClientProperty<String> playerId, characterName;
	protected IWarlockStyle currentStyle = WarlockStyle.EMPTY_STYLE;
	protected ServerSettings serverSettings;
	protected RoundtimeRunnable rtRunnable;
	protected ArrayList<IScript> runningScripts;
	protected ArrayList<IScriptListener> scriptListeners;
	protected DefaultSkin skin;
	
	public StormFrontClient() {
		compass = new Compass(this);
		status = new CharacterStatus(this);
		leftHand = new ClientProperty<String>(this, "leftHand", null);
		rightHand = new ClientProperty<String>(this, "rightHand", null);
		currentSpell = new ClientProperty<String>(this, "currentSpell", null);
		
		roundtime = new ClientProperty<Integer>(this, "roundtime", 0);
		health = new ClientProperty<Integer>(this, "health", 0);
		mana = new ClientProperty<Integer>(this, "mana", 0);
		fatigue = new ClientProperty<Integer>(this, "fatigue", 0);
		spirit = new ClientProperty<Integer>(this, "spirit", 0);
		playerId = new ClientProperty<String>(this, "playerId", null);
		characterName = new ClientProperty<String>(this, "characterName", null);
		serverSettings = new ServerSettings(this);
		skin = new DefaultSkin(serverSettings);
		rtRunnable = new RoundtimeRunnable();
		runningScripts = new ArrayList<IScript>();
		scriptListeners = new ArrayList<IScriptListener>();
		
		WarlockClientRegistry.activateClient(this);
	}

	@Override
	public void send(String command) {
		if (command.startsWith(".")){
			runScriptCommand(command);
		} else {
			super.send(command);
		}
	}
	
	protected  void runScriptCommand(String command) {
		command = command.substring(1);
		command = command.replaceAll("[\\r\\n]", "");
		
		int firstSpace = command.indexOf(" ");
		String scriptName = command.substring(0, (firstSpace < 0 ? command.length() : firstSpace));
		String[] arguments = new String[0];
		
		if (firstSpace > 0)
		{
			String args = command.substring(firstSpace+1);
			arguments = CommandLineTokenizer.tokenize(args);
		}
		
		IScript script = ScriptEngineRegistry.getScriptForName(scriptName);
		if (script != null)
		{
			script.getScriptEngine().startScript(script, this, arguments);
			
			script.addScriptListener(this);
			for (IScriptListener listener : scriptListeners) listener.scriptStarted(script);
			runningScripts.add(script);
		}
		
		getCommandHistory().addCommand("." + command);
	}
	
	public void scriptAdded(IScript script) {
		for (IScriptListener listener : scriptListeners) listener.scriptAdded(script);
	}
	
	public void scriptRemoved(IScript script) {
		for (IScriptListener listener : scriptListeners) listener.scriptRemoved(script);
	}
	
	public void scriptPaused(IScript script) {
		for (IScriptListener listener : scriptListeners) listener.scriptPaused(script);
	}
	
	public void scriptResumed(IScript script) {
		for (IScriptListener listener : scriptListeners) listener.scriptResumed(script);
	}
	
	public void scriptStarted(IScript script) {
		for (IScriptListener listener : scriptListeners) listener.scriptStarted(script);
	}
	
	public void scriptStopped(IScript script, boolean userStopped) {
		runningScripts.remove(script);
		
		for (IScriptListener listener : scriptListeners) listener.scriptStopped(script, userStopped);
	}
	
	public IProperty<Integer> getRoundtime() {
		return roundtime;
	}

	private class RoundtimeRunnable implements Runnable
	{
		public int roundtime;
		
		public synchronized void run () 
		{
			for (int i = 0; i < roundtime; i++)
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				updateRoundtime(StormFrontClient.this.roundtime.get() - 1);
			}
		}
	}
	
	public void startRoundtime (int seconds)
	{
		roundtime.activate();
		roundtime.set(seconds);
		rtRunnable.roundtime = seconds;
		
		new Thread(rtRunnable).start();
	}
	
	public void updateRoundtime (int currentRoundtime)
	{
		roundtime.set(currentRoundtime);
	}
	
	public IProperty<Integer> getHealth() {
		return health;
	}

	public IProperty<Integer> getMana() {
		return mana;
	}

	public IProperty<Integer> getFatigue() {
		return fatigue;
	}

	public IProperty<Integer> getSpirit() {
		return spirit;
	}

	public ICompass getCompass() {
		return compass;
	}
	
	public void connect(String server, int port, String key) throws IOException {
		connection = new StormFrontConnection(this, key);
		connection.connect(server, port);
		
		WarlockClientRegistry.clientConnected(this);
	}
	
	public void streamCleared() {
		// TODO Auto-generated method stub
		
	}
	
	public void setPrompting() {
		isPrompting = true;
	}
	
	public boolean isPrompting() {
		return isPrompting;
	}
	
	public void setBold(boolean bold) {
		isBold = bold;
	}
	
	public boolean isBold() {
		return isBold;
	}

	public IWarlockStyle getCurrentStyle() {
		return currentStyle;
	}

	public void setCurrentStyle(IWarlockStyle currentStyle) {
		this.currentStyle = currentStyle;
	}

	public ClientProperty<String> getPlayerId() {
		return playerId;
	}
	
	public ServerSettings getServerSettings() {
		return serverSettings;
	}
	
	public IProperty<String> getCharacterName() {
		return characterName;
	}
	
	public IProperty<String> getLeftHand() {
		return leftHand;
	}
	
	public IProperty<String> getRightHand() {
		return rightHand;
	}
	
	public IProperty<String> getCurrentSpell() {
		return currentSpell;
	}
	
	public ICharacterStatus getCharacterStatus() {
		return status;
	}
	
	public List<IScript> getRunningScripts() {
		return runningScripts;
	}
	
	public void addScriptListener(IScriptListener listener)
	{
		scriptListeners.add(listener);
	}
	
	public void removeScriptListener (IScriptListener listener)
	{
		if (scriptListeners.contains(listener))
			scriptListeners.remove(listener);
	}
	
	public IWarlockSkin getSkin() {
		return skin;
	}
	
	public IStormFrontSkin getStormFrontSkin() {
		return skin;
	}
	
	@Override
	protected void finalize() throws Throwable {
		WarlockClientRegistry.removeClient(this);
		super.finalize();
	}
}