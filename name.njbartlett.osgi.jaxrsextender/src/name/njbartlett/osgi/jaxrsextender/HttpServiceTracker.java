/*******************************************************************************
 * Copyright (c) 2010 Neil Bartlett.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Neil Bartlett - initial API and implementation
 ******************************************************************************/
package name.njbartlett.osgi.jaxrsextender;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class HttpServiceTracker extends ServiceTracker {
	
	private final LogService log;

	public HttpServiceTracker(BundleContext context, LogService log) {
		super(context, HttpService.class.getName(), null);
		this.log = log;
	}
	
	@Override
	public Object addingService(ServiceReference reference) {
		HttpService httpService = (HttpService) context.getService(reference);
		
		log.log(LogService.LOG_INFO, "Starting to track JAX-RS bundles");
		
		ResourceBundleTracker bundleTracker = new ResourceBundleTracker(context, httpService, log);
		bundleTracker.open();
		
		context.addServiceListener(bundleTracker);
		
		return bundleTracker;
	}
	
	@Override
	public void removedService(ServiceReference reference, Object service) {
		log.log(LogService.LOG_INFO, "Stopping tracking JAX-RS bundles");
		
		ResourceBundleTracker bundleTracker = (ResourceBundleTracker) service;
		bundleTracker.close();
		
		context.removeServiceListener(bundleTracker);
		
		context.ungetService(reference);
	}
}
