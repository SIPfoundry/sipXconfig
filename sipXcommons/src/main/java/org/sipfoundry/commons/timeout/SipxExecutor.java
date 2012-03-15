/**
 *
 *
 * Copyright (c) 2010 eZuce, Inc. All rights reserved.
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
package org.sipfoundry.commons.timeout;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SipxExecutor {
    public static Result execute(Timeout timeout, int seconds) throws TimeoutException, ExecutionException, InterruptedException{
        ExecutorService executor = Executors.newCachedThreadPool();
        SipxCallable task = new SipxCallable(timeout);
        Future<Object> future = executor.submit(task);
        try {
           return (Result)future.get(seconds, TimeUnit.SECONDS);
        } finally {
           future.cancel(true);
        }
    }
}
