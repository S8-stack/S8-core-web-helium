package com.s8.core.web.helium.ssl;

public class SumUp {




	private final Object lock = new Object();

	private boolean isUnwrapping = false;

	private int nUnwraps = 0;



	private boolean isLoopEnterable() {
		/* equivalent to compare and set */
		synchronized (lock) {
			if(isUnwrapping) {
				return false;
			}
			else {
				/* idle, so can enter */
				isUnwrapping = true;
				return true;
			}
		}
	}



	private boolean isLooping() {
		synchronized (lock) {
			if(nUnwraps > 0) {
				nUnwraps--;
				return true;
			}
			else {
				isUnwrapping = false;
				return false;
			}
		}
	}


	private void addOneMoreUnwrap() {
		synchronized (lock) { nUnwraps++; }	
	}


	/**
	 * Main SSL method
	 */
	void ssl_unwrap() {

		/* add one more loop turn */
		synchronized (lock) { nUnwraps++; }

		if(isLoopEnterable()) {
			while(isLooping()) {
				

				/*
				 *  /!\ Critical section here
				 */
				if(Math.random() > 0.5) { addOneMoreUnwrap(); }
				
			
			}
		}
	}

}
