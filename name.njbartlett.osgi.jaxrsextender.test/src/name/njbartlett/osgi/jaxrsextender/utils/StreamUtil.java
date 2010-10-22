package name.njbartlett.osgi.jaxrsextender.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class StreamUtil {

	private StreamUtil() {
	}

	public static final String readFully(InputStream stream) throws IOException {
		StringWriter writer = new StringWriter();
		try {
			InputStreamReader reader = new InputStreamReader(stream);

			char[] buffer = new char[1024];
			while (true) {
				int charsRead = reader.read(buffer, 0, 1024);
				if (charsRead < 0)
					break;
				writer.write(buffer, 0, charsRead);
			}
		} finally {
			stream.close();
		}
		return writer.toString();
	}
}
