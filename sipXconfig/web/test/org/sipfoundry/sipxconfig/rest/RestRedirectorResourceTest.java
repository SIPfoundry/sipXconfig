package org.sipfoundry.sipxconfig.rest;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.classextension.EasyMock.createMock;

import java.io.StringWriter;

import junit.framework.TestCase;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.address.AddressManager;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.ivr.Ivr;
import org.sipfoundry.sipxconfig.rest.RestRedirectorResource.HttpInvoker;
import org.sipfoundry.sipxconfig.restserver.RestServer;
import org.sipfoundry.sipxconfig.security.TestAuthenticationToken;

public class RestRedirectorResourceTest extends TestCase {
    private User m_user;
    private CoreContext m_coreContext;
    private AddressManager m_addressManager;

    @Override
    protected void setUp() throws Exception {
        m_user = new User();
        m_user.setUniqueId();
        m_user.setUserName("200");
        m_user.setFirstName("John");
        m_user.setLastName("Doe");

        Authentication token = new TestAuthenticationToken(m_user, false, false).authenticateToken();
        SecurityContextHolder.getContext().setAuthentication(token);

        m_coreContext = createMock(CoreContext.class);
        m_coreContext.loadUser(m_user.getId());
        expectLastCall().andReturn(m_user);
        replay(m_coreContext);

        m_addressManager = createMock(AddressManager.class);
        m_addressManager.getSingleAddress(RestServer.HTTP_API);
        expectLastCall().andReturn(new Address(RestServer.HTTP_API, "host.example.com", 6667));
        m_addressManager.getSingleAddress(Ivr.REST_API);
        expectLastCall().andReturn(new Address(Ivr.REST_API, "host.example.com", 8085));
        replay(m_addressManager);
    }

    public void testRepresentIvr() throws Exception {
        represent("https://host.example.com:8085", "https://host.example.com:8085/mailbox/200/messages", RestRedirectorResource.MAILBOX, "<messages></messages>");
    }

    public void testRepresentCdr() throws Exception {
        represent("http://host.example.com:6667", "https://host.example.com:6666/cdr/200", RestRedirectorResource.CDR, "<cdr></cdr>");
    }

    public void testPost() throws Exception {
        post("http://host.example.com:6667", "https://host.example.com:6666/callcontroller/200/201", RestRedirectorResource.CALLCONTROLLER);
    }

    public void testPut() throws Exception {
        put("https://host.example.com:8085", "https://host.example.com:8085/mailbox/200/message/0000001/heard", RestRedirectorResource.MAILBOX);
    }

    public void testDelete() throws Exception {
        delete("https://host.example.com:8085", "https://host.example.com:8085/mailbox/200/message/0000001/heard", RestRedirectorResource.MAILBOX);
    }

    private void represent(String address, String resIdentifier, String resourceType, String result) throws Exception{
        HttpInvoker invoker = createMock(HttpInvoker.class);
        String uri = StringUtils.substringAfter(resIdentifier, resourceType);
        invoker.invokeGet(address + resourceType + uri);
        expectLastCall().andReturn(result).once();
        replay(invoker);

        RestRedirectorResource resource = createResource(invoker, resIdentifier);

        Representation representation = resource.represent(new Variant(MediaType.TEXT_XML));
        StringWriter writer = new StringWriter();
        representation.write(writer);
        String generated = writer.toString();
        assertEquals(result, generated);
    }

    private void post(String address, String resIdentifier, String resourceType) throws Exception{
        HttpInvoker invoker = createMock(HttpInvoker.class);
        String uri = StringUtils.substringAfter(resIdentifier, resourceType);
        invoker.invokePost(address + resourceType + uri);
        expectLastCall().once();
        replay(invoker);

        RestRedirectorResource resource = createResource(invoker, resIdentifier);

        resource.acceptRepresentation(null);
    }

    private void put(String address, String resIdentifier, String resourceType) throws Exception{
        HttpInvoker invoker = createMock(HttpInvoker.class);
        String uri = StringUtils.substringAfter(resIdentifier, resourceType);
        invoker.invokePut(address + resourceType + uri);
        expectLastCall().once();
        replay(invoker);

        RestRedirectorResource resource = createResource(invoker, resIdentifier);

        resource.storeRepresentation(null);
    }

    private void delete(String address, String resIdentifier, String resourceType) throws Exception{
        HttpInvoker invoker = createMock(HttpInvoker.class);
        String uri = StringUtils.substringAfter(resIdentifier, resourceType);
        invoker.invokeDelete(address + resourceType + uri);
        expectLastCall().once();
        replay(invoker);

        RestRedirectorResource resource = createResource(invoker, resIdentifier);

        resource.removeRepresentations();
    }

    private RestRedirectorResource createResource(HttpInvoker invoker, String resIdentifier) {
        RestRedirectorResource resource = new RestRedirectorResource();
        resource.setCoreContext(m_coreContext);
        resource.setAddressManager(m_addressManager);
        resource.setHttpInvoker(invoker);

        ChallengeResponse challengeResponse = new ChallengeResponse(null, "200", new char[0]);
        Request request = new Request();
        Reference resourceRef = new Reference();
        resourceRef.setIdentifier(resIdentifier);
        request.setResourceRef(resourceRef);
        request.setChallengeResponse(challengeResponse);
        resource.init(null, request, null);

        return resource;
    }
}
