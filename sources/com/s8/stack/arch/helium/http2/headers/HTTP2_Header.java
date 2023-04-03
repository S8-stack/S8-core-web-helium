package com.s8.stack.arch.helium.http2.headers;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pc
 *
 */
public abstract class HTTP2_Header {
	
	public final static HTTP2_Header.Prototype[] PROTOTYPES = new HTTP2_Header.Prototype[] {
			
			/**
			 * From Chrome debug: " "error":"Pseudo header must not follow regular headers.","header_name":":status""
			 */
			/* <pseudo-headers> */
			Authority.PROTOTYPE, 
			Method.PROTOTYPE, 
			Path.PROTOTYPE, 
			Scheme.PROTOTYPE, 
			Status.PROTOTYPE, 
			/* </pseudo-headers> */
			
			AcceptCharset.PROTOTYPE, 
			Accept.PROTOTYPE, 
			AcceptCharset.PROTOTYPE, 
			AcceptEncoding.PROTOTYPE, 
			AcceptLanguage.PROTOTYPE, 
			AcceptRanges.PROTOTYPE, 
			AccessControlAllowOrigin.PROTOTYPE, 
			Age.PROTOTYPE, 
			Allow.PROTOTYPE, 
			
			Authorization.PROTOTYPE, 
			CacheControl.PROTOTYPE, 
			Connection.PROTOTYPE, 
			ContentDisposition.PROTOTYPE, 
			ContentEncoding.PROTOTYPE, 
			ContentLanguage.PROTOTYPE, 
			ContentLength.PROTOTYPE, 
			ContentLocation.PROTOTYPE, 
			ContentRange.PROTOTYPE, 
			ContentType.PROTOTYPE, 
			Cookie.PROTOTYPE, 
			Date.PROTOTYPE, 
			ETag.PROTOTYPE, 
			Expect.PROTOTYPE, 
			Expires.PROTOTYPE, 
			From.PROTOTYPE, 
			Host.PROTOTYPE, 
			
			IfMatch.PROTOTYPE, 
			IfModifiedSince.PROTOTYPE, 
			IfNoneMatch.PROTOTYPE, 
			IfRange.PROTOTYPE, 
			IfUnmodifiedSince.PROTOTYPE, 
			LastModified.PROTOTYPE, 
			Link.PROTOTYPE, 
			Location.PROTOTYPE, 
			MaxForwards.PROTOTYPE, 
			
			
			Pragma.PROTOTYPE, 
			ProxyAuthenticate.PROTOTYPE, 
			ProxyAuthorization.PROTOTYPE, 
			Range.PROTOTYPE, 
			Referer.PROTOTYPE, 
			Refresh.PROTOTYPE, 
			RetryAfter.PROTOTYPE, 
			
			Server.PROTOTYPE, 
			SetCookie.PROTOTYPE, 
			StrictTransportSecurity.PROTOTYPE, 
			Unmapped.PROTOTYPE, 
			UpgradeInsecureRequest.PROTOTYPE, 
			UserAgent.PROTOTYPE, 
			Vary.PROTOTYPE, 
			Via.PROTOTYPE, 
			WWW_Authenticate.PROTOTYPE, 
			XSpecial.PROTOTYPE
		};

	/**
	 * 
	 * @author pierreconvert
	 *
	 */
	public static abstract class Prototype {
		
		public final int code;
		
		public String preferredName;
		
		public final String[] names;
		
		public final boolean isPseudo;
		
		public final HTTP2_HeaderTarget target;
	
		public final HTTP2_HeaderRefresh behavior;
		
		
		public Prototype(int code, String[] names, boolean isPseudo, HTTP2_HeaderTarget target, HTTP2_HeaderRefresh behavior) {
			super();
			this.code = code;
			this.preferredName = names[0];
			this.names = names;
			this.isPseudo = isPseudo;
			this.target = target;
			this.behavior = behavior;
		}	

		/**
		 * create empty
		 * @return
		 */
		public abstract HTTP2_Header create();
		
		
		/**
		 * parse from value
		 * @param value
		 * @return
		 */
		public abstract HTTP2_Header parse(String value);

		/**
		 * 
		 * @return header name
		 */
		public String getPreferredName() {
			return preferredName;
		}
		
		/**
		 * 
		 */
		public abstract HTTP2_Header retrieve(HTTP2_Message message);
		
	}
	
	
	public abstract void bind(HTTP2_Message message);
	
	
	public abstract Prototype getPrototype();
	
	//public String value;

	//public HTTP2_Header previous, next;
	
	
	public HTTP2_Header() {
		super();
	}
	

	
	
	//public abstract boolean isPseudo();
		
	//public abstract HTTP2_HeaderTarget getTarget();
	
	//public abstract HTTP2_HeaderRefresh getBehavior();
	
	
	/**
	 * 
	 * @param name
	 * @return true if header name is the same as argument
	 */
	public boolean hasName(String name) {
		return getPrototype().names[0].equals(name);
	}
	
	/**
	 * 
	 * @param name
	 * @return true if header name is the same as argument
	 */
	public boolean isValueEqualTo(String value) {
		if(getValue()!=null) {
			return getValue().equals(value);
		}
		else {
			return false;
		}
	}

	
	
	/**
	 * 
	 * @return header name
	 */
	public String getName() {
		return getPrototype().getPreferredName();
	}
	
	public abstract String getValue();
	
	public abstract void setValue(String string);
	

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getName());
		builder.append(": ");
		builder.append(getValue());
		return builder.toString();
	}
	
	
	/*
	public void pop() {
		if(previous!=null) {
			previous.next = next;
		}
		if(next!=null) {
			next.previous = previous;
		}
	}
	*/


	/**
	 * 
	 * @return
	 */
	public int getCode() {
		return getPrototype().code;
	}


	

}
