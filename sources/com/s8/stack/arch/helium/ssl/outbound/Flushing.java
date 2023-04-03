package com.s8.stack.arch.helium.ssl.outbound;

class Flushing extends Mode {


	public Flushing() {
		super();
	}

	@Override
	public String declare() {
		return "is flushing...";
	}


	@Override
	public void run(SSL_Outbound.Flow process) {


		// if there is actually new bytes, send them
		if(process.getNetworkBuffer().position()>0) {


			/*
			 *  stop this process here (trigger sending)
			 * setup callback as this to continue on this mode asynchronously
			 */
			process.push(this); // trigger another write attempt
		}
		else {
			// In any case, stop this process here (trigger sending)
			process.stop();
			
		}
	}

}