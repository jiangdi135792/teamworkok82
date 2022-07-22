package com.work.plugin.rest;

import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.work.plugin.ao.GsryldStateService;
import com.work.plugin.ao.PowerEntity;
import com.work.plugin.ao.PowerService;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 2021/7/4.
 */
@Path("gsryldState")
//@Consumes({MediaType.APPLICATION_JSON})
//@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class GsryldStateResource {

    private final GlobalPermissionManager globalPermissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final GsryldStateService gsryldStateService;
    private final PowerService powerService;
    @GET
    public Response get(){
      //  if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, jiraAuthenticationContext.getLoggedInUser()))
        if (null==jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();

        return Response.ok(gsryldStateService.all()).build();
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
}