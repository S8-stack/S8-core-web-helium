package com.s8.core.web.helium.http1;

import com.s8.core.io.xml.annotations.XML_Type;
import com.s8.core.web.helium.rx.RxWebConfiguration;


@XML_Type(root = true, name="HTTP1_WebConfiguration", sub= {})
public class HTTP1_WebConfiguration extends RxWebConfiguration {

	public HTTP1_WebConfiguration() {
		super();
		isServer = true;
		port = 80;
	}
}
