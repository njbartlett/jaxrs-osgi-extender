package name.njbartlett.osgi.jaxrsextender.utils;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

public class BundleUtils {
	public static final Bundle findHighestBundle(BundleContext context, String symbolicName) {
		Bundle matched = null;
		Version matchedVersion = null;
		Bundle[] bundles = context.getBundles();
		for (Bundle bundle : bundles) {
			String name = bundle.getSymbolicName();
			Version version = getBundleVersion(bundle);
			if (symbolicName.equals(name)) {
				if (matched == null || version.compareTo(matchedVersion) > 0) {
					matched = bundle;
					matchedVersion = version;
				}
			}
		}
		return matched;
	}

	public static Version getBundleVersion(Bundle bundle) {
		String versionStr = (String) bundle.getHeaders().get(Constants.BUNDLE_VERSION);
		Version version = versionStr != null ? new Version(versionStr) : new Version(0, 0, 0);
		return version;
	}
}
