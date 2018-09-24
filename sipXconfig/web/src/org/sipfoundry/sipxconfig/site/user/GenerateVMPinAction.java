package org.sipfoundry.sipxconfig.site.user;

import java.util.Collection;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.hivemind.Messages;
import org.apache.tapestry.IComponent;
import org.apache.tapestry.IRequestCycle;
import org.sipfoundry.sipxconfig.common.AbstractUser;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.mail.EmailNotifier;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.site.setting.BulkVMAction;

public class GenerateVMPinAction extends BulkVMAction {
    private CoreContext m_coreContext;
    private EmailNotifier m_emailNotifier;

    public GenerateVMPinAction(String label, CoreContext coreContext
            , EmailNotifier emailNotifier) {
        super(null, label);
        m_coreContext = coreContext;
        m_emailNotifier = emailNotifier;
    }

    public void actionTriggered(IComponent component_, IRequestCycle cycle_) {
        Collection<Integer> ids = getIds();
        for (Integer id : ids) {
            User user = m_coreContext.loadUser(id);
            String randomPin = RandomStringUtils.randomNumeric(AbstractUser.VOICEMAIL_PIN_LEN);
            user.setVoicemailPin(randomPin);
            user.setForcePinChange(false);
            m_coreContext.saveUser(user);
            
            // Send email.
            m_emailNotifier.sendMail(user.getUserName(), "vmpin.generate", createMailArgument(randomPin));
        }
    }

    public String getSuccessMsg(Messages messages) {
        return messages.format("msg.success.generatePinAction", Integer.toString(getIds().size()));
    }
    
    @Override
    public String squeezeOption(Object option_, int index_) {
        return getClass().getName() + ".generate";
    }
    
    public Object[] createMailArgument(String pin) {
        Object[] args = new Object[1];
        args[0] = pin;
        return args;
    }
}
