package name.njbartlett.osgi.jaxrsextender.test;

import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import name.njbartlett.osgi.jaxrsextender.utils.BundleUtils;
import name.njbartlett.osgi.jaxrsextender.utils.StreamUtil;

import org.easymock.Capture;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import bndtools.runtime.junit.OSGiTestCase;

public class ExtenderTest extends OSGiTestCase {
	
	private HttpService mockHttpSvc;

	@Override
	protected void setUp() throws Exception {
		mockHttpSvc = createStrictMock(HttpService.class);
	}

	public void testServletRegistration() throws ServletException, NamespaceException, IOException {
		// We will register and unregister an HttpService. We expect to see a servlet
		// registration come and go.
		Capture<HttpContext> contextCapture = new Capture<HttpContext>();
		mockHttpSvc.registerServlet(eq("/samples"), notNull(HttpServlet.class), anyObject(Dictionary.class), capture(contextCapture));
		mockHttpSvc.unregister("/samples");
		replay(mockHttpSvc);
		
		// Register the service
		ServiceRegistration svcReg = getBundleContext().registerService(HttpService.class.getName(), mockHttpSvc, null);
		
		// Check that we can read resources from the bundle via the supplied HttpContext
		HttpContext httpContext = contextCapture.getValue();
		URL resourceURL = httpContext.getResource("hello.txt");
		assertNotNull(resourceURL);
		String resourceContent = StreamUtil.readFully(resourceURL.openStream());
		assertEquals("Hello world!", resourceContent);
		
		// Unregister the service
		svcReg.unregister();
		
		// Verify
		verify(mockHttpSvc);
	}
	
	public void testBundleTracking() throws ServletException, NamespaceException, BundleException {
		/*
		 * Expectations:
		 * 1. We register the HttpService... expect a servlet to be registered.
		 * 2. We deacivate the bundle containing the resources... expect the servlet to be unregistered.
		 * 3. We reactivate the bundle containing the resources... expec the servlet to be registered again.
		 * 4. We unregister the HttpService... expect the servlet to be unregistered again.
		 */
		mockHttpSvc.registerServlet(eq("/samples"), notNull(HttpServlet.class), anyObject(Dictionary.class), anyObject(HttpContext.class));
		mockHttpSvc.unregister("/samples");
		mockHttpSvc.registerServlet(eq("/samples"), notNull(HttpServlet.class), anyObject(Dictionary.class), anyObject(HttpContext.class));
		mockHttpSvc.unregister("/samples");
		replay(mockHttpSvc);
		
		// Do it
		ServiceRegistration svcReg = getBundleContext().registerService(HttpService.class.getName(), mockHttpSvc, null);

		Bundle bundle = BundleUtils.findHighestBundle(getBundleContext(), "name.njbartlett.osgi.jaxrsextender.samples");
		bundle.stop();
		bundle.start();
		
		svcReg.unregister();

		verify(mockHttpSvc);
	}

}
