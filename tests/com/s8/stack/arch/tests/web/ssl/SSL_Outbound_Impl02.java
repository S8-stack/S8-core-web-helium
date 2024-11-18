package com.s8.stack.arch.tests.web.ssl;

import com.s8.core.web.helium.ssl.SSL_Connection;
import com.s8.core.web.helium.ssl.SSL_Outbound;
import com.s8.core.web.helium.ssl.SSL_WebConfiguration;

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
