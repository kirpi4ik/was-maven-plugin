package com.siemens.maven.plugin;

import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.management.exception.ConnectorException;

public abstract class BaseMojo extends AbstractMojo 
{
    /**
     * The host where the SOAP connector is located. Default host value is "localhost"
     * @parameter default-value="localhost"
     */
    protected String connectorHost;
    
    /**
     * The port used by the SOAP connector. Default port for the SOAP connector is 8880.
     * @parameter default-value="8880"
     */
    protected String connectorPort;
    
    /**
     * Administration Security enabled. Default set to TRUE.
     * @parameter default-value="true"
     */
    protected String securityEnabled;
    
    /**
     * The username of the administrative account.
     * @parameter
     * @required
     */
    protected String username;
    
    /**
     * The password for the administrative account.
     * @parameter
     * @required
     */
    protected String password;
    
    /**
     * The location of the WebSphere installation
     * @parameter default-value="${env.WAS_HOME}"
     */
    protected String wasLocation;
    
    /**
     * The WebSphere profile name
     * @parameter default-value="AppSrv01"
     */
    protected String wasProfileName;
    
    public AdminClient getAdminClient() throws MojoExecutionException 
    {
        AdminClient adminClient = null;
        
        Properties clientConfig = new Properties();
        clientConfig.setProperty(AdminClient.CONNECTOR_TYPE, AdminClient.CONNECTOR_TYPE_SOAP);
        clientConfig.setProperty(AdminClient.CONNECTOR_HOST, connectorHost);
        clientConfig.setProperty(AdminClient.CONNECTOR_PORT, connectorPort);
        clientConfig.setProperty(AdminClient.CONNECTOR_SECURITY_ENABLED, securityEnabled);
        clientConfig.setProperty(AdminClient.USERNAME, username);
        clientConfig.setProperty(AdminClient.PASSWORD, password);
        clientConfig.setProperty("javax.net.ssl.trustStore", getWasProfileDir() + "/etc/DummyClientTrustFile.jks");
        clientConfig.setProperty("javax.net.ssl.keyStore", getWasProfileDir() + "/etc/DummyClientKeyFile.jks");
        clientConfig.setProperty("javax.net.ssl.trustStorePassword", "WebAS");
        clientConfig.setProperty("javax.net.ssl.keyStorePassword", "WebAS");
        
        getLog().info("truststore "  + getWasProfileDir());
        
        try {
            adminClient = AdminClientFactory.createAdminClient(clientConfig);
        } catch (ConnectorException e) {
            getLog().error(e);
            throw new MojoExecutionException(null,"Failed to create the admin client", e.getMessage());
        }
        
        return adminClient;
    }
    
    public String getWasProfileDir() {
        return wasLocation + "/profiles/" + wasProfileName;
    }
}
