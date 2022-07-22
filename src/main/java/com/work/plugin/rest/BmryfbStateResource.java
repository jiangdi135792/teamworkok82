package com.work.plugin.rest;

import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.work.plugin.ao.BmryfbStateService;
import com.work.plugin.ao.PowerEntity;
import com.work.plugin.ao.PowerService;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 2021/7/4.
 */
@Path("bmryfbState")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class BmryfbStateResource {

    private final GlobalPermissionManager globalPermissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final BmryfbStateService bmryfbStateService;
    private final PowerService powerService;
//    private final BmryfbStateServiceLazyImpl bmryfbStateServiceLazy;

    @GET
    @Path("{sign}")
    @AnonymousAllowed
    public Response get(@PathParam("sign") final String sign){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }

//        return Response.ok(bmryfbStateService.all(sign)).build();

        if("bmryfb".equals(sign) || "tdryfb".equals(sign)){
            List<ReportJoinChildrenBean> allOfBm = bmryfbStateService.getAllOfBm(sign);
            System.out.println(allOfBm);
            return Response.ok(bmryfbStateService.getAllOfBm(sign)).build();
        } else if("bmrygz".equals(sign) || "tdrygz".equals(sign)){
            return Response.ok(bmryfbStateService.getAllOfTd(sign)).build();
        }
        return Response.ok().build();
    }

    @GET
    @AnonymousAllowed
    public Response get(@QueryParam("a") final String a,@QueryParam("b") final int b,@QueryParam("c") final int c){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(bmryfbStateService.getInfos(a,b,c)).build();
    }
    @GET
    @Path("controlPower")
    public Response controlPower(@Context HttpServletRequest request){
        Map<String,Boolean> map = getPower(request);
        return Response.ok(map).build();
    }
    @GET
    @Path("getPower")
    public Response controlPowers(@Context HttpServletRequest request){
        Map<String,Boolean> map = getPower(request);
        return Response.ok(map).build();
    }
    @GET
    @Path("getPowerZ")
    public Response controlPowersZ(@Context HttpServletRequest request){
        Map<String,Boolean> map = getPower(request);
        return Response.ok(map).build();
    }
    private Map<String,Boolean> getPower(HttpServletRequest request){
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
