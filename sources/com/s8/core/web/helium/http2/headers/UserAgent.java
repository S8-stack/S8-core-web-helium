package com.s8.core.web.helium.http2.headers;

import static com.s8.core.web.helium.http2.headers.HTTP2_HeaderRefresh.STATIC_OVER_CONNECTION;
import static com.s8.core.web.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.core.web.helium.http2.messages.HTTP2_Message;

/**
 * <p>
 * </p>
 * @author pierre convert
 */
public class UserAgent extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x63,
			new String[] { "user-agent"}, 
			false,
			REQUEST, 
			STATIC_OVER_CONNECTION) {

		@Override
		public HTTP2_Header parse(String value) {
			return new UserAgent(value);
		}

		@Override
		public HTTP2_Header create() {
			return new UserAgent();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.userAgent;
		}
	};

	
	public String value;

	public UserAgent() {
		super();
	}
	
	public UserAgent(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.userAgent = this;
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
