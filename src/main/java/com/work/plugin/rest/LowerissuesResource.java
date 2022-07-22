package com.work.plugin.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.work.plugin.ao.PowerEntity;
import com.work.plugin.ao.PowerService;
import com.work.plugin.ao.StrEmployee;
import com.work.plugin.ao.StrEmployeeService;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by work on 2022/6/10.
 */
@Path("lowerissues")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class LowerissuesResource {
    private final StrEmployeeService strEmployeeService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PowerService powerService;
    @GET
    @Path("getAll")
    public Response getLowerIssues(){
        ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        StrEmployee byJiraUserKey = strEmployeeService.getByJiraUserKey(loggedInUser.getKey());
        List<LowerIssuesChildBean> allLowerIssues = strEmployeeService.getAllLowerIssues(byJiraUserKey);
        return Response.ok(allLowerIssues).build();
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
