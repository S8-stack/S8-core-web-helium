package com.s8.core.web.helium.rx;

import com.s8.core.io.xml.annotations.XML_SetElement;
import com.s8.core.io.xml.annotations.XML_Type;

@XML_Type(name="RxWebConfiguration")
public class RxWebConfiguration {

	public boolean isServer = true;

	/**
	 * The backlog parameter is the maximum number of pending connections on the
	 * socket. Its exact semantics are implementation specific. In particular, an
	 * implementation may impose a maximum length or may choose to ignore the
	 * parameter altogther.
	 */
	public int backlog = 50;
	

	/**
	 * <p>
	 * <b>active for server-side</b>
	 * </p>
	 */
	public int port = 1024;
	
	
	
	/**
	 * 
	 */
	public String hostname = "localhost";
	
	
	/**
	 * socket level settings
	 */
	public RxSocketConfiguration socketConfiguration = new RxSocketConfiguration();

	
	public boolean isRxVerbose;

	
	public int poolCapacity = 1024;
	
	
	public RxWebConfiguration() {
		super();
	}
	

	@XML_SetElement(tag="isServer")
	public void setServerSide(boolean isServer) {
		this.isServer = isServer;
	}


	@XML_SetElement(tag="backlog")
	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}
	

	@XML_SetElement(tag="port")
	public void setPort(int port) {
		this.port = port;
	}

	
	@XML_SetElement(tag="host")
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@XML_SetElement(tag="socket")
	public void setSocketConfiguration(RxSocketConfiguration configuration) {
		this.socketConfiguration = configuration;
	}
	
	
	@XML_SetElement(tag="isRxVerbose")
	public void setRxVerbose(boolean isVerbose) {
		this.isRxVerbose = isVerbose;
	}
	
	
	@XML_SetElement(tag="pool-capacity")
	public void setPoolCapacity(int capacity) {
		this.poolCapacity = capacity;
	}
	

}
