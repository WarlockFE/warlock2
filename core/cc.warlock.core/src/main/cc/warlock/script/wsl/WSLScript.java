package cc.warlock.script.wsl;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import cc.warlock.script.AbstractScript;
import cc.warlock.script.IScriptCommands;
import cc.warlock.script.IScriptListener;
import cc.warlock.script.Match;

public class WSLScript extends AbstractScript {
	
	protected String script, scriptName;
	protected boolean running, stopped;
	protected HashMap<String, WSLScriptLine> labels = new HashMap<String, WSLScriptLine>();
	protected WSLScriptLine nextLine;
	protected WSLScriptLine curLine;
	protected WSLScriptLine endLine;
	protected HashMap<String, String> variables = new HashMap<String, String>();
	protected Stack<WSLScriptLine> callstack = new Stack<WSLScriptLine>();
	protected HashMap<String, WSLCommand> wslCommands = new HashMap<String, WSLCommand>();
	protected int pauseLine;
	protected Thread scriptThread;
	private ArrayList<Match> matchset = new ArrayList<Match>();
	
	private final Lock lock = new ReentrantLock();
	private final Condition gotResume = lock.newCondition();
	
	private static final String argSeparator = "\\s+";
	
	public WSLScript (IScriptCommands commands, String scriptName, Reader scriptReader)
		throws IOException
	{
		super(commands);
		
		// add command handlers
		addCommand("put", new WSLPut());
		addCommand("echo", new WSLEcho());
		addCommand("pause", new WSLPause());
		addCommand("shift", new WSLShift());
		addCommand("save", new WSLSave());
		addCommand("counter", new WSLCounter());
		addCommand("deletevariable", new WSLDeleteVariable());
		addCommand("setvariable", new WSLSetVariable());
		addCommand("goto", new WSLGoto());
		WSLCall call = new WSLCall();
		addCommand("call", call);
		addCommand("gosub", call);
		addCommand("random", new WSLRandom());
		addCommand("return", new WSLReturn());
		addCommand("matchwait", new WSLMatchWait());
		addCommand("matchre", new WSLMatchRe());
		addCommand("match", new WSLMatch());
		addCommand("waitforre", new WSLWaitForRe());
		addCommand("waitfor", new WSLWaitFor());
		addCommand("wait", new WSLWait());
		addCommand("move", new WSLMove());
		addCommand("nextroom", new WSLNextRoom());
		addCommand("exit", new WSLExit());
		// TODO change these to be added/removed as variables are set/deleted
		for(int i = 1; i <= 9; i++) {
			String var = Integer.toString(i);
			addCommand("if_" + var, new WSLIf_(var));
		}
		
		this.scriptName = scriptName;
		
		StringBuffer script = new StringBuffer();
		
		char[] bytes = new char[1024];
		int size = 0;
		
		while (size != -1)
		{	
			size = scriptReader.read(bytes);
			if (size != -1)
				script.append(bytes, 0, size);
		}
		scriptReader.close();
		
		this.script = script.toString();
	}
	
	public String getName() {
		return scriptName;
	}

	public Map<String, String> getVariables() {
		return variables;
	}
	
	public Map<String, WSLCommand> getCommands() {
		return wslCommands;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	private class ScriptRunner  implements Runnable {
		public void run() {
			doStart();
			
			while(curLine != null && !stopped) {
				checkState();
				nextLine = curLine.getNext();
				
				curLine.execute();
				curLine = nextLine;
			}
			
			if(!stopped)
				stop();
		}
	}
	
	public void start (ArrayList<String> arguments)
	{
		for (int i = 0; i < arguments.size(); i++) {
			variables.put(Integer.toString(i + 1), arguments.get(i));
		}
		
		for (String varName : commands.getClient().getServerSettings().getVariableNames())
		{
			variables.put(varName, commands.getClient().getServerSettings().getVariable(varName));
		}
		
		scriptThread = new Thread(new ScriptRunner());
		scriptThread.setName("Wizard Script: " + scriptName);
		scriptThread.start();
		
		for (IScriptListener listener : listeners) listener.scriptStarted(this);
	}
	
	protected void doStart ()
	{
		CharStream input = new ANTLRStringStream(script + "\n");
		WSLLexer lex = new WSLLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lex);
		WSLParser parser = new WSLParser(tokens);
		
		parser.setScript(this);
		
		try {
			parser.script();
		} catch (RecognitionException e) {
			// TODO handle the exception
		}

		commands.echo("[script started: " + scriptName + "]");
		running = true;
		stopped = false;
	}
	
