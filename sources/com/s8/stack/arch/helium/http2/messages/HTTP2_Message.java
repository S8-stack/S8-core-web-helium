package com.s8.stack.arch.helium.http2.messages;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

import com.s8.io.bytes.linked.LinkedBytes;
import com.s8.stack.arch.helium.http2.HTTP2_Method;
import com.s8.stack.arch.helium.http2.headers.Accept;
import com.s8.stack.arch.helium.http2.headers.AcceptCharset;
import com.s8.stack.arch.helium.http2.headers.AcceptEncoding;
import com.s8.stack.arch.helium.http2.headers.AcceptLanguage;
import com.s8.stack.arch.helium.http2.headers.AcceptRanges;
import com.s8.stack.arch.helium.http2.headers.AccessControlAllowHeaders;
import com.s8.stack.arch.helium.http2.headers.AccessControlAllowMethod;
import com.s8.stack.arch.helium.http2.headers.AccessControlAllowOrigin;
import com.s8.stack.arch.helium.http2.headers.AccessControlRequestHeaders;
import com.s8.stack.arch.helium.http2.headers.AccessControlRequestMethod;
import com.s8.stack.arch.helium.http2.headers.Age;
import com.s8.stack.arch.helium.http2.headers.Allow;
import com.s8.stack.arch.helium.http2.headers.Authority;
import com.s8.stack.arch.helium.http2.headers.Authorization;
import com.s8.stack.arch.helium.http2.headers.CacheControl;
import com.s8.stack.arch.helium.http2.headers.Connection;
import com.s8.stack.arch.helium.http2.headers.ContentDisposition;
import com.s8.stack.arch.helium.http2.headers.ContentEncoding;
import com.s8.stack.arch.helium.http2.headers.ContentLanguage;
import com.s8.stack.arch.helium.http2.headers.ContentLength;
import com.s8.stack.arch.helium.http2.headers.ContentLocation;
import com.s8.stack.arch.helium.http2.headers.ContentRange;
import com.s8.stack.arch.helium.http2.headers.ContentType;
import com.s8.stack.arch.helium.http2.headers.Cookie;
import com.s8.stack.arch.helium.http2.headers.Date;
import com.s8.stack.arch.helium.http2.headers.ETag;
import com.s8.stack.arch.helium.http2.headers.Expires;
import com.s8.stack.arch.helium.http2.headers.From;
import com.s8.stack.arch.helium.http2.headers.HTTP2_Header;
import com.s8.stack.arch.helium.http2.headers.Host;
import com.s8.stack.arch.helium.http2.headers.IfMatch;
import com.s8.stack.arch.helium.http2.headers.IfModifiedSince;
import com.s8.stack.arch.helium.http2.headers.IfNoneMatch;
import com.s8.stack.arch.helium.http2.headers.IfRange;
import com.s8.stack.arch.helium.http2.headers.IfUnmodifiedSince;
import com.s8.stack.arch.helium.http2.headers.LastModified;
import com.s8.stack.arch.helium.http2.headers.Link;
import com.s8.stack.arch.helium.http2.headers.Location;
import com.s8.stack.arch.helium.http2.headers.MaxForwards;
import com.s8.stack.arch.helium.http2.headers.Method;
import com.s8.stack.arch.helium.http2.headers.Origin;
import com.s8.stack.arch.helium.http2.headers.Path;
import com.s8.stack.arch.helium.http2.headers.Pragma;
import com.s8.stack.arch.helium.http2.headers.ProxyAuthenticate;
import com.s8.stack.arch.helium.http2.headers.ProxyAuthorization;
import com.s8.stack.arch.helium.http2.headers.Range;
import com.s8.stack.arch.helium.http2.headers.Referer;
import com.s8.stack.arch.helium.http2.headers.Refresh;
import com.s8.stack.arch.helium.http2.headers.RetryAfter;
import com.s8.stack.arch.helium.http2.headers.Scheme;
import com.s8.stack.arch.helium.http2.headers.Server;
import com.s8.stack.arch.helium.http2.headers.SetCookie;
import com.s8.stack.arch.helium.http2.headers.Status;
import com.s8.stack.arch.helium.http2.headers.StrictTransportSecurity;
import com.s8.stack.arch.helium.http2.headers.Unmapped;
import com.s8.stack.arch.helium.http2.headers.UpgradeInsecureRequest;
import com.s8.stack.arch.helium.http2.headers.UserAgent;
import com.s8.stack.arch.helium.http2.headers.Vary;
import com.s8.stack.arch.helium.http2.headers.Via;
import com.s8.stack.arch.helium.http2.headers.WWW_Authenticate;
import com.s8.stack.arch.helium.http2.headers.XSpecial;
import com.s8.stack.arch.helium.http2.hpack.HPACK_Context;
import com.s8.stack.arch.helium.http2.streams.HTTP2_Stream;
/**
 * 
 * @author pc
 *
 */
public class HTTP2_Message implements Iterable<HTTP2_Header> {
	/* <accept> */
	public Accept accept;
	public AcceptCharset acceptCharset;
	public AcceptEncoding acceptEncoding;
	public AcceptLanguage acceptLanguage;
	public AcceptRanges acceptRanges;
	/* <accept/> */
	
	/* <access-control-request> */
	public Origin origin;
	public AccessControlRequestHeaders accessControlRequestHeaders;
	public AccessControlRequestMethod accessControlRequestMethod;
	/* <access-control-request/> */

	/* <access-control-allow> */
	public Allow allow;
	public AccessControlAllowHeaders accessControlAllowHeaders; 
	public AccessControlAllowMethod accessControlAllowMethod;
	public AccessControlAllowOrigin accessControlAllowOrigin;
	/* </access-control-allow/> */


