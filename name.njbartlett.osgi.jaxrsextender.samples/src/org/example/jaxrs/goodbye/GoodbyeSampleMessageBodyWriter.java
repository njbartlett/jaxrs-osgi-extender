package org.example.jaxrs.goodbye;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import aQute.bnd.annotation.component.Component;

@Component(provide=GoodbyeSampleMessageBodyWriter.class)
@Provider
@Produces("text/plain")
public class GoodbyeSampleMessageBodyWriter implements MessageBodyWriter<GoodbyeSample> {
 
	@Override
	public long getSize(GoodbyeSample sample, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}
	
	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotation, MediaType mediaType) {
		return GoodbyeSample.class.equals(clazz);
	}
	
	@Override
	public void writeTo(GoodbyeSample sample, Class<?> clazz, Type type,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> headers, OutputStream output)
			throws IOException, WebApplicationException {

		output.write(sample.getString().getBytes("UTF-8"));
		
	}
		
}
