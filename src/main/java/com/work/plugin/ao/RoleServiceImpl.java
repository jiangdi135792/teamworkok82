package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.google.common.collect.Lists;
import com.work.plugin.util.license.license.GlobalConfig;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by work on 2022/3/15.
 */
public class RoleServiceImpl implements RoleService {
    private final ActiveObjects ao;
    private final MemberAOService memberAOService;
    private final ProjectRoleManager projectRoleManager;
    public RoleServiceImpl(ActiveObjects ao, MemberAOService memberAOService, ProjectManager projectManager, ProjectRoleManager projectRoleManager) {
        this.ao = ao;
        this.memberAOService = memberAOService;
        this.projectRoleManager = projectRoleManager;
    }

    @Override
    public RoleEntity getId(Integer id) {
        return ao.get(RoleEntity.class, id);
    }

    @Override
    public List<RoleEntity> getAll() {
        return Arrays.asList(ao.find(RoleEntity.class));
    }

    @Override
    public List<RoleEntity> getTeamRoles() {
        RoleEntity[] roleEntities = ao.find(RoleEntity.class, "TYPE = 2");
        return Lists.newArrayList(roleEntities);
    }

    @Override
    public String getPowerByRoleId(Integer id) {
        StrPowerRole[] strPowerRoles = ao.find(StrPowerRole.class, String.format(" ROLE_ENTITY_ID = '%d' ", id));
        String powerString = null;
        for (StrPowerRole strPowerRole : strPowerRoles) {
            powerString += strPowerRole.getPowerEntity().getUrl();
        }
        return powerString;
    }

    @Override
    public RoleEntity creatRole(String name, Integer type,String desc,Integer order) {
        //type=1  组织机构角色  =2 团队角色
        RoleEntity roleEntity = null;
        RoleEntity[] roleEntities = ao.find(RoleEntity.class, String.format(" NAME = '%s' ", name));
        if (roleEntities.length == 0) {
            roleEntity = ao.create(RoleEntity.class);
            roleEntity.setName(name);
            roleEntity.setOrder(order);
            roleEntity.setDesc(desc);
            roleEntity.setType(type);
            roleEntity.setStatus(1);
            roleEntity.setParentId(0);
            roleEntity.save();
            return roleEntity;
        } else {
            //exited
            return roleEntities[0];
        }
    }

    @Override
    public void maintainRelationshipRoleAndPower(RoleEntity roleEntity, PowerEntity powerEntity) {
        StrPowerRole[] strPowerRoles = ao.find(StrPowerRole.class, String.format(" POWER_ENTITY_ID = '%d' AND  ROLE_ENTITY_ID = '%d'", powerEntity.getID(), roleEntity.getID()));
        if (strPowerRoles.length == 0) {
            StrPowerRole strPowerRole = ao.create(StrPowerRole.class);
            strPowerRole.setPowerEntity(powerEntity);
            strPowerRole.setRoleEntity(roleEntity);
            strPowerRole.save();
        }
    }

    @Override
    public void setMenuPower(RoleEntity roleEntity, OrganizationEntity menu) {
        MemberEntity[] memberEntities = ao.find(MemberEntity.class, String.format("USER_KEY = '%s' AND MENU_ID = '%s' ", roleEntity.getName(), menu.getMenuNameid()));
        if (memberEntities.length == 0) {
            memberAOService.create(menu.getID(), roleEntity.getName(),menu.getMenuNameid(), 2);
        }
    }

