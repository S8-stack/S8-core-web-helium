package com.s8.core.web.helium.rx;

public class RxOpRequest {

	public static final int REGISTER = 1;
	public static final int CHANGEOPS = 2;

	public RxConnection connection;
	public int type;
	public int ops;

	public RxOpRequest(RxConnection connection, int type, int ops) {
		this.connection = connection;
		this.type = type;
		this.ops = ops;
	}
}
