package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.FEW_STATES;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 * 
 * PROXY_AUTHENTICATE(0x47, false, RESPONSE, FEW_STATES, ""),
 */
public class ProxyAuthenticate extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x47,
			new String[] { "proxy-authenticate" }, 
			false,
			RESPONSE, 
			FEW_STATES) {

		@Override
		public HTTP2_Header parse(String value) {
			return new ProxyAuthenticate(value);
		}

		@Override
		public HTTP2_Header create() {
			return new ProxyAuthenticate();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.proxyAuthenticate;
		}
	};

	
	public String value;

	public ProxyAuthenticate() {
		super();
	}
	
	public ProxyAuthenticate(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.proxyAuthenticate = this;
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
