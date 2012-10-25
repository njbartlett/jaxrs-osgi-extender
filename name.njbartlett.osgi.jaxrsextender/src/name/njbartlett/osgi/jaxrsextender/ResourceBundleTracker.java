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

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.osgi.framework.AllServiceListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTracker;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class ResourceBundleTracker extends BundleTracker implements AllServiceListener {

	private static final Set<String> JAXRS_CLASSNAMES = new HashSet<String>(
				Arrays.asList(Path.class.getName(), Provider.class.getName()));
	
	private final LogService log;
	private final HttpService httpService;

	public ResourceBundleTracker(BundleContext context,
			HttpService httpService, LogService log) {
		super(context, Bundle.ACTIVE, null);
		this.httpService = httpService;
		this.log = log;
	}

	@Override
	public Object addingBundle(Bundle bundle, BundleEvent event) {
		@SuppressWarnings("unchecked")
		Dictionary<String, String> headers = bundle.getHeaders();
		
		String alias = headers.get(Constants.PROP_JAXRS_ALIAS);
		
		if (alias == null)
			return null; // ignore this bundle
		
		return processBundle(bundle, alias);
	}

	private Object processBundle(Bundle bundle, String alias) {
		try {
			httpService.unregister(alias);
		} catch (IllegalArgumentException e) {
			// could happen if not previously registered
		}

		ServletContainer servlet = processBundle(bundle);
		if (servlet == null) {
			return null;
		}
		
		try {
			log.log(LogService.LOG_INFO, MessageFormat.format("Registering HTTP servlet under alias \"{0}\" for JAX-RS resources in bundle {1}", alias, bundle.getLocation()));
			httpService.registerServlet(alias, servlet, null, new BundleHttpContext(bundle));
			return alias;
		} catch (ServletException e) {
			log.log(LogService.LOG_ERROR, "Error registering servlet.", e);
			return null;
		} catch (NamespaceException e) {
			log.log(LogService.LOG_ERROR, "Error registering servlet.", e);
			return null;
		} catch (ContainerException e) {
			// this happens due to a bug in Jersey if the app only contains Singletons
			log.log(LogService.LOG_WARNING, "Exception while registering servlet.", e);
			return alias;
		}
	}

	@Override
	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
		String alias = (String) object;
		log.log(LogService.LOG_INFO, MessageFormat.format("Unregistering HTTP servlet under alias \"{0}\" for JAX-RS resources in bundle {1}", alias, bundle.getLocation()));
		httpService.unregister(alias);
	}
	
	@Override
	public void serviceChanged(ServiceEvent event) {
		
		Bundle bundle = event.getServiceReference().getBundle();
		String alias = (String)bundle.getHeaders().get(Constants.PROP_JAXRS_ALIAS);
		if (alias == null) {
			return;
		}
		
		processBundle(bundle, alias);
	}
	
	private ServletContainer processBundle(Bundle bundle) {
		String listStr = (String)bundle.getHeaders().get(Constants.PROP_JAXRS_CLASSES);
		StringTokenizer tokenizer = new StringTokenizer(listStr == null ? "" : listStr, ",");
		
		Set<Class<?>> classes = new HashSet<Class<?>>();
		while (tokenizer.hasMoreTokens()) {
			String path = tokenizer.nextToken().trim();

			try {
				classes.add(bundle.loadClass(path));
			} catch (Exception e) {
				log.log(LogService.LOG_ERROR, MessageFormat.format("Error laoding class \"{0}\" from bundle \"{1}\".", path, bundle.getLocation()), e);
			}
		}
		
		Set<Object> singletons = new HashSet<Object>();
		ServiceReference[] refs = bundle.getRegisteredServices();
		if (null != refs) {
			for (ServiceReference ref : refs) {
				Object service = context.getService(ref);
				if (isJaxRsSingleton(service)) {
					log.log(LogService.LOG_INFO, MessageFormat.format("Registering JAX-RS Singleton \"{0}\" for JAX-RS resources in bundle {1}", service, bundle.getLocation()));

					singletons.add(service);
				} else {
					context.ungetService(ref);
				}
			}
		}
		
		if (classes.isEmpty() && singletons.isEmpty()) return null;
		
		BundleApplication application = new BundleApplication(bundle, classes, singletons);
		return new ServletContainer(application);
	}

	private boolean isJaxRsSingleton(Object service) {
		
		for (Annotation annot : service.getClass().getAnnotations()) {
			if (JAXRS_CLASSNAMES.contains(annot.annotationType().getName())) {
				return true;
			}
		}
		
		return false;
	}
	
	
}
