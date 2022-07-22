package com.work.plugin.rest;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.work.plugin.ao.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by work on 2022/3/26.
 */
@Path("/power")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class PowerManagementResource {
    private  final RoleService roleService;
    private  final JiraAuthenticationContext jiraAuthenticationContext;
    private  final StrEmployeeService strEmployeeService;
    private final OrganizationAOService organizationAOService;
    private final MemberAOService memberAOService;
    private  final CrowdService crowdService;
    private final PowerService powerService;
    private final  StrPowerRoleService strPowerRoleService;
    @POST
    @Path("allOrgParentRole")
    public Response getAllOrgParentRole(){
        ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        if (loggedInUser!=null){
        RoleEntity[] orgParentRole = roleService.getOrgParentRole(loggedInUser.getKey());
        List<RoleBean> roleBeans = Stream.of(orgParentRole)
                .map(e -> new RoleBean(e))
                .collect(Collectors.toList());
        List<RoleBean> collect = roleBeans.stream().sorted(Comparator.comparing(RoleBean::getOrder)).collect(Collectors.toList());
            return Response.ok(collect).build();
        }else {
            return  Response.ok(Response.Status.NO_CONTENT).build();
        }
    }
    @POST
    @Path("allProRole")
    public Response getAllDepRole(){
        RoleEntity[] orgParentRole= roleService.getAllDepRole();
        List<RoleBean> roleBeans = Stream.of(orgParentRole)
                .map(e -> new RoleBean(e))
                .collect(Collectors.toList());
        return Response.ok(roleBeans).build();
    }

    /**
     *
     * @param roleName 根据角色名字
     * @return 该角色的菜单权限
     */
    @GET
    @Path("getMenuPermitByRoleName/{roleName}")
    public Response getMenuPermitById(@PathParam("roleName") String roleName){
        String loginUserName = jiraAuthenticationContext.getLoggedInUser().getKey();
        String loginRole = null;
        Map<Integer,Object> returnMap=new HashMap<>();
        List<String> menuPowerByRoleId=roleService.getMenuPowerByRoleId(roleName);
        List<String> orgRoleByUserName = strEmployeeService.getOrgRoleByUserName(loginUserName);
        boolean contains = orgRoleByUserName.contains(roleName);
        //boolean contains =roleName.equals(loginRole);
        if (contains){
            returnMap.put(1,false);
        }else {
            returnMap.put(1,true);
        }
        if(roleName.equals("System Administrator")){
            returnMap.put(1,false);
        }
        OrganizationEntity[] all = organizationAOService.all();
        List<String> collect = Stream.of(all).map(s -> s.getMenuNameid()).collect(Collectors.toList());
        collect.removeAll(menuPowerByRoleId);
        returnMap.put(2,menuPowerByRoleId);
        returnMap.put(3,collect);
        return Response.ok(returnMap).build();
    }

    @GET
    @Path("getRoleOfLoginUser")
    public Response getRoleOfLoginUser(){
        String loginUserName = jiraAuthenticationContext.getLoggedInUser().getKey();
        List<String> orgRoleByUserName = strEmployeeService.getOrgRoleByUserName(loginUserName);
        return Response.ok().build();
    }
    @POST
    @Path("delMenuPower/{roleName}/{menuName}")
    public Response delMenuPower(@PathParam("roleName") String roleName,@PathParam("menuName") String menuName){
        roleService.updateMenuPower(roleName,menuName,false);
        roleService.delOrgRoleMenuPower(roleService.getRoleByName(roleName),organizationAOService.get(menuName));
        return  Response.ok().build();
    }
    @POST
    @Path("addMenuPower/{roleName}/{menuName}")
    public Response addMenuPower(@PathParam("roleName") String roleName,@PathParam("menuName") String menuName){
        if (StringUtils.isNotBlank(roleName) && StringUtils.isNotBlank(menuName)){
        roleService.updateMenuPower(roleName,menuName,true);
        //初始化 默认为false
        OrganizationEntity organizationEntity = organizationAOService.get(menuName);
        Map<PowerEntity,Boolean> map =roleService.initMenuPower(organizationEntity);
        roleService.setOrgRoleMenuPower(roleService.getRoleByName(roleName), organizationEntity,map,-1);
        roleService.setOrgRoleMenuPower(roleService.getRoleByName(roleName), organizationEntity,map,-2);
        roleService.setOrgRoleMenuPower(roleService.getRoleByName(roleName), organizationEntity,map,-3);
        return  Response.ok().build();
        }
        return  Response.status(Response.Status.BAD_REQUEST).build();
    }
    @GET
    @Path("getLastWeight/{RoleId}")
    public Response getLastWeight(@PathParam("RoleId") String RoleId){
        RoleEntity roleEntity = roleService.getId(Integer.valueOf(RoleId));
        Map map=new HashMap();
        map.put("weights",roleEntity.getOrder());
        return  Response.ok(map).build();
    }

    @POST
    @Path("saveNewRole/{rolename}/{roleDes}/{weight}/{type}")
    public Response saveNewRole(@PathParam("roleDes") String roleDes,@PathParam("rolename") String rolename,@PathParam("weight") String weight,@PathParam("type") String type){
        RoleEntity roleEntity = roleService.creatNewRole(rolename, roleDes, Integer.valueOf(weight), Integer.valueOf(type));
        if (type.equals("2")){
            roleService.createProjectRoleOfJiraInnerByCustomRole(roleEntity);
        }
        return  Response.ok(roleEntity != null?new RoleBean(roleEntity):Response.Status.FORBIDDEN).build();
    }
    @POST
    @Path("delRole/{roleName}/{type}")
    public Response delRole(@PathParam("roleName") String roleName,@PathParam("type") String type){
        /*if (type.equals("2")){//仅仅删 插件的 不删 jira
            roleService.deleteProjectRoleOfJiraInnerByCustomRole(roleService.getRoleByName(roleName));
        }*/
        roleService.delRole(roleName, Integer.valueOf(type));
        return Response.ok().build();
    }
    @GET
    @Path("getDetailP/{roleName}/{menuName}")
    public Response getDetailP(@PathParam("roleName") String roleName,@PathParam("menuName") String menuName){
        System.out.println("roleName" + roleName + "menuName" + menuName);
        //获取当前菜单 内 有哪些 可操作权限
        MemberEntity memberEntity = memberAOService.getByRoleAndMenu(roleName, menuName);
        if (memberEntity != null) {
            List<String> idlist= new ArrayList();
            String powerList = memberEntity.getPowerList();
            Map<String, Map<String, Object>> map = new HashMap<>();
            String[] split1 = powerList.split("\\|");
            IntStream.range(0, split1.length).forEach(num -> {
                Map innerMap = new HashMap();
                String[] split = split1[num].split(";");
                Stream.of(split).forEach(ss -> {
                    int i1 = ss.indexOf(":");
                    int length = ss.length();
                    String substring1 = ss.substring(0, i1);
                    idlist.add(substring1);
                    PowerEntity byId = powerService.getById(Integer.valueOf(substring1));
                    char c = ss.charAt(length - 1);
                    innerMap.put(byId.getDescription(), Integer.valueOf(String.valueOf(c)) == 0 ? false : true);
                });
                map.put("domain" + num, innerMap);
            });
            Map<String,Object> hiddenMap = new HashMap();
            idlist.stream().forEach(s -> hiddenMap.put(powerService.getById(Integer.valueOf(s)).getDescription(),s));
            map.put("hiddens",hiddenMap);
            return Response.ok(map).build();
        } else {
            return Response.ok(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("saveChangeP")
    @AnonymousAllowed
    public Response saveChangeP(String str){
        //ggg,bmryfb,自身权限;1;1;1;|下级权限;1;1;1;|全部权限;1;1;1;| saveChange ;|
        String[] split = str.split(",");
        roleService.changeDetailPower(split[0],split[1],str);
        return Response.ok().build();
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
        //System.out.println(map);
        return map;
    }

    @GET
    @Path("getProjectRolePower/{id}")
    public Response getProjectRolePower(@PathParam("id") String id) {
        List<PowerEntity> allPowerOfSelf = strPowerRoleService.getAllPowerOfSelf(Integer.valueOf(id));
        List<PowerEntity> allPowewr = strPowerRoleService.getAllPowewr();
        List<ProRolePowerBean> proRolePowerBeans = new LinkedList<>();
        allPowewr.removeAll(allPowerOfSelf);
        allPowerOfSelf.stream().forEach(powerEntity -> {
            proRolePowerBeans.add(new ProRolePowerBean(powerEntity.getID(),true,powerEntity.getDescription()));
        });
        allPowewr.stream().forEach(powerEntity -> {
            proRolePowerBeans.add(new ProRolePowerBean(powerEntity.getID(),false,powerEntity.getDescription()));
        });
        return Response.ok(proRolePowerBeans).build();
    }
    @GET
    @Path("changeProPermission/{proRoleId}/{perm}/{type}")
    public Response changeProPermission(@PathParam("proRoleId") String id,@PathParam("perm") String perm,@PathParam("type") String type) {
        if (Boolean.valueOf(type)) {
            RoleEntity roleEntity = roleService.getId(Integer.valueOf(id));
            PowerEntity byDesc = powerService.getByDesc(perm);
            strPowerRoleService.createRelationOfPowerAndRole(roleEntity, byDesc);
            return Response.ok().build();
        } else {
            RoleEntity roleEntity = roleService.getId(Integer.valueOf(id));
            PowerEntity byDesc = powerService.getByDesc(perm);
            strPowerRoleService.delRelationOfPowerAndRole(roleEntity, byDesc);
            return Response.ok().build();
        }
    }
}