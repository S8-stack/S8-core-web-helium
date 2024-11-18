/**
 * 
 */
/**
 * @author pierreconvert
 *
 */
module com.s8.core.web.helium {
	

	exports com.s8.core.web.helium.mime;
	
	exports com.s8.core.web.helium.rx;
	
	exports com.s8.core.web.helium.ssl;
	
	exports com.s8.core.web.helium.http1;
	exports com.s8.core.web.helium.http1.headers;
	exports com.s8.core.web.helium.http1.lines;
	exports com.s8.core.web.helium.http1.messages;
	exports com.s8.core.web.helium.http1.pre;
	
	exports com.s8.core.web.helium.http2;
	exports com.s8.core.web.helium.http2.frames;
	exports com.s8.core.web.helium.http2.headers;
	exports com.s8.core.web.helium.http2.hpack;
	exports com.s8.core.web.helium.http2.messages;
	exports com.s8.core.web.helium.http2.settings;
	exports com.s8.core.web.helium.http2.streams;
	exports com.s8.core.web.helium.http2.utilities;
	
	
	
	requires transitive com.s8.api;
	requires transitive com.s8.core.io.bytes;
	requires transitive com.s8.core.io.xml;
	requires transitive com.s8.core.arch.silicon;
	
	
}