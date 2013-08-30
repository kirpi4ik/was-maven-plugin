package com.siemens.maven.plugin;

import java.io.File;
import java.util.Hashtable;

import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.application.AppConstants;
import com.ibm.websphere.management.application.AppManagement;
import com.ibm.websphere.management.application.AppNotification;
import com.ibm.websphere.management.exception.AdminException;

/**
 * Updates the specified module of the aplication.
 * 
 * @goal updateModule
 */
public class ApplicationModuleUpdateMojo extends ApplicationInstallMojo
{
    /**
     * The name of the module to be updated
     * @parameter expression="${moduleName}"
     * @required
     */
    private String moduleName;
    
    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException
    {
        // check if the ear file location is valid
        testFileLocation(getModulePath());
        
        getLog().info("Updating " + moduleName + " from location " + getModuleAbsoluthePath());
        
        // get the admin client and the AppManagement mbean proxy
        AdminClient adminClient = getAdminClient();
        AppManagement appManagement = getAppManagementJMXProxy();
        
        // get the deployment preferences
        Hashtable config = getDeploymentConfig(adminClient);
        config.put(AppConstants.APPUPDATE_CONTENTTYPE, AppConstants.APPUPDATE_CONTENT_MODULEFILE);
        
        // create notification listener
        ApplicationManagementListener listener = 
            new ApplicationManagementListener(adminClient, AppNotification.UPDATE, getLog());
        
        try {
            appManagement.updateApplication(applicationName, moduleName, getModuleAbsoluthePath(), AppConstants.APPUPDATE_UPDATE, config, null);
            
            // wait for update to finish
            listener.waitForFinish();
            
            // check uninstall status
            if(AppNotification.STATUS_FAILED.equals(listener.getStatus(AppNotification.UPDATE)))
            {
                throw new MojoExecutionException(listener.getMessage(AppNotification.UPDATE));
            }
        } 
        catch (AdminException e) {
            listener.removeListener();
            getLog().error(e);
            throw new MojoExecutionException(null,"Error occured during application update", e.getMessage());
        }
    }
    
    private String getModulePath()
    {
        return "target/" + moduleName;
    }
    
    private String getModuleAbsoluthePath()
    {
        File moduleFile = new File(getModulePath());
        String absPath = null;
        absPath = moduleFile.getAbsolutePath();
        
        return absPath;
    }
    
}
