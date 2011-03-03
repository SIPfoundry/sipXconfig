/*
 *
 *
 * Copyright (C) 2011 eZuce, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UserValidationUtils {

    private static final Pattern VALID_USER_NAME = Pattern.compile("([-_.!~*'\\(\\)&amp;=+$,;?/"
            + "a-zA-Z0-9]|(&#37;[0-9a-fA-F]{2}))+");

    private UserValidationUtils() {
        // Utility class - do not instantiate
    }

    public static boolean isValidUserName(String userName) {
        if (userName == null) {
            return false;
        }
        Matcher m = VALID_USER_NAME.matcher(userName);
        return m.matches();
    }
}