    @Override
    public RoleEntity[] getOrgParentRole(String loginUserName) {
        RoleEntity[] roleEntities;
       /* if (loginUserName.equals("admin")){
            roleEntities = ao.find(RoleEntity.class, String.format("STATUS = '%d' AND TYPE = '%d' ", 1, 1));
        }else {
        StrEmployee[] strEmployees = ao.find(StrEmployee.class, String.format("JIRA_USER_KEY = '%s' ", loginUserName));
        StrEmployeeOfRole[] strEmployeeOfRoles = ao.find(StrEmployeeOfRole.class, String.format("EMPLOYEE_ID = '%s' ", strEmployees[0].getID()));
        List<RoleEntity> collects = Stream.of(strEmployeeOfRoles).map(s -> ao.get(RoleEntity.class,s.getRole().getID())).collect(Collectors.toList());
            roleEntities =new RoleEntity[collects.size()];
            for (int i = 0; i < collects.size(); i++) {
                roleEntities[i] = collects.get(i);
            }
        }*/
        roleEntities = ao.find(RoleEntity.class, String.format("STATUS = '%d' AND TYPE = '%d' ", 1, 1));
        return roleEntities;

    }

    @Override
    public RoleEntity[] getAllDepRole() {
        return ao.find(RoleEntity.class, String.format("STATUS = '%d' AND TYPE = '%d' ", 1, 2));
    }

    @Override
    public List<String> getMenuPowerByRoleId(String roleName) {
        MemberEntity[] memberEntities = ao.find(MemberEntity.class, String.format("USER_KEY = '%s' ", roleName));
        List<String> list = new ArrayList<>();
        Stream.of(memberEntities)
                .forEach(s -> list.add(s.getMenuId()));
        return list;
    }

    @Override
    public void updateMenuPower(String roleName, String menuName, boolean status) {
        if (status){
            RoleEntity[] roleEntities = ao.find(RoleEntity.class, String.format("NAME = '%s' ", roleName));
            OrganizationEntity[] organizationEntities = ao.find(OrganizationEntity.class, String.format("MENU_NAMEID = '%s' ", menuName));
            this.setMenuPower(roleEntities[0],organizationEntities[0]);
        }else {
        MemberEntity[] memberEntities = ao.find(MemberEntity.class, String.format("USER_KEY = '%s' AND MENU_ID = '%s' ", roleName, menuName));
        Stream.of(memberEntities).forEach(s -> ao.delete(s));
        }
    }

    @Override
    public void setRoleSuperior(RoleEntity roleEntity, RoleEntity superiorRole) {
        roleEntity.setParentId(superiorRole.getID());
        roleEntity.save();
    }

    @Override
    public List<RoleEntity> getSuperByRole(RoleEntity roleEntity) {
        List<RoleEntity> roleEntityList = new ArrayList<>();
        Integer  parentId = roleEntity.getParentId();
        while (true) {
            if (parentId != 0) {
                RoleEntity roleEntity1 = ao.get(RoleEntity.class, parentId);
                Integer parentId1 = roleEntity1.getParentId();
                roleEntityList.add(roleEntity1);
                parentId = parentId1;
            } else {
                roleEntityList.add(roleEntity);
                break;
            }
        }
        Set<RoleEntity> roleEntitySet =new HashSet<>();
        for (RoleEntity roleEntity1:roleEntityList){
            roleEntitySet.add(roleEntity1);
        }
        List<RoleEntity> newRoleEntities =new ArrayList<>(roleEntitySet);
        return newRoleEntities;
    }
    @Override
    public List<RoleEntity> getLowerByRole(RoleEntity roleEntity) {
        List<RoleEntity> roleEntityList = new ArrayList<>();
        RoleEntity[] roleEntities = ao.find(RoleEntity.class, String.format("PARENT_ID = '%d' ", roleEntity.getID()));
        if (roleEntities.length != 0) {
            for (RoleEntity roleE:roleEntities){
                roleEntityList.add(roleE);
            }
            return roleEntityList;
        }else {
            return null;
        }
    }

