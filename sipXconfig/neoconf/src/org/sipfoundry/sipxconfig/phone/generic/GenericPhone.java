package org.sipfoundry.sipxconfig.phone.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.phone.LineInfo;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.setting.Setting;

public class GenericPhone extends Phone {
    public static final String BEAN_ID = "genericPhone";

    public GenericPhone() {
    }

    @Override
    public void initialize() {
        GenericPhoneDefaults defaults = new GenericPhoneDefaults(getPhoneContext().getPhoneDefaults(), this);
        addDefaultBeanSettingHandler(defaults);
    }

    @Override
    public void initializeLine(Line line) {
    	GenericPhoneLineDefaults defaults = new GenericPhoneLineDefaults(getPhoneContext().getPhoneDefaults(), line);
        line.addDefaultBeanSettingHandler(defaults);
    }

    @Override
    protected ProfileContext<GenericPhone> createContext() {
        return new GenericPhoneProfileContext(this, getModel().getProfileTemplate());
    }

    @Override
    protected void setLineInfo(Line line, LineInfo externalLine) {
        line.setSettingValue(GenericPhoneConstant.DISPLAY_NAME, externalLine.getDisplayName());
        line.setSettingValue(GenericPhoneConstant.SIP_ID, externalLine.getUserId());
        line.setSettingValue(GenericPhoneConstant.AUTHENTICATION_PASSWORD, externalLine.getPassword());
        line.setSettingValue(GenericPhoneConstant.PROXY, externalLine.getRegistrationServer());
        line.setSettingValue(GenericPhoneConstant.VOICE_MAIL_ACCESS_CODE, externalLine.getVoiceMail());
        line.setSettingValue(GenericPhoneConstant.DOMAIN, externalLine.getRegistrationServer());
    }

    @Override
    protected LineInfo getLineInfo(Line line) {
        LineInfo lineInfo = new LineInfo();
        lineInfo.setUserId(line.getSettingValue(GenericPhoneConstant.SIP_ID));
        lineInfo.setDisplayName(line.getSettingValue(GenericPhoneConstant.DISPLAY_NAME));
        lineInfo.setPassword(line.getSettingValue(GenericPhoneConstant.AUTHENTICATION_PASSWORD));
        lineInfo.setRegistrationServer(line.getSettingValue(GenericPhoneConstant.PROXY));
        lineInfo.setVoiceMail(line.getSettingValue(GenericPhoneConstant.VOICE_MAIL_ACCESS_CODE));
        return lineInfo;
    }

    @Override
    public String getProfileFilename() {
        StringBuilder buffer = new StringBuilder(getSerialNumber().toUpperCase());
        buffer.append(".xml");
        return buffer.toString();
    }

    public int getMaxLineCount() {
        return getModel().getMaxLineCount();
    }

    public Collection<Setting> getProfileLines() {
        int lineCount = getModel().getMaxLineCount();
        List<Setting> linesSettings = new ArrayList<Setting>(getMaxLineCount());

        Collection<Line> lines = getLines();
        int i = 0;
        Iterator<Line> ilines = lines.iterator();
        for (; ilines.hasNext() && (i < lineCount); i++) {
            linesSettings.add(ilines.next().getSettings());
        }
        return linesSettings;
    }

    @Override
    public void restart() {
        sendCheckSyncToFirstLine();
    }
}
