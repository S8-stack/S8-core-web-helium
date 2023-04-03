package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.STATIC_OVER_CONNECTION;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * ACCEPT_LANGUAGE(0x13, false, REQUEST, STATIC_OVER_CONNECTION, ),
 * 
 * @author pierreconvert
 *
 */
public class AcceptLanguage extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(0x13,
			new String[] { "accept-language", "Accept-Language"}, 
			false, REQUEST, STATIC_OVER_CONNECTION) {

		@Override
		public HTTP2_Header parse(String value) {
			return new AcceptLanguage(value);
		}

		@Override
		public HTTP2_Header create() {
			return new AcceptLanguage("utf8");
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.acceptLanguage;
		}
	};

	
	public static AcceptLanguage parse(String value) {
		return new AcceptLanguage(value);
	}

	public String value;

	public AcceptLanguage(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.acceptLanguage = this;
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