    public void updateRoles(List<Integer> roleIds, Integer teamId, Integer employeeId) {
        // 先删�??
        ao.deleteWithSQL(StrEmployeeOfRole.class,
                String.format("%s = ? AND %s = ?", "EMPLOYEE_ID", "TEAM_ID"),
                employeeId, teamId);
        // 再插�??
        roleIds.forEach(roleId -> {
            StrEmployeeOfRole strEmployeeOfRole = ao.create(StrEmployeeOfRole.class);
            strEmployeeOfRole.setTeamId(teamId);
            RoleEntity roleEntity = ao.get(RoleEntity.class, Integer.valueOf(roleId));
            strEmployeeOfRole.setRole(roleEntity);
            StrEmployee strEmployee = ao.get(StrEmployee.class, employeeId);
            strEmployeeOfRole.setEmployee(strEmployee);
            strEmployeeOfRole.save();
        });
    }
    public SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    @Override
    public Integer updateRole(Integer roleId, Integer teamId, Integer employeeId,Integer teamemployeeId) {
//        Integer returnTeamEmployeeId = null;
//        if (teamemployeeId == null) {
//            StruGroupOfEmployee struGroupOfEmployee = ao.create(StruGroupOfEmployee.class);
//            struGroupOfEmployee.setCreateDate(format.format(new Date()));
//            struGroupOfEmployee.setEmployee(ao.get(StrEmployee.class, employeeId));
//            struGroupOfEmployee.setGroup(ao.get(DepartmentEntity.class, teamId));
//            struGroupOfEmployee.setPostType(1);
//            struGroupOfEmployee.save();
//            returnTeamEmployeeId = struGroupOfEmployee.getID();
//            teamemployeeId =  employeeId;
//        }

        StrEmployeeOfRole[] strEmployeeOfRoles = ao.find(StrEmployeeOfRole.class,
                String.format("%s = ? AND %s = ?", "EMPLOYEE_ID", "TEAM_ID"),
                employeeId, teamId);
        if (strEmployeeOfRoles.length == 1) { // 更新
            StrEmployeeOfRole strEmployeeOfRole = strEmployeeOfRoles[0];
            RoleEntity roleEntity = ao.get(RoleEntity.class, roleId);
            strEmployeeOfRole.setRole(roleEntity);
            strEmployeeOfRole.save();
        } else {
            if (strEmployeeOfRoles.length > 1) { // 如果有多条数据，有冗余数据。先删除，新建
                ao.deleteWithSQL(StrEmployeeOfRole.class,
                        String.format("%s = ? AND %s = ?", "EMPLOYEE_ID", "TEAM_ID"),
                        employeeId, teamId);
            }

            StrEmployee strEmployee = ao.get(StrEmployee.class, employeeId);
            if (strEmployee != null) {
                StrEmployeeOfRole strEmployeeOfRole = ao.create(StrEmployeeOfRole.class);
                strEmployeeOfRole.setTeamId(teamId);
                RoleEntity roleEntity = ao.get(RoleEntity.class, Integer.valueOf(roleId));
                strEmployeeOfRole.setRole(roleEntity);
                strEmployeeOfRole.setEmployee(strEmployee);
                strEmployeeOfRole.save();
            }
        }
        return null;
    }

    @Override
    public List<StrEmployeeOfRole> getRoleByEmployeeId(Integer employeeId) {
        StrEmployeeOfRole[] strEmployeeOfRoles = ao.find(StrEmployeeOfRole.class, "EMPLOYEE_ID = " + employeeId);
        return Lists.newArrayList(strEmployeeOfRoles);
    }


    @Override
    public RoleEntity getDefaultTeamRole(){
        RoleEntity[] roleEntities = ao.find(RoleEntity.class, String.format("NAME = 'Developer' AND TYPE = 2"));
        if (roleEntities.length > 0) {
            return roleEntities[0];
        } else {
            return null;
        }
    }
    @Override
    public void setRoleToEmployee(RoleEntity roleEntity, StrEmployee strEmployee) {
        StrEmployeeOfRole[] strEmployeeOfRoles = ao.find(StrEmployeeOfRole.class, String.format("EMPLOYEE_ID = '%d' AND ROLE_ID = '%d' ", strEmployee.getID(), roleEntity.getID()));
        if (strEmployeeOfRoles.length == 0){
        StrEmployeeOfRole strEmployeeOfRole = ao.create(StrEmployeeOfRole.class);
        strEmployeeOfRole.setEmployee(strEmployee);
        strEmployeeOfRole.setRole(roleEntity);
        strEmployeeOfRole.save();
        }
    }

