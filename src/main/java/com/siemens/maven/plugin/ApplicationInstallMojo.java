package com.siemens.maven.plugin;

import java.io.File;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;

import javax.management.ObjectName;

import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.ObjectNameHelper;
import com.ibm.websphere.management.application.AppConstants;
import com.ibm.websphere.management.application.AppManagement;
import com.ibm.websphere.management.application.AppNotification;
import com.ibm.websphere.management.exception.AdminException;
import com.ibm.websphere.management.exception.ConnectorException;

/**
 * Installs an aplication on the server.
 * 
 * @goal installApp
 */
public class ApplicationInstallMojo extends ApplicationManagementMojo
{
    /**
     * The location of the ear file to be deployed
     * @parameter
     * @required
     */
    protected String earFileLocation;
    
    /**
     * The class loading mode. Can be PARENT_FIRST or PARENT_LAST
     * @parameter default-value="PARENT_LAST"
     */
    protected String classLoadingMode;

    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException
    {
        // check if the ear file location is valid
        //testFileLocation(earFileLocation);
        
        // get the admin client and the AppManagement mbean proxy
        AdminClient adminClient = getAdminClient();
        AppManagement appManagement = getAppManagementJMXProxy();
        
        // get the deployment preferences
        Hashtable config = getDeploymentConfig(adminClient);
        
        // create notification listener
        ApplicationManagementListener listener = 
            new ApplicationManagementListener(adminClient, AppNotification.INSTALL, getLog());
        
        try 
        {
            appManagement.installApplication(earFileLocation, applicationName, config, null);
            
            // wait for install to finish
            listener.waitForFinish();
            
            if(AppNotification.STATUS_FAILED.equals(listener.getStatus(AppNotification.INSTALL)))
            {
                throw new MojoExecutionException(listener.getMessage(AppNotification.INSTALL));
            }
        } 
        catch (AdminException e) {
            listener.removeListener();
            getLog().error(e);
            throw new MojoExecutionException(null,"Error occured during application install", e.getMessage());
        }
    }
    
    protected void testFileLocation(String fileLocation) throws MojoExecutionException
    {
        File earFile = new File(fileLocation);
        if(!earFile.exists())
        {
            throw new MojoExecutionException("The specified location is not valid : " + fileLocation);
        }        
    }
    
    @SuppressWarnings("unchecked")
    protected Hashtable getDeploymentConfig(AdminClient adminClient) throws MojoExecutionException
    {
        Hashtable config = new Hashtable();
        config.put(AppConstants.APPDEPL_LOCALE, Locale.getDefault());

        // add the binding preferences
        Properties defaultBnd = new Properties();
        defaultBnd.put (AppConstants.APPDEPL_DFLTBNDG_VHOST, "default_host");
        config.put (AppConstants.APPDEPL_DFLTBNDG, defaultBnd);
        
        // create module to server relations table
        Hashtable module2server = new Hashtable();
        module2server.put ("*", getTargetServer(adminClient));
        config.put (AppConstants.APPDEPL_MODULE_TO_SERVER, module2server);
        
        // set the classloader preferences
        if("PARENT_LAST".equals(classLoadingMode)) {
            config.put(AppConstants.APPDEPL_CLASSLOADINGMODE, AppConstants.APPDEPL_CLASSLOADINGMODE_PARENTLAST);
        }
        else if("PARENT_FIRST".equals(classLoadingMode)) {
            config.put(AppConstants.APPDEPL_CLASSLOADINGMODE, AppConstants.APPDEPL_CLASSLOADINGMODE_PARENTFIRST);
        }
        
        return config;
    }

    protected String getTargetServer(AdminClient adminClient) throws MojoExecutionException
    {
        StringBuffer target = new StringBuffer();
        ObjectName appManagementName = MBeanHelper.queryMBeanName(adminClient, "WebSphere:type=AppManagement,*");
        
        // add the domain name
        try {
            target.append(adminClient.getDomainName() + ":");
        } catch (ConnectorException e) {
            target.append("WebSphere:"); //the default domain name
        }
        
        // add the cell name
        target.append("cell=" + ObjectNameHelper.getCellName(appManagementName) + ",");
        
        // add the node name
        target.append("node=" + ObjectNameHelper.getNodeName(appManagementName) + ",");
        
        // add the server name
        target.append("server=" + ObjectNameHelper.getProcessName(appManagementName));
        
        getLog().info("Target : " + target.toString());
                
        return target.toString();
    }
}
