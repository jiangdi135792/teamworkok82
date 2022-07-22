package com.work.plugin.ao;

import com.atlassian.crowd.embedded.api.Directory;

import java.util.*;

/**
 * Created by work on 2022/1/31.
 */
public class ScheduleWay {
    public static HashMap<Long, SchedulerTask> scheduleMap=new HashMap<Long, SchedulerTask>();
    static HashMap<Long, SchedulerTask> initMap=new HashMap<Long, SchedulerTask>();
    public void startAllTask(HashMap taskMap, List<Directory> alldirectories, LdapSyncTimeService ldapSyncTimeService) {
        int directoryNum=alldirectories.size() - 1;
        if (directoryNum >= 1) {
            for (Directory directory : alldirectories) {
                   if (directory.isActive()){
                    String implementationClass=directory.getImplementationClass();
                     if (directory.getId() != 1&& (implementationClass.equals("com.atlassian.crowd.directory.OpenLDAP") ||implementationClass.equals("com.atlassian.crowd.directory.MicrosoftActiveDirectory"))) {
                    Long directoryid=directory.getId();
                    Integer timeDb=Integer.valueOf(directory.getValue("directory.cache.synchronise.interval")) / 60;
                    int timeOfTable=ldapSyncTimeService.getTime(directoryid);
                    if (timeOfTable == -1) {
                        timeOfTable=timeDb;
                        ldapSyncTimeService.create(directoryid, timeDb);
                    }
                    SchedulerTask schedulerTask=new SchedulerTask(directoryid);
                    Timer timer=new Timer();
                    timer.schedule(schedulerTask, timeDb*60 * 1000, timeOfTable * 60000);
                    taskMap.put(directoryid, schedulerTask);
                    System.out.println(directoryid + "----------------" + taskMap.get(directoryid));
                } }
            }
        }
        SchedulerTask schedulerTask=new SchedulerTask(-1);
        Timer timer=new Timer();
        long time=System.currentTimeMillis();
        timer.schedule(schedulerTask, 10 * 1000, 30000);
        initMap.put(time, schedulerTask);
        scheduleMap=taskMap;
    }

    public void ShutDownAllTask() {
        if (scheduleMap.size() != 0) {
            Set<Long> longs=scheduleMap.keySet();
            for (long lo : longs) {
                SchedulerTask schedulerTask=scheduleMap.get(lo);
                schedulerTask.cancel();
                System.out.println("close all task ");
            }
        }
    }

    public void shutDownInitTask() {
        if (initMap.size() != 0) {
            Set<Long> longs=initMap.keySet();
            for (Long lo : longs) {
                SchedulerTask schedulerTask=initMap.get(lo);
                schedulerTask.cancel();
                System.out.println("close initmap ");
            }
        }
    }

    public void ChangeTaskTime(long directoryId,long delayUpdateTime, int time) {
        SchedulerTask schedulerTask=scheduleMap.get(directoryId);
        if (schedulerTask!=null){
        schedulerTask.cancel();
        }
        SchedulerTask schedulerTask1=new SchedulerTask(directoryId);
        Timer timer=new Timer();
        timer.schedule(schedulerTask1, delayUpdateTime,time * 60000);
        scheduleMap.put(directoryId, schedulerTask1);
    }

    public  void PauseSync(long directoryId){
        SchedulerTask schedulerTask=scheduleMap.get(directoryId);
        schedulerTask.cancel();
        scheduleMap.put(directoryId,null);
    }
    public  void SyncBySelf(long directoryId){
        SchedulerTask schedulerTask12=new SchedulerTask(directoryId);
        Timer timer=new Timer();
        timer.schedule(schedulerTask12,0);
    }
    public void StartSync(long directoryId,long delayUpdateTime,long intervalTime ){
        SchedulerTask schedulerTask1=new SchedulerTask(directoryId);
        Timer timer=new Timer();
        timer.schedule(schedulerTask1, delayUpdateTime, intervalTime * 60000);
        scheduleMap.put(directoryId, schedulerTask1);
    }
    public boolean confirmExitTaskByDirectoryId(long directoryId){
        return scheduleMap.get(directoryId)!=null?true:false;
    }
}