    @Override
    public RoleEntity creatNewRole(String name,String desc,Integer order,Integer type) {
        RoleEntity[] roleEntities = ao.find(RoleEntity.class, String.format("NAME = '%s' ", name));
        RoleEntity roleEntity = null;
        if (roleEntities.length == 0) {
            String maxStr = String.valueOf(order).substring(0, 3) + "99";
            Integer newOder = getTheLargeWeigh(order);
            if (newOder == Integer.valueOf(maxStr)){//不能创建了
                return null;
            }
            roleEntity = ao.create(RoleEntity.class);
            roleEntity.setDesc(desc);
            roleEntity.setName(name);
            roleEntity.setOrder(newOder+1);
            roleEntity.setStatus(1);
            roleEntity.setParentId(0);
            roleEntity.setType(type);
            roleEntity.save();
        }else {
            roleEntity =roleEntities[0];
        }
        return roleEntity;
    }

    @Override
    public boolean delRole(String roleName, Integer type) {
        RoleEntity[] roleEntities = ao.find(RoleEntity.class, String.format("NAME = '%s' AND TYPE = '%d' ", roleName, type));
        if (roleEntities.length > 0) {
            ao.delete(roleEntities[0]);
        }
        MemberEntity[] memberEntities = ao.find(MemberEntity.class, String.format("USER_KEY = '%s' ", roleName));
        Stream.of(memberEntities).forEach(s -> ao.delete(s));
        return true;
    }

    @Override
    public MemberEntity getMenuDetailPower(String roleName, String menuName) {
        MemberEntity[] memberEntities = ao.find(MemberEntity.class, String.format("MENU_ID = '%s' AND USER_KEY = '%s' ", menuName, roleName));
        return memberEntities[0];
    }

