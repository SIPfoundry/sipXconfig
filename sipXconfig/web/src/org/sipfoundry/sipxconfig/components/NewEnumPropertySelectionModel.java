/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 * $
 */
package org.sipfoundry.sipxconfig.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tapestry.form.IPropertySelectionModel;

/**
 * Property selection model for Java 1.5 enums
 */
public class NewEnumPropertySelectionModel<E extends Enum<E>> implements IPropertySelectionModel {

    private List<E> m_options;

    public void setOptions(E[] options) {
        m_options = new ArrayList<E>(Arrays.asList(options));
    }

    public void setEnumType(Class<E> elementType) {
        E[] options = elementType.getEnumConstants();
        m_options = new ArrayList<E>(Arrays.asList(options));
    }

    public int getOptionCount() {
        return m_options.size();
    }

    public Object getOption(int index) {
        return m_options.get(index);
    }

    public String getLabel(int index) {
        return m_options.get(index).name();
    }

    public String getValue(int index) {
        return new Integer(index).toString();
    }

    public void removeOption(E option) {
        m_options.remove(option);
    }

    public Object translateValue(String value) {
        int i = Integer.parseInt(value);
        // This is a robustness handling. If ever run into the indexOutOfBound
        // situation, reset the index to 0.
        if (i >= m_options.size()) {
            i = 0;
        }
        return m_options.get(i);
    }

    public boolean isDisabled(int index) {
        return false;
    }
}
