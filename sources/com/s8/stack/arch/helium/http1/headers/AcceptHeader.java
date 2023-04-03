package com.s8.stack.arch.helium.http1.headers;


import java.io.IOException;

import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Response;


/**
 * The Accept request HTTP header advertises which content types, 
 * expressed as MIME types, the client is able to understand. 
 * Using content negotiation, the server then selects one of the
 *  proposals, uses it and informs the client of its choice with the 
 *  Content-Type response header. Browsers set adequate values 
 *  for this header depending of the context where the request 
 *  is done: when fetching a CSS stylesheet a different 
 *  value is set for the request than when fetching an image, video or a script.
 *  
 *  <p>
 *  Syntax:
 *  <ul>
 *  <li> Accept: {MIME_type}/{MIME_subtype} </li>
 *  <li> Accept: <MIME_type>\\/* </li>
 * 	<li> Accept: *\\/*</li>
 *  </ul>
 *  Multiple types, weighted with the quality value syntax:
 *  Accept: text/html, application/xhtml+xml, application/xml;q=0.9, *\\/*;q=0.8
 *  </p>
 */
public class AcceptHeader extends HTTP1_Header {
	
	public final static Prototype PROTOTYPE = new Prototype(0x02, "Accept") {
		public @Override HTTP1_Header create() {
			return new AcceptHeader();
		}

		@Override
		public void set(HTTP1_Request request, HTTP1_Header header) {
			request.accept = (AcceptHeader) header;
		}

		@Override
		public HTTP1_Header get(HTTP1_Response response) {
			return response.accept;
		}
	};
	
	
	@Override
	public HTTP1_Header.Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	@Override
	public void parse(String value) {
		
	}

	@Override
	public String compose() throws IOException {
		return "accept";
	}
}
