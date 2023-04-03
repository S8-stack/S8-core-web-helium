package com.s8.stack.arch.helium.http2;


/**
 * Standard definitions of status
 * 
 * @author pc
 *
 */
public enum HTTP2_Status {
	
	
	/**
	 * This interim response indicates that everything so far is OK and that 
	 * the client should continue with the request or ignore it if it is already finished.
	 */
	CONTINUE(100),
	
	/**
	 * This code is sent in response to an Upgrade request header by the client,
	 *  and indicates the protocol the server is switching to.
	 */
	SWITCHING_PROTOCOL(101),
	
	/**
	 * (WebDAV)
	 * This code indicates that the server has received and is processing the request, 
	 * but no response is available yet.
	 */
	PROCESSING(102),
	
	
	/**
	 * This status code is primarily intended to be used with the Link header to allow 
	 * the user agent to start preloading resources while the server is still preparing a response.
	 */
	Early_Hints(103),
	
	
	// Successful responses Section
	
	/**
	 * The request has succeeded. The meaning of a success varies depending on the HTTP method
	 * <ul>
	 * <li>GET: The resource has been fetched and is transmitted in the message body.</li>
	 * <li>HEAD: The entity headers are in the message body.</li>
	 * <li>PUT or POST: The resource describing the result of the action is transmitted in the message body.</li>
	 * <li>TRACE: The message body contains the request message as received by the server</li>
	 * </ul>
	 */
	OK(200),
	
	
	/**
	 * The request has succeeded and a new resource has been created as a result of it. 
	 * This is typically the response sent after a POST request, or after some PUT requests.
	 */
	Created(201),
	
	/**
	 * The request has been received but not yet acted upon. It is non-committal, meaning that 
	 * there is no way in HTTP to later send an asynchronous response indicating the outcome of 
	 * processing the request. It is intended for cases where another process or server handles 
	 * the request, or for batch processing.
	 */
	ACCEPTED(202),
	
	
	/**
	 * This response code means returned meta-information set is not exact set as available from 
	 * the origin server, but collected from a local or a third party copy. Except this condition, 
	 * 200 OK response should be preferred instead of this response.
	 */
	NonAuthoritative_Information(203),
	
	/**
	 * There is no content to send for this request, but the headers may be useful. The user-agent 
	 * may update its cached headers for this resource with the new ones.
	 */
	NO_CONTENT(204),
	
	/**
	 * This response code is sent after accomplishing request to tell user agent reset document view 
	 * which sent this request.
	 */
	RESET_CONTENT(205),
	
	/**
	 * This response code is used because of range header sent by the client to separate download 
	 * into multiple streams.
	 */
	Partial_Content(206),
	
	/**
	 * (WebDAV)
	 * A Multi-Status response conveys information about multiple resources in situations 
	 * where multiple status codes might be appropriate.
	 */
	MultiStatus(207),
	
	/**
	 * (WebDAV)
	 * Used inside a DAV: propstat response element to avoid enumerating the internal 
	 * members of multiple bindings to the same collection repeatedly.
	 */
	MultiStatus_InsideDAV(208),
	
	/**
	 * (HTTP Delta encoding)
	 * The server has fulfilled a GET request for the resource, and the response is a 
	 * representation of the result of one or more instance-manipulations applied to the 
	 * current instance.
	 */
	IM_Used(226), 
	
	// Redirection messagesSection
	
	/**
	 * The request has more than one possible response. The user-agent or user should 
	 * choose one of them. There is no standardized way of choosing one of the responses.
	 */
	Multiple_Choice(300),
	
	/**
	 * This response code means that the URI of the requested resource has been changed. 
	 * Probably, the new URI would be given in the response.
	 */
	MOVED_PERMANENTLY(301),
	
	/**
	 * This response code means that the URI of requested resource has been changed 
	 * temporarily. New changes in the URI might be made in the future. Therefore, 
	 * this same URI should be used by the client in future requests.
	 */
	FOUND(302),
	
	/**
	 * The server sent this response to direct the client to get the requested 
	 * resource at another URI with a GET request.
	 */
	See_Other(303),
	
	/**
	 * This is used for caching purposes. It tells the client that the response 
	 * has not been modified, so the client can continue to use the same cached 
	 * version of the response.
	 */
	Not_Modified(304),
	
	/**
	 * Was defined in a previous version of the HTTP specification to indicate 
	 * that a requested response must be accessed by a proxy. It has been deprecated 
	 * due to security concerns regarding in-band configuration of a proxy.
	 */
	Use_Proxy(305),
	
