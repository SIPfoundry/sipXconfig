/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.cert;

import static java.lang.String.format;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.common.DaoUtils;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.domain.Domain;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.sipfoundry.sipxconfig.setup.SetupListener;
import org.sipfoundry.sipxconfig.setup.SetupManager;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Certificate Management Implementation.
 */
public class CertificateManagerImpl extends SipxHibernateDaoSupport implements CertificateManager, SetupListener {
    private static final Log LOG = LogFactory.getLog(CertificateManager.class);
    private static final String WEB_CERT = "ssl-web";
    private static final String COMM_CERT = "ssl";
    private static final String AUTHORITY_TABLE = "authority";
    private static final String CERT_TABLE = "cert";
    private static final String CERT_COLUMN = "data";
    private static final String KEY_COLUMN = "private_key";
    private static final String SELF_SIGN_AUTHORITY_PREFIX = "ca.";
    private BeanWithSettingsDao<CertificateSettings> m_settingsDao;
    private SetupListener m_locationSetup;
    private LocationsManager m_locationsManager;
    private JdbcTemplate m_jdbc;

    public CertificateSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    public void saveSettings(CertificateSettings settings) {
        m_settingsDao.upsert(settings);
    }

    @Override
    public void setWebCertificate(String cert) {
        setWebCertificate(cert, null);
    }

    @Override
    public void setWebCertificate(String cert, String key) {
        validateCert(cert, key);
        updateCertificate(WEB_CERT, cert, key, getSelfSigningAuthority());
    }

    @Override
    public void setCommunicationsCertificate(String cert) {
        setCommunicationsCertificate(cert, null);
    }

    public void setCommunicationsCertificate(String cert, String key) {
        validateCert(cert, key);
        updateCertificate(COMM_CERT, cert, key, getSelfSigningAuthority());
    }

    void updateCertificate(String name, String cert, String key, String authority) {
        m_jdbc.update("delete from cert where name = ?", name);
        m_jdbc.update("insert into cert (name, data, private_key, authority) values (?, ?, ?, ?)", name, cert, key,
                authority);
    }

    void addAuthority(String name, String data, String key) {
        m_jdbc.update("delete from authority where name = ? ", name);
        m_jdbc.update("delete from cert where authority = ? ", name); // should be zero
        m_jdbc.update("insert into authority (name, data, private_key) values (?, ?, ?)", name, data, key);
    }

    String getSecurityData(String table, String column, String name) {
        String sql = format("select %s from %s where name = ?", column, table);
        return DaoUtils.requireOneOrZero(m_jdbc.queryForList(sql, String.class, name), sql);
    }

    @Override
    public String getWebCertificate() {
        return getSecurityData(CERT_TABLE, CERT_COLUMN, WEB_CERT);
    }

    @Override
    public String getWebPrivateKey() {
        return getSecurityData(CERT_TABLE, KEY_COLUMN, WEB_CERT);
    }

    @Override
    public String getCommunicationsCertificate() {
        return getSecurityData(CERT_TABLE, CERT_COLUMN, COMM_CERT);
    }

    @Override
    public String getCommunicationsPrivateKey() {
        return getSecurityData(CERT_TABLE, KEY_COLUMN, COMM_CERT);
    }

    @Override
    public List<String> getAuthorities() {
        List<String> authorities = m_jdbc.queryForList("select name from authority order by name", String.class);
        return authorities;
    }

    @Override
    public String getAuthorityCertificate(String authority) {
        return getSecurityData(AUTHORITY_TABLE, CERT_COLUMN, authority);
    }

    @Override
    public String getAuthorityKey(String authority) {
        return getSecurityData(AUTHORITY_TABLE, KEY_COLUMN, authority);
    }

    public String getSelfSigningAuthority() {
        String domain = Domain.getDomain().getName();
        return SELF_SIGN_AUTHORITY_PREFIX + domain;
    }

    @Override
    public void addTrustedAuthority(String authority, String cert) {
        validateAuthority(cert);
        addAuthority(authority, cert, null);
    }

    @Override
    public void deleteTrustedAuthority(String authority) {
        if (authority.equals(getSelfSigningAuthority())) {
            throw new UserException("Cannot delete self signing certificate authority");
        }

        m_jdbc.update("delete from authority where name = ?", authority);
        m_jdbc.update("delete from cert where authority = ?", authority);
    }

    void checkSetup() {
        String domain = Domain.getDomain().getName();
        String authority = getSelfSigningAuthority();
        String authorityCertificate = getAuthorityCertificate(authority);
        if (authorityCertificate == null) {
            CertificateAuthorityGenerator gen = new CertificateAuthorityGenerator(domain);
            addAuthority(authority, gen.getCertificateText(), gen.getPrivateKeyText());
        }
        if (!hasCertificate(COMM_CERT, authority)) {
            createCommunicationsCert(authority);
        }
        if (!hasCertificate(WEB_CERT, authority)) {
            String hostname = m_locationsManager.getPrimaryLocation().getHostname();
            createWebCert(authority, hostname);
        }
    }

    boolean hasCertificate(String id, String authority) {
        int check = m_jdbc.queryForInt("select count(*) from cert where name = ? and authority = ?", id, authority);
        return (check >= 1);
    }

    void createCommunicationsCert(String authority) {
        String authKey = getAuthorityKey(authority);
        String issuer = getIssuer(authority);
        String domain = Domain.getDomain().getName();
        CertificateGenerator gen = CertificateGenerator.sip(domain, issuer, authKey);
        updateCertificate(COMM_CERT, gen.getCertificateText(), gen.getPrivateKeyText(), authority);
    }

    void createWebCert(String authority, String host) {
        String authKey = getAuthorityKey(authority);
        String issuer = getIssuer(authority);
        String domain = Domain.getDomain().getName();
        CertificateGenerator gen = CertificateGenerator.web(domain, host, issuer, authKey);
        updateCertificate(WEB_CERT, gen.getCertificateText(), gen.getPrivateKeyText(), authority);
    }

    String getIssuer(String authority) {
        String authCertText = getSecurityData(AUTHORITY_TABLE, CERT_COLUMN, authority);
        X509Certificate authCert = CertificateUtils.readCertificate(authCertText);
        return authCert.getSubjectDN().getName();
    }

    void validateCert(String certTxt, String keyTxt) {
        X509Certificate cert = CertificateUtils.readCertificate(certTxt);
        try {
            cert.checkValidity();
        } catch (CertificateExpiredException e) {
            throw new UserException("Certificate has expired.");
        } catch (CertificateNotYetValidException e) {
            throw new UserException("Certificate valid date range is in the future, it is not yet valid.");
        }
        if (StringUtils.isNotBlank(keyTxt)) {
            CertificateUtils.readCertificateKey(keyTxt);
        }
        // to do, validate key w/cert and cert w/authorities
    }

    void validateAuthority(String cert) {
        validateCert(cert, null);
    }

    @Override
    public void setup(SetupManager manager) {
        // ensure it's run first to get primary location
        m_locationSetup.setup(manager);
        checkSetup();
    }

    public void setJdbc(JdbcTemplate jdbc) {
        m_jdbc = jdbc;
    }

    public void setLocationSetup(SetupListener locationSetup) {
        m_locationSetup = locationSetup;
    }

    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }

    public void setSettingsDao(BeanWithSettingsDao<CertificateSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }
}
