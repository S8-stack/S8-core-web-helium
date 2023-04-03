package com.s8.stack.arch.helium.http1.lines;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.s8.stack.arch.helium.http1.HTTP1_IOReactive;
import com.s8.stack.arch.helium.http1.HTTP1_Protocol;


public class HTTP1_StatusLine {

	

	/**
	 * 
	 */
	public String version = HTTP1_Protocol.VERSION;

	/**
	 * <p>
	 * Status code, indicating success or failure of the request.
	 * </p>
	 * <ul>
	 * <li>The HTTP 200 OK success status response code indicates that the
	 * request has succeeded</li>
	 * <li>The HTTP 404 Not Found client error response code indicates that a
	 * server can not find the requested resource</li>
	 * </ul>
	 */
	public int statusCode;

	/**
	 * status associated text
	 */
	public String statusText;

	public HTTP1_StatusLine() {
		super();
	}

	public HTTP1_StatusLine(int statusCode, String statusText) {
		super();
		this.statusCode = statusCode;
		this.statusText = statusText;
	}

	
	@Override
	public String toString() {
		return version+" "+statusCode+" "+statusText;
	}
	
	private enum ParsingState {
		CLOSING_LINE, READING_VERSION, READING_STATUS_CODE, READING_STATUS_TEXT;
	}
	
	public class Parsing implements HTTP1_IOReactive {
		
	

		boolean isEndOfHeaderReach = false;
		ParsingState state = ParsingState.READING_VERSION;
		StringBuilder builder = new StringBuilder();
		char c;

		/**
		 * 
		 */
		private boolean isVerbose;
		
		public Parsing(boolean isVerbose) {
			super();
			this.isVerbose = isVerbose;
		}
		
		@Override
		public Result onBytes(ByteBuffer readable) {

			while (!isEndOfHeaderReach) {

				if (!readable.hasRemaining()) {
					if (isVerbose) {
						System.out.println("[HTTP_ResponseLine]: Unpexcted end of stream");
					}
					return Result.NEED_MORE_BYTES;
				}

				c = (char) readable.get();
				
				switch (state) {

				// <method>
				case READING_VERSION:
					if (c != HTTP1_Protocol.FIRST_LINE_DELIMITER) {
						builder.append(c);
					} else {
						version = builder.toString();
						builder = new StringBuilder();
						state = ParsingState.READING_STATUS_CODE;
					}
					break;
				// </method>

				// <path>
				case READING_STATUS_CODE:
					if (c != HTTP1_Protocol.FIRST_LINE_DELIMITER) {
						builder.append(c);
					} else {
						int statusCodeIntValue = HTTP1_Protocol.NOT_OK_CODE;
						try {
							statusCodeIntValue = Integer.valueOf(builder.toString());
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
						statusCode = statusCodeIntValue;
						builder = new StringBuilder();
						state = ParsingState.READING_STATUS_TEXT;
					}
					break;

				// <version>
				case READING_STATUS_TEXT:
					if (c == HTTP1_Protocol.EOL[0]) {
						statusText = builder.toString();
						builder = new StringBuilder();
						state = ParsingState.CLOSING_LINE;
					} else {
						builder.append(c);
					}
					break;

				case CLOSING_LINE:
					if (c == HTTP1_Protocol.EOL[1]) {
						isEndOfHeaderReach = true;
					} else {
						if (isVerbose) {
							System.out.println("[HTTP_ResponseLine]: Ill formed HTTP response");
						}
						return Result.ERROR;
					}
					break;
				}
			}
			return Result.OK;
		}
	}
	
	
	
	
	public class Composing implements HTTP1_IOReactive {
	
		@Override
		public Result onBytes(ByteBuffer writable) {

			// write method
			writable.put(version.getBytes(StandardCharsets.US_ASCII));
			writable.put(HTTP1_Protocol.FIRST_LINE_DELIMITER);

			// write path
			writable.put(Integer.toString(statusCode).getBytes(StandardCharsets.US_ASCII));
			writable.put(HTTP1_Protocol.FIRST_LINE_DELIMITER);

			// write version
			writable.put(statusText.getBytes(StandardCharsets.US_ASCII));
			writable.put(HTTP1_Protocol.EOL);
			
			return Result.OK;
		}	
	}

	

}