	/**
	 * This response code is no longer used, it is just reserved currently. 
	 * It was used in a previous version of the HTTP 1.1 specification.
	 */
	UNSUED_STATUS(306),
	
	/**
	 * The server sends this response to direct the client to get the requested 
	 * resource at another URI with same method that was used in the prior request. 
	 * This has the same semantics as the 302 Found HTTP response code, with the exception 
	 * that the user agent must not change the HTTP method used: If a POST was used in the 
	 * first request, a POST must be used in the second request.
	 */
	Temporary_Redirect(307),
	
	/**
	 * This means that the resource is now permanently located at another URI, specified 
	 * by the Location: HTTP Response header. This has the same semantics as the 301 Moved 
	 * Permanently HTTP response code, with the exception that the user agent must not change 
	 * the HTTP method used: If a POST was used in the first request, a POST must be used 
	 * in the second request.
	 */
	Permanent_Redirect(308),
	
	// Client error responsesSection
	
	/**
	 * This response means that server could not understand the request due to invalid syntax.
	 */
	BAD_REQUEST(400),
	
	/**
	 * Although the HTTP standard specifies "unauthorized", semantically this response means 
	 * "unauthenticated". That is, the client must authenticate itself to get the requested response.
	 */
	UNAUTHORIZED(401),
	
	/**
	 * This response code is reserved for future use. Initial aim for creating this code was using 
	 * it for digital payment systems however this is not used currently.
	 */
	Payment_Required(402),
	
	/**
	 * The client does not have access rights to the content, i.e. they are unauthorized, so 
	 * server is rejecting to give proper response. Unlike 401, the client's identity is known to 
	 * the server.
	 */
	Forbidden(403),
	
	/**
	 * The server can not find requested resource. In the browser, this means the URL 
	 * is not recognized. In an API, this can also mean that the endpoint is valid but the 
	 * resource itself does not exist. Servers may also send this response instead of 403 to 
	 * hide the existence of a resource from an unauthorized client. This response code is 
	 * probably the most famous one due to its frequent occurence on the web.
	 */
	NOT_FOUND(404),
	
	/**
	 * The request method is known by the server but has been disabled and cannot be used. 
	 * For example, an API may forbid DELETE-ing a resource. The two mandatory methods, 
	 * GET and HEAD, must never be disabled and should not return this error code.
	 */
	METHOD_NOT_ALLOWED(405),
	
	/**
	 * This response is sent when the web server, after performing server-driven content 
	 * negotiation, doesn't find any content following the criteria given by the user agent.
	 */
	NOT_ACCEPTABLE(406),
	
	/**
	 * This is similar to 401 but authentication is needed to be done by a proxy.
	 */
	Proxy_Authentication_Required(407),
	
	/**
	 * This response is sent on an idle connection by some servers, even without 
	 * any previous request by the client. It means that the server would like to 
	 * shut down this unused connection. This response is used much more since some browsers, 
	 * like Chrome, Firefox 27+, or IE9, use HTTP pre-connection mechanisms to speed up surfing. 
	 * Also note that some servers merely shut down the connection without sending this message.
	 */
	REQUEST_TIMEOUT(408),
	
	/**
	 * This response is sent when a request conflicts with the current state of the server.
	 */
	Conflict(409),
	
	/**
	 * This response would be sent when the requested content has been permanently deleted 
	 * from server, with no forwarding address. Clients are expected to remove their caches 
	 * and links to the resource. The HTTP specification intends this status code to be used 
	 * for "limited-time, promotional services". APIs should not feel compelled to indicate 
	 * resources that have been deleted with this status code.
	 */
	Gone(410),
	
	/**
	 * Server rejected the request because the Content-Length header field is not defined 
	 * and the server requires it.
	 */
	Length_Required(411),
	
	/**
	 * The client has indicated preconditions in its headers which the server does not meet.
	 */
	Precondition_Failed(412),
	
	/**
	 * Request entity is larger than limits defined by server; the server might close the 
	 * connection or return an Retry-After header field.
	 */
	Payload_Too_Large(413),
	
	/**
	 * The URI requested by the client is longer than the server is willing to interpret.
	 */
	URI_Too_Long(414),
	
	/**
	 * The media format of the requested data is not supported by the server, so the server 
	 * is rejecting the request.
	 */
	Unsupported_Media_Type(415),
	
	/**
	 * The range specified by the Range header field in the request can't be fulfilled; 
	 * it's possible that the range is outside the size of the target URI's data.
	 */
	Requested_Range_Not_Satisfiable(416),
	
	/**
	 * This response code means the expectation indicated by the Expect request header 
	 * field can't be met by the server.
	 */
	Expectation_Failed(417),
	