    @Override
    public void setOrgRoleMenuPower(RoleEntity roleEntity, OrganizationEntity organizationEntity, Map<PowerEntity, Boolean> map,Integer olu) {
        MemberEntity[] memberEntities = ao.find(MemberEntity.class, String.format("MENU_ID = '%s' AND USER_KEY = '%s' ", organizationEntity.getMenuNameid(), roleEntity.getName()));
        String powerList = memberEntities[0].getPowerList();
        //String[] split = powerList.split("\\|");
            if (StringUtils.isBlank(powerList)) {
                powerList = "";
                Set<PowerEntity> powerEntities = map.keySet();
                for (PowerEntity powerEntity : powerEntities) {
                    int id = powerEntity.getID();
                    Boolean aBoolean = map.get(powerEntity);
                    String s = null;
                    if (aBoolean) {
                        s = "1";
                    } else {
                        s = "0";
                    }
                    powerList += (id + ":" + s + ";");
                }
                if (olu != -3) {
                    powerList += "|";
                }
                memberEntities[0].setPowerList(powerList);
                memberEntities[0].save();
            } else {
                String[] split = powerList.split("\\|");
                if (split.length < 3){
                Set<PowerEntity> powerEntities = map.keySet();
                for (PowerEntity powerEntity : powerEntities) {
                    int id = powerEntity.getID();
                    Boolean aBoolean = map.get(powerEntity);
                    String s = null;
                    if (aBoolean) {
                        s = "1";
                    } else {
                        s = "0";
                    }
                    powerList += (id + ":" + s + ";");
                }
                if (olu != -3) {
                    powerList += "|";
                }
                memberEntities[0].setPowerList(powerList);
                memberEntities[0].save();
              }else {
                    if (olu == -1){
                        if (split[0].split(";").length != map.size()){
                            String powerLi = "";
                            Set<PowerEntity> powerEntities = map.keySet();
                            for (PowerEntity powerEntity : powerEntities) {
                                int id = powerEntity.getID();
                                Boolean aBoolean = map.get(powerEntity);
                                String s = null;
                                if (aBoolean) {
                                    s = "1";
                                } else {
                                    s = "0";
                                }
                                powerLi += (id + ":" + s + ";");
                            }
                            powerLi += "|";
                            powerLi += split[1];
                            powerLi += "|";
                            powerLi += split[2];
                            memberEntities[0].setPowerList(powerLi);
                            memberEntities[0].save();
                        }
                    }else if (olu == -2){
                        if (split[1].split(";").length != map.size()){
                            String newlll = "";
                            String powerLi = "";
                            Set<PowerEntity> powerEntities = map.keySet();
                            for (PowerEntity powerEntity : powerEntities) {
                                int id = powerEntity.getID();
                                Boolean aBoolean = map.get(powerEntity);
                                String s = null;
                                if (aBoolean) {
                                    s = "1";
                                } else {
                                    s = "0";
                                }
                                powerLi += (id + ":" + s + ";");
                            }
                            newlll += split[0];
                            newlll += "|";
                            newlll += powerLi;
                            newlll += "|";
                            newlll += split[2];
                            memberEntities[0].setPowerList(newlll);
                            memberEntities[0].save();
                        }
                    }else if (olu == -3){
                        if (split[2].split(";").length != map.size()){
                            String newlll = "";
                            String powerLi = "";
                            Set<PowerEntity> powerEntities = map.keySet();
                            for (PowerEntity powerEntity : powerEntities) {
                                int id = powerEntity.getID();
                                Boolean aBoolean = map.get(powerEntity);
                                String s = null;
                                if (aBoolean) {
                                    s = "1";
                                } else {
                                    s = "0";
                                }
                                powerLi += (id + ":" + s + ";");
                            }
                            newlll += split[0];
                            newlll += "|";
                            newlll += split[1];
                            newlll += "|";
                            newlll += powerLi;
                            memberEntities[0].setPowerList(newlll);
                            memberEntities[0].save();
                        }
                    }
                }
            }


    }

    @Override
    public void changeDetailPower(String role, String menuName, String str) {
        if (role.equals("System Administrator")){
        }else {
            //ggg,bmryfb,;编辑;创建;删除;|自身权限;1;1;1;|下级权限;1;1;1;|全部权限;1;1;1;| saveChange ;|
            String[] split = str.split("\\|");
            int length1 = split.length;
            String[] split1 = split[0].split(";");
            final String[] strs = {""};
            IntStream.range(1,length1-1).forEach(s -> {
                String[] split2 = split[s].split(";");
                int length2 = split2.length;
                IntStream.range(1,length2).forEach(ss ->{
                    PowerEntity powerEntity = ao.get(PowerEntity.class, Integer.valueOf(split1[ss]));
                    strs[0] +=  powerEntity.getID()+":"+split2[ss]+";";
                });
                if (s != length1-2){
                    strs[0] += "|";
                }
            });
            MemberEntity[] memberEntities = ao.find(MemberEntity.class, String.format("MENU_ID = '%s' AND USER_KEY = '%s' ", menuName, role));
            memberEntities[0].setPowerList(strs[0]);
            memberEntities[0].save();
        }
    }

    @Override
    public void parseMenuDetailPower(String role, String menuName) {
        MemberEntity[] memberEntities = ao.find(MemberEntity.class, String.format("MENU_ID = '%s' AND USER_KEY = '%s' ", menuName, role));
        String powerList = memberEntities[0].getPowerList();
        //2:0;4:1;3:1;|2:1;4:0;3:1;|2:1;4:1;3:1;
        String[] split = powerList.split("\\|");
        //TODO
    }

