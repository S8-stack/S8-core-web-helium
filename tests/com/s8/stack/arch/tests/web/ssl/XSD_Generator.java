package com.s8.stack.arch.tests.web.ssl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.s8.core.io.xml.codebase.XML_Codebase;
import com.s8.core.web.helium.ssl.SSL_WebConfiguration;

public class XSD_Generator {

	public static void main(String[] args) throws Exception {
		
		XML_Codebase context = XML_Codebase.from(SSL_WebConfiguration.class);
		
		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("config/schema.xsd"))));
		context.xsd_writeSchema(writer);
		writer.close();
		
		System.out.println("XSD schema generated");
	}
	
	
}
