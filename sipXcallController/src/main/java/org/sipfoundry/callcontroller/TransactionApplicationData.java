/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.callcontroller;

import java.text.ParseException;
import java.util.EventObject;
import java.util.TimerTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionState;
import javax.sip.header.AllowHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ReferToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import gov.nist.javax.sip.TransactionExt;
import gov.nist.javax.sip.clientauthutils.UserCredentialHash;
import gov.nist.javax.sip.clientauthutils.UserCredentials;
import gov.nist.javax.sip.header.extensions.ReferredByHeader;

import org.apache.log4j.Logger;
import org.sipfoundry.sipxrest.RestServer;
import org.sipfoundry.sipxrest.SipHelper;

/**
 * Transaction context data. Register one of these per transaction to track data that is specific
 * to a transaction.
 * 
 */
class TransactionApplicationData {

    private static final Logger LOG = Logger.getLogger(TransactionApplicationData.class);

    private final Operator m_operator;

    private final SynchronousQueue<EventObject> m_queue = new SynchronousQueue<EventObject>();

    private final JainSipMessage m_message;

    private final SipHelper m_helper;

    private int m_counter;

    private UserCredentialHash m_userCredentials;

    private int m_timeout;

    private ClientTransaction m_clientTransaction;

    public TransactionApplicationData(Operator operator, JainSipMessage message, int timeout,
            ClientTransaction clientTransaction) {
        m_operator = operator;
        m_message = message;
        m_timeout = timeout;
        m_clientTransaction = clientTransaction;
        m_helper = SipListenerImpl.getInstance().getHelper();

        Request request = clientTransaction.getRequest();

        if (request.getMethod().equals(Request.INVITE)) {

            RestServer.timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    try {
                        if (m_clientTransaction.getState() != TransactionState.TERMINATED) {
                            Request cancelRequest = m_clientTransaction.createCancel();
                            SipProvider provider = ((TransactionExt) m_clientTransaction)
                                    .getSipProvider();
                            ClientTransaction ctx = provider
                                    .getNewClientTransaction(cancelRequest);
                            ctx.sendRequest();
                        }
                    } catch (Exception ex) {
                        LOG.debug("Exception in canceling request", ex);
                    }

                }

            }, m_timeout * 1000);
        }
    }

    /**
     * Blocks until transaction completes or timeout elapses
     * 
     * @return false if timeout, true if response received
     * @throws InterruptedException
     */
    public EventObject block() throws InterruptedException {
        return m_queue.poll(5000, TimeUnit.MILLISECONDS);
    }

    public void response(ResponseEvent responseEvent) {
        try {
            Response response = responseEvent.getResponse();
            ClientTransaction clientTransaction = responseEvent.getClientTransaction();
            Dialog dialog = responseEvent.getDialog();
            String method = m_helper.getCSeqMethod(response);
            LOG.debug("method = " + method);
            LOG.debug("Operator = " + m_operator);
            if (response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED) {
                if (m_counter == 1) {
                    m_helper.tearDownDialog(dialog);
                    return;
                }
                ClientTransaction ctx = m_helper.handleChallenge(response, clientTransaction);
                DialogContext dialogContext = (DialogContext) clientTransaction.getDialog()
                        .getApplicationData();
                dialogContext.addDialog(ctx.getDialog());
                ctx.getDialog().setApplicationData(dialogContext);
                m_counter++;
                if (ctx != null) {
                    ctx.setApplicationData(this);
                    this.m_clientTransaction = ctx;
                    if (ctx.getDialog().getState() == DialogState.CONFIRMED) {
                        ctx.getDialog().sendRequest(ctx);
                    } else {
                        ctx.sendRequest();
                    }
                }
            } else if (m_operator == Operator.SEND_NOTIFY) {
                // We ignore 1xx responses. 2xx and above are put into the queue.
                if (responseEvent.getResponse().getStatusCode() / 100 >= 2) {
                    m_queue.add(responseEvent);
                }
            } else if (method.equals(Request.INVITE)) {
                if (response.getStatusCode() / 100 == 2) {
                    if (m_operator == Operator.SEND_3PCC_REFER_CALL_SETUP) {
                        long seqno = SipHelper.getSequenceNumber(response);
                        Request ack = dialog.createAck(seqno);
                        dialog.sendAck(ack);
                        InviteMessage inviteMessage = (InviteMessage) m_message;

                        // Create a REFER pointing back at the caller.
                        Request referRequest = dialog.createRequest(Request.REFER);
                        referRequest.removeHeader(AllowHeader.NAME);
                        String referTarget = inviteMessage.getReferTarget();
                        ReferToHeader referTo = m_helper.createReferToHeader(referTarget);
                        referRequest.setHeader(referTo);
                        ReferredByHeader referredBy = m_helper
                                .createReferredByHeader(inviteMessage.getToAddrSpec());
                        referRequest.setHeader(referredBy);
                        ContactHeader contactHeader = m_helper.createContactHeader();
                        referRequest.setHeader(contactHeader);
                        ClientTransaction ctx = m_helper.getSipProvider()
                                .getNewClientTransaction(referRequest);

                        // And send it to the other side.
                        TransactionApplicationData tad = new TransactionApplicationData(
                                Operator.SEND_REFER, null, m_timeout, ctx);
                        tad.setUserCredentials(m_userCredentials);
                        ctx.setApplicationData(tad);
                        dialog.sendRequest(ctx);
                    }
                } else if (response.getStatusCode()/100 > 2){
                    DialogContext dialogContext = (DialogContext) dialog.getApplicationData();
                    dialogContext.removeMe(dialog);
                   
                }
            } else if (method.equals(Request.REFER)) {
                LOG.debug("Got REFER Response " + response.getStatusCode());
                // We set up a timer to terminate the INVITE dialog if we do not see a 200 OK in
                // the transfer.
                SipUtils.scheduleTerminate(dialog, 32);
            }
        } catch (InvalidArgumentException e) {
            LOG.error("Invalid argument", e);
            throw new SipxSipException(e);
        } catch (SipException e) {
            LOG.error("SipException ", e);
            throw new SipxSipException(e);
        } catch (ParseException e) {
            LOG.error("ParseException ", e);
            throw new SipxSipException(e);
        } catch (SipxSipException e) {
            LOG.error("SipxSipException ", e);
            throw e;
        }
    }

    public void timeout(TimeoutEvent timeoutEvent) {
        LOG.debug("Timeout processing");
        if (m_operator == Operator.SEND_NOTIFY) {
            m_queue.add(timeoutEvent);
        } else {
            ClientTransaction ctx = timeoutEvent.getClientTransaction();
            Dialog dialog = ctx.getDialog();
            m_helper.tearDownDialog(dialog);
        }
    }

    public UserCredentialHash getUserCredentials() {
        return m_userCredentials;
    }

    public void setUserCredentials(UserCredentialHash userCredentials) {
        m_userCredentials = userCredentials;
    }
}
