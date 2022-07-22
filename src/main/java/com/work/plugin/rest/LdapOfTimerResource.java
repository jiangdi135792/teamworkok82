package com.work.plugin.rest;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.work.plugin.ao.*;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by work on 2022/2/2.
 */
@Path("/info")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class LdapOfTimerResource {
    private final CrowdDirectoryService crowdDirectoryService;
    private final LdapSyncTimeService ldapSyncTimeService;
    private final PowerService powerService;
     ScheduleWay scheduleWay=new ScheduleWay();
    @GET
    @Path("kindNum")
    public Response showInfoOfDireKind(){
        long time1=new Date().getTime();
        List<Directory> allDirectories=crowdDirectoryService.findAllDirectories();
        HashMap hashMap=new HashMap();
        for (int i=0; i < allDirectories.size(); i++) {
            Directory directory=allDirectories.get(i);
            String implementationClass=directory.getImplementationClass();
            if (directory.getId() != 1&& (implementationClass.equals("com.atlassian.crowd.directory.OpenLDAP") ||implementationClass.equals("com.atlassian.crowd.directory.MicrosoftActiveDirectory"))) {
                HashMap Map=new HashMap();
                Date updatedDate=directory.getUpdatedDate();
                Long id=directory.getId();
                String ip=directory.getValue("ldap.basedn");
                //String ip=primaryIp.substring(7, primaryIp.lastIndexOf(":"));
                String name=directory.getName();
                int time=ldapSyncTimeService.getTime(id);
               if (time==-1){
                 time=Integer.parseInt(directory.getValue("directory.cache.synchronise.interval"))/60;
                }
                String directoryType=directory.getImplementationClass();
                directoryType=directoryType.substring(directoryType.lastIndexOf(".") + 1);
                long delayUpdateTime=Long.valueOf(directory.getValue("directory.cache.synchronise.interval")) * 1000 + directory.getUpdatedDate().getTime() - time1;
                double ceil=Math.ceil((double)delayUpdateTime / 60000);
                String strTime=String.valueOf(ceil);
                String substring=strTime.substring(0, strTime.indexOf("."));
                boolean exit=scheduleWay.confirmExitTaskByDirectoryId(id);
                Map.put("id", id);
                Map.put("time", time);
                Map.put("substring", substring);
                Map.put("ip", ip);
                Map.put("exit", exit);
                Map.put("name", name);
                Map.put("directoryType", directoryType);
                hashMap.put(i, Map);
            }
        }
        return Response.ok(hashMap).build();
    }
    @GET
    @Path("changeTimer/{directoryId}/{time}")
    public Response changeTimer(@PathParam("time")int time,@PathParam("directoryId")long directoryId){
        long time1=new Date().getTime();
        Directory directoryById=crowdDirectoryService.findDirectoryById(directoryId);
        Long aLong=Long.valueOf(directoryById.getValue("directory.cache.synchronise.interval"));
        long delayUpdateTime=aLong * 1000 + directoryById.getUpdatedDate().getTime() - time1;
        LdapSyncTime directory=ldapSyncTimeService.getDirectory(directoryId);
        directory.setTime(time);
        ldapSyncTimeService.update(directory);
        //long delayUpdateTimeEdite=Math.abs(delayUpdateTime) % (aLong.intValue() / 60);
        double ceil=Math.ceil((double)delayUpdateTime / 60000);
        String strTime=String.valueOf(ceil);
        String substring=strTime.substring(0, strTime.indexOf("."));
        Map<String,String> map =new HashMap<>();
        scheduleWay.ChangeTaskTime(directoryId,delayUpdateTime, (int) (ceil+time));
        //System.out.println("设置"+directoryId+"------"+ceil+time+"---------------分钟同步"); Test
        map.put("delayTime",substring);
        return Response.ok(map).build();
    }
    @POST
    @Path("pause/{directoryId}")
    public Response pauseSync(@PathParam("directoryId")long directoryId){
        scheduleWay.PauseSync(directoryId);
        return  Response.ok().build();
    }
    @POST
    @Path("sync/{directoryId}")
    public Response syncBySelf(@PathParam("directoryId")long directoryId){
        scheduleWay.SyncBySelf(directoryId);
        return  Response.ok().build();
    }
    @POST
    @Path("start/{directoryId}")
    public Response startSync(@PathParam("directoryId")long directoryId){
        long time=new Date().getTime();
        Map<String,String> map =new HashMap<>();
        Directory directoryById=crowdDirectoryService.findDirectoryById(directoryId);
        Long aLong=Long.valueOf(directoryById.getValue("directory.cache.synchronise.interval"));
        long delayUpdateTime=aLong * 1000 + directoryById.getUpdatedDate().getTime() - time;
        int intervalTime=ldapSyncTimeService.getTime(directoryId);
        double ceil=Math.ceil((double)delayUpdateTime / 60000);
        String strTime=String.valueOf(ceil);
        String substring=strTime.substring(0, strTime.indexOf("."));
        scheduleWay.StartSync(directoryId,delayUpdateTime, (long) (ceil+intervalTime));
        map.put("delayTime",substring);
        return  Response.ok(map).build();
    }
    @POST
    @Path("getStatus")
    public  Response getStatus(){
        List<Directory> allDirectories=crowdDirectoryService.findAllDirectories();
        HashMap hashMap=new HashMap();
        Map<Integer,String> map1 =new HashMap<>();
        Map<Integer,String> map2 =new HashMap<>();
        int i=0;
        int j=0;
        for (Directory directory:allDirectories){
            String implementationClass=directory.getImplementationClass();
            Long directoryId=directory.getId();
            if (directoryId != 1&& (implementationClass.equals("com.atlassian.crowd.directory.OpenLDAP") ||implementationClass.equals("com.atlassian.crowd.directory.MicrosoftActiveDirectory"))) {
                boolean active=directory.isActive();
                if (!active) {
                    i++;
                    map1.put(i, String.valueOf(directoryId));
                }
                else {
                    j++;
                    map2.put(j, String.valueOf(directoryId));
                }
            }
            hashMap.put(1,map1);
            hashMap.put(2,map2);
        }
        return  Response.ok(hashMap).build();
    }
    @GET
    @Path("controlPower")
    public Response controlPower(@Context HttpServletRequest request){
        Map<String,Boolean> map =getPower(request);
        return Response.ok(map).build();
    }
    private   Map<String,Boolean> getPower(HttpServletRequest request){
        Map<Integer,Boolean> powerInfo = (Map<Integer, Boolean>) request.getAttribute("powerInfo");
        Map<String,Boolean> map = new HashMap<>();
        Set<Integer> integers = powerInfo.keySet();
        for (Integer integer:integers){
            PowerEntity power = powerService.getById(integer);
            map.put(power.getDescription(),powerInfo.get(integer));
        }
        return map;
    }
    @GET
    @Path("getDirectoryId")
    public Response getDirectoryId(){
        List<Directory> allDirectories = crowdDirectoryService.findAllDirectories();
        List<Long> collect = allDirectories.stream().map(s -> s.getId()).collect(Collectors.toList());
        return Response.ok(collect).build();
    }
}
