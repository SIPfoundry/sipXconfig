/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.phonebook;

import junit.framework.TestCase;

import com.google.gdata.data.Extension;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.City;
import com.google.gdata.data.extensions.Country;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.FullName;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.OrgDepartment;
import com.google.gdata.data.extensions.OrgName;
import com.google.gdata.data.extensions.OrgTitle;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.PostCode;
import com.google.gdata.data.extensions.Region;
import com.google.gdata.data.extensions.Street;
import com.google.gdata.data.extensions.StructuredPostalAddress;
import com.google.gdata.data.extensions.Where;

public class PhonebookGoogleEntryHelperTest extends TestCase {

    public void testExtractName() {
        ContactEntry ce = new ContactEntry();
        Name name = new Name();
        FamilyName familyName = new FamilyName();
        familyName.setValue("Flinstone");
        name.setFamilyName(familyName);
        GivenName givenName = new GivenName();
        givenName.setValue("Fred");
        name.setGivenName(givenName);
        ce.setName(name);

        PhonebookEntry phoneBookEntry = new PhonebookGoogleEntryHelper(ce, "me@gmail.com").getPhonebookEntry();
        assertEquals("Flinstone", phoneBookEntry.getLastName());
        assertEquals("Fred", phoneBookEntry.getFirstName());

        ContactEntry ce1 = new ContactEntry();
        Name name1 = new Name();
        FamilyName familyName1 = new FamilyName();
        familyName1.setValue("Duffy");
        name1.setFamilyName(familyName1);
        ce1.setName(name1);

        PhonebookEntry phoneBookEntry1 = new PhonebookGoogleEntryHelper(ce1, "me@gmail.com").getPhonebookEntry();
        assertEquals("Duffy", phoneBookEntry1.getLastName());
        assertEquals("", phoneBookEntry1.getFirstName());

        ContactEntry ce2 = new ContactEntry();
        Name name2 = new Name();
        GivenName givenName2 = new GivenName();
        givenName2.setValue("Bunny");
        name2.setGivenName(givenName2);
        ce2.setName(name2);

        PhonebookEntry phoneBookEntry2 = new PhonebookGoogleEntryHelper(ce2, "me@gmail.com").getPhonebookEntry();
        assertEquals("Bunny", phoneBookEntry2.getFirstName());
        assertEquals("", phoneBookEntry2.getLastName());

        ContactEntry ce3 = new ContactEntry();
        Name name3 = new Name();
        FullName fullName = new FullName();
        fullName.setValue("Yosemite Sam");
        name3.setFullName(fullName);
        ce3.setName(name3);

        PhonebookEntry phoneBookEntry3 = new PhonebookGoogleEntryHelper(ce3, "me@gmail.com").getPhonebookEntry();
        assertEquals("Yosemite", phoneBookEntry3.getFirstName());
        assertEquals("Sam", phoneBookEntry3.getLastName());

        fullName.setValue("Yosemite Sam II");
        name3.setFullName(fullName);
        ce3.setName(name3);

        phoneBookEntry3 = new PhonebookGoogleEntryHelper(ce3, "me@gmail.com").getPhonebookEntry();
        assertEquals("Yosemite", phoneBookEntry3.getFirstName());
        assertEquals("Sam II", phoneBookEntry3.getLastName());

        fullName.setValue("Yosemite");
        name3.setFullName(fullName);
        ce3.setName(name3);

        phoneBookEntry3 = new PhonebookGoogleEntryHelper(ce3, "me@gmail.com").getPhonebookEntry();
        assertEquals("Yosemite", phoneBookEntry3.getFirstName());
        assertEquals(null, phoneBookEntry3.getLastName());

    }

    public void testExtractIMs() {
        ContactEntry ce = new ContactEntry();
        Im im1 = new Im();
        im1.setAddress("yahooAddress");
        ce.addImAddress(im1);

        PhonebookEntry phoneBookEntry = new PhonebookGoogleEntryHelper(ce, "me@gmail.com").getPhonebookEntry();
        assertEquals("yahooAddress", phoneBookEntry.getAddressBookEntry().getImId());
        assertEquals(null, phoneBookEntry.getAddressBookEntry().getAlternateImId());

        Im im2 = new Im();
        im2.setAddress("googleAddress");
        ce.addImAddress(im2);

        phoneBookEntry = new PhonebookGoogleEntryHelper(ce, "me@gmail.com").getPhonebookEntry();
        assertEquals("yahooAddress", phoneBookEntry.getAddressBookEntry().getImId());
        assertEquals("googleAddress", phoneBookEntry.getAddressBookEntry().getAlternateImId());
    }

    public void testExtractLocation() {
        ContactEntry ce = new ContactEntry();
        Extension ext = new Where("","", "testLocation");
        ce.addExtension(ext);
        PhonebookEntry phoneBookEntry = new PhonebookGoogleEntryHelper(ce, "me@gmail.com").getPhonebookEntry();
        String location = phoneBookEntry.getAddressBookEntry().getLocation();
        assertEquals("testLocation", location);
    }

