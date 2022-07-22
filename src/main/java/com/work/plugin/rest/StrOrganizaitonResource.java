package com.work.plugin.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.*;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.work.plugin.util.exception.IntegrityConstraintViolationException;
import com.work.plugin.util.license.license.LicenseService;
import com.work.plugin.ao.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2021/6/22.
 */
@Path("/orgstr")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class StrOrganizaitonResource {
    private final StrOrganizeService strOrganizeService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final StrEmployeeService strEmployeeService;
    private final LicenseService licenseService;
    private final DepartmentAOService departmentAOService;
    private final PowerService powerService;
    private final RoleService roleService;

    @Deprecated
    @GET
    @Path("test")
    public Response test(@QueryParam("keys") final String keys) {
        ProjectRoleManager component = ComponentAccessor.getComponent(ProjectRoleManager.class);
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        Project dee = projectManager.getProjectByCurrentKeyIgnoreCase("HAHA");
        ApplicationUser loggedInUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        Collection<ProjectRole> projectRoles = component.getProjectRoles();

        for (ProjectRole projectRole : projectRoles) {//项目对应的角色
            Set<RoleActor> actors = projectRoleManager.getProjectRoleActors(projectRole, dee).getRoleActors();
            if (actors.size() != 0) {
                for (RoleActor actor : actors) {//角色下的组（单个人也可以为一个组）
                    Set<ApplicationUser> user_actor = actor.getUsers();
                    for (ApplicationUser user : user_actor) {//每个组下面的人
                        if (user.getKey() == loggedInUser.getKey()) {
//                                jirakeys.add(user.getName());
//                            roleActors.add(actor);
                            break;
                        }
                    }
                }
            }
        }

        return Response.ok().build();
    }

    @GET
    @AnonymousAllowed
    public Response getAll(@QueryParam("q") final String q) {
        if (null == jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();
        List<StrOrganize> list;
        if (StringUtils.isEmpty(q)) {
            list = strOrganizeService.getAll();
        } else {
            list = Arrays.asList(strOrganizeService.query(q));
        }
        StrOrganizaitonBean[] result = list.stream()
                .map(e -> new StrOrganizaitonBean(e.getID(), e.getName(), e.getParent()))
                .toArray(StrOrganizaitonBean[]::new);
        return Response.ok(result).build();
    }

    /**
     * 获取组织机构与部门的全部集合
     */
    @GET
    @Path("getOrgAndDept")
    @AnonymousAllowed
    public Response getOrgAndDept() {
        if (!licenseService.hasValidLicense()) {
            return Response.ok(Lists.newArrayList()).build();
        } else {
            List<StrOrganizaitonBean> orgAndDept = strOrganizeService.getOrgAndDept();
            orgAndDept.addAll(getUnmappedMember());

            return Response.ok(orgAndDept).build();
        }
    }


    private List<StrOrganizaitonBean> getUnmappedMember() {
        return Lists.newArrayList(
                // 未分配人员
//                StrOrganizaitonBean.builder().id("m_0").name(i18n.getText("workorg.property.organization.unmappedEmployee")).parent(null).type("unmappedmember").build(),
//                // 未分配公司
//                StrOrganizaitonBean.builder().id("m_1").name(i18n.getText("workorg.property.organization.unmappedDept")).parent("m_0").type("unmappedmember").build(),
//                // 未分配部门
                StrOrganizaitonBean.builder().id("m_0").name(i18n.getText("workorg.property.organization.unmappedOrg")).parent("m_0").type("unmappedmember").build()
        );
    }

    /**
     * 获取组织机构的直接部�???
     *
     * @deprecated 已弃用，使用 {@link com.work.plugin.rest.StrOrganizaitonResource#getSubDeptByOrgId(Integer)}
     */
    @GET
    @Path("{orgId}/getDirectDeptByOrgId")
    @Deprecated
    public Response getDirectDeptByOrgId(@PathParam("orgId") Integer orgId) {
        List<DepartmentEntity> depts = departmentAOService.getDirectDeptsByOrgId(orgId);

        return Response.ok(depts.stream().map(e -> new DepartmentBean(e)).toArray()).build();
    }

    /**
     * 获取组织机构的所有子部门，進入all
     */
    @GET
    @Path("{orgId}/getSubDeptByOrgId")
    public Response getSubDeptByOrgId(@PathParam("orgId") Integer orgId) {
        List<DepartmentEntity> depts = departmentAOService.getSubDeptsByOrgId(orgId);

        return Response.ok(Optional.ofNullable(depts)
                .orElseGet(() -> Lists.newArrayList())
                .stream().map(e -> new DepartmentBean(e)).toArray()).build();
    }


    /**
     * 获取组织机构与团队的全部集合
     */
    @GET
    @Path("getOrgAndTeam")
    @AnonymousAllowed
    public Response getOrgAndTeam() {
        if (!licenseService.hasValidLicense()) {
            List<StrOrganizaitonBean> str = new ArrayList<>();
            return Response.ok(str).build();
        } else {
            return Response.ok(strOrganizeService.getOrgAndTeam()).build();
        }
    }

    /**
     * 获取组织机构与团队的全部集合
     */
    @GET
    @Path("getOrgAndTeamAndProject")
    @AnonymousAllowed
    public Response getOrgAndTeamAndProject() {
        if (!licenseService.hasValidLicense()) {
            List<StrOrganizaitonBean> str = new ArrayList<>();
            return Response.ok(str).build();
        } else {
            return Response.ok(strOrganizeService.getOrgAndTeamAndProject()).build();
        }
    }

    /**
     * 获取组织机构与团队以及成员的全部集合
     *
     * @return
     */
    @GET
    @Path("getDetailOrgTree")
    @AnonymousAllowed
    public Response getDetailOrgTree() throws Exception {
        if (!licenseService.hasValidLicense()) {
            List<StrOrganizaitonBean> str = new ArrayList<>();
            return Response.ok(str).build();
        } else {
            List<StrOrganizaitonBean> orgAndDept = strOrganizeService.getDetailOrgTree();
            orgAndDept.addAll(getUnmappedMember());
            return Response.ok(orgAndDept).build();

        }
    }


    @GET
    @Path("{id}")
    @AnonymousAllowed
    public Response get(@PathParam("id") final int id) {
        if (null == jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();
        StrOrganize entity = strOrganizeService.get(id);

        return Response.ok(new StrOrganizaitonBean(entity)).build();
    }

    @GET
    @Path("query")
    @AnonymousAllowed
    public Response query(@QueryParam("q") final String name) {
        if (null == jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();
        StrOrganizaitonBean[] result = Arrays.asList(strOrganizeService.query(name)).stream()
                .map(e -> new StrOrganizaitonBean(e.getID(), e.getName(), e.getParent()))
                .toArray(StrOrganizaitonBean[]::new);
        return Response.ok(result).build();
    }

    @GET
    @Path("query1")
    @AnonymousAllowed
    public Response query1(@QueryParam("q") final String name) {
        if (null == jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();
        StrOrganize[] entitys = strOrganizeService.query(name);
        StrOrganizaitonBean[] result = Arrays.asList(entitys).stream()
                .map(e -> new StrOrganizaitonBean(e))
                .filter(e -> e.getType().equals("0"))
                .toArray(StrOrganizaitonBean[]::new);
        return Response.ok(result).build();
    }

    @POST
    @AnonymousAllowed
    public Response create(final StrOrganizaitonBean bean) {
        if (!licenseService.hasValidLicense()) {
            return Response.ok("ok").build();
        }
        if (null == jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();
        ApplicationUser user = jiraAuthenticationContext.getLoggedInUser();

        bean.setOwner(user.getId().intValue());
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        bean.setCreateDate(date);
        bean.setModifierDate(date);
        bean.setModifier(user.getId().intValue());
        StrOrganize strOrganize = strOrganizeService.create(bean);
        return Response.ok(new StrOrganizaitonBean(strOrganize)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") final int id) {
        try {
            strOrganizeService.delete(id);
        } catch (IntegrityConstraintViolationException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

    @PUT
    public Response update(final StrOrganizaitonBean bean) {
        if (null == jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();
        StrOrganize entity = strOrganizeService.update(bean);
        return Response.ok(new StrOrganizaitonBean(entity)).build();
    }

    /**
     * 组织部门
     *
     * @param id 组织的前缀为o，部门的前缀为d
     * @return
     */
    @GET
    @Path("{id}/getEmployees/")
    public Response getEmployees(@PathParam("id") final String id,
                                 @QueryParam("pageNo") int page, @QueryParam("pageCount") int pageCount, @QueryParam("q") String q) {
        Collection<StrEmployee> list = Lists.newArrayList();
        if (StringUtils.startsWith(id, "o_")) {
            list = strEmployeeService.getEmployeeByOrgId(Integer.valueOf(id.replace("o_", "")));
        } else if (StringUtils.startsWith(id, "d_")) {
            list = Lists.newArrayList(strEmployeeService.getEmployeeByGroupId(Integer.valueOf(id.replace("d_", ""))));
        } else if (StringUtils.equals(id, "m_1")) {
            list = strEmployeeService.listUnmappedToDeptMember();
        } else if (StringUtils.equals(id, "m_2")) {
            list = strEmployeeService.listUnmappedToOrgMember();
        }
        else if (StringUtils.equals(id, "m_0")) {
            list = strEmployeeService.listUnmappedToOrgMember();
        }
        if (StringUtils.isNotEmpty(q)) {
            list = Lists.newArrayList(list.stream().filter(e -> {
                if (StringUtils.isNotEmpty(e.getEmployeeName()) && e.getEmployeeName().contains(q)) {
                    return true;
                }
                if (StringUtils.isNotEmpty(e.getEmployeeNo()) && e.getEmployeeNo().contains(q)) {
                    return true;
                }
                return false;
            }).toArray(StrEmployee[]::new));
        }

        page = page < 0 ? 1 : page;
        pageCount = pageCount <= 0 ? 1 : pageCount;
        int startIndex = (page - 1) * pageCount;

        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("data", list.stream().skip(startIndex).limit(pageCount).map(e -> new StrEmployeeBean(e)).toArray(StrEmployeeBean[]::new));
        map.put("pageCount", pageCount);
        map.put("currentPage", page);
        map.put("pageSize", Math.ceil(list.size() / (double) pageCount == 0 ? 1 : list.size() / (double) pageCount));
        map.put("total", list.size());

        return Response.ok(map).build();
    }


    /**
     * @param id 组织的前缀为o，部门的前缀为d
     */
    @GET
    @Path("{id}/getStruGroupMembers/")
    public Response getStruGroupMembers(@PathParam("id") final String id,
                                        @QueryParam("pageNo") int page, @QueryParam("pageCount") int pageCount, @QueryParam("q") String q) {
        List<StruGroupOfEmployee> list = Lists.newArrayList();
        if (StringUtils.startsWith(id, "o_")) {
            list = strEmployeeService.getStruGroupOfEmployeeByOrgId(Integer.valueOf(id.replace("o_", "")));
        } else if (StringUtils.startsWith(id, "d_")) {
            list = Lists.newArrayList(strEmployeeService.getStruGroupOfEmployeeByTeamId(Integer.valueOf(id.replace("d_", ""))));
        }

        List<StruGroupOfEmployeeBean> struGroupOfEmployeeBeanList = list.stream().map(e -> new StruGroupOfEmployeeBean(e)).collect(Collectors.toList());
        if (StringUtils.isNotEmpty(q)) { // 过滤
            struGroupOfEmployeeBeanList =
                    Lists.newArrayList(
                            struGroupOfEmployeeBeanList.stream()
                                    .filter(e -> {
                                        if (StringUtils.isNotEmpty(e.getEmployeeName()) &&
                                                e.getEmployeeName().contains(q)) {
                                            return true;
                                        }
                                        if (StringUtils.isNotEmpty(e.getEmployeeNo()) &&
                                                e.getEmployeeNo().contains(q)) {
                                            return true;
                                        }
                                        return false;
                                    })
                                    .toArray(StruGroupOfEmployeeBean[]::new));
        }

        page = page < 0 ? 1 : page;
        pageCount = pageCount <= 0 ? 1 : pageCount;
        int startIndex = (page - 1) * pageCount;

        HashMap<Object, Object> map = Maps.newHashMap();
        List<StruGroupOfEmployeeBean> collect = struGroupOfEmployeeBeanList.stream()
                .skip(startIndex).limit(pageCount)
                .collect(Collectors.toList());
        collect.forEach(e -> {
            RoleEntity roleEntity = strEmployeeService.getRoleByEmployeeAndTeam(e.getTeamId(), e.getEmployeeId());
            String roles = roleEntity.getName();
            String roleIds = roleEntity.getID() + "";
            e.setRoleNames(roles);
            e.setRoleIds(roleIds);
        });
        map.put("data", collect);
        map.put("pageCount", pageCount);
        map.put("currentPage", page);
        map.put("pageSize", Math.ceil(list.size() / (double) pageCount == 0 ? 1 : list.size() / (double) pageCount));
        map.put("total", list.size());

        return Response.ok(map).build();
    }

    /**
     * 获取只属于当前团队的成员，不包括子团队中的成�???
     *
     * @param teamid 组织的前缀为o，部门的前缀为d
     * @return
     */
    @GET
    @Path("{id}/getDirectStruGroupOfEmployeeByTeamId/")
    public Response getDirectStruGroupOfEmployeeByTeamId(@PathParam("id") final Integer teamid) {
        List<StruGroupOfEmployee> list = Lists.newArrayList(strEmployeeService.getDirectStruGroupOfEmployeeByTeamId(teamid));
        List<StruGroupOfEmployeeBean> struGroupOfEmployeeBeanList =
                list.stream().map(e -> {
                    StruGroupOfEmployeeBean struGroupOfEmployeeBean = new StruGroupOfEmployeeBean(e);
                    RoleEntity roleEntity = strEmployeeService.getRoleByEmployeeAndTeam(teamid, e.getEmployee().getID());
                    String roles = roleEntity.getName();
                    String roleIds = roleEntity.getID() + "";
                    struGroupOfEmployeeBean.setRoleNames(roles);
                    struGroupOfEmployeeBean.setRoleIds(roleIds);
                    return struGroupOfEmployeeBean;
                }).collect(Collectors.toList());

        return Response.ok(struGroupOfEmployeeBeanList).build();
    }

    @GET
    @Path("/getEmployeeNotInTeam/{id}")
    public Response getEmployeeNotInTeam(@PathParam("id") final String id) {
        if (id.startsWith("d_")) {
            List<StrEmployee> strEmployees = strEmployeeService.getAvailableEmployeesByTeamId(id.replace("d_", ""));
            return Response.ok(strEmployees.stream().filter(e -> e !=
                    null).map(e -> new StrEmployeeBean(e)).toArray(StrEmployeeBean[]::new)).build();
        }

        return Response.ok().build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Path("/addEmployeeToTeam/{teamId}")
    public Response addEmployeeToTeam(@PathParam("teamId") int teamId, @FormParam("employeeIds") List<String> employeeIds,
                                      @FormParam("roleIds") List<String> roleIds) {
        strEmployeeService.addEmployeeToTeam(teamId, employeeIds, roleIds);
        return Response.ok().build();
    }

    private final TeamProjectService teamProjectService;
    private final I18nHelper i18n;

    /**
     * 获取未映射团队
     *
     * @return
     */
    @GET
    @Path("unmappedProject")
    public Response getUnmappedProject() {
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        List<Project> projects = projectManager.getProjects();

        TeamProjectEntity[] teamProjectEntities = teamProjectService.getAll();
        Map<Integer, Integer> teamProjectMap = Maps.newHashMap();

        Lists.newArrayList(teamProjectEntities).stream().forEach(teamProjectEntity -> {
            teamProjectMap.put(teamProjectEntity.getProjectId(), teamProjectEntity.getTeamId());
        });
        Set<Integer> keyset = teamProjectMap.keySet();
        List<Map<String, Object>> data = Lists.newArrayList();

        Map<String, Object> unMappingTeamRoot = Maps.newHashMap();
        unMappingTeamRoot.put("projectKey", "project");
        unMappingTeamRoot.put("projectName", i18n.getText("workorg.property.message.alert.notMappedProject"));// 未映射项目
        unMappingTeamRoot.put("id", "o_9999");
        unMappingTeamRoot.put("parent", "o_0");
        unMappingTeamRoot.put("type", "projectroot");
        data.add(unMappingTeamRoot);

        projects.stream().forEach(project -> {
            Integer projectId = project.getId().intValue();
            if (!keyset.contains(projectId)) {
                Map<String, Object> projectMap = Maps.newHashMap();
                projectMap.put("projectKey", project.getKey());
                projectMap.put("projectName", project.getName());
                projectMap.put("id", projectId);
                projectMap.put("parent", "o_9999");
                projectMap.put("type", "project");
                data.add(projectMap);
            }
        });

        return Response.ok(data).build();
    }

    /**
     * 获取未映射的团队
     *
     * @return
     */
    @GET
    @Path("unmappedTeam")
    public Response getUnmappedTeam() {
        List<DepartmentEntity> unmappedTeam = strOrganizeService.getUnmappedTeam();

        return Response.ok(unmappedTeam.stream().map(e -> new DepartmentBean(e)).collect(Collectors.toList())).build();
    }

    @GET
    @Path("mappingTeamProject")
    public Response mappingTeamProjcet(@QueryParam("projectId") Integer projectId,
                                       @QueryParam("teamId") Integer teamId) {
        TeamProjectEntity save = teamProjectService.save(teamId, projectId);
        return Response.ok().build();
    }

    /**
     * 获取所有project，如果关联了，属性则为true
     *
     * @param teamId 团队id
     * @return
     */
    @GET
    @Path("projectRelated")

    public Response getTeamRelatedProject(@QueryParam("teamId") Integer teamId) {
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        List<Project> projects = projectManager.getProjects(); // 获取所有的project

        TeamProjectEntity[] teamProjectEntities = teamProjectService.getByTeamId(teamId);
        List<Long> projectIdList = Lists.newArrayList(teamProjectEntities).stream()
                .map(teamProjectEntity -> teamProjectEntity.getProjectId().longValue()).collect(Collectors.toList());

        TeamProjectEntity[] all = teamProjectService.getAll();
        ArrayList<TeamProjectEntity> allRelatedProject = Lists.newArrayList(all);

        List<Map<String, Object>> data = Lists.newArrayList();
        projects.forEach(project -> {
            Map<String, Object> projectMap = Maps.newHashMap();
            String projectKey = project.getKey();
            Integer projectId = project.getId().intValue();
            projectMap.put("projectKey", projectKey);
            projectMap.put("projectName", project.getName());
            projectMap.put("id", project.getId());
            boolean isRelated = allRelatedProject.stream().anyMatch(
                    e -> e.getProjectId().equals(projectId) && e.getTeamId().equals(teamId));
            projectMap.put("isRelated", isRelated);
            boolean disabled = allRelatedProject.stream().anyMatch(
                    e -> e.getProjectId().equals(projectId) && !e.getTeamId().equals(teamId));
            projectMap.put("disabled", disabled);
            data.add(projectMap);
        });

        return Response.ok(data).build();
    }

    @POST
    @Path("saveTeamProject")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Response saveTeamProject(@FormParam("teamId") Integer teamId, @FormParam("projectId") Integer projectId) {
        teamProjectService.save(teamId, projectId);


        return Response.ok().build();
    }

    private final ProjectRoleManager projectRoleManager;

    /**
     * project映射到team
     */
    @GET
    @Path("projectMapToTeam")
    public Response projectMapToTeam(@QueryParam("projectKey") String projectKey,
                                     @QueryParam("teamId") Integer teamId) {

        Map<String, List<ApplicationUser>> projectRoleMember = Maps.newHashMap();
        Map<String, String> projectUserRoleMap = Maps.newHashMap();

        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager.class);
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        Project project = projectManager.getProjectByCurrentKey(projectKey);
        Collection<ProjectRole> projectRoles = projectRoleManager.getProjectRoles();

        for (ProjectRole projectRole : projectRoles) {//项目对应的角色
            Set<RoleActor> actors = projectRoleManager.getProjectRoleActors(projectRole, project).getRoleActors();
            if (actors.size() != 0) {
                List<ApplicationUser> userList = Lists.newArrayList();
                for (RoleActor actor : actors) {//角色下的组（单个人也可以为一个组）
                    Set<ApplicationUser> user_actor = actor.getUsers();
                    for (ApplicationUser user : user_actor) {//每个组下面的人
                        userList.add(user);
                        projectUserRoleMap.put(projectRole.getName(), user.getKey());
                    }
                }
                if (!userList.isEmpty()) {
                    projectRoleMember.put(projectRole.getName(), userList);
                }
            }
        }

        List<String> teamEmployeesJirauserKeys = Lists.newArrayList();
        List<DepartmentEntity> teams = departmentAOService.getSubTeamOfTeam(teamId); // 获取团队的子团队
        teams.forEach(team -> {
            int teamid = team.getID();
            List<StruGroupOfEmployee> list = Lists.newArrayList(strEmployeeService.getDirectStruGroupOfEmployeeByTeamId(teamid));
            list.forEach(e -> {
                StrEmployee employee = e.getEmployee();
                String jiraUserKey = employee.getJiraUserKey();
                if (StringUtils.isNotEmpty(jiraUserKey)) {
                    teamEmployeesJirauserKeys.add(jiraUserKey);
                }
            });
        });

        projectUserRoleMap.forEach((projectRolename, userKey) -> {
            if (!teamEmployeesJirauserKeys.contains(userKey)) { // 未映射的则创建
                StrEmployee byJiraUserKey = strEmployeeService.getByJiraUserKey(userKey);
                if (byJiraUserKey != null) { // 找到jiruserkey对应的雇员,将雇员放入到该团队中
                    RoleEntity roleByName = roleService.getRoleByName(projectRolename);
                    if (roleByName != null) {
                        strEmployeeService.addEmployeeToTeam(teamId, byJiraUserKey.getID(), roleByName.getID());
                    }
                }
            }
        });
        return Response.ok().build();
    }

    @GET
    @Path("getProjectrole")
    public Response getProjectrole(@QueryParam("projectkey") String projectkey) {
        ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        Project project = projectManager.getProjectByCurrentKeyIgnoreCase(projectkey);
        Collection<ProjectRole> projectRoles = projectRoleManager.getProjectRoles(loggedInUser, project);
        projectRoles.forEach(projectRole -> {
            ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(projectRole, project);
            DefaultRoleActors defaultRoleActors = projectRoleManager.getDefaultRoleActors(projectRole);
        });

        return Response.ok().build();
    }

    /**
     * 获取团队项目成员角色信息
     */
    @GET
    @Path("teamprojectMemberRole")
    public Response getTeamprojcetMemberRole(@QueryParam("teamId") Integer teamId) {
        List<DepartmentEntity> teams = departmentAOService.getSubTeamOfTeam(teamId);
        List<Map<String, Object>> data = Lists.newArrayList();
        teams.forEach(team -> {
            Map<String, Object> teamMap = Maps.newHashMap();
            teamMap.put("name", team.getGroupName());
            teamMap.put("id", "t_" + team.getID());
            teamMap.put("parent", "t_" + StringUtils.defaultIfEmpty(team.getParent(), "0"));
            teamMap.put("type", "team");

            data.add(teamMap);
            int teamid = team.getID();
            List<StruGroupOfEmployee> list = Lists.newArrayList(strEmployeeService.getDirectStruGroupOfEmployeeByTeamId(teamid));
            list.forEach(e -> {
                Map<String, Object> employeeMap = Maps.newHashMap();
                StrEmployee employee = e.getEmployee();
                employeeMap.put("name", employee.getEmployeeName());
                employeeMap.put("id", e.getID());
                employeeMap.put("parent", "t_" + team.getID());
                employeeMap.put("type", "user");
                employeeMap.put("jiraUserKey", employee.getJiraUserKey());

                RoleEntity roleEntity = strEmployeeService.getRoleByEmployeeAndTeam(teamid, e.getEmployee().getID());
                employeeMap.put("roleId", roleEntity.getID());
                employeeMap.put("roleName", roleEntity.getName());
                data.add(employeeMap);
            });

        });
        TeamProjectMember[] unMappedMemberByTeamIdAndProject = teamProjectMemberService.getUnMappedMemberByTeamId(teamId);
        if (unMappedMemberByTeamIdAndProject.length > 0) {
            Map<String, Object> unMappedRoot = Maps.newHashMap();
            unMappedRoot.put("name", "未映射成员");
            unMappedRoot.put("id", "unmappedroot");
            unMappedRoot.put("parent", "0");
            unMappedRoot.put("type", "unmappedroot");
            data.add(unMappedRoot);
            UserManager userManager = ComponentAccessor.getUserManager();
            for (TeamProjectMember teamProjectMember : unMappedMemberByTeamIdAndProject) {
                String jiraUserKey = teamProjectMember.getJiraUserKey();
                ApplicationUser userByKey = userManager.getUserByKey(jiraUserKey);
                Map<String, Object> unMappedMember = Maps.newHashMap();
                unMappedMember.put("name", userByKey.getName());
                unMappedMember.put("jiraUserKey", jiraUserKey);
                unMappedMember.put("parent", "unmappedroot");
                unMappedMember.put("type", "unmappedmember");
                data.add(unMappedMember);
            }
        }

        return Response.ok(data).build();

    }


    @GET
    @Path("controlPower")
    public Response controlPower(@Context HttpServletRequest request) {
        Map<Integer, Boolean> powerInfo = (Map<Integer, Boolean>) request.getAttribute("powerInfo");
        Map<String, Boolean> map = new HashMap<>();
        Set<Integer> integers = powerInfo.keySet();
        for (Integer integer : integers) {
            PowerEntity power = powerService.getById(integer);
            map.put(power.getDescription(), powerInfo.get(integer));
        }
        return Response.ok(map).build();
    }

    private final TeamProjectMemberService teamProjectMemberService;

    /**
     * 团队映射
     *
     * @return
     */
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Path("mappingTeam")
    public Response mappingTeam(@FormParam("teamId") Integer teamId,
                                @FormParam("userkey") String userkey,
                                @FormParam("projectkey") String projectkey) {

        List<StruGroupOfEmployee> struGroupOfEmployees = strEmployeeService.getStruGroupOfEmployeeByTeamId(teamId);
        boolean isExist = false;
        for (StruGroupOfEmployee struGroupOfEmployee : struGroupOfEmployees) {
            StrEmployee employee = struGroupOfEmployee.getEmployee();
            if (employee != null) {
                String jiraUserKey = employee.getJiraUserKey();
                if (StringUtils.isNotEmpty(jiraUserKey)) {
                    if (jiraUserKey.equals(userkey)) { // 如果已经存在了，则不处理
                        isExist = true;
                        break;
                    }
                }
            }
        }
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        Project project = projectManager.getProjectByCurrentKey(projectkey);

        int projectId = project.getId().intValue();
        isExist = teamProjectMemberService.isExist(teamId, projectId, userkey);

        if (isExist) {
            return Response.ok().build();
        } else { // 不存在，则放入未关联项目里

            teamProjectMemberService.save(teamId, projectId, userkey, null, false);
            return Response.ok().build();
        }

    }

}

