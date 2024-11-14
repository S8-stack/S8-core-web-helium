package com.s8.core.web.helium.ssl.v1.inbound;


@FunctionalInterface
public interface Operation {
	
	public abstract boolean operate(SSL_Inbound in);
	
}
