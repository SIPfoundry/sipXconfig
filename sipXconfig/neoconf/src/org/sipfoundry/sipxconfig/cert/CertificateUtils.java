/*
 * Copyright (C) 2012 eZuce Inc., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the AGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.cert;

import static java.lang.String.format;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.sipfoundry.sipxconfig.common.UserException;

public final class CertificateUtils {
    private static final String PROVIDER = "BC";
    private static final int MAX_HEADER_LINE_COUNT = 512;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private CertificateUtils() {
    }

    public static String getProvider() {
        // making method instead of constant ensure static code block to add provider
        // has been executed
        return PROVIDER;
    }

    public static X509Certificate readCertificate(String in) {
        return readCertificate(new StringReader(in));
    }

    public static X509Certificate readCertificate(Reader in) {
        Object o = readObject(in);
        if (!(o instanceof X509Certificate)) {
            String msg = format("Certificate was expected but found %s instead", o.getClass().getSimpleName());
            throw new UserException(msg);
        }
        return (X509Certificate) o;
    }

    public static X500Name x500(String txt) {
        return X500Name.getInstance(new X509Principal(txt).getEncoded());
    }

    public static X509Certificate generateCert(X509v3CertificateBuilder gen, String algorithm, PrivateKey key)
        throws GeneralSecurityException {
        ContentSigner sigGen;
        try {
            sigGen = new JcaContentSignerBuilder(algorithm).setProvider(PROVIDER).build(key);
            JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider(PROVIDER);
            return converter.getCertificate(gen.build(sigGen));
        } catch (OperatorCreationException e) {
            throw new GeneralSecurityException(e);
        } catch (CertificateException e) {
            throw new GeneralSecurityException(e);
        }
    }

    public static Object readObject(Reader in) {
        PEMReader rdr = new PEMReader(in);
        try {
            for (int i = 0; i < MAX_HEADER_LINE_COUNT; i++) {
                Object o = rdr.readObject();
                if (o != null) {
                    return o;
                }
            }
        } catch (IOException e) {
            throw new UserException("Error reading certificate. " + e.getMessage(), e);
        }
        throw new UserException("No recognized security information was found. Files "
                + "should be in PEM style format.");
    }

    public static PrivateKey readCertificateKey(String in) {
        return readCertificateKey(new StringReader(in));
    }

    public static PrivateKey readCertificateKey(Reader in) {
        Object o = readObject(in);
        if (o instanceof KeyPair) {
            return ((KeyPair) o).getPrivate();
        }
        if (o instanceof PrivateKey) {
            return (PrivateKey) o;
        }

        String msg = format("Private key was expected but found %s instead", o.getClass().getSimpleName());
        throw new UserException(msg);
    }

    public static void writeObject(Writer w, Object o, String description) {
        PEMWriter pw = new PEMWriter(w);
        try {
            if (description != null) {
                w.write(description);
            }
            pw.writeObject(o);
            pw.close();
        } catch (IOException e) {
            throw new UserException("Problem updating certificate authority. " + e.getMessage(), e);
        }
    }

    static String stripPath(String s) {
        if (StringUtils.isBlank(s)) {
            return s;
        }
        int i = s.lastIndexOf('/');
        if (i < 0) {
            return s;
        }
        if (i  + 1 == s.length()) {
            return StringUtils.EMPTY;
        }
        return s.substring(i + 1);
    }
}
