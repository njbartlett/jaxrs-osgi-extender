package name.njbartlett.osgi.jaxrsextender;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.osgi.framework.Bundle;

public class BundleApplication extends Application {
	private final Set<Class<?>> classes;

	public BundleApplication(Bundle bundle, Set<Class<?>> classes) {
		this.classes = classes;
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		return this.classes;
	}
}
