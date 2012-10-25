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
package org.example.jaxrs.hello;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/helloworld")
public class HelloWorldResource {
	
	@GET
	@Produces("text/plain")
	public String getMessage() {
		return "Hello world!"; 
	}
	
	@GET
	@Path("/object")
	public HelloSample getSample() {
		return new HelloSample("Hello World from a Sample object!");
	}
	
}
