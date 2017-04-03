/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.freeswitch.api;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sipfoundry.sipxconfig.conference.ActiveConference;
import org.sipfoundry.sipxconfig.conference.ActiveConferenceMember;
import org.sipfoundry.sipxconfig.conference.Conference;
import org.sipfoundry.sipxconfig.conference.NoSuchConferenceException;
import org.sipfoundry.sipxconfig.conference.NoSuchMemberException;
import org.sipfoundry.sipxconfig.setting.type.SipUriSetting;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses the string returned from a FreeSWITCH XML-RPC call.
 */
public class FreeswitchApiResultParserImpl implements FreeswitchApiResultParser {
    private static final Log LOG = LogFactory.getLog(FreeswitchApiResultParser.class);

    private static final String EMPTY_STRING = "No active conferences.";

    private static final Pattern CONFERENCE_NOT_FOUND_PATTERN = Pattern.compile("Conference (.+) not found\\n");

    private static final Pattern CONFERENCE_NAME_PATTERN = Pattern.compile("Conference ([" + SipUriSetting.USER_NAME
            + "]+) \\((\\d+) members? rate: \\d+( locked)? flags: .*\\)");
    
    private static final int CONFERENCE_NAME_PATTERN_GROUP_INDEX = 1;
    private static final int CONFERENCE_MEMBERS_PATTERN_GROUP_INDEX = 2;
    private static final int CONFERENCE_LOCKED_PATTERN_GROUP_INDEX = 3;

    private static final Pattern ACTIVE_CALLS_PATTERN = Pattern.compile("(\\d+)\\stotal\\.");
    
    private static final int ACTIVE_CALLS_COUNT_PATTERN_GROUP_INDEX = 1;
    
    // the misspelling is intentional - typo in freeswitch API
    private static final Pattern INVALID_MEMBER_PATTERN = Pattern.compile("Non-Exist[ae]nt ID [\\d]+\\n");

    private static final String COMMAND_LIST_DELIM = ">,<";

    /**
     * Verifies that a member action was completed successfully and that the target member exists.
     *
     * @throws NoSuchMemberException if the member does not exist
     */
    public boolean verifyMemberAction(String resultString, ActiveConferenceMember member) {
        if (StringUtils.isBlank(resultString)) {
            throw new NoSuchMemberException(member);
        }
        if (INVALID_MEMBER_PATTERN.matcher(resultString).matches()) {
            throw new NoSuchMemberException(member);
        }
        return true;
    }

    /**
     * Verifies that a conference action was completed successfully and that the target conference
     * exists.
     *
     * @throws NoSuchConferenceException if the conference does not exist
     */
    public boolean verifyConferenceAction(String resultString, Conference conference) {
        if (StringUtils.isBlank(resultString)) {
            throw new NoSuchConferenceException(conference);
        }
        if (CONFERENCE_NOT_FOUND_PATTERN.matcher(resultString).matches()) {
            new NoSuchConferenceException(conference).printStackTrace();
            throw new NoSuchConferenceException(conference);
        }
        return true;
    }

    /**
     * Parses a result string and determines the total number of active conferences.
     *
     * @param resultString the string returned from the FreeSWITCH API call
     * @return the number of active conferences reported by FreeSWITCH.
     */
    public int getActiveConferenceCount(String resultString) {
        if (StringUtils.isBlank(resultString)) {
            return 0;
        }
        if (resultString.equals(EMPTY_STRING)) {
            return 0;
        }
        int count = 0;
        Matcher matcher = CONFERENCE_NAME_PATTERN.matcher(resultString);
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    /**
     * Parses a result string and determines if the conference is locked.
     *
     * @param resultString the string returned from the FreeSWITCH API call
     * @return whether or not the conference is locked.
     */
    public boolean isConferenceLocked(String resultString) {
        Matcher matcher = CONFERENCE_NAME_PATTERN.matcher(resultString);
        boolean isLocked = false;

        if (matcher.find()) {
            isLocked = (matcher.group(CONFERENCE_LOCKED_PATTERN_GROUP_INDEX) != null);
        }

        return isLocked;
    }

    /**
     * Parses a result string and creates a list of ActiveConference objects representing each
     * conference.
     *
     * @param resultString the string returned from the FreeSWITCH API call
     * @return a List of ActiveConference objects
     */
    public List<ActiveConference> getActiveConferences(String resultString) {
        if (StringUtils.isBlank(resultString)) {
            return Collections.emptyList();
        }
        if (resultString.equals(EMPTY_STRING)) {
            return Collections.emptyList();
        }
        List<ActiveConference> activeConferences = new ArrayList<ActiveConference>();

        Matcher matcher = CONFERENCE_NAME_PATTERN.matcher(resultString);
        while (matcher.find()) {
            String conferenceName = matcher.group(CONFERENCE_NAME_PATTERN_GROUP_INDEX);
            int members = Integer.parseInt(matcher.group(CONFERENCE_MEMBERS_PATTERN_GROUP_INDEX));
            boolean locked = (matcher.group(CONFERENCE_LOCKED_PATTERN_GROUP_INDEX) != null);
            activeConferences.add(new ActiveConference(conferenceName, members, locked));
        }

        return activeConferences;
    }
    
    @Override
    public List<FreeswitchSofiaStatus> getSofiaStatuses(String resultString) {
        if (StringUtils.isBlank(resultString)) {
            return Collections.emptyList();
        }
        if (resultString.equals(EMPTY_STRING)) {
            return Collections.emptyList();
        }
        
        List<FreeswitchSofiaStatus> statuses = new ArrayList<FreeswitchSofiaStatus>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource xmlSource = new InputSource(new StringReader(resultString));
            Document doc = dBuilder.parse(xmlSource);
            
            doc.getDocumentElement().normalize();
            
            getSofiaStatuses(doc.getElementsByTagName("profile"), statuses);
            getSofiaStatuses(doc.getElementsByTagName("gateway"), statuses);
            
        } catch(ParserConfigurationException|SAXException|IOException ex)
        {
            LOG.error("Failed to parse xml: " + resultString, ex);
        }
        
        return statuses;
    }
    
