package cc.warlock.build;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;

public class ProgressCopyTask extends Copy {

	protected String name = "DEFAULT";
	protected String bar;
	
	protected void doFileOperations ()
	{
		if (name == null)
		{
			throw new BuildException("No name was specified for this progress!");
		}
		
		if (bar == null)
		{
			throw new BuildException("No progress bar was specified for this progress!");
		}
		
		ProgressDialog dialog = ProgressDialog.getProgressDialog(name);
		
		if (dialog == null)
		{
			throw new BuildException("A dialog for the name \"" + name + "\" could not be found");
		}

		panel = dialog.getPanel(bar);
		if (panel == null)
		{
			throw new BuildException("A progress bar for the name \"" + bar + "\" could not be  found");
		}
		
		panel.setMin(0);
		panel.setMax(fileCopyMap.size()+1);
		panel.setCurrent(0);
		
		loggingToProgress = true;
		super.doFileOperations();
		loggingToProgress = false;
	}
	
	protected boolean loggingToProgress = false;
	protected ProgressPanel panel;
	
	public void log(String msg, int msgLevel) {
		if (loggingToProgress)
		{
			panel.setCurrent(panel.getCurrent()+1);
			panel.setMessage(msg);
		}
		else super.log(msg, msgLevel);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBar() {
		return bar;
	}

	public void setBar(String bar) {
		this.bar = bar;
	}
}
