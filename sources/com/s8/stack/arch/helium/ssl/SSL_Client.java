package com.s8.stack.arch.helium.ssl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import com.s8.stack.arch.helium.rx.RxClient;

public abstract class SSL_Client extends RxClient implements SSL_Endpoint {
	
	private SSLContext SSL_context;

	public SSL_Client() throws 
			KeyManagementException, 
			UnrecoverableKeyException, 
			NoSuchAlgorithmException, 
			CertificateException, 
			FileNotFoundException, 
			KeyStoreException, 
			IOException {
		super();
		
		
	}

	@Override
	public void start() throws Exception {
	
		
		startSSL();
		
		startRxLayer();
		
	}
	
	
	public void startSSL() throws Exception {
		// start lower level
				SSL_WebConfiguration configuration = getWebConfiguration();
				SSL_context = SSL_Module.createContext(configuration);
	}
	
	@Override
	public SSLContext ssl_getContext() {
		return SSL_context;
	}

	
	/**
	 * Up-casting definition method
	 * @return
	 */
	@Override
	public abstract SSL_WebConfiguration getWebConfiguration();
}