    @Override
    public int getCallCount(String resultString)
    {
        if (StringUtils.isBlank(resultString)) {
            return 0;
        }
        if (resultString.equals(EMPTY_STRING)) {
            return 0;
        }
        
        int count = 0;
        Matcher matcher = ACTIVE_CALLS_PATTERN.matcher(resultString);
        if (matcher.find()) {
            try {
                count = Integer.parseInt(matcher.group(ACTIVE_CALLS_COUNT_PATTERN_GROUP_INDEX));    
            } catch(NumberFormatException ex) {
                LOG.warn("Failed to parse active call: " + resultString, ex);
            }
        }
        
        return count;
    }

    public List<ActiveConferenceMember> getConferenceMembers(String resultString, Conference conference) {
        List<ActiveConferenceMember> members = new ArrayList<ActiveConferenceMember>();

        String conferenceName = conference.getName();
        Scanner scanner = new Scanner(resultString);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            try {
                ActiveConferenceMember member = parseConferenceMember(line, conferenceName);
                members.add(member);
            } catch (NoSuchElementException e) {
                LOG.error("Skipping conference line:" + line);
            }
        }

        return members;
    }

    private ActiveConferenceMember parseConferenceMember(String line, String conferenceName) {
        ActiveConferenceMember member = new ActiveConferenceMember();

        Scanner scan = new Scanner(line);
        scan.useDelimiter(COMMAND_LIST_DELIM);

        member.setId(scan.nextInt());

        String sipAddress = scan.next().split("/")[2];

        member.setUuid(scan.next());

        String callerIdName = scan.next();
        if (callerIdName.equals(conferenceName)) {
            callerIdName = "";
        }

        String number = scan.next();

        String permissions = scan.next();
        member.setCanHear(permissions.contains("hear"));
        member.setCanSpeak(permissions.contains("speak"));

        member.setNumber(number);

        member.setName(callerIdName + " (" + sipAddress + ")");

        member.setVolumeIn(scan.nextInt());
        member.setVolumeOut(scan.nextInt());
        member.setEnergyLevel(scan.nextInt());
        return member;
    }
    
    private static void getSofiaStatuses(NodeList nodeList, List<FreeswitchSofiaStatus> statuses) {
        for (int index = 0; index < nodeList.getLength(); index++) {
            Node node = nodeList.item(index);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                FreeswitchSofiaStatus sofiaStatus = createFromXml(element);
                if(sofiaStatus != null) {
                    statuses.add(sofiaStatus);
                }
            }
        }
    }
    
    private static FreeswitchSofiaStatus createFromXml(Element element) {
        FreeswitchSofiaStatus status = new FreeswitchSofiaStatus();
        status.setName(getTextFromElement(element, "name"));
        status.setData(getTextFromElement(element, "data"));
        status.setState(getTextFromElement(element, "state"));
        try {
            status.setType(FreeswitchSofiaStatus.Type.fromString(
                    getTextFromElement(element, "type")
                ));
        } catch(IllegalArgumentException ex) {
            LOG.warn("Unable to parse sofia type/status for " + status.getName(), ex);
            return null;
        }
                
        return status;
    }
    
    private static String getTextFromElement(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if(nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        
        return "";
    }

}
