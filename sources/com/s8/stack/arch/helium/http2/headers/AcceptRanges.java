package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.STATIC_OVER_CONNECTION;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierreconvert
 *
 */
public class AcceptRanges extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x14,
			new String[] { "accept-ranges"}, 
			false, 
			RESPONSE, 
			STATIC_OVER_CONNECTION) {

		@Override
		public HTTP2_Header parse(String value) {
			return new AcceptRanges(value);
		}

		@Override
		public HTTP2_Header create() {
			return new AcceptRanges("utf8");
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.acceptRanges;
		}
	};

	
	public static AcceptRanges parse(String value) {
		return new AcceptRanges(value);
	}

	public String value;

	public AcceptRanges(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.acceptRanges = this;
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
