package com.siemens.maven.plugin;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.application.AppConstants;
import com.ibm.websphere.management.application.AppManagement;
import com.ibm.websphere.management.application.AppNotification;
import com.ibm.websphere.management.exception.AdminException;

/**
 * Updates an aplication on the server.
 * 
 * @goal updateApp
 */
public class ApplicationUpdateMojo extends ApplicationInstallMojo
{
    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException
    {
        // check if the ear file location is valid
        testFileLocation(earFileLocation);
        
        // get the admin client and the AppManagement mbean proxy
        AdminClient adminClient = getAdminClient();
        AppManagement appManagement = getAppManagementJMXProxy();
        
        // get the deployment preferences
        Hashtable config = getDeploymentConfig(adminClient);
        config.put(AppConstants.APPUPDATE_CONTENTTYPE, AppConstants.APPUPDATE_CONTENT_APP);

        // create notification listener
        List<String> eventTypes = new ArrayList<String>();
        eventTypes.add(AppNotification.UNINSTALL);
        eventTypes.add(AppNotification.INSTALL);
        ApplicationManagementListener listener = 
            new ApplicationManagementListener(adminClient, eventTypes, getLog());
        
        try 
        {
            appManagement.updateApplication(applicationName, null, earFileLocation, AppConstants.APPUPDATE_UPDATE, config, null);
            
            // wait for update to finish
            listener.waitForFinish();
            
            // check uninstall status
            if(AppNotification.STATUS_FAILED.equals(listener.getStatus(AppNotification.UNINSTALL)))
            {
                throw new MojoExecutionException(listener.getMessage(AppNotification.UNINSTALL));
            }
            
            // check install status
            if(AppNotification.STATUS_FAILED.equals(listener.getStatus(AppNotification.INSTALL)))
            {
                throw new MojoExecutionException(listener.getMessage(AppNotification.INSTALL));
            }
        } 
        catch (AdminException e) {
            listener.removeListener();
            getLog().error(e);
            throw new MojoExecutionException(null,"Error occured during application update", e.getMessage());
        } 
    }
}
