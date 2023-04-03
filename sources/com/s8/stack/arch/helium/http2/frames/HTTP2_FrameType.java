package com.s8.stack.arch.helium.http2.frames;

public enum HTTP2_FrameType {

	DATA(0x0),
	
	HEADERS(0x1),
	
	PRIORITY(0x2),
	
	RESET(0x3),
	
	SETTINGS(0x4),
	
	PUSH_PROMISE(0x5),
	
	PING(0x6),
	
	GOAWAY(0x7),
	
	WINDOW_UPDATE(0x8),
	
	CONTINUATION(0x9);
	

	public final int code;
	
	private HTTP2_FrameType(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
}
