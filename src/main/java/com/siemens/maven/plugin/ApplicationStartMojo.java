package com.siemens.maven.plugin;

import java.util.Hashtable;

import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.websphere.management.application.AppManagement;
import com.ibm.websphere.management.exception.AdminException;

/**
 * Starts an aplication deployed on the server.
 * 
 * @goal startApp
 */
public class ApplicationStartMojo extends ApplicationManagementMojo
{    
    @Override
    @SuppressWarnings("unchecked")
    public void execute()throws MojoExecutionException
    {
        AppManagement appManagement = getAppManagementJMXProxy();
        
        try {
            boolean appExists = appManagement.checkIfAppExists(applicationName, new Hashtable(), null);
            if(appExists)
            {
                String status = appManagement.startApplication(applicationName, new Hashtable(), null);
                if(status!=null && status.length()>0) {
                    getLog().info("Application '" + applicationName + "' started on : " + status);
                }else {
                    getLog().info("Application '" + applicationName + "' already started");
                }
            }
            else 
            {
                getLog().warn("The specified application '" + applicationName + "' does not exist");
            }
        } 
        catch (AdminException e) {
            getLog().error(e);
            throw new MojoExecutionException(null,"Error occured during application start", e.getMessage());
        }
    }   
}
