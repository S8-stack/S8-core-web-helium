package com.s8.stack.arch.tests.web.ssl;

import com.s8.core.web.helium.ssl.v1.SSL_Connection;
import com.s8.core.web.helium.ssl.v1.SSL_WebConfiguration;
import com.s8.core.web.helium.ssl.v1.outbound.SSL_Outbound;

public abstract class SSL_Outbound_Impl02 extends SSL_Outbound {

	public SSL_Outbound_Impl02(String name, SSL_WebConfiguration configuration) {
		super(name, configuration);
	}

	SSL_Connection connection;
	
	@Override
	public SSL_Connection getConnection() {
		return connection;
	}


}
