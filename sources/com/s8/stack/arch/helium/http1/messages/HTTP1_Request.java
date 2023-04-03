package com.s8.stack.arch.helium.http1.messages;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import com.s8.stack.arch.helium.http1.HTTP1_IOReactive;
import com.s8.stack.arch.helium.http1.headers.AcceptHeader;
import com.s8.stack.arch.helium.http1.headers.CacheControl;
import com.s8.stack.arch.helium.http1.headers.Connection;
import com.s8.stack.arch.helium.http1.headers.ContentLength;
import com.s8.stack.arch.helium.http1.headers.ContentType;
import com.s8.stack.arch.helium.http1.headers.HTTP1_HeadersParsing;
import com.s8.stack.arch.helium.http1.headers.Location;
import com.s8.stack.arch.helium.http1.headers.MIME_Type;
import com.s8.stack.arch.helium.http1.headers.TransferEncoding;
import com.s8.stack.arch.helium.http1.lines.HTTP1_RequestLine;

public class HTTP1_Request {


	/**
	 * composed of:
	 * <ul>
	 * <li>mapping part (mandatory)</li>
	 * <li>params part (optional)</li>
	 * <li>session part (optional)</li>
	 * </ul>
	 */
	public final static String POST_REQUEST_REGEX = 
			"(?<mapping>[\\w-_\\.]+)"
					+ "(:(?<params>[\\w-_\\.!\\&= /\\(\\)\\|\\[\\]\\^\\*\\{\\}\\;\\:\\\\]+))?"
					+ "(\\?(?<token>[\\w-_.!&]+))?";

	/**
	 * 
	 */
	public final static Pattern POST_REQUEST_PATTERN = Pattern.compile(POST_REQUEST_REGEX);


	public HTTP1_RequestLine line;

	public AcceptHeader accept;
	
	public CacheControl cacheControl;
	
	public Connection connection;
	
	public ContentLength contentLength;
	
	public ContentType contentType;
	
	public Location location;

	public TransferEncoding transferEncoding;
	
	public byte[] body;

	public HTTP1_Request() {
		super();
		this.line = new HTTP1_RequestLine();
	}

	@Override
	public String toString() {
		return line.toString();
	}
	


	public HTTP1_IOReactive parse() {
		return new HTTP1_IOReactive.Compound(new HTTP1_IOReactive[] {
				line.new Parsing(false), 
				new HTTP1_HeadersParsing(HTTP1_Request.this, false),
				new BodyParsing()
		});
	}
	
	

	private class BodyParsing implements HTTP1_IOReactive {

		@Override
		public Result onBytes(ByteBuffer buffer) {
			switch (line.method) {

			case GET:
				// no more content to read
				break;

			case POST:
				if(contentType!=null && contentType.type==MIME_Type.APPLICATION_XHR){
					if(contentLength!=null){
						body = new byte[contentLength.length];
					}
				}

			default:
				return Result.ERROR;
			}

			return Result.OK;
		}
	}
}
