package com.s8.core.web.helium.ssl.v1.outbound;

public enum Mode {

	CONTINUE {
		@Override
		public boolean isValid(SSL_Outbound out) {
			return true;
		}
	},
	
	
	REQUIRE_NETWORK_BUFFER_SENT {
		@Override
		public boolean isValid(SSL_Outbound out) {
			return out.networkBuffer.position() == 0;
		}
	};
	
	
	
	public abstract boolean isValid(SSL_Outbound out);
}
