package com.s8.stack.arch.tests.web.ssl;

import com.s8.core.web.helium.ssl.SSL_Connection;
import com.s8.core.web.helium.ssl.SSL_WebConfiguration;
import com.s8.core.web.helium.ssl.inbound.SSL_Inbound;

public abstract class SSL_Inbound_Impl02 extends SSL_Inbound {

	public SSL_Inbound_Impl02(SSL_WebConfiguration configuration) {
		super(configuration);
	}

	SSL_Connection connection;
	
	@Override
	public SSL_Connection getConnection() {
		return connection;
	}


}
