package com.s8.core.web.helium.ssl.v1.outbound;

import java.io.IOException;

import com.s8.core.web.helium.ssl.v1.outbound.SSL_Outbound.Flow;

abstract class Mode {
	
	
	
	public Mode() {
		super();
	}
	
	
	public void advertise(SSL_Outbound.Flow outbound) {
		if(outbound.isVerbose()) {
			System.out.println("\t--->"+outbound.getName()+": "+declare());
		}
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	public abstract String declare();
	
	/**
	 * @throws IOException 
	 * 
	 */
	public abstract void run(Flow process);



}
