package org.example.jaxrs.hello;

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

@Provider
@Produces("text/plain")
public class HelloSampleMessageBodyWriter implements MessageBodyWriter<HelloSample> {
 
	@Override
	public long getSize(HelloSample sample, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}
	
	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotation, MediaType mediaType) {
		return HelloSample.class.equals(clazz);
	}
	
	@Override
	public void writeTo(HelloSample sample, Class<?> clazz, Type type,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> headers, OutputStream output)
			throws IOException, WebApplicationException {
		
		output.write(sample.getString().getBytes("UTF-8"));
		
	}
	
}
