/*
 * Created on Jan 15, 2005
 */
package cc.warlock.core.client.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.prefs.Preferences;

import cc.warlock.core.client.ICommand;
import cc.warlock.core.client.ICommandHistory;
import cc.warlock.core.client.ICommandHistoryListener;


/**
 * @author Marshall
 * 
 * A Command History implementation
 */
public class CommandHistory implements ICommandHistory {

	protected int position = 0;
	protected Stack<ICommand> commands = new Stack<ICommand>();
	protected ArrayList<ICommandHistoryListener> listeners = new ArrayList<ICommandHistoryListener>();
	static Preferences prefs = Preferences.userNodeForPackage(Command.class);
	
	public CommandHistory () {
		// load saved history
		byte[] array = prefs.getByteArray("command", null);
		if(array != null) {
			ByteArrayInputStream bytes = new ByteArrayInputStream(array);
			try {
				ObjectInputStream stream = new ObjectInputStream(bytes);
				commands = (Stack<ICommand>)stream.readObject();
				stream.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public ICommand getLastCommand() {
		try {
			return commands.peek();
		} catch(EmptyStackException e) {
			return null;
		}
	}

	public ICommand prev() {
		try {
			if (position > 0)
				position--;
			
			ICommand command = commands.get(position);
			for (ICommandHistoryListener listener : listeners) listener.historyPrevious(command);
			
			return command;
		} catch(ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ICommand next() {

		try {
			if (position < commands.size() - 1)
				position++;
			
			ICommand command = commands.get(position);
			for (ICommandHistoryListener listener : listeners) listener.historyNext(command);
			
			return command;
		} catch(ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ICommand current () {
		try {
			ICommand command = commands.get(position);
			return command;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int size() {
		return commands.size();
	}
	
	public ICommand getCommandAt(int position) {
		return commands.elementAt(position);
	}
	
	public void resetPosition() {
		position = commands.size() - 1;
		
		for (ICommandHistoryListener listener : listeners) listener.historyReset(current());
	}
	
	public void addCommand (String string)
	{
		addCommand(new Command(string, new Date()));
	}
	
	public void addCommand(ICommand command) {
		if (commands.size() == 0) {
			command.setFirst(true);
			command.setLast(true);
		}
		else {
			commands.get(commands.size()-1).setLast(false);
			command.setLast(true);
		}
		
		command.setInHistory(true);
		commands.push(command);
		
		resetPosition();
		for (ICommandHistoryListener listener : listeners) listener.commandAdded(command);
	}
	
	public void save () {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream stream = new ObjectOutputStream(bytes);
			stream.writeObject(commands);
			stream.flush();
			stream.close();
			prefs.putByteArray("command", bytes.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addCommandHistoryListener(ICommandHistoryListener listener) {
		listeners.add(listener);
	}
	
	public void removeCommandHistoryListener(ICommandHistoryListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}
}