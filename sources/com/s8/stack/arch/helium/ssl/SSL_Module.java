package com.s8.stack.arch.helium.ssl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.s8.io.xml.codebase.XML_Codebase;

/**
 * Handle SSL stuff
 * 
 * <p>
 * how-to: GENERATE a new keystore with cmd (on terminal):
 * </p>
 * 
 * <ul>
 * <li><b>Generate the server certificate.</b>: Type the keytool command all on
 * one line:
 * <code>pc$ keytool -genkey -alias server-alias -keyalg RSA -keypass rDfe4_!xef 
 * -storepass rDfe4_!xef -keystore keystore.jks</code></li>
 * 
 * 
 * 
 * When you press Enter, keytool prompts you to enter the server name,
 * organizational unit, organization, locality, state, and country code.
 * 
 * You must type the server name in response to keytool’s first prompt, in which
 * it asks for first and last names. For testing purposes, this can be
 * localhost.
 * 
 * When you run the example applications, the host (server name) specified in
 * the keystore must match the host identified in the javaee.server.name
 * property specified in the file
 * tut-install/examples/bp-project/build.properties.
 * 
 * <p>
 * </p>
 * 
 * @author pc
 *
 *
 */
public class SSL_Module {



	public static SSLContext createContext(SSL_WebConfiguration configuration) 
			throws 
			KeyManagementException, 
			NoSuchAlgorithmException, 
			CertificateException, 
			FileNotFoundException, 
			IOException, 
			KeyStoreException, 
			UnrecoverableKeyException 
	{
		char[] password = configuration.getKeystorePassword();

		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		try(InputStream is = new BufferedInputStream(new FileInputStream(configuration.getKeystoreFile()))){
			keyStore.load(is, password);
			is.close();
		}
		catch (IOException e) {
			System.out.println("[SSL_Module] Failed to load: "+configuration.getKeystoreFile());
			e.printStackTrace();
		}

		// Create key managers
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
		keyManagerFactory.init(keyStore, password);
		KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

		// Create trust managers
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
		trustManagerFactory.init(keyStore);
		TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

		SSLContext sslContext = SSLContext.getInstance(configuration.getEncryptionProtocol());
		sslContext.init(keyManagers, trustManagers, new SecureRandom());
		
		
		return sslContext;
	}


	public static SSLContext createContext(String pathname) throws Exception {

		// retrieve configuration
		Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(pathname))));
		XML_Codebase context = XML_Codebase.from(SSL_WebConfiguration.class);
		SSL_WebConfiguration configuration = (SSL_WebConfiguration) context.deserialize(reader);
		reader.close();

		return createContext(configuration);
	}


}
