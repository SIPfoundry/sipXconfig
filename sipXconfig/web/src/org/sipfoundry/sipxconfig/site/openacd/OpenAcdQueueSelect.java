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
package org.sipfoundry.sipxconfig.site.openacd;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.IActionListener;
import org.apache.tapestry.IComponent;
import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.components.TapestryContext;
import org.sipfoundry.sipxconfig.components.TapestryUtils;
import org.sipfoundry.sipxconfig.components.selection.AdaptedSelectionModel;
import org.sipfoundry.sipxconfig.components.selection.OptGroupPropertySelectionRenderer;
import org.sipfoundry.sipxconfig.components.selection.OptionAdapter;
import org.sipfoundry.sipxconfig.openacd.OpenAcdContext;
import org.sipfoundry.sipxconfig.openacd.OpenAcdQueue;

public abstract class OpenAcdQueueSelect extends BaseComponent {

    @InjectObject("spring:openAcdContext")
    public abstract OpenAcdContext getOpenAcdContext();

    @InjectObject(value = "spring:tapestry")
    public abstract TapestryContext getTapestry();

    @Bean
    public abstract OptGroupPropertySelectionRenderer getRender();

    @Parameter(required = true)
    public abstract void setSelectedQueue(OpenAcdQueue selectedQueue);

    public abstract OpenAcdQueue getSelectedQueue();

    public abstract void setSelectedAction(IActionListener selectedAction);

    public abstract IActionListener getSelectedAction();

    @Override
    protected void renderComponent(IMarkupWriter writer, IRequestCycle cycle) {
        if (getSelectedQueue() != null) {
            setSelectedAction(new AddExistingQueueAction(getSelectedQueue()));
        } else {
            setSelectedAction(null);
        }

        super.renderComponent(writer, cycle);
        if (TapestryUtils.isRewinding(cycle, this) && TapestryUtils.isValid(this)) {
            triggerAction(cycle);
        }
    }

    private void triggerAction(IRequestCycle cycle) {
        IActionListener a = getSelectedAction();
        if (!(a instanceof OpenAcdQueueAction)) {
            return;
        }

        OpenAcdQueueAction action = (OpenAcdQueueAction) a;
        OpenAcdQueue queue = action.getQueue();
        if (queue != null) {
            action.setId(queue.getId());
        }
        action.actionTriggered(this, cycle);
    }

    public IPropertySelectionModel decorateModel(IPropertySelectionModel model) {
        return getTapestry().addExtraOption(model, getMessages(), "label.select");
    }

    public IPropertySelectionModel getModel() {
        Collection<OptionAdapter> actions = new ArrayList<OptionAdapter>();
        Collection<OpenAcdQueue> queues = getOpenAcdContext().getQueues();
        if (!queues.isEmpty()) {
            for (OpenAcdQueue queue : queues) {
                AddExistingQueueAction action = new AddExistingQueueAction(queue);
                actions.add(action);
            }
        }

        AdaptedSelectionModel model = new AdaptedSelectionModel();
        model.setCollection(actions);
        return model;
    }

    private class AddExistingQueueAction extends OpenAcdQueueAction {

        public AddExistingQueueAction(OpenAcdQueue queue) {
            super(queue);
        }

        public void actionTriggered(IComponent component, final IRequestCycle cycle) {
            setSelectedQueue(getQueue());
        }

        @Override
        public Object getValue(Object option, int index) {
            return this;
        }

        @Override
        public String squeezeOption(Object option, int index) {
            return getQueue().getId().toString();
        }

        @Override
        public boolean equals(Object obj) {
            return ObjectUtils.equals(this.getQueue(), ((AddExistingQueueAction) obj).getQueue());
        }

        @Override
        public int hashCode() {
            return this.getQueue().hashCode();
        }
    }
}
