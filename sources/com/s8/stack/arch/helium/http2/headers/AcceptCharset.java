package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.STATIC_OVER_CONNECTION;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * <p>
 * The Accept-Charset request HTTP header advertises which character encodings
 * the client understands. Using content negotiation, the server selects one of
 * the encodings, uses it, and informs the client of its choice within the
 * Content-Type response header, usually in a charset= parameter. Browsers
 * usually don't send this header, as the default value for each resource is
 * usually correct and transmitting it would allow fingerprinting.
 * </p>
 * <p>
 * If the server cannot serve any character encoding from this request header,
 * it can theoretically send back a 406 Not Acceptable error code. But for a
 * better user experience, this is rarely done and the Accept-Charset header is
 * ignored.
 * </p>
 */
//ACCEPT_CHARSET(0x10, false, , , "accept-charset", "Accept-Charset"),
public class AcceptCharset extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x10,
			new String[] { "accept-charset", "Accept-Charset"}, 
			false, REQUEST, STATIC_OVER_CONNECTION) {

		@Override
		public HTTP2_Header parse(String value) {
			return new AcceptCharset(value);
		}

		@Override
		public HTTP2_Header create() {
			return new AcceptCharset("utf8");
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.acceptCharset;
		}
	};

	
	public static AcceptCharset parse(String value) {
		return new AcceptCharset(value);
	}

	public String value;

	public AcceptCharset(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.acceptCharset = this;
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
