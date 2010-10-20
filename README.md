JAX-RS Extender Bundle for OSGi
===============================

This is an OSGi extender bundle that can be used to declaratively create JAX-RS applications. It has the following benefits:

* Dynamically deploy and undeploy JAX-RS applications at runtime.
* Completely declarative... just build the Resource classes and list them in the bundle manifest.
* No OSGi or Jersey code to write... just code against the standard JAX-RS APIs/annotations.
* Your application bundles are decoupled from OSGi and Jersey. They could be used in a non-OSGi environment or with an alternative JAX-RS implementation.

From an application developer's point of view, the extender is extremely simple to use. Simply build a bundle containing the Resource and Provider classes -- marked up with standard JAX-RS annotations -- and add the following two headers to the bundle manifest:

	JAX-RS-Alias: /example
	JAX-RS-Classes: org.example.HelloResource, org.example.GoodbyeResource

`JAX-RS-Alias` is the URL prefix that will be used for all resource in this application. `JAX-RS-Classes` is a comma-separated list of Resource or Provider classes. *N.B.* these classes do not need to be exported from your bundle, and it is recommended to keep them private.

Example
-------

The following example was adapted from the Jersey [Getting Started](https://jersey.dev.java.net/use/getting-started.html) guide:

	package org.example;

	import javax.ws.rs.*;

	@Path("/helloworld")
	public class HelloWorldResource {
		@GET
		@Produces("text/plain")
		public String getMessage() {
			return "Hello world!";
		}
	}

Compile this and build into a bundle using [Bnd](http://www.aQute.biz/Code/Bnd). I recommend using [Bndtools](http://njbartlett.name/bndtools_intro.html).

The Bnd descriptor should look like this:

	Private-Package: org.example
	JAX-RS-Alias: /example
	JAX-RS-Classes: org.example.HelloWorldResource
	
As an alternative for maintaining the `JAX-RS-Classes` header, you can use the following Bnd macro. This will expand at build-time to the list of classes in the bundle that are annotated with `@Path`:

	JAX-RS-Classes: ${classes;ANNOTATION;javax.ws.rs.Path}

Deploy to an OSGi runtime containing at least the following bundles:

* An `HttpService` implementation, e.g. `org.apache.felix.http.jetty`
* `jersey-core-1.4.jar`
* `jersery-server-1.4.jar`
* The extender bundle from this project, i.e. `name.njbartlett.osgi.jaxrsextender`

You should then be able to open the following URL and see the "Hello world!" message in your browser:

	http://localhost:8080/example/helloworld

Licence
-------

This code is distributed under the terms of the [Eclipse Public Licence version 1.0](http://www.eclipse.org/legal/epl-v10.html).

<a href="http://flattr.com/thing/74659/JAX-RS-Extender-Bundle-for-OSGi" target="_blank">
<img src="http://api.flattr.com/button/button-static-50x60.png" alt="Flattr this" title="Flattr this" border="0" /></a>