package com.siemens.maven.plugin;

import javax.management.ObjectName;

import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.ObjectNameHelper;

/**
 * @goal info
 */
public class InfoMojo extends BaseMojo 
{    
    @Override
    public void execute() throws MojoExecutionException
    {
        AdminClient adminClient = getAdminClient();
        
        // get the AppManagement MBean
        ObjectName appManagementName = MBeanHelper.queryMBeanName(adminClient, "WebSphere:type=AppManagement,*");
        
        // display the cell name
        String cellName = ObjectNameHelper.getCellName(appManagementName);
        getLog().info("Cell : " + cellName);
        
        // display the node name
        String nodeName = ObjectNameHelper.getNodeName(appManagementName);
        getLog().info("Node : " + nodeName);
        
        // display the server name
        String serverName = ObjectNameHelper.getProcessName(appManagementName);
        getLog().info("Server : " + serverName);
    }
}
