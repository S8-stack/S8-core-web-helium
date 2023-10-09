/**
 * 
 */
/**
 * @author pierreconvert
 *
 */
module com.s8.web.helium {
	

	exports com.s8.stack.arch.helium.mime;
	exports com.s8.stack.arch.helium.rx;
	
	exports com.s8.stack.arch.helium.ssl;
	exports com.s8.stack.arch.helium.ssl.inbound;
	exports com.s8.stack.arch.helium.ssl.outbound;
	
	exports com.s8.stack.arch.helium.http1;
	exports com.s8.stack.arch.helium.http1.headers;
	exports com.s8.stack.arch.helium.http1.lines;
	exports com.s8.stack.arch.helium.http1.messages;
	exports com.s8.stack.arch.helium.http1.pre;
	
	exports com.s8.stack.arch.helium.http2;
	exports com.s8.stack.arch.helium.http2.frames;
	exports com.s8.stack.arch.helium.http2.headers;
	exports com.s8.stack.arch.helium.http2.hpack;
	exports com.s8.stack.arch.helium.http2.messages;
	exports com.s8.stack.arch.helium.http2.settings;
	exports com.s8.stack.arch.helium.http2.streams;
	exports com.s8.stack.arch.helium.http2.utilities;
	
	
	requires transitive com.s8.api;
	requires transitive com.s8.io.bytes;
	requires transitive com.s8.io.xml;
	requires transitive com.s8.arch.silicon;
}