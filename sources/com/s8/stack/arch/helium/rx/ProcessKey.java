package com.s8.stack.arch.helium.rx;

import java.nio.channels.SelectionKey;

import com.s8.arch.silicon.async.AsyncSiTask;
import com.s8.arch.silicon.async.MthProfile;

public class ProcessKey implements AsyncSiTask {

	private SelectionKey key;

	public ProcessKey(SelectionKey key) {
		super();
		this.key = key;
	}

	@Override
	public MthProfile profile() {
		return MthProfile.WEB_REQUEST_PROCESSING;
	}
	
	@Override
	public void run(){
		((RxConnection) key.attachment()).pushReadyOps();
	}

	@Override
	public String describe() {
		return "(Rx) PROCESS_KEY";
	}
}
