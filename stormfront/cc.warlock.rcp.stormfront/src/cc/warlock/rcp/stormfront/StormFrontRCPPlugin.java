package cc.warlock.rcp.stormfront;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import cc.warlock.core.stormfront.client.internal.StormFrontClient;
import cc.warlock.rcp.plugin.Warlock2Plugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class StormFrontRCPPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "cc.warlock.rcp.stormfront";

	// The shared instance
	private static StormFrontRCPPlugin plugin;
	
	/**
	 * The constructor
	 */
	public StormFrontRCPPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		// force-load our initial client so we can do offline scripting
		Warlock2Plugin.getDefault().addClient(new StormFrontClient());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static StormFrontRCPPlugin getDefault() {
		return plugin;
	}

}
