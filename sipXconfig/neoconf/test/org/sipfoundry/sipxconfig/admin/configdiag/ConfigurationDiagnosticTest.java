package org.sipfoundry.sipxconfig.admin.configdiag;

import java.io.InputStream;

import junit.framework.TestCase;

import org.sipfoundry.sipxconfig.admin.configdiag.ConfigurationDiagnosticResult.Status;

public class ConfigurationDiagnosticTest extends TestCase {

    private ConfigurationDiagnostic m_out;
    
    public void setUp() {
        m_out = new ConfigurationDiagnostic();
    }
    
    public void testLoadFromXml() throws Exception {
        ConfigurationDiagnostic out = new ConfigurationDiagnostic();
        loadXmlForTest(out, "20dhcp.test.xml");
        assertEquals("dhcp", out.getName());
        assertEquals("DHCP", out.getLabel());
        assertEquals("Verifies if DHCP server is configured properly.",
                out.getDescription());
        assertEquals("/usr/bin/sipx-dhcp-test", out.getCommand().getCommand());
        assertTrue(out.getCommand().getArgs().contains("--non-interactive"));
        assertEquals(Status.Success, out.getResultParser().parseResult(0).getStatus());
        assertEquals(Status.Warning, out.getResultParser().parseResult(-1).getStatus());
        assertEquals(Status.Error, out.getResultParser().parseResult(-2).getStatus());
        assertEquals(Status.Unknown, out.getResultParser().parseResult(-99).getStatus());
    }
    
    public void testExecuteExpectSuccess() throws Exception {
        loadXmlForTest(m_out, "10simple.test.xml");
        
        // override command from descriptor file to use test stub
        m_out.setCommand(new ExternalCommand() {
            public int execute() {
                return 0;
            }
        });
        
       m_out.execute();
       assertEquals(ConfigurationDiagnosticResult.Status.Success, m_out.getResult().getStatus());
    }
    
    public void testExecuteExpectError() throws Exception {
        loadXmlForTest(m_out, "10simple.test.xml");
        
        // override command from descriptor file to use test stub
        m_out.setCommand(new ExternalCommand() {
            public int execute() {
                return -1;
            }
        });
        
       m_out.execute();
       assertEquals(ConfigurationDiagnosticResult.Status.Error, m_out.getResult().getStatus());
    }
    
    private void loadXmlForTest(ConfigurationDiagnostic diagnostic, String descriptorFileName) 
        throws Exception {
        String descriptorPath = "org/sipfoundry/sipxconfig/admin/configdiag/"
            + descriptorFileName;
        InputStream testDefInputStream = 
            getClass().getClassLoader().getResourceAsStream(descriptorPath);
        diagnostic.loadFromXml(testDefInputStream);
    }
}
