package com.work.plugin.rest;

import com.atlassian.annotations.security.XsrfProtectionExcluded;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.work.plugin.ao.RoleEntity;
import com.work.plugin.ao.RoleService;
import com.work.plugin.ao.StrEmployee;
import com.work.plugin.ao.StrEmployeeService;
import com.work.plugin.util.exception.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2021/6/21.
 * Update by admin on 2021/6/28.
 */
@Path("strEmployee")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class StrEmployeeResource {

    private final GlobalPermissionManager globalPermissionManager;
    private final StrEmployeeService strEmployeeService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final RoleService roleService;
    private final CrowdService crowdService;

    @POST
    @XsrfProtectionExcluded
    public Response create(final StrEmployeeBean bean) {
        try {
            final StrEmployee e = strEmployeeService.create(bean);
            String roleId = bean.getRoleId();
            String[] split = roleId.split(",");
            List<String> roleIdList = getRoleIdList(split);
            Set<RoleEntity> roleEntitySet = new HashSet<>();
            if (split.length != 0) {
                for (int i = 0; i < roleIdList.size(); i++) {
                    roleEntitySet.add(roleService.getId(Integer.parseInt(roleIdList.get(i))));
                }
                strEmployeeService.setOrgRoleToEmployee(e, roleEntitySet);
            }
            return Response.ok(new StrEmployeeBean(e)).build();
        } catch (DuplicateKeyException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    public Response get() {
        return Response.ok(
                strEmployeeService.getValidStrEmployee().stream()
                        .map(e -> new StrEmployeeBean(e)).toArray()
        ).build();
    }

    @GET
    @Path("{employeeId}")
    public Response getEmployee(@PathParam("employeeId") final int employeeId) {
        final StrEmployee e = strEmployeeService.getEmployee(employeeId);
        return Response.ok(new StrEmployeeBean(e)).build();
    }

    @PUT
    public Response edit(final StrEmployeeBean bean) {
        try {
            final StrEmployee e = strEmployeeService.update(bean);
            String roleId = bean.getRoleId();
            String[] split = roleId.split(",");
            List<String> roleIdList = getRoleIdList(split);
            Set<RoleEntity> roleEntitySet = new HashSet<>();
            if (split.length != 0) {
                for (int i = 0; i < roleIdList.size(); i++) {
                    roleEntitySet.add(roleService.getId(Integer.parseInt(roleIdList.get(i))));
                }
                strEmployeeService.setOrgRoleToEmployee(e, roleEntitySet);
            }
            return Response.ok(new StrEmployeeBean(e)).build();
        } catch (DuplicateKeyException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("Operation Failed").build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") final int id) throws SQLException {
        strEmployeeService.delete(id);
        return Response.ok().build();
    }

    @DELETE
    @Path("deleteTeamMember/{id}")
    public Response deleteTeamMember(@PathParam("id") final int id) throws SQLException {
        strEmployeeService.deleteTeamMember(id);
        return Response.ok().build();
    }

    @GET
    @Path("query")
    public Response queryMember(@QueryParam("q") final String q) {
        StrEmployee[] strEmployees = strEmployeeService.queryMember(q);
        List<StrEmployeeBean> employees = Lists.newArrayList(strEmployees)
                .stream().map(e -> new StrEmployeeBean(e))
                .collect(Collectors.toList());
        return Response.ok(employees).build();
    }

    @POST
    @Path("validEmployeeno")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Response vaildDeptno(@FormParam("employeeNo") String employeeNo, @QueryParam("employeeId") String employeeId) {

        Map<String, Object> data = Maps.newHashMap();
        if (strEmployeeService.isExistEmployeeNo(employeeNo, employeeId)) {
            data.put("ok", "");
           // data.put("error", "the employee is existing!!!");
        } else {
            data.put("ok", "");
        }

        return Response.ok(data).build();
    }

    /**
     * 未关联组织机构成员的jira用户
     *
     * @return
     */
    @GET
    @Path("unrelatedJirauser/{employeeId}")
    public Response getUnrelatedJirauser(@PathParam("employeeId") String employeeId, @QueryParam("q") String queryStr) {
        UserManager userManager = ComponentAccessor.getUserManager();
        List<StrEmployee> strEmployees = Lists.newArrayList(strEmployeeService.getEmployeeRelatedJirauserkey());
        strEmployees = strEmployees.stream().filter(s -> {
            String jiraUserKey = s.getJiraUserKey();
            if (StringUtils.isNotBlank(jiraUserKey)) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
        List<String> relatedJirauserkey = strEmployees.stream().
                map(strEmployee -> strEmployee.getJiraUserKey()).collect(Collectors.toList());
        UserSearchService userSearchService = ComponentAccessor.getComponent(UserSearchService.class);
        UserSearchParams.Builder searchParamsBuilder = UserSearchParams.builder()
                .sorted(true)
                .maxResults(20)
                .allowEmptyQuery(true)
                .filter(user -> {
                    ApplicationUser userByName = userManager.getUserByName(user.getName());
                    String key = userByName.getKey();
                    boolean b = !relatedJirauserkey.contains(key);
                    if (StringUtils.isNotEmpty(queryStr)) {
                        return b && user.getName().contains(queryStr);
                    } else {
                        return b;
                    }
                });

        List<ApplicationUser> users = userSearchService.findUsers("", searchParamsBuilder.build());

        List<Map<String, String>> data = Lists.newArrayList();
        users.forEach(user -> {
            Map<String, String> map = Maps.newHashMap();
            map.put("key", user.getKey());
            map.put("displayName", user.getDisplayName());
            map.put("name", user.getName());
            map.put("emailAddress", user.getEmailAddress());
            data.add(map);
//            }
        });
        int i = Integer.parseInt(employeeId);
        if (i != -1) {
            StrEmployee employee = strEmployeeService.getEmployee(i);
            String jiraUserKey = employee.getJiraUserKey();
            ApplicationUser user = userManager.getUserByKey(jiraUserKey);
            if (user != null) {
                Map<String, String> map = Maps.newHashMap();
                map.put("displayName", user.getDisplayName());
                map.put("key", user.getKey());
                map.put("name", user.getName());
                map.put("emailAddress", user.getEmailAddress());
                data.add(map);
            }
        }
        return Response.ok(data).build();
    }

    /**
     * 未关联组织机构成员的jira用户
     *
     * @return
     */
    @GET
    @Path("getUnrelatedJirauser/{employeeId}")
    public Response getUnrelatedJirauser(@PathParam("employeeId") String employeeId) {
        UserManager userManager = ComponentAccessor.getUserManager();

        StrEmployee employee = strEmployeeService.getEmployee(Integer.valueOf(employeeId));
        String jiraUserKey = employee.getJiraUserKey();
        ApplicationUser user = userManager.getUserByKey(jiraUserKey);
        Map<String, String> map = Maps.newHashMap();
        if (user != null) {
            map.put("text", user.getDisplayName());
            map.put("id", user.getKey());
            map.put("emailAddress", user.getEmailAddress());
            map.put("name", user.getName());
        }
        return Response.ok(map).build();
    }

    /**
     * 获取雇员下属员工
     *
     * @param employeeId
     * @return
     */
    @GET
    @Path("getSubEmployee")
    public Response getSubEmployee(@QueryParam("employeeId") Integer employeeId) {
        List<StrEmployee> subEmployee = strEmployeeService.getSubEmployee(employeeId);
        return Response.ok(subEmployee.stream().map(e -> new StrEmployeeBean(e)).collect(Collectors.toList())).build();
    }

    private List<String> getRoleIdList(String[] split) {
        List<String> collect = Arrays.stream(split).filter(s -> {
            if (StringUtils.isNotBlank(s)) {
                try {
                    Integer integer = Integer.valueOf(s);
                } catch (Exception e1) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
        return collect;
    }

    @GET
    @Path("getEmployeeByGroupId/{groupId}")
    public Response getEmployeeByGroupId(@PathParam("groupId") String groupId) {
        if (groupId.startsWith("d_")) {
            groupId = groupId.replace("d_", "");
        }
        StrEmployee[] employeeByGroupId = strEmployeeService.getEmployeeByGroupId(Integer.parseInt(groupId));

        List<StrEmployeeBean> list = new ArrayList<>();
        Arrays.stream(employeeByGroupId).forEach(employee -> {
            list.add(new StrEmployeeBean(employee));
        });
        return Response.ok(list).build();
    }

}
