package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.STATIC_OVER_CONNECTION;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * <p>
 * The Accept-Encoding request HTTP header advertises which content encoding,
 * usually a compression algorithm, the client is able to understand. Using
 * content negotiation, the server selects one of the proposals, uses it and
 * informs the client of its choice with the Content-Encoding response header.
 * </p>
 * <p>
 * Even if both the client and the server supports the same compression
 * algorithms, the server may choose not to compress the body of a response, if
 * the identity value is also acceptable. Two common cases lead to this:
 * </p>
 * <p>
 * The data to be sent is already compressed and a second compression won't lead
 * to smaller data to be transmitted. This may be the case with some image
 * formats; The server is overloaded and cannot afford the computational
 * overhead induced by the compression requirement. Typically, Microsoft
 * recommends not to compress if a server uses more than 80% of its
 * computational power. As long as the identity value, meaning no encoding, is
 * not explicitly forbidden, by an identity;q=0 or a *;q=0 without another
 * explicitly set value for identity, the server must never send back a 406 Not
 * Acceptable error.
 * </p>
 * ACCEPT_ENCODING(0x11, false, REQUEST, STATIC_OVER_CONNECTION, ),

 */
//
public class AcceptEncoding extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x11,
			new String[] { "accept-encoding"}, 
			false, REQUEST, STATIC_OVER_CONNECTION) {

		@Override
		public HTTP2_Header parse(String value) {
			return new AcceptEncoding(value);
		}

		@Override
		public HTTP2_Header create() {
			return new AcceptEncoding("none");
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.acceptEncoding;
		}
	};

	
	public static AcceptEncoding parse(String value) {
		return new AcceptEncoding(value);
	}

	public String value;

	public AcceptEncoding(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.acceptEncoding = this;
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