    @Override
    public Map<PowerEntity, Boolean> initMenuPower(OrganizationEntity organizationEntity) {
        Map<PowerEntity,Boolean> map = new HashMap<>();
        OrganizationAndPowerEntity[] organizationAndPowerEntity = organizationEntity.getOrganizationAndPowerEntity();
        Arrays.stream(organizationAndPowerEntity).forEach(s ->{
            map.put(s.getPower(),false);
        });
        return map;
    }

    @Override
    public RoleEntity getRoleByName(String roleName) {
        RoleEntity[] roleEntities = ao.find(RoleEntity.class, String.format("NAME = '%s' ", roleName));
        if (roleEntities.length != 0){
            return roleEntities[0];
        }else {
            return null;
        }
    }

    @Override
    public void delOrgRoleMenuPower(RoleEntity roleEntity, OrganizationEntity organizationEntity) {
        MemberEntity[] memberEntities = ao.find(MemberEntity.class, String.format("MENU_ID = '%s' AND USER_KEY = '%s' ", organizationEntity.getMenuNameid(), roleEntity.getName()));
        Arrays.stream(memberEntities).forEach( s ->{ao.delete(s);});
    }

    private Integer getTheLargeWeigh(Integer weight){
        String substring = String.valueOf(weight).substring(0, 3);
        String minStr = substring + "00";
        String maxStr = substring + "99";
        RoleEntity[] roleEntities = ao.find(RoleEntity.class);
        OptionalInt max = Arrays.stream(roleEntities).filter(s -> s.getOrder() >= Integer.valueOf(minStr) && s.getOrder() <= Integer.valueOf(maxStr)).mapToInt(RoleEntity::getOrder).max();
        return max.getAsInt();
    }
    @Override
    public void createProjectRoleOfJiraInnerByCustomRole(RoleEntity roleEntity){
        ProjectRole projectRole =new ProjectRoleImpl(roleEntity.getName(),roleEntity.getDesc());
        projectRoleManager.createRole(projectRole);
    }
    @Override
    public void deleteProjectRoleOfJiraInnerByCustomRole(RoleEntity roleEntity){
        projectRoleManager.deleteRole(projectRoleManager.getProjectRole(roleEntity.getName()));
    }

    @Override
    public void maintainData(String... strings) {
        GlobalConfig.printDebug("00000000000000000000000000000000000_1");
        Arrays.stream(strings).forEach(s -> {
            RoleEntity[] roleEntities = ao.find(RoleEntity.class, Query.select().where(String.format(" NAME = '%s' ", s)));
            if (roleEntities.length > 0) {
                RoleEntity roleEntity = roleEntities[0];
                StrEmployeeOfRole[] strEmployeeOfRoles = ao.find(StrEmployeeOfRole.class, Query.select().where(String.format("ROLE_ID = '%d' ", roleEntity.getID())));
        GlobalConfig.printDebug("00000000000000000000000000000000000_2");
                if (strEmployeeOfRoles.length > 0) {
                    Arrays.stream(strEmployeeOfRoles).forEach(strEmployeeOfRole -> ao.delete(strEmployeeOfRole));
                }
                StrPowerRole[] strPowerRoles = ao.find(StrPowerRole.class, Query.select().where(String.format("ROLE_ENTITY_ID = '%d' ", roleEntity.getID())));
        GlobalConfig.printDebug("00000000000000000000000000000000000_3");
                if (strPowerRoles.length > 0) {
                    Arrays.stream(strPowerRoles).forEach(strPowerRole -> ao.delete(strPowerRole));
                }
                MemberEntity[] memberEntities = ao.find(MemberEntity.class, Query.select().where(String.format("USER_KEY = '%s' ", roleEntity.getName())));
        GlobalConfig.printDebug("00000000000000000000000000000000000_4");
                if (memberEntities.length > 0) {
                    Arrays.stream(memberEntities).forEach(memberEntity -> ao.delete(memberEntity));
                }
                ao.delete(roleEntity);
        GlobalConfig.printDebug("00000000000000000000000000000000000_5");
            }
        });
    }

}
