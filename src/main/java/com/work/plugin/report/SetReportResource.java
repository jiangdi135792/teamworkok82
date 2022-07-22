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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 2022/3/28.
 */
@Path("/setreport")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class SetReportResource {

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SetReportService service;
    private final PowerService powerService;
    /**
     * 获取所有报表设置
     * @return
     */
    @GET
    @Path("report/{type}")
    @AnonymousAllowed
    public Response getReport(@PathParam("type") final String type){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String jira_user_key = jiraAuthenticationContext.getLoggedInUser().getKey();//获取当前登录用户的key
        return Response.ok(service.getSetReport(jira_user_key,type)).build();
    }

    /**
     * 初始化issue展示的列
     * @return
     */
    @GET
    @Path("initcolumn")
    @AnonymousAllowed
    public Response getInitColumn(){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(service.getInitColumn()).build();
    }

    /**
     * 获取issue展示的列及列对应数据
     * @return
     */
    @GET
    @Path("reportColumn")
    @AnonymousAllowed
    public Response getColumn(@QueryParam("reportKey") final String reportKey,
                              @QueryParam("type") final String type,
                              @QueryParam("jira_user_key") final String jira_user_key){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(service.getColumn(jira_user_key,reportKey,type)).build();
    }

    /**
     * 获取issue展示的列
     * @return
     */
    @GET
    @Path("create")
    @AnonymousAllowed
    public Response create(final ArrayList<SetReportBean> setReportBeans,
                           final ArrayList<SetReportColumnBean> setReportColumnBeans){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String jira_user_key = jiraAuthenticationContext.getLoggedInUser().getKey();//获取当前登录用户的key
//        service.createReport(setReportBeans,jira_user_key);
//        service.createColumn(setReportColumnBeans,jira_user_key);
        return Response.ok().build();
    }

    /**
     * 点击保存时存储报表设置数据
     * @return
     */
    @POST
    @Path("saveReport/{reportName}/{reportKey}/{group_one}/{group_two}/{modelName}/{modelType}/{model_show}/{startLine}/{endLine}/{orgRole}/{employee}")
    @AnonymousAllowed
    public Response save(@PathParam("reportName") final String reportName,
                         @PathParam("reportKey") final String reportKey,
                         @PathParam("group_one") final String group_one,
                         @PathParam("group_two") final String group_two,
                         @PathParam("modelName") final String modelName,
                         @PathParam("modelType") final int modelType,
                         @PathParam("model_show") final int model_show,
                         @PathParam("startLine") final int startLine,
                         @PathParam("endLine") final int endLine,
                         @PathParam("orgRole") final String orgRole,
                         @PathParam("employee") final String employee,
                         ArrayList<SetReportColumnBean> setReportColumnBeans){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String jira_user_key = jiraAuthenticationContext.getLoggedInUser().getKey();//获取当前登录用户的key
        SetReportBean bean = new SetReportBean();
        bean.setReportName(reportName);
        bean.setReportKey(reportKey);
        bean.setGroup_one(group_one);
        bean.setGroup_two(group_two);
        bean.setModelName(modelName);
        bean.setModelType(modelType);
        bean.setModel_show(model_show);
        bean.setStartLine(startLine);
        bean.setEndLine(endLine);
        service.createReport(bean,jira_user_key);
        service.createColumn(setReportColumnBeans,jira_user_key,reportKey);
        service.createShare(orgRole,employee,jira_user_key,reportKey,null);
        return Response.ok().build();
    }

    /**
     * 点击修改时存储报表设置数据
     * @return
     */
    @POST
    @Path("updateReport/{reportName}/{reportKey}/{group_one}/{group_two}/{modelName}/{modelType}/{model_show}/{startLine}/{endLine}/{orgRole}/{employee}")
    @AnonymousAllowed
    public Response update(@PathParam("reportName") final String reportName,
                           @PathParam("reportKey") final String reportKey,
                           @PathParam("group_one") final String group_one,
                           @PathParam("group_two") final String group_two,
                           @PathParam("modelName") final String modelName,
                           @PathParam("modelType") final int modelType,
                           @PathParam("model_show") final int model_show,
                           @PathParam("startLine") final int startLine,
                           @PathParam("endLine") final int endLine,
                           @PathParam("orgRole") final String orgRole,
                           @PathParam("employee") final String employee,
                         ArrayList<SetReportColumnBean> setReportColumnBeans){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String jira_user_key = jiraAuthenticationContext.getLoggedInUser().getKey();//获取当前登录用户的key
        SetReportBean bean = new SetReportBean();
        bean.setReportName(reportName);
        bean.setReportKey(reportKey);
        bean.setGroup_one(group_one);
        bean.setGroup_two(group_two);
        bean.setModelName(modelName);
        bean.setModelType(modelType);
        bean.setModel_show(model_show);
        bean.setStartLine(startLine);
        bean.setEndLine(endLine);
        service.updateReport(bean,jira_user_key);
        service.updateColumn(setReportColumnBeans,jira_user_key,reportKey);
        service.updateShare(orgRole,employee,jira_user_key,reportKey);
        return Response.ok().build();
    }

    /**
     * 点击删除时删除此报表的所有信息
     * @return
     */
    @POST
    @Path("deleteReport/{reportKey}")
    @AnonymousAllowed
    public Response delete(@PathParam("reportKey") final String reportKey){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String jira_user_key = jiraAuthenticationContext.getLoggedInUser().getKey();//获取当前登录用户的key
        service.deleteReport(reportKey,jira_user_key);
        return Response.ok().build();
    }

    /**
     * 判断报表名称是否存在
     * @return
     */
    @GET
    @Path("reportNameIsExist")
    @AnonymousAllowed
    public Response reportNameIsExist(@QueryParam("reportName") final String reportName){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String jira_user_key = jiraAuthenticationContext.getLoggedInUser().getKey();//获取当前登录用户的key
        return Response.ok(service.reportNameIsExist(reportName,jira_user_key)).build();
    }

    /**
     * 判断报表key是否存在
     * @return
     */
    @GET
    @Path("reportKeyIsExist/{reportKey}")
    @AnonymousAllowed
    public Response reportKeyIsExist(@PathParam("reportKey") final String reportKey){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String jira_user_key = jiraAuthenticationContext.getLoggedInUser().getKey();//获取当前登录用户的key
        return Response.ok(service.reportKeyIsExist(reportKey,jira_user_key)).build();
    }

    /**
     * 二级关联获取第二分组的值
     * @return
     */
    @POST
    @Path("getGrouptwo/{groupone}")
    @AnonymousAllowed
    public Response getGrouptwo(@PathParam("groupone") final String groupone){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(service.getGrouptwo(groupone)).build();
    }

    /**
     *  get detail power
     * @param request
     * @return
     */
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

    /**
     * 获取组织角色
     * @return
     */
    @GET
    @Path("orgrole")
    @AnonymousAllowed
    public Response getOrgRole(){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(service.getOrgRole()).build();
    }

    /**
     * 获取所有雇员
     * @return
     */
    @GET
    @Path("employee")
    @AnonymousAllowed
    public Response getEmployee(){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        GlobalReport.jira_user_key = jiraAuthenticationContext.getLoggedInUser().getKey();//获取当前登录用户的key
        return Response.ok(service.getEmployee()).build();
    }

    /**
     * 获取所有雇员
     * @return
     */
    @GET
    @Path("sharereport")
    @AnonymousAllowed
    public Response getShareReport(@QueryParam("reportKey") final String reportKey){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        GlobalReport.jira_user_key = jiraAuthenticationContext.getLoggedInUser().getKey();//获取当前登录用户的key
        return Response.ok(service.getShareReport(reportKey)).build();
    }

}
