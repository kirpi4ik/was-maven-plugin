package com.siemens.maven.plugin;

import java.util.Set;

import javax.management.ObjectName;

import org.apache.maven.plugin.MojoExecutionException;

import com.ibm.websphere.management.AdminClient;

public class MBeanHelper 
{
    @SuppressWarnings("unchecked")
    public static ObjectName queryMBeanName(AdminClient adminClient, String query) 
        throws MojoExecutionException
    {
        ObjectName mbeanName;
        
        try 
        {
            Set objectNames = adminClient.queryNames(new ObjectName(query), null);
            mbeanName = (ObjectName) objectNames.iterator().next();
        } 
        catch (Exception e) {
            throw new MojoExecutionException("Unable to query MBean", e);
        } 
        
        return mbeanName;
    }
}