	/**
	 * The server refuses the attempt to brew coffee with a teapot.
	 */
	IM_A_TEAPOT(418),
	
	/**
	 * The request was directed at a server that is not able to produce a response. 
	 * This can be sent by a server that is not configured to produce responses for the combination 
	 * of scheme and authority that are included in the request URI.
	 */
	Misdirected_Request(421),
	
	/**
	 * (WebDAV)
	 * The request was well-formed but was unable to be followed due to semantic errors.
	 */
	Unprocessable_Entity(422), 
	
	/**
	 * (WebDAV) The resource that is being accessed is locked.
	 */
	Locked(423), 
	
	/**
	 * (WebDAV) The request failed due to failure of a previous request.
	 */
	Failed_Dependency(424),
	
	/**
	 * Indicates that the server is unwilling to risk processing a request that might be replayed.
	 */
	Too_Early(425),
	
	/**
	 * The server refuses to perform the request using the current protocol but might be willing 
	 * to do so after the client upgrades to a different protocol. The server sends an Upgrade 
	 * header in a 426 response to indicate the required protocol(s).
	 */
	Upgrade_Required(426),
	
	/**
	 * The origin server requires the request to be conditional. Intended to prevent the 
	 * 'lost update' problem, where a client GETs a resource's state, modifies it, and 
	 * PUTs it back to the server, when meanwhile a third party has modified the state on the 
	 * server, leading to a conflict.
	 */
	Precondition_Required(428),
	
	/**
	 * The user has sent too many requests in a given amount of time ("rate limiting").
	 */
	Too_Many_Requests(429),
	
	/**
	 * The server is unwilling to process the request because its header fields are too large. 
	 * The request MAY be resubmitted after reducing the size of the request header fields.
	 */
	Request_Header_Fields_Too_Large(431),
	
	/**
	 * The user requests an illegal resource, such as a web page censored by a government.
	 */
	Unavailable_For_Legal_Reasons(451),
	
	
	
	
	// Server error responsesSection
	/**
	 * The server has encountered a situation it doesn't know how to handle.
	 */
	INTERNAL_SERVER_ERROR(500),
	
	/**
	 * The request method is not supported by the server and cannot be handled. 
	 * The only methods that servers are required to support (and therefore that 
	 * must not return this code) are GET and HEAD.
	 */
	NOT_IMPLEMENTED(501),
	
	/**
	 * This error response means that the server, while working as a gateway 
	 * to get a response needed to handle the request, got an invalid response.
	 */
	Bad_Gateway(502),
	
	/**
	 * The server is not ready to handle the request. Common causes are a server 
	 * that is down for maintenance or that is overloaded. Note that together with 
	 * this response, a user-friendly page explaining the problem should be sent. 
	 * This responses should be used for temporary conditions and the Retry-After: 
	 * HTTP header should, if possible, contain the estimated time before the 
	 * recovery of the service. The webmaster must also take care about the 
	 * caching-related headers that are sent along with this response, as these 
	 * temporary condition responses should usually not be cached.
	 */
	Service_Unavailable(503),
	
	/**
	 * This error response is given when the server is acting as a gateway and cannot 
	 * get a response in time.
	 */
	Gateway_Timeout(504),
	
	/**
	 * The HTTP version used in the request is not supported by the server.
	 */
	HTTP_Version_Not_Supported(505),
	
	/**
	 * The server has an internal configuration error: transparent 
	 * content negotiation for the request results in a circular reference.
	 */
	Variant_Also_Negotiates(506),
	
	/**
	 * The server has an internal configuration error: the chosen variant resource 
	 * is configured to engage in transparent content negotiation itself, and is 
	 * therefore not a proper end point in the negotiation process.
	 */
	Insufficient_Storage(507),
	
	/**
	 * (WebDAV) The server detected an infinite loop while processing the request.
	 */
	Loop_Detected(508),
	
	/**
	 * Further extensions to the request are required for the server to fulfill it.
	 */
	Not_Extended(510),
	
	/**
	 * The 511 status code indicates that the client needs to authenticate to gain 
	 * network access.
	 */
	Network_Authentication_Required(511);
	
	
	public int code;
	
	
	private HTTP2_Status(int code) {
		this.code = code;
	}
	
	private static HTTP2_Status[] MAP;
	
	public static HTTP2_Status byCode(int code) {
		if(MAP==null) {
			// build mapping
			MAP = new HTTP2_Status[512];
			for(HTTP2_Status status : HTTP2_Status.values()) {
				MAP[status.code] = status;
			}
		}
		return MAP[code];	
	}
	
}
