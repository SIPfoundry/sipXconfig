/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.rest;

import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Router;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.sipfoundry.sipxconfig.common.UserException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import com.noelios.restlet.ext.servlet.ServletConverter;

/**
 * Style II of RESTlet integration where you keep Spring as ApplicationContext and pull in
 * restlets.
 *
 * The magic relies on restletSpringBeanRouter bean that finds references to all beans of type
 * Resource: URIs have to be configured as aliases for beans. Aliases starting with '/' are
 * treated as URIs. Internal spring finder instantiates resources lazily - they need to have
 * prototype scope.
 */
@SuppressWarnings("serial")
public class RestSpringAdapterServlet extends HttpServlet {
    private static final Log LOG = LogFactory.getLog(RestSpringAdapterServlet.class);
    private ServletConverter m_converter;
    private MessageSource m_messageSource;

    @Override
    public void init() throws ServletException {
        super.init();
        final ServletContext servletContext = getServletContext();
        ApplicationContext app = getRequiredWebApplicationContext(servletContext);
        m_converter = new ServletConverter(servletContext);
        Router router = (Router) app.getBean("restletSpringBeanRouter", Router.class);
        m_converter.setTarget(router);
        m_messageSource = (MessageSource) app.getBean("globalMessageSource");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            m_converter.service(req, res);
        } catch (UserException e) {
            String msg = l8n(e.getMessage(), e.getRawParams(), req.getLocale());
            onError(req, res, Status.CLIENT_ERROR_BAD_REQUEST, e, msg);
        } catch (Exception re) {
            onError(req, res, Status.SERVER_ERROR_INTERNAL, re, "Internal error: " + re.getMessage());
        }
    }

    String l8n(String msg, Object[] rawParams, Locale locale) {
        if (!msg.isEmpty() && msg.charAt(0) == '&') {
            String key = msg.substring(1);
            try {
                // localizing the raw parameters may not be expected but it's incredibly
                // useful.
                //  Example:
                //   throw new UserException("&error.fieldInvalid", "&label.name", obj.getName());
                if (rawParams != null) {
                    for (int i = 0; i < rawParams.length; i++) {
                        if (rawParams[i] instanceof String) {
                            // NOTE: recursive
                            // HACK: writing back to rawParams array when probably
                            // should create new array, but it's unlikely rawParams
                            // will be used for anything else and localization needs
                            // to be lightweight.
                            rawParams[i] = l8n((String) rawParams[i], null, locale);
                        }
                    }
                }
                return m_messageSource.getMessage(key, rawParams, locale);
            } catch (NoSuchMessageException badLocalization) {
                LOG.error("Missing localization key " + key);
                return key;
            }
        }
        return msg;
    }

    private void onError(HttpServletRequest req, HttpServletResponse res, Status status, Throwable e, String msg)
        throws IOException {
        String where = String.format("%s %s", req.getMethod(), req.getRequestURI());
        LOG.error(where, e);
        res.setStatus(status.getCode());
        res.setContentType(MediaType.APPLICATION_JSON.getName());
        String json = String.format("{\"error\":\"%s\",\"type\":\"%s\"} ", msg, e.getClass().getName());
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        res.getWriter().append(json);
    }
}
