package com.s8.stack.arch.helium.http1.headers;

import java.io.IOException;

import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Response;

/**
 * 
 * The Location response header indicates the URL to redirect a page to. It only
 * provides a meaning when served with a 3xx (redirection) or 201 (created)
 * status response.
 * 
 * In cases of redirection, the HTTP method used to make the new request to
 * fetch the page pointed to by Location depends of the original method and of
 * the kind of redirection:
 * 
 * If 303 (See Also) responses always lead to the use of a GET method, 307
 * (Temporary Redirect) and 308 (Permanent Redirect) don't change the method
 * used in the original request; 301 (Permanent Redirect) and 302 (Found)
 * doesn't change the method most of the time, though older user-agents may (so
 * you basically don't know). All responses with one of these status codes send
 * a Location header.
 * 
 * In cases of resource creation, it indicates the URL to the newly created
 * resource.
 * 
 * Location and Content-Location are different: Location indicates the target of
 * a redirection (or the URL of a newly created resource), while
 * Content-Location indicates the direct URL to use to access the resource when
 * content negotiation happened, without the need of further content
 * negotiation. Location is a header associated with the response, while
 * Content-Location is associated with the entity returned.
 * 
 * @author pierreconvert
 *
 */
public class Location extends HTTP1_Header {
	
	public final static Prototype PROTOTYPE = new Prototype(0x10, "Location") {
		
		@Override
		public HTTP1_Header create() {
			return new Location();
		}

		@Override
		public void set(HTTP1_Request request, HTTP1_Header header) {
			request.location = (Location) header;
		}

		@Override
		public HTTP1_Header get(HTTP1_Response response) {
			return response.location;
		}
	};
	
	public String value;
	
	public Location() {
		super();
	}
	
	
	

	public Location(String value) {
		super();
		this.value = value;
	}




	@Override
	public HTTP1_Header.Prototype getPrototype() {
		return PROTOTYPE;
	}

	@Override
	public void parse(String value) {
		this.value = value;
	}

	@Override
	public String compose() throws IOException {
		return value;
	}

}
