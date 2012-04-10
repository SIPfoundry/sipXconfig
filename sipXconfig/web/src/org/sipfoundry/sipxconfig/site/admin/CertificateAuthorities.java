/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.site.admin;



import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.request.IUploadFile;
import org.sipfoundry.sipxconfig.cert.CertificateManager;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.SelectMap;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;
import org.sipfoundry.sipxconfig.components.TapestryUtils;

@ComponentClass
public abstract class CertificateAuthorities extends BaseComponent {

    public abstract IUploadFile getUploadFile();

    @Bean
    public abstract SipxValidationDelegate getValidator();

    public abstract String getAuthority();

    @Persist(value = "client")
    public abstract String getRowToShow();

    public abstract void setRowToShow(String row);

    @Bean
    public abstract SelectMap getSelections();

    @InjectObject(value = "spring:certificateManager")
    public abstract CertificateManager getCertificateManager();

    @InitialValue(value = "false")
    public abstract boolean isShowCertificate();

    public abstract void setShowCertificate(boolean showCertificate);

    public abstract String getCertificateText();

    public abstract void setCertificateText(String certificateText);

    public abstract String getSavedCertificateText();

    public abstract void setSavedCertificateText(String savedCertificateText);

    public boolean isShowDescription() {
        return getRowToShow() == null ? false : getRowToShow().equals(getAuthority());
    }

    public void clickRow() {
        if (getRowToShow() != null && getRowToShow().equals(getAuthority())) {
            setRowToShow(null);
        } else {
            setRowToShow(getAuthority());
            runShowDescription();
        }
    }

    public void importCA() {
        if (!TapestryUtils.isValid(this)) {
            // do nothing on errors
            return;
        }

        IUploadFile uploadFile = getUploadFile();
        if (uploadFile == null) {
            getValidator().record(new UserException("&error.certificate"), getMessages());
            return;
        }
        String caFileName = uploadFile.getFileName();
        Reader r = null;
        try {
            String ca = IOUtils.toString(uploadFile.getStream());
            getCertificateManager().addTrustedAuthority(caFileName, ca);
            setShowCertificate(true);
            setCertificateText(ca.toString());
        } catch (UserException err) {
            getValidator().record(new UserException("&error.valid", caFileName), getMessages());
        } catch (IOException e) {
            getValidator().record(new UserException("Error reading file " + e.getMessage()), getMessages());
        } finally {
            IOUtils.closeQuietly(r);
        }
    }

    public void runShowDescription() {
        String ca = getCertificateManager().getAuthorityCertificate(getAuthority());
        setSavedCertificateText(ca);
    }

    public void deleteCertificates() {
        @SuppressWarnings("rawtypes")
        Collection selections = getSelections().getAllSelected();
        CertificateManager mgr = getCertificateManager();
        for (Object authority : selections) {
            mgr.deleteTrustedAuthority(authority.toString());
        }
    }
}
