/*
 *
 *
 * Copyright (C) 2009 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxconfig.rest;

import static org.restlet.data.MediaType.APPLICATION_JSON;
import static org.restlet.data.MediaType.TEXT_XML;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sipfoundry.sipxconfig.common.BeanWithId;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.phonebook.AddressBookEntry;

import com.thoughtworks.xstream.XStream;

public class ContactInformationResource extends UserResource {

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(TEXT_XML));
        getVariants().add(new Variant(APPLICATION_JSON));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        User user = getUser();
        AddressBookEntry addressBook = user.getAddressBookEntry();

        boolean imEnabled = (Boolean) user.getSettingTypedValue("im/im-account");
        Representable representable = new Representable(user.getFirstName(), user.getLastName(), addressBook,
                imEnabled);

        return new AddressBookRepresentation(variant.getMediaType(), representable);
    }

    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        AddressBookRepresentation representation = new AddressBookRepresentation(entity);
        Representable representable = representation.getObject();

        AddressBookEntry reprAddressBook = new AddressBookEntry();
        reprAddressBook.update(representable);

        User user = getUser();
        AddressBookEntry addressBook = user.getAddressBookEntry();

        if (addressBook == null) {
            user.setAddressBookEntry(reprAddressBook);
        } else {
            //the IM id needs to be uneditable via rest.(XX-8022)
            reprAddressBook.setImId(addressBook.getImId());
            addressBook.update(reprAddressBook);
            user.setAddressBookEntry(addressBook);
        }
        user.setFirstName(representable.getFirstName());
        user.setLastName(representable.getLastName());

        getCoreContext().saveUser(user);
    }

    static class Representable extends AddressBookEntry implements Serializable {
        private final String m_firstName;
        private final String m_lastName;

        public Representable(String firstName, String lastName, AddressBookEntry addressBook, boolean imEnabled) {
            m_firstName = firstName;
            m_lastName = lastName;
            if (addressBook != null) {
                this.update(addressBook);
                if (!imEnabled) {
                    setImId(StringUtils.EMPTY);
                }

            }
        }

        public String getFirstName() {
            return m_firstName;
        }

        public String getLastName() {
            return m_lastName;
        }
    }

    static class AddressBookRepresentation extends XStreamRepresentation<Representable> {
        public AddressBookRepresentation(MediaType mediaType, Representable object) {
            super(mediaType, object);
        }

        public AddressBookRepresentation(Representation representation) {
            super(representation);
        }

        @Override
        protected void configureXStream(XStream xstream) {
            xstream.omitField(BeanWithId.class, "m_id");
            xstream.alias("contact-information", Representable.class);
            xstream.aliasField("avatar", Representable.class, "m_avatar");
        }
    }
}
