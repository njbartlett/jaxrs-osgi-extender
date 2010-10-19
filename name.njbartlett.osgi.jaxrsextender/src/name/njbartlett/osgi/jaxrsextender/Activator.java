package name.njbartlett.osgi.jaxrsextender;

import name.njbartlett.osgi.utils.LogTracker;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private LogTracker logTracker;
	private HttpServiceTracker httpTracker;

	@Override
	public void start(BundleContext context) throws Exception {
		logTracker = new LogTracker(context);
		logTracker.open();
		
		httpTracker = new HttpServiceTracker(context, logTracker);
		httpTracker.open();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		httpTracker.close();
		logTracker.close();
	}

}
