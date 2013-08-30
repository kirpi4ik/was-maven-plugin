package com.siemens.maven.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.maven.plugin.logging.Log;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.application.AppConstants;
import com.ibm.websphere.management.application.AppNotification;

public class ApplicationManagementListener implements NotificationListener
{
    private NotificationFilterSupport notificationFilter;
    private AdminClient adminClient;
    private Object handback;
    private Log log;
    private Map<String, AppNotification> eventTypes;
    private ObjectName appMgmtName;
    private boolean finished;
    private boolean exitOnFail;

    
    public ApplicationManagementListener(AdminClient adminClient, String eventType, Log log)
    {
        this(adminClient, log);
        eventTypes.put(eventType, null);
    }
    
    public ApplicationManagementListener(AdminClient adminClient, List<String> eventTypesList, Log log)
    {
        this(adminClient, log);
        for (String eventType : eventTypesList) {
            eventTypes.put(eventType, null);
        }
    }
    
    private ApplicationManagementListener(AdminClient adminClient, Log log)
    {
        this.adminClient = adminClient;
        this.log = log;
        this.eventTypes =  new HashMap<String, AppNotification>();
        this.exitOnFail = true;
        
        // create notification filter
        notificationFilter = new NotificationFilterSupport();
        notificationFilter.enableType(AppConstants.NotificationType);
        
        try {
            // get the AppManagement MBean objectName
            appMgmtName = MBeanHelper.queryMBeanName(adminClient, "WebSphere:type=AppManagement,*");
            
            // attach the listener
            adminClient.addNotificationListener(appMgmtName, this, notificationFilter, handback);
        } 
        catch (Exception e) {
            log.error("Unable to registrer notification listener", e);
        } 
    }
    
    @Override
    public void handleNotification(Notification notification, Object handback)
    {
        AppNotification appNotification = (AppNotification)notification.getUserData();
        if(eventTypes.containsKey(appNotification.taskName))
        {
            if(appNotification.taskStatus.equals(AppNotification.STATUS_COMPLETED))
            {
                log.info(appNotification.taskName + " " + AppNotification.STATUS_COMPLETED);
                setEventStatus(appNotification.taskName, appNotification);
            }
            else if(appNotification.taskStatus.equals(AppNotification.STATUS_FAILED))
            {
                log.error(appNotification.taskName + " " + AppNotification.STATUS_FAILED);
                log.error(appNotification.message);
                setEventStatus(appNotification.taskName, appNotification);
                if(exitOnFail){finished = true;}
            }
            else 
            {
                log.info(appNotification.message);
            }
        }
        
        if(isFinished()){
            removeListener();
        }
    }
    
    public void removeListener()
    {
        try {
            adminClient.removeNotificationListener(appMgmtName, this);
        } 
        catch (Exception e) {
            log.error("Unable to remove notification listener", e);
        }
        
        finished = true;
    }
    
    private void setEventStatus(String eventType, AppNotification notification)
    {
        eventTypes.put(eventType, notification);
    }
    
    public boolean isFinished() 
    {    
        boolean allEventsCompleted = true;
        for (AppNotification notification : eventTypes.values()) {
            if(notification==null){
                allEventsCompleted = false;
                break;
            }
        }
        
        return finished || allEventsCompleted;
    }

    public String getStatus(String eventType) {
        AppNotification notification = eventTypes.get(eventType);
        return notification==null ? null: notification.taskStatus;
    }

    public String getMessage(String eventType) {
        AppNotification notification = eventTypes.get(eventType);
        return notification==null ? null: notification.message;
    }
    
    public void waitForFinish()
    {
        while(!isFinished())
        {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }

    public boolean isExitOnFail() {
        return exitOnFail;
    }

    public void setExitOnFail(boolean exitOnFail) {
        this.exitOnFail = exitOnFail;
    }
}
