package com.s8.stack.arch.helium.rx;

/**
 * 
 * @author pierreconvert
 *
 */
abstract class State {


	RxConnection connection;
	
	public int ops;

	public State(RxConnection connection) {
		super();
		this.connection = connection;
	}


	/**
	 * (can operate the connection level)
	 */
	public abstract void updateObservables();

	
	/**
	 * 
	 */
	public abstract void operate();


}
