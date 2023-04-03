package com.s8.stack.arch.helium.http2;

import java.io.File;
import java.nio.file.Path;

import com.s8.io.xml.annotations.XML_SetElement;
import com.s8.io.xml.annotations.XML_Type;
import com.s8.io.xml.handler.XML_Lexicon;
import com.s8.stack.arch.helium.ssl.SSL_WebConfiguration;

@XML_Type(root = true, name="HTTP2_WebConfiguration", sub= {})
public class HTTP2_WebConfiguration extends SSL_WebConfiguration {
	

	public long timeout = 10;
	
	public boolean isHTTP2Verbose;

	public boolean isHPACKVerbose;
	
	public boolean isHuffmanEncoding;


	@XML_SetElement(tag="HPACK-isVerbose")
	public void setHPACKVerbose(boolean isVerbose) {
		this.isHPACKVerbose = isVerbose;
	}

	@XML_SetElement(tag="HPACK-isHuffmanEncoding")
	public void setHuffmanEncoding(boolean isEncoding) {
		this.isHuffmanEncoding = isEncoding;
	}
	
	@XML_SetElement(tag="HTTP2-isVerbose")
	public void setHTTP2Verbose(boolean isVerbose) {
		this.isHTTP2Verbose = isVerbose;
	}
	
	
	/**
	 * 
	 * @param root
	 * @param pathname
	 * @return
	 * @throws Exception
	 */
	public static HTTP2_WebConfiguration load(Path root, String pathname) throws Exception {
		XML_Lexicon context = new XML_Lexicon(HTTP2_WebConfiguration.class);
		HTTP2_WebConfiguration configuration = 
				(HTTP2_WebConfiguration) context.deserialize(root.resolve(pathname).toFile());
		configuration.setRoot(root);
		return configuration;
	}
	
	
	/**
	 * 
	 * @param pathname
	 * @return
	 * @throws Exception
	 */
	public static HTTP2_WebConfiguration load(String pathname) throws Exception {
		XML_Lexicon context = new XML_Lexicon(HTTP2_WebConfiguration.class);
		return (HTTP2_WebConfiguration) context.deserialize(new File(pathname));
	}
}
