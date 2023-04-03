package com.s8.stack.arch.helium.http1.lines;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.s8.stack.arch.helium.http1.HTTP1_IOReactive;
import com.s8.stack.arch.helium.http1.HTTP1_Protocol;


public class HTTP1_RequestLine {



	/**
	 * 
	 */
	public HTTP1_Method method = HTTP1_Method.POST;

	/**
	 * 
	 */
	public String path = "/";

	/**
	 * 
	 */
	public String version = "HTTP/1.1";



	private enum ParsingState {
		CLOSING_LINE,
		READING_METHOD,
		READING_PATH,
		READING_VERSION;
	}

	
	@Override
	public String toString() {
		return method+" "+path+" "+version;
	}
	

	public class Parsing implements HTTP1_IOReactive {

		private boolean isVerbose;
		private boolean isTerminated = false;
		private ParsingState state = ParsingState.READING_METHOD;
		private StringBuilder builder = new StringBuilder();
		private char c;

		public Parsing(boolean isVerbose) {
			super();
			this.isVerbose = isVerbose;
		}

		/**
		 * 
		 * @throws Exception
		 */
		@Override
		public Result onBytes(ByteBuffer readable) {

			while(!isTerminated){

				if(!readable.hasRemaining()){
					if(isVerbose){
						System.out.println("[HTTP_Request.FirstLine]: "
								+ "Unexpected end of input stream, "
								+ "request start is: "+builder.toString());
					}
					return Result.NEED_MORE_BYTES;
				}

				/**
				 * ASCII
				 */
				c = (char) readable.get();

				if(isVerbose){
					System.out.print(c);
				}
				switch(state){

				// <method>
				case READING_METHOD:
					if(c!=HTTP1_Protocol.FIRST_LINE_DELIMITER){
						builder.append(c);
					}
					else{
						method = HTTP1_Method.get(builder.toString());
						if(method==null){
							return Result.ERROR;
						}
						builder = new StringBuilder();
						state = ParsingState.READING_PATH;
					}
					break;
					// </method>

					// <path>
				case READING_PATH:
					if(c!=HTTP1_Protocol.FIRST_LINE_DELIMITER){
						builder.append(c);
					}
					else{
						path = builder.toString();
						builder = new StringBuilder();
						state = ParsingState.READING_VERSION;
					}
					break;

					// <version>
				case READING_VERSION:
					if(c==HTTP1_Protocol.EOL[0]){
						version = builder.toString();
						builder = new StringBuilder();
						state = ParsingState.CLOSING_LINE;
					}
					else{
						builder.append(c);
					}
					break;

				case CLOSING_LINE:
					if(c==HTTP1_Protocol.EOL[1]){
						isTerminated = true;
						return Result.OK;
					}
					else{
						if(isVerbose){
							System.out.println("[HTTP_Request.FirstLine]: Ill formed request header");
						}
						return Result.ERROR;
					}
				}
			}
			return Result.OK;
		}
	}



	/**
	 * 
	 * @author pierreconvert
	 *
	 */
	public class Composing implements HTTP1_IOReactive {

		@Override
		public Result onBytes(ByteBuffer writable) {
			// write method
			writable.put(method.name().getBytes(StandardCharsets.US_ASCII));
			writable.put(HTTP1_Protocol.FIRST_LINE_DELIMITER);

			// write path
			writable.put(path.getBytes(StandardCharsets.US_ASCII));
			writable.put(HTTP1_Protocol.FIRST_LINE_DELIMITER);

			// write version
			writable.put(version.getBytes(StandardCharsets.US_ASCII));
			writable.put(HTTP1_Protocol.EOL);

			return Result.OK;
		}
	}

}
