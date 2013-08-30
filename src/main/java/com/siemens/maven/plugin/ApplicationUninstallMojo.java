package com.siemens.maven.plugin;

import java.util.Hashtable;

import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.application.AppManagement;
import com.ibm.websphere.management.application.AppNotification;
import com.ibm.websphere.management.exception.AdminException;

/**
 * Uninstalls an aplication deployed on the server.
 * 
 * @goal uninstallApp
 */
public class ApplicationUninstallMojo extends ApplicationManagementMojo
{
    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException
    {
        AdminClient adminClient = getAdminClient();
        AppManagement appManagement = getAppManagementJMXProxy();
        
        try {
            boolean appExists = appManagement.checkIfAppExists(applicationName, new Hashtable(), null);
            if(appExists)
            {                
                // create notification listener
                ApplicationManagementListener listener = 
                    new ApplicationManagementListener(adminClient, AppNotification.UNINSTALL, getLog());
                
                appManagement.uninstallApplication(applicationName, new Hashtable(), null);
                
                // wait for uninstall to finish
                listener.waitForFinish();
                
                if(AppNotification.STATUS_FAILED.equals(listener.getStatus(AppNotification.UNINSTALL)))
                {
                    throw new MojoExecutionException(listener.getMessage(AppNotification.UNINSTALL));
                }
            }
            else 
            {
                getLog().warn("The specified application '" + applicationName + "' does not exist");
            }
        } 
        catch (AdminException e) {
            getLog().error(e);
            throw new MojoExecutionException(null,"Error occured during application uninstall", e.getMessage());
        }
    }
}
