package com.s8.stack.arch.helium.rx;

import java.net.Socket;
import java.net.SocketException;

import com.s8.io.xml.annotations.XML_SetElement;
import com.s8.io.xml.annotations.XML_Type;

@XML_Type(name="RxSocketConfiguration", sub= {})
public class RxSocketConfiguration {

	/**
	 * 
	 */
	private int receiveBufferSize = -1;

	private boolean isReceiveBufferSizeSet = false;

	@XML_SetElement(tag="receive-buffer-size")
	public void setReceiveBufferSize(int size) {
		receiveBufferSize = size;
		isReceiveBufferSizeSet = true;
	}


	/**
	 * -1: not set
	 */
	private int sendBufferSize = -1;

	private boolean isSendBufferSizeSet = false;

	@XML_SetElement(tag="send-buffer-size")
	public void setSendBufferSize(int size) {
		sendBufferSize = size;
		isSendBufferSizeSet = true;
	}


	private boolean isKeepAliveEnabled = false;

	private boolean isKeepAliveEnabledSet = false;

	@XML_SetElement(tag="keep-alive")
	public void setKeepAlive(boolean isEnabled) {
		isKeepAliveEnabled = isEnabled;
		isKeepAliveEnabledSet = true;
	}

	private int trafficClass = 0;

	private boolean isTrafficClassSet = false;

	@XML_SetElement(tag="traffic-class")
	public void setKeepAlive(int trafficClass) {
		this.trafficClass = trafficClass;
		isTrafficClassSet = true;
	}

	private boolean OOBINLINE_isEnabled = false;

	private boolean OOBINLINE_isSet = false;

	@XML_SetElement(tag="OOBINLINE")
	public void OOBINLINE_set(boolean flag) {
		this.OOBINLINE_isEnabled = flag;
		this.OOBINLINE_isSet = true;
	}

	private boolean TCP_NODELAY_isEnabled = false;

	private boolean TCP_NODELAY_isSet = false;

	@XML_SetElement(tag="TCP_NODELAY")
	public void TCP_NODELAY_set(boolean flag) {
		this.TCP_NODELAY_isEnabled = flag;
		this.TCP_NODELAY_isSet = true;
	}



	public void setup(Socket socket, boolean isVerbose) {

		if(isReceiveBufferSizeSet) {
			try {
				socket.setReceiveBufferSize(receiveBufferSize);	
			}
			catch (SocketException exception) {
				if(isVerbose) {
					System.out.println("\t ERROR while setting socket SO_RCVBUF option, discarded");
					exception.printStackTrace();	
				}
			}
		}

		if(isSendBufferSizeSet) {
			try {
				socket.setSendBufferSize(sendBufferSize);
			}
			catch (SocketException exception) {
				if(isVerbose) {
					System.out.println("\t ERROR while setting socket SO_SNDBUF option, discarded");
					exception.printStackTrace();
				}
			}
		}

		if(isKeepAliveEnabledSet) {
			try {
				socket.setKeepAlive(isKeepAliveEnabled);
			}
			catch (SocketException exception) {
				if(isVerbose) {
					System.out.println("\t ERROR while setting socket KeepAlive option, discarded");
					exception.printStackTrace();
				}
			}
		}

		if(isTrafficClassSet) {
			try {
				socket.setTrafficClass(trafficClass);
			}
			catch (SocketException exception) {
				if(isVerbose) {
					System.out.println("\t ERROR while setting socket traffic class option, discarded");
					exception.printStackTrace();
				}
			}
		}

		if(OOBINLINE_isSet) {
			try {
				socket.setOOBInline(OOBINLINE_isEnabled);
			}
			catch (SocketException exception) {
				if(isVerbose) {
					System.out.println("\t ERROR while setting socket OOBINLINE option, discarded");
					exception.printStackTrace();
				}
			}
		}

		if(TCP_NODELAY_isSet) {
			try {
				socket.setTcpNoDelay(TCP_NODELAY_isEnabled);
			}
			catch (SocketException exception) {
				if(isVerbose) {
					System.out.println("\t ERROR while setting socket TCP_NODELAY option, discarded");
					exception.printStackTrace();
				}
			}
		}
	}

	public static void read(Socket socket) {
		// verbose part
		try {
			System.out.println("\t <Socket-configuration>");
			System.out.println("\t\t remote address: "+socket.getRemoteSocketAddress());
			System.out.println("\t\t receive buffer size: "+socket.getReceiveBufferSize());
			System.out.println("\t\t send buffer size: "+socket.getSendBufferSize());
			System.out.println("\t\t keep alive: "+socket.getKeepAlive());
			System.out.println("\t\t traffic class: "+socket.getTrafficClass());
			System.out.println("\t\t is connected?: "+socket.isConnected());
			System.out.println("\t\t is bound?: "+socket.isBound());
			System.out.println("\t\t is OOBINLINE enabled?: "+socket.getOOBInline());
			System.out.println("\t\t is TCP_NODELAY enabled?: "+socket.getTcpNoDelay());
			System.out.println("\t </Socket-configuration>");
		} 
		catch (SocketException exception) {
			System.out.println("\t ERROR while reading socket configuration");
			exception.printStackTrace();
		}
	}
}
