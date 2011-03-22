/**
 *
 *
 * Copyright (c) 2010 / 2011 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.security;

import javax.servlet.http.HttpServletRequest;

import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;
import org.apache.commons.lang.StringUtils;

public class SipxAuthenticationProcessingFilter extends AuthenticationProcessingFilter {

    public static final String ORIGINAL_REFERER = "originalReferer";

    protected String determineTargetUrl(HttpServletRequest request) {
        if (isAlwaysUseDefaultTargetUrl()) {
            return getDefaultTargetUrl();
        }

        String targetUrl = obtainFullRequestUrl(request);

        // fix for XX-9064 - use http Referer as target url (redirected to)
        if (targetUrl == null) {
            String referer = (String) request.getSession().getAttribute(ORIGINAL_REFERER);

            // if no original http referer saved on session then use current one
            if (referer == null) {
                referer = request.getHeader("Referer");
            }

            // redirect to referer only if mailbox page encoder
            if (StringUtils.contains(referer, "mailbox")) {
                targetUrl = referer;
            }
            request.getSession().removeAttribute(ORIGINAL_REFERER);
        }

        if (targetUrl == null) {
            targetUrl = getDefaultTargetUrl();
        }

        return targetUrl;
    }
}
