// License: MIT
// Author: io.github.odys-z
//
// Credits: org.eclipse.jetty.http.MimeTypes;

package io.odysz.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static io.odysz.common.LangExt.isblank;

/**
 * MIME MimeTypes enum and utilities
 * 
 * see <a href='https://docs.w3cub.com/http/basics_of_http/mime_types/complete_list_of_mime_types.html'>docs.w3cub.com</a>
 */
public enum MimeTypes {

	html("text/html"),
	txt("text/plain"),
	pdf("application/pdf"),
	image("image/"),
	video("video/"),
	audio("audio/"),
	xml("application/xml"),
	xml_("text/xml"),
	json_("text/json", StandardCharsets.UTF_8),
	json("application/json", StandardCharsets.UTF_8),
	zip("application/zip"),

	html_8859_1("text/html;charset=iso-8859-1", html),
	html_utf_8("text/html;charset=utf-8", html),

	txt_8859_1("text/plain;charset=iso-8859-1", txt),
	txt_utf_8("text/plain;charset=utf-8", txt),

	xml_8859_1("text/xml;charset=iso-8859-1", xml_),
	xml_utf_8("text/xml;charset=utf-8", xml_),

	json_8859_1("text/json;charset=iso-8859-1", json_),
	json_utf_8("text/json;charset=utf-8", json_),

	json8859_1("application/json;charset=iso-8859-1", json),
	jsonUtf_8("application/json;charset=utf-8", json);
	
    private final String _string;
    private final MimeTypes _base;
    private final Charset _charset;
    private final String _charsetString;
    private final boolean _assumedCharset;

    MimeTypes(String s) {
        _string = s;
        _base = this;
        _charset = null;
        _charsetString = null;
        _assumedCharset = false;
    }

    MimeTypes(String s, MimeTypes base) {
        _string = s;
        _base = base;
        int i = s.indexOf(";charset=");
        _charset = Charset.forName(s.substring(i + 9));
        _charsetString = _charset.toString().toLowerCase(Locale.ENGLISH);
        _assumedCharset = false;
    }

    MimeTypes(String s, Charset cs) {
        _string = s;
        _base = this;
        _charset = cs;
        _charsetString = _charset == null ? null : _charset.toString().toLowerCase(Locale.ENGLISH);
        _assumedCharset = true;
    }

    public Charset getCharset() {
        return _charset;
    }

    public String getCharsetString() {
        return _charsetString;
    }

    public boolean is(String s) {
        return isblank(s) ? false : _string.equalsIgnoreCase(s.substring(0, Math.min(_string.length(), s.length())));
    }

    public String string() {
        return _string;
    }

    @Override
    public String toString() {
        return _string;
    }

    public boolean isCharsetAssumed() {
        return _assumedCharset;
    }

    public MimeTypes getBaseType() {
        return _base;
    }
    
    public static boolean isImgVideo(String t) {
    	return t != null && (t.startsWith(image.string()) || t.startsWith(video.string()));
    }

	public static boolean isAudio(String t) {
    	return t != null && t.startsWith(audio.string());
	}

	public static boolean isVideo(String t) {
    	return t != null && t.startsWith(video.string());
	}

	public static boolean isPdf(String t) {
    	return t != null && t.startsWith("application/pdf");
	}
}
