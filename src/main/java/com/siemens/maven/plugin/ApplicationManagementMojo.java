package com.siemens.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.application.AppManagement;
import com.ibm.websphere.management.application.AppManagementProxy;

public abstract class ApplicationManagementMojo extends BaseMojo
{
    /**
     * The application name
     * @parameter
     * @required
     */
    protected String applicationName;
    
    public AppManagement getAppManagementJMXProxy() throws MojoExecutionException 
    {
        AppManagement appManagement = null;
        AdminClient adminClient = getAdminClient();
        try {
            appManagement = AppManagementProxy.getJMXProxyForClient(adminClient);
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException(null,"Unable to get the app management mbean proxy", e.getMessage());
        }
        
        return appManagement;
    }
}