    public void testExtractAddress() {
        ContactEntry ce = new ContactEntry();
        StructuredPostalAddress address = new StructuredPostalAddress();
        City city = new City("City");
        address.setCity(city);
        Country country = new Country();
        country.setValue("Country");
        address.setCountry(country);
        Region region = new Region("Region");
        address.setRegion(region);
        address.setRel("home");

        ce.addStructuredPostalAddress(address);

        PhonebookEntry phoneBookEntry = new PhonebookGoogleEntryHelper(ce, "me@gmail.com").getPhonebookEntry();
        Address homeAddr = phoneBookEntry.getAddressBookEntry().getHomeAddress();
        assertEquals("City", homeAddr.getCity());
        assertEquals("Country", homeAddr.getCountry());
        assertEquals("Region", homeAddr.getState());

        Street street = new Street("Street");
        address.setStreet(street);
        PostCode postCode = new PostCode("PostCode");
        address.setPostcode(postCode);
        address.setRel("work");

        ContactEntry ce1 = new ContactEntry();
        ce1.addStructuredPostalAddress(address);
        PhonebookEntry phoneBookEntry1 = new PhonebookGoogleEntryHelper(ce1, "me@gmail.com").getPhonebookEntry();
        Address workAddr = phoneBookEntry1.getAddressBookEntry().getOfficeAddress();
        assertEquals("Street", workAddr.getStreet());
        assertEquals("PostCode", workAddr.getZip());
    }

    public void testExtractPhones() {
        ContactEntry ce = new ContactEntry();
        ce.addPhoneNumber(createPhoneNumber("work", "1234"));
        ce.addPhoneNumber(createPhoneNumber("mobile", "1111"));
        ce.addPhoneNumber(createPhoneNumber("home", "2222"));
        ce.addPhoneNumber(createPhoneNumber("fax", "3333"));

        PhonebookEntry phoneBookEntry = new PhonebookGoogleEntryHelper(ce, "me@gmail.com").getPhonebookEntry();
        assertEquals("1234", phoneBookEntry.getNumber());
        assertEquals("1111", phoneBookEntry.getAddressBookEntry().getCellPhoneNumber());
        assertEquals("2222", phoneBookEntry.getAddressBookEntry().getHomePhoneNumber());
        assertEquals("3333", phoneBookEntry.getAddressBookEntry().getFaxNumber());

        //test set Mobile or Home phone as phoneBookEntry number
        ce.addPhoneNumber(createPhoneNumber("work", null));
        phoneBookEntry = new PhonebookGoogleEntryHelper(ce, "me@gmail.com").getPhonebookEntry();
        assertEquals("1111", phoneBookEntry.getNumber());

        ce.addPhoneNumber(createPhoneNumber("mobile", null));
        phoneBookEntry = new PhonebookGoogleEntryHelper(ce, "me@gmail.com").getPhonebookEntry();
        assertEquals("2222", phoneBookEntry.getNumber());
    }

    private PhoneNumber createPhoneNumber(String rel, String value) {
        PhoneNumber phone = new PhoneNumber();
        phone.setRel(rel);
        phone.setPhoneNumber(value);
        return phone;
    }

    public void testExtractOrgs() {
        ContactEntry ce = new ContactEntry();

        Organization org = new Organization();
        OrgTitle title = new OrgTitle("Title");
        org.setOrgTitle(title);

        OrgName name = new OrgName("Name");
        org.setOrgName(name);

        OrgDepartment dept = new OrgDepartment("Dept");
        org.setOrgDepartment(dept);

        ce.addOrganization(org);

        PhonebookEntry phoneBookEntry = new PhonebookGoogleEntryHelper(ce, "me@gmail.com").getPhonebookEntry();
        AddressBookEntry abe = phoneBookEntry.getAddressBookEntry();
        assertEquals("Title", abe.getJobTitle());
        assertEquals("Name", abe.getCompanyName());
        assertEquals("Dept", abe.getJobDept());
    }

    public void testExtractEmails() {
        ContactEntry ce = new ContactEntry();
        Email email1 = new Email();
        email1.setAddress("kovalchuk@thrashers.com");
        email1.setPrimary(true);

        Email email2 = new Email();
        email2.setAddress("ilya.k@gmail.com");
        email2.setPrimary(false);

        ce.addEmailAddress(email1);
        ce.addEmailAddress(email2);

        PhonebookEntry phoneBookEntry = new PhonebookGoogleEntryHelper(ce, "me@gmail.com").getPhonebookEntry();
        AddressBookEntry abe = phoneBookEntry.getAddressBookEntry();
        assertEquals("kovalchuk@thrashers.com", abe.getEmailAddress());
        assertEquals("ilya.k@gmail.com", abe.getAlternateEmailAddress());
    }
}
