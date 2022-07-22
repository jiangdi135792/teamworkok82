package com.work.plugin.report;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.work.plugin.ao.PowerEntity;
import com.work.plugin.ao.PowerService;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 2022/3/28.
 */
@Path("showReport")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class ShowReportResource {

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ShowReportService service;
    private final SetReportService setReportService;
    private final PowerService powerService;
    private final GetOrgTreeService orgTreeService;

    /**
     * 获取所有报表信息
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param reportKey 报表的key
     * @param proname 项目名称
     * @param orgid 组织或部门id
     * @param orgname 组织或部门名称
     * @param orgtype 类型 组织还是部门
     * @param radioType 机构或团队的单选框类型,orgRadio表示点击的是机构,teamRadio表示点击的是团队
     * @param teamids 团队的id集合，对于未映射的团队的项目则为项目id
     * @return
     */
    @GET
    @Path("all")
    @AnonymousAllowed
    public Response getReport(@QueryParam("startTime") final String startTime,
                              @QueryParam("endTime") final String endTime,
                              @QueryParam("reportKey") final String reportKey,
                              @QueryParam("proname") final String proname,
                              @QueryParam("orgid") final int orgid,
                              @QueryParam("orgname") final String orgname,
                              @QueryParam("orgtype") final String orgtype,
                              @QueryParam("radioType") final String radioType,
                              @QueryParam("teamids") final String teamids,
                              @QueryParam("jira_user_key") final String jira_user_key) {
        if (null == jiraAuthenticationContext.getLoggedInUser()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        GlobalReport.startTime = startTime;
        GlobalReport.endTime = endTime;
        GlobalReport.reportKey = reportKey;
        GlobalReport.proname = proname;
        if(orgtype.equals("org_type")){
            GlobalReport.orgid = orgid - 10000;
        }
        if(orgtype.equals("dept_type")){
            GlobalReport.orgid = orgid - 20000;
        }
        GlobalReport.orgname = orgname;
        GlobalReport.orgtype = orgtype;
        GlobalReport.radioType = radioType;
        GlobalReport.teamids = teamids;
        GlobalReport.jira_user_key = jiraAuthenticationContext.getLoggedInUser().getKey();//获取当前登录用户的key
//        GlobalReport.jira_user_key = jira_user_key;
        return Response.ok(service.all()).build();
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
    @AnonymousAllowed
    @Path("proNames")
    /**
     * 获取项目名称
     */
    public Response getProName(){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(service.getProName()).build();
    }

    @GET
    @AnonymousAllowed
    @Path("orgTree")
    /**
     * 获取组织结构树
     */
    public Response getOrgTree(){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(orgTreeService.getAllTreeData()).build();
    }

    @GET
    @AnonymousAllowed
    @Path("teamTree")
    /**
     * 获取团队树
     */
    public Response getTeamTree(@QueryParam("proname") final String proname){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(orgTreeService.getTeamTreeData(proname)).build();
    }

    @GET
    @AnonymousAllowed
    @Path("test")
    /**
     * 测试雇员信息
     */
    public Response testNames(){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        return Response.ok(GlobalReport.testKey).build();
    }

}
