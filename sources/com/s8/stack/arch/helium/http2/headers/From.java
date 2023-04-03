package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.STATIC_OVER_CONNECTION;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * <p>
 * The From request header contains an Internet email address for a human user
 * who controls the requesting user agent.
 * 
 * If you are running a robotic user agent (e.g. a crawler), the From header
 * should be sent, so you can be contacted if problems occur on servers, such as
 * if the robot is sending excessive, unwanted, or invalid requests.
 * </p>
 * 
 * @author pierreconvert
 * 
 */
public class From extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x34,
			new String[] { "from"}, 
			false,
			REQUEST, 
			STATIC_OVER_CONNECTION) {

		@Override
		public HTTP2_Header parse(String value) {
			return new From(value);
		}

		@Override
		public HTTP2_Header create() {
			return new From();
		}

		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.from;
		}
	};



	public String value;


	public From() {
		super();
	}

	public From(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.from = this;
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