	private void checkState() {
		while(!running && !stopped) {
			lock.lock();
			try {
				gotResume.await();
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
		}
	}
	
	public void addLabel(String label, WSLScriptLine line) {
		labels.put(label.toLowerCase(), line);
	}
	
	public void addLine(WSLScriptLine line) {
		if(curLine == null) {
			curLine = line;
		}
		if(endLine != null) {
			endLine.setNext(line);
		}
		endLine = line;
	}
	
	public void stop() {
		running = false;
		stopped = true;
		commands.stop();
		
		commands.echo("[script stopped: " + scriptName + "]");
		super.stop();
	}

	public void suspend() {
		running = false;
		//pauseLine = nextLine;
		
		commands.echo("[script paused: " + scriptName + "]");
		super.suspend();
	}
	
	public void resume() {
		
		//nextLine = pauseLine;
		running = true;
		
		commands.echo("[script resumed: " + scriptName + "]");

		super.resume();
		
		lock.lock();
		try {
			gotResume.signalAll();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	protected void addCommand (String name, WSLCommand command) {
		wslCommands.put(name, command);
	}
	
	abstract protected class WSLCommand {
		
		abstract public void execute(String arguments);
		
	}
	
	protected class WSLSave extends WSLCommand {
		
		public void execute(String arguments) {
			variables.put("s", arguments);
		}
	}

	protected class WSLShift extends WSLCommand {
		
		public void execute (String arguments) {
			for (int i = 0; ; i++) {
				String arg = variables.get(Integer.toString(i + 1));
				if (arg == null) {
					variables.remove(Integer.toString(i));
					break;
				}
				variables.put(Integer.toString(i), arg);
			}
		}
	}

	protected class WSLCounter extends WSLCommand {
		
		public void execute (String arguments) {
			String[] args = arguments.split(argSeparator);
			if (args.length >= 2)
			{
				String counterFunction = args[0];
				int value = variables.containsKey("c") ? Integer.parseInt(variables.get("c")) : 0;

				if ("set".equalsIgnoreCase(counterFunction))
				{
					variables.put("c", args[1]);
				}
				else if ("add".equalsIgnoreCase(counterFunction))
				{	
					int newValue = value + Integer.parseInt(args[1]);
					variables.put("c", "" + newValue);
				}
				else if ("subtract".equalsIgnoreCase(counterFunction))
				{
					int newValue = value - Integer.parseInt(args[1]);
					variables.put("c", "" + newValue);
				}
				else if ("multiply".equalsIgnoreCase(counterFunction))
				{
					int newValue = value * Integer.parseInt(args[1]);
					variables.put("c", "" + newValue);
				}
				else if ("divide".equalsIgnoreCase(counterFunction))
				{
					int newValue = value / Integer.parseInt(args[1]);
					variables.put("c", "" + newValue);
				}
			} else { /*throw error */ }
		}
	}

	protected class WSLDeleteVariable extends WSLCommand {
		
		public void execute (String arguments) {
			String var = arguments.split(argSeparator)[0];
			variables.remove(var);
		}
	}

	private void setVariable(String name, String value) {
		variables.put(name, value);
	}
	
	protected class WSLSetVariable extends WSLCommand {
		
		private Pattern format = Pattern.compile("^([\\w_]+)\\s+(.*)");
		
		public void execute (String arguments) {
			Matcher m = format.matcher(arguments);
			if (m.matches())
			{
				// System.out.print("variable: \"" + m.group(1) + "\" value: \"" + m.group(2) + "\"\n");
				setVariable(m.group(1), m.group(2));
			} else {
				// System.out.print("Didn't match \"" + arguments + "\"\n");
			}
		}
	}
	
	protected void gotoLabel (String label)
	{
		// System.out.println("going to label: \"" + label + "\"");
		
		WSLScriptLine command = labels.get(label.toLowerCase());
		
		if (command != null)
		{
			// System.out.println("found label");
			curLine = nextLine = command;
		}
		else {
			// System.out.println("label not found");
			command = labels.get("labelerror");
			if (command != null)
			{
				curLine = nextLine = command;
			}
			else { // TODO: Fix gotoLabel to throw an exception instead of outputting to user
				commands.echo ("***********");
				commands.echo ("*** WARNING: Label \"" + label + "\" doesn't exist, skipping goto statement ***");
				commands.echo ("***********");
			}
		}
	}
	
	protected class WSLGoto extends WSLCommand {
		
		public void execute (String arguments) {
			String[] args = arguments.split(argSeparator);
			if (args.length >= 1)
			{
				String label = args[0];
				gotoLabel(label);
			} else { /*throw error*/ }
		}
	}
	
	protected void callLabel (String label, String arguments)
	{
		String[] args = arguments.split(argSeparator);
		
		// TODO save previous state of variables
		setVariable("$0", arguments);
		for(int i = 0; i < args.length; i++) {
			setVariable("$" + (i + 1), args[i]);
		}
		
		callstack.push(nextLine);
		gotoLabel(label);
	}
	
	protected class WSLCall extends WSLCommand {
		
		private Pattern format = Pattern.compile("^([\\w_]+)\\s*(.*)?");
		
		public void execute (String arguments) {
			Matcher m = format.matcher(arguments);
			
			if (m.matches())
			{
				System.out.println("calling label " + m.group(1));
				callLabel(m.group(1), m.group(2));
			} else {
				System.out.println("label not found");
				/*throw error*/ 
			}
		}
	}
	
	protected void callReturn () {
		if (callstack.empty()) {
			commands.echo ("***********");
			commands.echo ("*** WARNING: No outstanding calls were executed, skipping return statement ***");
			commands.echo ("***********");
		} else {
			curLine = nextLine = callstack.pop();
		}
	}
	
	protected class WSLReturn extends WSLCommand {
		
		public void execute (String arguments) {
			callReturn();
		}
	}

	protected class WSLMatchWait extends WSLCommand {
		
		public void execute (String arguments) {
			// mode = Mode.waiting;
			
			Match match = commands.matchWait(matchset);
			
			if (match != null)
			{
				// System.out.println("matched label: \"" + match.getAttribute("label") + "\"");
				matchset.clear();
				gotoLabel((String)match.getAttribute("label"));
				commands.waitForPrompt();
				commands.waitForRoundtime();
			} else {
				if(!stopped)
					commands.echo("*** Internal error, no match was found!! ***\n");
			}
		}
	}

	protected class WSLMatchRe extends WSLCommand {
		
		private Pattern format = Pattern.compile("^([\\w_]+)\\s+/(.*)/(\\w*)");
		
		public void execute (String arguments) {
			Matcher m = format.matcher(arguments);
			
			if (m.matches())
			{
				String regex = m.group(2);
				Match match = new Match();
				
				if (m.group(3).contains("i"))
				{
					match.setRegex(regex, true);
				} else {
					match.setRegex(regex, false);
				}
				
				match.setAttribute("label", m.group(1));
				
				matchset.add(match);
			} else { /* TODO throw error */ }
		}

	}

	protected class WSLMatch extends WSLCommand {
		
		private Pattern format = Pattern.compile("^([\\w_]+)\\s+(.*)");
		
		public void execute (String arguments) {
			Matcher m = format.matcher(arguments);
			
			if (m.matches())
			{
				Match match = new Match();
				match.setAttribute("label", m.group(1));
				match.setMatchText(m.group(2));
				
				// System.out.println("adding match \"" + m.group(1) + "\": \"" + m.group(2) + "\"");
				
				matchset.add(match);
			} else { /* TODO throw error */ }
		}
	}

	protected class WSLWaitForRe extends WSLCommand {
		
		private Pattern format = Pattern.compile("^/(.*)/(\\w*)");
		
		public void execute (String arguments) {
			Matcher m = format.matcher(arguments);
			
			if (m.matches())
			{
				String flags = m.group(2);
				boolean ignoreCase = false;
				
				if (flags != null && flags.contains("i"))
				{
					ignoreCase = true;
				}
				
				Match match = new Match();
				match.setRegex(m.group(1), ignoreCase);
				
				commands.waitFor(match);
			} else { /* TODO throw error */ }
		}
	}
	
	protected class WSLWaitFor extends WSLCommand {
		
		public void execute (String arguments) {
			if (arguments.length() >= 1)
			{
				Match match = new Match();
				match.setMatchText(arguments);
				commands.waitFor(match);
				
			} else { /* TODO throw error */ }
		}
	}

	protected class WSLWait extends WSLCommand {
		
		public void execute (String arguments) {
			commands.waitForPrompt();
		}
	}
	
	protected class WSLPut extends WSLCommand {
		
		public void execute(String arguments) {
			commands.put(WSLScript.this, arguments);
		}
	}
	
	protected class WSLEcho extends WSLCommand {
		
		public void execute (String arguments)
		{
			commands.echo(WSLScript.this, arguments);
		}
	}
	
	protected class WSLPause extends WSLCommand {
		
		public void execute (String arguments)
		{
			String[] args = arguments.split(argSeparator);
			int time = 1;
			
			if (args.length >= 1)
			{
				try {
					time = Integer.parseInt(args[0]);
				} catch(NumberFormatException e) {
					// time = 1;
				}
			} else {
				// "empty" pause.. means wait 1 second
			}
			commands.pause(time);
		}
	}
	
	protected class WSLMove extends WSLCommand {
		
		public void execute (String arguments)
		{
			commands.move(arguments);
		}
	}
	
	protected class WSLNextRoom extends WSLCommand {
		
		public void execute (String arguments)
		{
			commands.nextRoom();
		}
	}
	
	protected class WSLExit extends WSLCommand {
		
		public void execute (String arguments) {
			running = false;
			stopped = true;
			
			// TODO figure out if we should make this call here or elsewhere
			stop();
		}
	}
	
	protected class WSLIf_ extends WSLCommand {
		
		protected String variableName;
		private Pattern format = Pattern.compile("^([\\w_]+)\\s+(.*)");
		
		public WSLIf_ (String variableName) {
			this.variableName = variableName;
		}
		
		public void execute (String arguments) {
			if (variables.containsKey(variableName)) {
				Matcher m = format.matcher(arguments);
				
				if(m.matches()) {
					String curCommandName = m.group(1).toLowerCase();
				
					WSLCommand command = wslCommands.get(curCommandName);
					if(command != null) {
						command.execute(m.group(2));
					}
					// else this acts as a comment
				}
			}
		}
	}
	
	private class WSLRandom extends WSLCommand {
		
		private Pattern format = Pattern.compile("^(\\d+)\\s+(\\d+)");
		
		public void execute(String arguments) {
			Matcher m = format.matcher(arguments);
			
			if(m.matches()) {
				int min = Integer.parseInt(m.group(1));
				int max = Integer.parseInt(m.group(2));
				int r = min + new Random().nextInt(max - min + 1);
				
				setVariable("r", Integer.toString(r));
			} else {
				// print an error?
			}
		}
	}
	
	private void handleDeleteFromHighlightNames(List<String> arguments) {
		// TODO Auto-generated method stub
		
	}

	private void handleDeleteFromHighlightStrings(List<String> arguments) {
		// TODO Auto-generated method stub
		
	}

	private void handleAddToHighlightStrings(List<String> arguments) {
		// TODO Auto-generated method stub
	}
	
	public void stopScript() {
		stopped = true;
	}
}
