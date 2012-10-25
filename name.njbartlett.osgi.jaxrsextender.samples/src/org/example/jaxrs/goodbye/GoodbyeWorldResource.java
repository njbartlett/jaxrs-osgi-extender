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
package org.example.jaxrs.goodbye;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import aQute.bnd.annotation.component.Component;

@Component(provide=GoodbyeWorldResource.class)
@Path("/goodbyeworld")
public class GoodbyeWorldResource {
	
	@GET
	@Produces("text/plain")
	public String getMessage() {
		return "Goodbye world!";
	}
	
	@GET
	@Path("/object")
	public GoodbyeSample getSample() {
		return new GoodbyeSample("Goodbye World from a Sample object!");
	}
	
}