	public Authority authority;
	public Authorization authorization;

	public CacheControl cacheControl;

	public Connection connection; 

	/* <content> */
	public ContentDisposition contentDisposition;
	public ContentEncoding contentEncoding;
	public ContentLanguage contentLanguage; 
	public ContentLocation contentLocation;
	public ContentLength contentLength;
	public ContentRange contentRange;
	public ContentType contentType; 
	/* </content> */




	public Cookie cookie;

	public Date date; 
	
	public ETag eTag;

	public HTTP2_Header expect;


	public Scheme scheme;

	public XSpecial xSpecial;

	public Method method;

	public Path path;

	public UserAgent userAgent;

	public Age age;

	public Expires expires; 

	public Status status;

	public From from;
	
	public Host host;
	
	public IfMatch ifMatch;

	public IfModifiedSince ifModifiedSince;
	
	public IfNoneMatch ifNoneMatch;

	public IfRange ifRange;

	public IfUnmodifiedSince ifUnmodifiedSince;

	public LastModified lastModified;

	public Link link;

	public Location location;

	public MaxForwards maxForwards;

	public Pragma pragma;

	public ProxyAuthenticate proxyAuthenticate;

	public ProxyAuthorization proxyAuthorization;

	public Range range;
	
	public Referer referer;

	public Refresh refresh;

	public RetryAfter retryAfter;

	public Server server;

	public SetCookie setCookie;

	public StrictTransportSecurity strictTransportSecurity;

	public Unmapped unmapped;

	public UpgradeInsecureRequest upgradeInsecureRequest;
	
	public Vary vary;

	public Via via;
	
	public WWW_Authenticate wwwAuthenticate;



	public HTTP2_Method getMethod() {
		return method.value;
	}



	/**
	 * Inner mechanism field
	 */
	protected HTTP2_Stream stream;

	/**
	 * Inner mechanism field
	 */
	protected HPACK_Context context; 




	/**
	 * content of the response
	 */
	private LinkedBytes dataFragmentHead, dataFragmentTail;

	/**
	 * 
	 * @param context
	 * @param streamIdentifier
	 */
	public HTTP2_Message(HPACK_Context context, HTTP2_Stream stream) {
		super();
		this.context = context;
		this.stream = stream;
	}


	public HTTP2_Message respond() {
		return new HTTP2_Message(context, stream);
	}



	/**
	 * 
	 * @param header
	 */
	public void setHeader(HTTP2_Header header) {
		header.bind(this);
	}


	public boolean isWaitingForData() {
		if(method==null) {
			return false;
		}
		else {
			//HTTP2_Method method = HTTP2_Method.get(methodHeaderValue);
			if(!method.value.isRequiringData) {
				return false;
			}
			else {
				if(contentLength!=null || contentType!=null) {
					return true;
				}
				else {
					return false;
				}
			}	
		}
	}
	

	/**
	 * Fire the response
	 * @throws IOException 
	 */
	public void send() {
		stream.sendMessage(this);
	}




	/**
	 * Append data fragment and move tail to the <b>actual</b> end of chain.
	 * @param fragment
	 */
	public void appendDataFragment(LinkedBytes fragment) {
		if(dataFragmentTail!=null) {
			dataFragmentTail.next = fragment;
			LinkedBytes newTail = dataFragmentTail;
			while(newTail!=null) {
				newTail = newTail.next;
			}
			dataFragmentTail = newTail;
		}
		else if(fragment!=null){
			dataFragmentHead = fragment;
			LinkedBytes newTail = fragment;
			while(newTail!=null) {
				newTail = newTail.next;
			}
			dataFragmentTail = newTail;
		}
	}

	public LinkedBytes getDataFragmentHead() {
		return dataFragmentHead;
	}

	
	public void onHeaders(Consumer<HTTP2_Header> consumer) {
		for(HTTP2_Header.Prototype prototype : HTTP2_Header.PROTOTYPES) {
			HTTP2_Header header = prototype.retrieve(this);
			if(header!=null) {
				consumer.accept(header);
			}
		}	
	}


	
	
	/**
	 * 
	 * @author pierre convert
	 *
	 */
	private class HeadersIterator implements Iterator<HTTP2_Header> {
		
		private HTTP2_Header.Prototype[] protos;
		private HTTP2_Header header;

		private int i=0;
		
		private int n = HTTP2_Header.PROTOTYPES.length;
		
		public HeadersIterator() {
			super();
			protos = HTTP2_Header.PROTOTYPES;
		}
		
		private HTTP2_Header getHeader(int index) {
			return protos[index].retrieve(HTTP2_Message.this);
		}
		
		@Override
		public boolean hasNext() {
			while(i<n && (header = getHeader(i))==null) {
				i++;
			}
			return i<n;
		}
		
		@Override
		public HTTP2_Header next() {
			i++;
			return header;
		}
	}
	
	
	@Override
	public Iterator<HTTP2_Header> iterator() {
		return new HeadersIterator();
	}



	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("HTTP2:{ stream-id="+stream.getIdentifier()+"\n");

		// <headers>
		for(HTTP2_Header.Prototype prototype : HTTP2_Header.PROTOTYPES) {
			HTTP2_Header header = prototype.retrieve(this);
			builder.append("\t"+header+"\n");
		}
		
		// </headers>

		// <payload>
		builder.append("\n\tContent: ");
		if(getDataFragmentHead()!=null) {
			builder.append("--has content--\n");
		}
		else {
			builder.append("--no content--\n");
		}
		// </payload>
		builder.append("}");
		return builder.toString();
	}





}
