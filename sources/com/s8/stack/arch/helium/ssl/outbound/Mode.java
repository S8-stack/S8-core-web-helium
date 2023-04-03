package com.s8.stack.arch.helium.ssl.outbound;

import java.io.IOException;

import com.s8.stack.arch.helium.ssl.outbound.SSL_Outbound.Flow;

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
