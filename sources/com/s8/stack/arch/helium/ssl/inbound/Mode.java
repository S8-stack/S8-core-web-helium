package com.s8.stack.arch.helium.ssl.inbound;

import java.io.IOException;

import com.s8.stack.arch.helium.ssl.inbound.SSL_Inbound.Flow;

abstract class Mode {

	public Mode() {
		
	}
	
	
	
	public void advertise(Flow process) {
		if(process.isVerbose()) {
			System.out.println("\t--->"+process.getName()+": "+advertise());
		}
	}

	public abstract String advertise();

	/**
	 * @throws IOException 
	 * 
	 */
	public abstract void run(Flow process);


}
