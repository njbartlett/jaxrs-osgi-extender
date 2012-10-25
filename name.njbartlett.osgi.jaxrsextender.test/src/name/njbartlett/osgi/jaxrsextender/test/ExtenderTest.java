package name.njbartlett.osgi.jaxrsextender.test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import name.njbartlett.osgi.jaxrsextender.utils.StreamUtil;

import org.easymock.Capture;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import aQute.junit.runtime.OSGiTestCase;

public class ExtenderTest extends OSGiTestCase {

	private static final String SAMPLE_BUNDLE_URL_PREFIX = "file:../name.njbartlett.osgi.jaxrsextender.samples/generated/";
	private static final String SAMPLE_BUNDLE_URL_1 = SAMPLE_BUNDLE_URL_PREFIX + "name.njbartlett.osgi.jaxrsextender.samples.sample1.jar";
	private static final String SAMPLE_BUNDLE_URL_2 = SAMPLE_BUNDLE_URL_PREFIX + "name.njbartlett.osgi.jaxrsextender.samples.sample2.jar";

	private HttpService mockHttpSvc;

	@Override
	protected void setUp() throws Exception {
		mockHttpSvc = createStrictMock(HttpService.class);
	}

	/**
	 * Test starting the HttpService before any resource bundles. Procedure:
	 * <ol>
	 * <li>Register the HttpService. Expect no calls.</li>
	 * <li>Install a resource bundle. Expect registerServlet() called.</li>
	 * <li>Verify that resources in the bundle are accessible through the
	 * supplied HttpContext.</li>
	 * <li>Uninstall the resource bundle. Expect unregister() to be called.</li>
	 * <li>Unregister the HttpService. Expect no calls.</li>
	 * </ol>
	 * 
	 * @throws Exception
	 */
	public void testServletRegistrationFirst() throws Exception {
		// Register HttpService
		reset(mockHttpSvc);
		replay(mockHttpSvc);
		ServiceRegistration svcReg = getBundleContext().registerService(HttpService.class.getName(), mockHttpSvc, null);
		verify(mockHttpSvc);

		// Install bundle
		reset(mockHttpSvc);
		Capture<HttpContext> contextCapture = recordRegisterServlet("/samples");
		replay(mockHttpSvc);
		Bundle resourceBundle = getBundleContext().installBundle(SAMPLE_BUNDLE_URL_1);
		resourceBundle.start();
		verify(mockHttpSvc);

		verifyHttpContext(contextCapture.getValue());

		// Unregister HttpService
		reset(mockHttpSvc);
		mockHttpSvc.unregister("/samples");
		replay(mockHttpSvc);
		resourceBundle.uninstall();
		verify(mockHttpSvc);

		// Unregister HttpService
		reset(mockHttpSvc);
		replay(mockHttpSvc);
		svcReg.unregister();
		verify(mockHttpSvc);
	}

	/**
	 * Test installing/starting the resource bundle before the HttpService
	 * <ol>
	 * <li>Install a resource bundle. Expect no calls.</li>
	 * <li>Register the HttpService. Expect registerServlet() to be called.</li>
	 * <li>Verify that resources in the bundle are accessible through the
	 * supplied HttpContext.</li>
	 * <li>Unregister the HttpService. Expect unregister.</li>
	 * <li>Uninstall the resource bundle. Expect no calls..</li>
	 * </ol>
	 * 
	 * @throws Exception
	 */
	public void testBundleInstallationFirst() throws Exception {
		// Install resource bundle
		reset(mockHttpSvc);
		replay(mockHttpSvc);
		Bundle resourceBundle = getBundleContext().installBundle(SAMPLE_BUNDLE_URL_1);
		resourceBundle.start();
		verify(mockHttpSvc);

		// Register HttpService
		reset(mockHttpSvc);
		Capture<HttpContext> contextCapture = recordRegisterServlet("/samples");
		replay(mockHttpSvc);
		ServiceRegistration svcReg = getBundleContext().registerService(HttpService.class.getName(), mockHttpSvc, null);
		verify(mockHttpSvc);

		verifyHttpContext(contextCapture.getValue());

		// Unregister service
		reset(mockHttpSvc);
		mockHttpSvc.unregister("/samples");
		replay(mockHttpSvc);
		svcReg.unregister();
		verify(mockHttpSvc);

		// Uninstall resource bundle
		reset(mockHttpSvc);
		replay(mockHttpSvc);
		resourceBundle.uninstall();
		verify(mockHttpSvc);
	}

	/**
	 * Try starting/stopping the resource bundle while it is being tracked. Procedure:
	 * <ol>
	 * <li>Install/start a resource bundle.
	 * <li>Register the HttpService. Expect a servlet to be registered.
	 * <li>Deactivate the resource bundle. Expect the servlet to be
	 * unregistered.
	 * <li>Reactivate the resource bundle. Expect the servlet to be registered
	 * again.
	 * <li>Unregister the HttpService. Expect the servlet to be unregistered
	 * again.
	 * </ol>
	 */
	public void testBundleTracking() throws ServletException, NamespaceException, BundleException {
		Bundle resourceBundle = getBundleContext().installBundle(SAMPLE_BUNDLE_URL_1);
		resourceBundle.start();

		reset(mockHttpSvc);
		recordRegisterServlet("/samples");
		replay(mockHttpSvc);
		ServiceRegistration svcReg = getBundleContext().registerService(HttpService.class.getName(), mockHttpSvc, null);
		verify(mockHttpSvc);

		reset(mockHttpSvc);
		mockHttpSvc.unregister("/samples");
		replay(mockHttpSvc);
		resourceBundle.stop();
		verify(mockHttpSvc);

		reset(mockHttpSvc);
		recordRegisterServlet("/samples");
		replay(mockHttpSvc);
		resourceBundle.start();
		verify(mockHttpSvc);

		reset(mockHttpSvc);
		mockHttpSvc.unregister("/samples");
		replay(mockHttpSvc);
		svcReg.unregister();
		verify(mockHttpSvc);

		reset(mockHttpSvc);
		resourceBundle.uninstall();
	}

	private Capture<HttpContext> recordRegisterServlet(String alias) throws ServletException, NamespaceException {
		Capture<HttpContext> contextCapture = new Capture<HttpContext>();
		mockHttpSvc.registerServlet(eq(alias), notNull(HttpServlet.class), anyObject(Dictionary.class), capture(contextCapture));
		return contextCapture;
	}

	private void verifyHttpContext(HttpContext httpContext) throws IOException {
		URL resourceURL = httpContext.getResource("hello.txt");
		assertNotNull(resourceURL);
		String resourceContent = StreamUtil.readFully(resourceURL.openStream());
		assertEquals("Hello world!", resourceContent);
	}

}
