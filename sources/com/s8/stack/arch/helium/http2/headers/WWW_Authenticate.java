package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.FEW_STATES;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * <p>
 * </p>
 * @author pierre convert
 * 
 */
public class WWW_Authenticate extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x66,
			new String[] { "www-authenticate"}, 
			false,
			RESPONSE, 
			FEW_STATES) {

		@Override
		public HTTP2_Header parse(String value) {
			return new WWW_Authenticate(value);
		}

		@Override
		public HTTP2_Header create() {
			return new WWW_Authenticate();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.wwwAuthenticate;
		}
	};

	
	public String value;

	public WWW_Authenticate() {
		super();
	}
	
	public WWW_Authenticate(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.wwwAuthenticate = this;
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public void setValue(String value) {
		this.value = value;
	}
	
}
