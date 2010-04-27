package org.sipfoundry.commons.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.BitSet;

import org.apache.log4j.Logger;


/**
 * Copied from Apache Excalibur project.
 * Source code available at http://www.google.com/codesearch?hl=en&q=+excalibur+decodePath+show:sK_gDY0W5Rw:OTjCHAiSuF0:th3BdHtpX20&sa=N&cd=1&ct=rc&cs_p=http://apache.edgescape.com/excalibur/excalibur-sourceresolve/source/excalibur-sourceresolve-1.1-src.zip&cs_f=excalibur-sourceresolve-1.1/src/java/org/apache/excalibur/source/SourceUtil.java
 *
 */
public class RFC2396UrlDecoder {

    static BitSet charactersDontNeedingEncoding;
    static final int characterCaseDiff = ('a' - 'A');
    private static Logger logger = Logger.getLogger(RFC2396UrlDecoder.class.getCanonicalName());
    
    /** Initialize the BitSet */
    static {
        charactersDontNeedingEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            charactersDontNeedingEncoding.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            charactersDontNeedingEncoding.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            charactersDontNeedingEncoding.set(i);
        }
        charactersDontNeedingEncoding.set('-');
        charactersDontNeedingEncoding.set('_');
        charactersDontNeedingEncoding.set('.');
        charactersDontNeedingEncoding.set('*');
        charactersDontNeedingEncoding.set('"');
    }

        
    /**
     * Translates a string into <code>x-www-form-urlencoded</code> format.
     *
     * @param   s   <code>String</code> to be translated.
     * @return  the translated <code>String</code>.
     */
    public static String encode(String s) {
        final StringBuffer out = new StringBuffer(s.length());
        final ByteArrayOutputStream buf = new ByteArrayOutputStream(32);
        final OutputStreamWriter writer = new OutputStreamWriter(buf);
        for (int i = 0; i < s.length(); i++) {
            int c = s.charAt(i);
            if (charactersDontNeedingEncoding.get(c)) {
                out.append((char) c);
            } else {
                try {
                    writer.write(c);
                    writer.flush();
                } catch (IOException e) {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for (int j = 0; j < ba.length; j++) {
                    out.append('%');
                    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
                    // converting to use uppercase letter as part of
                    // the hex value if ch is a letter.
                    if (Character.isLetter(ch)) {
                        ch -= characterCaseDiff;
                    }
                    out.append(ch);
                    ch = Character.forDigit(ba[j] & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= characterCaseDiff;
                    }
                    out.append(ch);
                }
                buf.reset();
            }
        }

        return out.toString();
    }

    
    /**
     * Decode a path.
     *
     * <p>Interprets %XX (where XX is hexadecimal number) as UTF-8 encoded bytes.
     * <p>The validity of the input path is not checked (i.e. characters that
     * were not encoded will not be reported as errors).
     * <p>This method differs from URLDecoder.decode in that it always uses UTF-8
     * (while URLDecoder uses the platform default encoding, often ISO-8859-1),
     * and doesn't translate + characters to spaces.
     *
     * @param uri the path to decode
     * @return the decoded path
     */
    public static String decode(String uri) {
        if(logger.isDebugEnabled()) {
            logger.debug("uri to decode " + uri);
        }
        if(uri == null) {
             return null;
        }
        StringBuffer translatedUri = new StringBuffer(uri.length());
        byte[] encodedchars = new byte[uri.length() / 3];
        int i = 0;
        int length = uri.length();
        int encodedcharsLength = 0;
        while (i < length) {
            if (uri.charAt(i) == '%') {
                //we must process all consecutive %-encoded characters in one go, because they represent
                //an UTF-8 encoded string, and in UTF-8 one character can be encoded as multiple bytes
                while (i < length && uri.charAt(i) == '%') {
                    if (i + 2 < length) {
                        try {
                            byte x = (byte)Integer.parseInt(uri.substring(i + 1, i + 3), 16);
                            encodedchars[encodedcharsLength] = x;
                        } catch (NumberFormatException e) {
                            // do not throw exception, a % could be part of a IPv6 address and still be valid
//                            throw new IllegalArgumentException("Illegal hex characters in pattern %" + uri.substring(i + 1, i + 3));
                        }
                        encodedcharsLength++;
                        i += 3;
                    } else {
                        // do not throw exception, a % could be part of a IPv6 address and still be valid
//                        throw new IllegalArgumentException("% character should be followed by 2 hexadecimal characters.");
                    }
                }
                try {
                    String translatedPart = new String(encodedchars, 0, encodedcharsLength, "UTF-8");
                    translatedUri.append(translatedPart);
                } catch (UnsupportedEncodingException e) {
                    //the situation that UTF-8 is not supported is quite theoretical, so throw a runtime exception
                    throw new RuntimeException("Problem in decodePath: UTF-8 encoding not supported.");
                }
                encodedcharsLength = 0;
            } else {
                //a normal character
                translatedUri.append(uri.charAt(i));
                i++;
            }
        }
        if(logger.isDebugEnabled()) {
            logger.debug("decoded uri " + translatedUri);
        }
        return translatedUri.toString();
    }
}
