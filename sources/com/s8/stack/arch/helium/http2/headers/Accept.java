package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.STATIC_OVER_CONNECTION;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;


/**
 * <p>
 * The Accept request HTTP header advertises which content types, expressed as
 * MIME types, the client is able to understand. Using content negotiation, the
 * server then selects one of the proposals, uses it and informs the client of
 * its choice with the Content-Type response header. Browsers set adequate
 * values for this header depending on the context where the request is done:
 * when fetching a CSS stylesheet a different value is set for the request than
 * when fetching an image, video or a script.
 * </p>
 * <p>
 * <b>CORS-safelisted request header</b>: yes, with the additional restriction that
 * values can't contain a CORS-unsafe request header byte: "():<>?@[\]{},
 * Delete, Tab and control characters: 0x00 to 0x19.
 * </p>
 * ACCEPT(0x12, false, REQUEST, STATIC_OVER_CONNECTION, ),
 */
public class Accept extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x12,
			new String[] { "accept", "Accept"}, 
			false, REQUEST, STATIC_OVER_CONNECTION) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Accept(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Accept("none");
		}

		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.accept;
		}
	};

	
	public static Accept parse(String value) {
		return new Accept(value);
	}

	public String value;

	public Accept(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.accept = this;
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
