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
package org.sipfoundry.sipxconfig.rest;

import static org.restlet.data.MediaType.APPLICATION_JSON;
import static org.restlet.data.MediaType.TEXT_XML;

import java.io.Serializable;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sipfoundry.sipxconfig.common.User;

import com.thoughtworks.xstream.XStream;

public class LoginDetailsResource extends UserResource {

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(TEXT_XML));
        getVariants().add(new Variant(APPLICATION_JSON));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        User user = getUser();
        return new LoginDetails(variant.getMediaType(), new Representable(user.getUserName(), user.getImId()));
    }

    @SuppressWarnings("serial")
    static class Representable implements Serializable {
        @SuppressWarnings("unused")
        private String m_userName;
        @SuppressWarnings("unused")
        private String m_imId;

        public Representable(String userName, String imId) {
            m_userName = userName;
            m_imId = imId;
        }
    }

    static class LoginDetails extends XStreamRepresentation<Representable> {

        public LoginDetails(MediaType mediaType, Representable object) {
            super(mediaType, object);
        }

        public LoginDetails(Representation representation) {
            super(representation);
        }

        @Override
        protected void configureXStream(XStream xstream) {
            xstream.alias("login-details", Representable.class);
        }
    }

}
