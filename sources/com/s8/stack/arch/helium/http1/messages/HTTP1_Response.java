package com.s8.stack.arch.helium.http1.messages;

import java.nio.ByteBuffer;

import com.s8.stack.arch.helium.http1.HTTP1_IOReactive;
import com.s8.stack.arch.helium.http1.headers.AcceptHeader;
import com.s8.stack.arch.helium.http1.headers.CacheControl;
import com.s8.stack.arch.helium.http1.headers.Connection;
import com.s8.stack.arch.helium.http1.headers.ContentLength;
import com.s8.stack.arch.helium.http1.headers.ContentType;
import com.s8.stack.arch.helium.http1.headers.HTTP1_HeadersComposing;
import com.s8.stack.arch.helium.http1.headers.Location;
import com.s8.stack.arch.helium.http1.headers.TransferEncoding;
import com.s8.stack.arch.helium.http1.lines.HTTP1_StatusLine;

public class HTTP1_Response {


	public HTTP1_StatusLine line = new HTTP1_StatusLine();

	public AcceptHeader accept;
	
	public CacheControl cacheControl;
	
	public Connection connection;
	
	public ContentLength contentLength;
	
	public ContentType contentType;
	
	public Location location;

	public TransferEncoding transferEncoding;
	
	public byte[] body = null;
	
	public HTTP1_Response() {
		super();
	}
	
	
	public HTTP1_IOReactive compose() {
		return new HTTP1_IOReactive.Compound(new HTTP1_IOReactive[] {
				line.new Composing(), 
				new HTTP1_HeadersComposing(HTTP1_Response.this),
				new BodyComposing()
		});
	}
	
	private class BodyComposing implements HTTP1_IOReactive {

		@Override
		public Result onBytes(ByteBuffer writable) {
			if(body!=null) {
				if(writable.remaining()>=body.length) {
					writable.put(body);
					return Result.OK;
				}
				else {
					return Result.NEED_MORE_BYTES;
				}
			}
			else {
				return Result.OK;	
			}
		}
	}
	
	@Override
	public String toString() {
		return line.toString();
	}
}
