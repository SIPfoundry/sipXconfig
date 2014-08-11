/**
 * Copyright (c) 2014 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.commons.hz;

public class HzVmEvent extends HzMediaEvent {
    private static final long serialVersionUID = 1L;

    public enum VmType implements HzMediaEvent.Type {
        START_LEAVE_VM,
        END_LEAVE_VM
    }
    public HzVmEvent(String userIdFrom, String userIdTo, String description, Type type) {
        super(userIdFrom, userIdTo, description, type);
    }
}
