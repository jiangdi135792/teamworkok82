package com.work.plugin.rest;

import com.atlassian.annotations.security.XsrfProtectionExcluded;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.Maps;
import com.work.plugin.ao.DepartmentAOService;
import com.work.plugin.ao.DepartmentEntity;
import com.work.plugin.ao.StrEmployee;
import com.work.plugin.util.exception.IntegrityConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by work on 2021/6/22.
 */

@Path("department")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class DepartmentResource {
    private final DepartmentAOService departmentAOService;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    @GET
    @AnonymousAllowed
    public Response getAll(@QueryParam("q") final String q) {
        if (null == jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();
        List<DepartmentEntity> list;
        if (StringUtils.isEmpty(q)) {
            list = departmentAOService.getAllGroup();

        } else {
            list = Arrays.asList(departmentAOService.query(q));
        }
        DepartmentBean[] result = list.stream()
                .map(e -> new DepartmentBean(e.getID(), e.getGroupNo(), e.getGroupName(), e.getParent(), e.getStatus(), e.getType(), e.getMappingCode()
                        , e.getMemo(), e.getOwner(), e.getCreateDate(), e.getModifier(), e.getModifierDate()))
                .toArray(DepartmentBean[]::new);
        return Response.ok(result).build();
    }

    @GET
    @Path("{id}")
    @AnonymousAllowed
    public Response get(@PathParam("id") final int id) {
        if (null == jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();
        DepartmentEntity entity = departmentAOService.get(id);

        return Response.ok(new DepartmentBean(entity)).build();
    }

    @GET
    @Path("query")
    @AnonymousAllowed
    public Response query(@QueryParam("q") final String name) {
        if (null == jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();
        DepartmentBean[] result = Arrays.asList(departmentAOService.query(name)).stream()
                .map(e -> new DepartmentBean(e.getID(), e.getGroupNo(), e.getGroupName(), e.getParent(), e.getStatus(), e.getType(), e.getMappingCode()
                        , e.getMemo(), e.getOwner(), e.getCreateDate(), e.getModifier(), e.getModifierDate()))
                .toArray(DepartmentBean[]::new);
        return Response.ok(result).build();
    }

    @POST
    @AnonymousAllowed
    @XsrfProtectionExcluded
    public Response create(final DepartmentBean bean) {
        if (null == jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();
        ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();

        bean.setOwner(user.getId().toString());
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        bean.setCreateDate(date);
        bean.setModifierDate(date);
        bean.setModifier(user.getId().toString());
        DepartmentEntity entity = departmentAOService.add(bean);
        return Response.ok(new DepartmentBean(entity)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") final int id) {
        try {
            departmentAOService.delete(id);
        } catch (IntegrityConstraintViolationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

    @PUT
    public Response update(final DepartmentBean bean) {
        ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        bean.setModifierDate(date);
        bean.setModifier(user.getId().toString());

        DepartmentEntity entity = departmentAOService.update(bean);
        return Response.ok(new DepartmentBean(entity)).build();
    }

    @POST
    @Path("validDeptno")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public Response vaildDeptno(@FormParam("groupNo") String groupNo, @QueryParam("groupId") String groupId) {

        Map<String, Object> data = Maps.newHashMap();
         if (departmentAOService.isExistDeptNo(groupNo, groupId)) {
            data.put("error", "the dept is existing!!!");
        } else {
            data.put("ok", "");
        }
        return Response.ok().build();
    }

    @POST
    @Path("setDeptDutyPerson/{jiraUserKey}")
    public Response setDeptDutyPersonByJiraUserKeyAndDeptId(@PathParam("jiraUserKey") String jiraUserKey){
        String[] split = jiraUserKey.split(",");
        int backData = departmentAOService.setDeptDutyPersonByJiraUserKey(split[0],split[1]);
        return Response.ok(backData).build();
    }
    @GET
    @Path("getDutyPersonByDeptId/{deptId}")
    public Response getDutyPersonByDeptId(@PathParam("deptId") String deptId){
        if(deptId.startsWith("d_")){
            deptId = deptId.replace("d_","");
        }
        StrEmployee person = departmentAOService.getDutyPersonByDeptId(deptId);
        return Response.ok(person==null?"0":new StrEmployeeBean(person)).build();
    }

}
