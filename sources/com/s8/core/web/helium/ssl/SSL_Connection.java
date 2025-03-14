package com.s8.core.web.helium.ssl;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLParameters;

import com.s8.core.web.helium.rx.RxConnection;


/**
 *
 * 
 * 
 * @author pc
 */
public abstract class SSL_Connection extends RxConnection {


	public final static int TARGET_PACKET_SIZE = 4096; // 2^12

	//private RxWebEndpoint base;

	private String name;

	private boolean isServerSide;

	private SSL_Phase phase;

	private SSLEngine engine;


	/**
	 * SSL layer verbosity
	 */
	private boolean isVerbose;


	public boolean isClosed;
	



	/**
	 * @throws IOException 
	 * 
	 * 
	 */
	public SSL_Connection(SelectionKey key, SocketChannel channel) throws IOException {
		super(key, channel);
	}
	
	
	@Override
	public abstract SSL_Endpoint getEndpoint();

	public SSLEngine ssl_getEngine() {
		return engine;
	}
	
	
	
	/**
	 * 
	 * @throws IOException
	 */
	public void ssl_initialize(SSL_WebConfiguration config) throws IOException {
		
		// rx level
		Rx_initialize(config);


		this.name = config.name;

		// configuration
		this.isServerSide = config.isServer;
		this.isVerbose = config.SSL_isVerbose;

		phase = SSL_Phase.CREATION;
		isClosed = false;

		// engine
		initializeSSLEngine();
		
		/* bind bounds */

		int networkBufferSize = engine.getSession().getPacketBufferSize();
		int applicationBufferSize = engine.getSession().getApplicationBufferSize();
		
		getInbound().ssl_initialize(networkBufferSize, applicationBufferSize);
		getOutbound().ssl_initialize(networkBufferSize, applicationBufferSize);
		

		/* now (and only now) ready for initial handshake */
	}
	

	@Override
	public abstract SSL_Inbound getInbound();

	@Override
	public abstract SSL_Outbound getOutbound();



	public String getName() {
		return name;
	}


	/**
	 * <p>
	 * Start the endpoint
	 * </p>
	 * <p>
	 * This choice determines who begins the handshaking process as well as which
	 * type of messages should be sent by each party. The method
	 * setUseClientMode(boolean) configures the mode. Once the initial handshaking
	 * has started, an SSLEngine can not switch between client and server modes,
	 * even when performing renegotiations.
	 * </p>
	 * @throws IOException 
	 */
	public void start() throws IOException {
		if(isServerSide) {
			receive();
		}
		else {
			send();
		}
	}

	public void resume() {
		receive();	
		send();
	}


	
	/**
	 * 
	 */
	void initializeSSLEngine() {
		/* <init_SSLEngine> */

		SSLContext context = getEndpoint().ssl_getContext();

		// initialize super layer
		SSL_WebConfiguration config = getEndpoint().getWebConfiguration();
		int maxPacketSize = config.ssl_maxPacketSize;
		boolean isServerSide = config.isServer;
		
		engine = context.createSSLEngine();

		if(isServerSide) {
			/*
			 * Configure the serverEngine to act as a server in the SSL/TLS
			 * handshake.  Also, require SSL client authentication.
			 */
			engine.setUseClientMode(false);

			// always require client authentication, as a secured implemenatation
			//engine.setNeedClientAuth(true);

		}
		else { // client side
			/*
			 * Similar to above, but using client mode instead.
			 */
			engine.setUseClientMode(true);
		}

		SSLParameters parameters = engine.getSSLParameters();
		parameters.setMaximumPacketSize(maxPacketSize);
		parameters.setApplicationProtocols(new String[]{"h2", "http/1.1"});

		if(config.SSL_isVerbose) {
			System.out.println("[SSL_Connection] ALPN:");
			for(String p : parameters.getApplicationProtocols()) {
				System.out.println("\t\t-> supported application protocol: "+p);
			}	
		}
		
		engine.setSSLParameters(parameters);
		
		/* copy engine cache on each bound */
		getInbound().engine = engine;
		getOutbound().engine = engine;
	}


	public void onResult(SSLEngineResult result) {
		phase = phase.transition(result);
	}


	/**
	 * 
	 * @return
	 */
	public boolean isHandshaking() {
		return phase == SSL_Phase.INITIAL_HANDSHAKE || phase == SSL_Phase.REHANDSHAKING;
	}


	public boolean SSL_isVerbose() {
		return isVerbose;
	}

	public boolean isServerSide() {
		return isServerSide;
	}

	
	public void ssl_close() {
		rx_initiateClosing();
		isClosed = true;
	}


	public boolean isClosed() {
		return isClosed;
	}


}
