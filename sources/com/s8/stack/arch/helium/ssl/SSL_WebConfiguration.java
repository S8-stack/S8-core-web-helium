package com.s8.stack.arch.helium.ssl;

import java.io.File;
import java.nio.file.Path;

import com.s8.io.xml.annotations.XML_SetElement;
import com.s8.io.xml.annotations.XML_Type;
import com.s8.io.xml.codebase.XML_Codebase;
import com.s8.stack.arch.helium.rx.RxWebConfiguration;

@XML_Type(root = true, name="SSL_WebConfiguration", sub= {})
public class SSL_WebConfiguration extends RxWebConfiguration {

	private Path root;
	
	public String name = "unnamed";
	
	public boolean SSL_isVerbose = false;

	public int ssl_maxPacketSize = SSL_Connection.TARGET_PACKET_SIZE;
	
	private String keystorePathname;

	private String keystorePassword;
	
	public String[] applicationProtocols = {"h2", "http/1.1"};

	private String encryptionProtocol = "TLSv1.2";
	
	private long timeout = 10;

	
	public String getName() {
		return name;
	}
	
	public long getTimeout() {
		return timeout;
	}

	public boolean isSSLVerbose() {
		return SSL_isVerbose;
	}
	
	public String getEncryptionProtocol() {
		return encryptionProtocol;
	}
	
	
	public char[] getKeystorePassword() {
		return keystorePassword.toCharArray();
	}
	
	public File getKeystoreFile() {
		if(root!=null) {
			return root.resolve(keystorePathname).toFile();		
		}
		else {
			return new File(keystorePathname);
		}
	}

	
	/**
	 * 
	 * @param root
	 */
	public void setRoot(Path root) {
		this.root = root;
	}

	@XML_SetElement(tag="SSL-isVerbose")
	public void setSSLVerbose(boolean isVerbose) {
		this.SSL_isVerbose = isVerbose;
	}
	
	@XML_SetElement(tag="SSL-maxPacketSize")
	public void SSL_setMaxPacketSize(int size) {
		this.ssl_maxPacketSize = size;
	}
	
	@XML_SetElement(tag="SSL-keystore_pathname")
	public void setKeystorePathname(String keystorePathname) {
		this.keystorePathname = keystorePathname;
	}

	@XML_SetElement(tag="SSL-keystore_password")
	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	@XML_SetElement(tag="SSL-encryption_protocol")
	public void setEncryptionProtocol(String encryptionProtocol) {
		this.encryptionProtocol = encryptionProtocol;
	}
	
	@XML_SetElement(tag="SSL-application_protocols")
	public void setApplicationProtocols(String protocols) {
		this.applicationProtocols = protocols.split("[ ,]*");
	}
	
	@XML_SetElement(tag="timeout")
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@XML_SetElement(tag="name")
	public void setName(String name) {
		this.name = name;
	}
	
	public static SSL_WebConfiguration load(Path root, String pathname) throws Exception {
		XML_Codebase context = XML_Codebase.from(SSL_WebConfiguration.class);
		SSL_WebConfiguration configuration = (SSL_WebConfiguration) context.deserialize(root.resolve(pathname).toFile());
		configuration.setRoot(root);
		return configuration;
	}
	
	public static SSL_WebConfiguration load(String pathname) throws Exception {
		XML_Codebase context = XML_Codebase.from(SSL_WebConfiguration.class);
		return (SSL_WebConfiguration) context.deserialize(new File(pathname));
	}
	
}
