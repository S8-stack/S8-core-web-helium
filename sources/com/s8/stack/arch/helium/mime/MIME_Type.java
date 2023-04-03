package com.s8.stack.arch.helium.mime;

import static com.s8.stack.arch.helium.mime.MIME_TopType.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




/**
 * </p>
 * 
 * </p>
 * @author pc
 *
 */
public enum MIME_Type {
		
	/**
	 * application/octet-stream for sending byte chain to the client
	 */
	OCTET_STREAM(APPLICATION, "octet-stream", ".glsh"),
	
	JS(APPLICATION, "javascript", ".js"),
	
	JSON(APPLICATION, "json", ".json"),
	
	
	/**
	 * 
	 */
	XML(APPLICATION, "xml", ".xml"),

	
	/**
	 * 
	 */
	ZIP(APPLICATION, "zip", ".zip"),

	
	/**
	 * 
	 */
	SVG(IMAGE, "svg+xml", ".svg"),
	
	MPEG(AUDIO, "mpeg", ".mpeg"),
	
	CSS(MIME_TopType.TEXT, "css", ".css"),
	
	HTML(MIME_TopType.TEXT, "html", ".html"),
	
	CSV(MIME_TopType.TEXT, "csv", ".csv"),
	
	PNG(IMAGE, "png", ".png"),
	
	GIF(IMAGE, "gif", ".gif"),
	
	JPEG(IMAGE, "jpeg", ".jpeg"), 
	
	TEXT(MIME_TopType.TEXT, "text", ".jpeg");
	
	
	
	public MIME_TopType topType;
	
	public String template;
	
	public String[] extensions;
	
	
	private MIME_Type(MIME_TopType topType, String template, String... extensions) {
		this.topType = topType;
		this.template = topType.template+"/"+template;
		this.extensions = extensions;
	}
	
	
	@Override
	public String toString() {
		return template;
	}
	
	
	private static boolean isInitialized = false;

	private static Map<String, MIME_Type> MAP;
	
	private static Map<String, MIME_Type> EXTENSIONS;
	
	

	
	private static void initialize() {
		if(!isInitialized) {
			
			MAP = new HashMap<>();
			
			EXTENSIONS = new HashMap<>();
			
			try (InputStream inputStream = MIME_Type.class.getResourceAsStream("types.xml")){
				for(MIME_Type type : values()) {
					MAP.put(type.template, type);
					if(type.extensions!=null) {
						for(String ext : type.extensions) {
							EXTENSIONS.put(ext, type);		
						}
					}
				}
			} 
			catch (IOException e) {
				System.err.println("Failed to read MIME types due to: "+e.getMessage());
				e.printStackTrace();
			}
			
			isInitialized = true;
		}
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	public static MIME_Type get(String template){
		initialize();
		return MAP.get(template);
	}
	
	
	public final static Pattern FILENAME_PATTERN = Pattern.compile("[\\w\\./-]*(\\.\\w+)");

	
	public static MIME_Type find(String filname) {
		initialize();
		Matcher matcher = FILENAME_PATTERN.matcher(filname);
		if(!matcher.matches()) {
			return null;
		}
		String extension = matcher.group(1);
		return EXTENSIONS.get(extension);
	}
}
