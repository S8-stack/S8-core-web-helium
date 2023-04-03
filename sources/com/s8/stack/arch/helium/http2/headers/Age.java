package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * <p>
 * The Age header contains the time in seconds the object has been in a proxy
 * cache.
 * 
 * The Age header is usually close to zero. If it is Age: 0, it was probably
 * just fetched from the origin server; otherwise It is usually calculated as a
 * difference between the proxy's current date and the Date general header
 * included in the HTTP response.
 * </p>
 * 
 * AGE(0x16, false, RESPONSE, ALWAYS_RENEWED, "age"),
 * 
 * @author pierreconvert
 *
 */
public class Age extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x16,
			new String[] { "age"}, 
			false,
			RESPONSE, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Age(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Age("0");
		}

		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.age;
		}
	};


	public static Age parse(String value) {
		return new Age(value);
	}

	public String value;

	public Age(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.age = this;
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
