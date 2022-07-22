package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.naming.ldap.LdapContext;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by work on 2022/1/24.
 */
@AllArgsConstructor
public class StrEmployeeExtendServiceImpl implements StrEmployeeExtendService {
    private final ActiveObjects ao;
    private StrEmployeeService strEmployeeService;
    private LdapSyncTimeService ldapSyncTimeService;
    private CrowdDirectoryService crowdDirectoryService;
    private StrOrganizeService strOrganizeService;
    private DepartmentAOService departmentAOService;
    private CrowdService crowdService;
    public StrEmployeeExtendServiceImpl(ActiveObjects ao) {
        this.ao=ao;
    }

    @Override
    public void updateInfo(long directoryId) {
        String updateTime = getNowTime();
        List<Directory> allDirectories = getActiveDirectory();
        if (directoryId == -1){ //check all directory
            checkStatusOfDirectory(allDirectories);
            checkExtendEmployee();
        }else { //begin  task  sync
            LdapContext init=null;
            for (Directory directory : allDirectories) {
                if (directory.getId() == directoryId) {
                    String[] returnedAtts=null;
                    String implementationClass=directory.getImplementationClass();
                    String LDAP_URL=directory.getValue("ldap.url");
                    String adminName=directory.getValue("ldap.userdn");
                    String userList=directory.getValue("ldap.user.dn");
                    String searchBase=directory.getValue("ldap.basedn");
                    String groupdn=directory.getValue("ldap.group.dn");
                    String searchObject=directory.getValue("ldap.user.filter");
                    String adminPassword=directory.getValue("ldap.password");
                    String LDAP_UniqueName=directory.getValue("ldap.user.username");
                    String OrganazitionAttrNameOfAd=null;
                    String departmentAttrNameOfAd=null;
                    String UserUniqueIDAttribute=null;
                    String UserCreatTimeOfAd=null;
                    String UserChangeTimeOfAd=null;
                    if (implementationClass.equals("com.atlassian.crowd.directory.OpenLDAP")){
                        OrganazitionAttrNameOfAd="o";
                        departmentAttrNameOfAd="departmentNumber";
                        UserUniqueIDAttribute="entryUUID";
                        UserCreatTimeOfAd="createTimestamp";
                        UserChangeTimeOfAd="modifyTimestamp";
                        returnedAtts=new String[]{LDAP_UniqueName, UserUniqueIDAttribute, "mail", "telephoneNumber", UserChangeTimeOfAd, UserCreatTimeOfAd, departmentAttrNameOfAd, OrganazitionAttrNameOfAd};
                    }else if (implementationClass.equals("com.atlassian.crowd.directory.MicrosoftActiveDirectory")){
                        OrganazitionAttrNameOfAd="company";
                        departmentAttrNameOfAd="department";
                        UserUniqueIDAttribute="objectGUID";
                        UserCreatTimeOfAd="whenCreated";
                        UserChangeTimeOfAd="whenChanged";
                        returnedAtts=new String[]{LDAP_UniqueName, UserUniqueIDAttribute,"cn", "mail", "telephoneNumber", UserChangeTimeOfAd, UserCreatTimeOfAd, departmentAttrNameOfAd, OrganazitionAttrNameOfAd};
                    }
                    init = getInit(LDAP_URL, adminName, adminPassword);
//                    StrEmployee[] correspondingFieldsStrEmployees = strEmployeeService.getCorrespondingFieldsStrEmployees(directoryId);
//                    if (correspondingFieldsStrEmployees.length != 0) {
//                        for (StrEmployee strEmployee : correspondingFieldsStrEmployees) {
//                            String jiraUserKey = strEmployee.getJiraUserKey();
//                            try {
//                                String searchB = "";
//                                if (StringUtils.isBlank(groupdn)) {
//                                    searchB = searchBase;
//                                } else {
//                                    searchB = groupdn + "," + searchBase;
//                                }
//                                String searchf = "";
//                                searchf = LDAP_UniqueName + "=" + jiraUserKey;
//                                if (LDAP_UniqueName.equals("userPrincipalName")) {
//                                    if (jiraUserKey.contains("@")) {
//                                        searchf = LDAP_UniqueName + "=" + jiraUserKey;
//                                    } else {
//                                        int i = adminName.indexOf("@");
//                                        String substring = adminName.substring(i, adminName.length());
//                                        searchf = LDAP_UniqueName + "=" + jiraUserKey + substring;
//                                    }
//                                } else {
//
//                                }
//                                Map<String, Map<String, Object>> stringMapMap = new HashMap<>();
//                                if (userList.contains(",")) {
//                                    String[] split = userList.split(",");
//                                    int length = split.length;
//                                    for (int c = 0; c < length; c++) {
//                                        String searcha = "";
//                                        searcha = searchB;
//                                        String searchc = split[c] + "," + searcha;
//                                        Map<String, Map<String, Object>> stringMapMaps = SyncLdapImpl.searchInfoByFilter(init, searchc, searchf, new String[]{LDAP_UniqueName});
//                                        Set<String> strings = stringMapMaps.keySet();
//                                        stringMapMap.putAll(stringMapMaps);
//                                        if (strings.size() > 0) {
//                                            Iterator<String> it = strings.iterator();
//                                            while (it.hasNext()) {
//                                                String str = it.next();
//                                                stringMapMap.put(str, stringMapMaps.get(str));
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    stringMapMap = SyncLdapImpl.searchInfoByFilter(init, searchB, searchf, new String[]{LDAP_UniqueName});
//                                }
//                                if (stringMapMap.size() != 0) {
//                                    strEmployee.setLdapId(directoryId);
//                                    strEmployee.save();
//                                }
//                            } catch (NamingException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
                    //String loginAdUser=adminName.substring(0, adminName.indexOf("@")).toLowerCase();
                    Set<String> Domain_Names=new HashSet<>();//连接AD域   域中 LDAP_UniqueName Set集合
                    Map<String, Map<String, Object>> stringMapMap = getDomainInfo(groupdn, searchBase, userList, init, searchObject, returnedAtts);
                   // stringMapMap = tolower(stringMapMap, LDAP_UniqueName);
                    Domain_Names=stringMapMap.keySet();
                    //库中 jiraUserKey 集合
                    Set<String> Employee_jiraUserkeys=new HashSet<>();
                    List<StrEmployee> allEmployee=strEmployeeService.allAppoint(directoryId);
                    allEmployee.stream().forEach(s -> {
                        String jiraUserKey = s.getJiraUserKey();
                        if (StringUtils.isNotBlank(jiraUserKey)) {
                            Employee_jiraUserkeys.add(jiraUserKey);
                        }
                    });
                    ArrayList EmployeeList=new ArrayList(Employee_jiraUserkeys);
                    Set<String> differSet=new HashSet<String>();
                    differSet.addAll(Domain_Names);
                    differSet.removeAll(Employee_jiraUserkeys);

//在jira 中已经关联的--------------------------------------------------------------------------------------------------------------------------------------------------------------
                    for (int i=0; i < EmployeeList.size(); i++) {
                        String ExsitJiraUserKey=(String) EmployeeList.get(i);  //根据雇员中的jirauserkey 去域中 获取信息
                        StrEmployee[] strEmployees =strEmployeeService.getEmployeeByJiraUserKeyAndDirectoryId(ExsitJiraUserKey,directoryId);
                        StrEmployee strEmpl = strEmployees[0];
                        int Employeeid = strEmpl.getID(); //获取雇员ID
                        /*for (StrEmployee strEmployee : strEmployees) {
                            strEmpl=strEmployee;
                            Employeeid=strEmployee.getID();
                        }*/
                        Map<String, Object> stringStringMap=stringMapMap.get(ExsitJiraUserKey);
                        if (stringStringMap == null){
                            Map<String, Object> stringStringMap1=stringMapMap.get(ExsitJiraUserKey.toLowerCase());
                            if (stringStringMap1 != null){
                                StrEmployee employee = strEmployeeService.getEmployee(Employeeid);
                                employee.setJiraUserKey(ExsitJiraUserKey.toLowerCase());
                                employee.save();
                                continue;
                            }else {
                                StrEmployeeExtend[] strEmployeeExtends=ao.find(StrEmployeeExtend.class, String.format(" EMPLOYEE_ID  = '%d' ", Employeeid));
                                String sourcePart=null;
                                StrEmployeeExtend strEmployeeExtend=null;
                                if (strEmployeeExtends.length != 0 ){
                                    ao.delete(strEmployeeExtends[0]);
                                }
                                strEmpl.setJiraUserKey(null);
                                strEmpl.save();
                            }
//                                for (String string:differSet){
//                                    Map<String, Object> map=stringMapMap.get(string);
//                                    if (map.get(UserUniqueIDAttribute).equals(sourcePart)){
//                                        stringStringMap=map;
//                                        strEmpl.setJiraUserKey(string.toLowerCase());
//                                        strEmpl.save();
//                                        strEmployee.setLoginName(string);
//                                        strEmployee.save();
//                                        differSet.remove(string);
//                                    }
//
                        }else {
                            String company=(String) stringStringMap.get(OrganazitionAttrNameOfAd);
                            String objectGUID=(String) stringStringMap.get(UserUniqueIDAttribute);
                            String ADdepartment=(String) stringStringMap.get(departmentAttrNameOfAd);
                            //算出新的Hash 值
                            Long hash = returnHash(company, ExsitJiraUserKey);
                            //查看扩展表中有木有
                            //根据雇员ID查看同步表中的Hash值，是否变化
                            checkRelation(Employeeid,company,ADdepartment,directoryId,updateTime);
                            StrEmployeeExtend[] strEmployeeExtends=ao.find(StrEmployeeExtend.class, String.format(" EMPLOYEE_ID = '%d' ", Employeeid));
                            if (strEmployeeExtends.length == 0) {//扩展表中没有 同步到扩展表
                                create( hash, Employeeid, updateTime, ExsitJiraUserKey, objectGUID);
                                if (StringUtils.isNotBlank(company)) {
                                    if (StringUtils.isNotBlank(ADdepartment)) {
                                        //AD有公司，有部门,不管jira用户是否有公司和部门都已AD域中为主,维护部门与公司，部门与雇员关系
                                        StrOrganize strOrganize=strEmpl.getStrOrganize();
                                        int OrID=strOrganize.getID();
                                        if (strOrganize.getName().equals(company)) {
                                            StruGroupOfEmployee depart=strEmployeeService.getDepart(Employeeid);
                                            if (depart.getGroup().getGroupName().equals(ADdepartment)) {
                                            } else {
                                                DepartmentEntity departmentEnt=null;
                                                DepartmentEntity departInfo=departmentAOService.getDepartInfo(OrID, ADdepartment);
                                                if (departInfo != null) {
                                                    departmentEnt=departInfo;
                                                } else {
                                                    departmentEnt=departmentAOService.createByInfo(ADdepartment, (int) directoryId, OrID, 0, 0);
                                                }
                                                strEmployeeService.deleteRalationEmployeeAndDepart(Employeeid);
                                                strEmployeeService.maintainRelationGroupEmployee(strEmpl, departmentEnt, (int) directoryId, updateTime, (int) directoryId, 0);
                                            }
                                        } else {
                                            //不一样，创建公司名字 和 部门 ，同时，解除 原先的雇员和公司 ，雇员和部门关系  同事添加新的关系
                                            StrOrganize newCreateOrg=strOrganizeService.createByInfo(company,10000 ,directoryId, updateTime, 0, 0, "0", "0");
                                            DepartmentEntity newCreateDepart=departmentAOService.createByInfo(ADdepartment, (int) directoryId, newCreateOrg.getID(), 0, 0);
                                            strEmpl.setStrOrganize(newCreateOrg);
                                            strEmpl.save();
                                            strEmployeeService.deleteRalationEmployeeAndDepart(Employeeid);
                                            strEmployeeService.maintainRelationGroupEmployee(strEmpl, newCreateDepart, (int) directoryId, updateTime, (int) directoryId, 0);
                                        }

                                    } else {
                                        //有公司，无部门 查看雇员是否有公司，没有则创建，维护关系，有则，
                                        StrOrganize strOrganize=strEmpl.getStrOrganize();
                                        if (strOrganize != null) {
                                            //有公司
                                            String name=strOrganize.getName();
                                            if (!name.equals(company)) {
                                                //查看此公司是否存在 没有则创建，
                                                StrOrganize strOrganizes;
                                                StrOrganize byOrgName=strOrganizeService.getByOrgName(company);
                                                if (byOrgName != null) {
                                                    strOrganizes=byOrgName;
                                                } else {
                                                    strOrganizes=strOrganizeService.createByInfo(company, 10000,directoryId, updateTime, 0, 0, "0", "0");
                                                }
                                                strEmpl.setStrOrganize(strOrganizes);
                                                strEmpl.save();
                                                strEmployeeService.deleteRalationEmployeeAndDepart(Employeeid);
                                            }
                                        } else {
                                            //无公司 查看表中是否有公司，有  没有 创建
                                            StrOrganize strOrganizes;
//                                            StrOrganize byOrgName=strOrganizeService.getByOrgName(company);
//                                            if (byOrgName != null) {
//                                                strOrganizes=byOrgName;
//                                            } else {
//                                            }
                                            strOrganizes=strOrganizeService.createByInfo(company, 10000,directoryId, updateTime, 0, 0, "0", "0");
                                            strEmpl.setStrOrganize(strOrganizes);
                                            strEmpl.save();
                                        }
                                    }
                                } else {
                                    if (StringUtils.isNotBlank(ADdepartment)) {
                                        //无公司 有部门    查看表里是否有公司，有则维护部门与公司关系，部门与雇员关系,没有则查询其是否有上级，获取组织信息
                                        StrOrganize strOrganize=strEmpl.getStrOrganize();
                                        if (strOrganize != null) {
                                            int id=strOrganize.getID();
                                            StruGroupOfEmployee depart=strEmployeeService.getDepart(Employeeid);
                                            if (depart != null) {
                                                String groupName=depart.getGroup().getGroupName();
                                                if (!groupName.equals(ADdepartment)) {
                                                    DepartmentEntity departmentEnt=null;
                                                    DepartmentEntity departInfo=departmentAOService.getDepartInfo(id, ADdepartment);
                                                    if (departInfo != null) {
                                                        departmentEnt=departInfo;
                                                    } else {
                                                        departmentEnt=departmentAOService.createByInfo(ADdepartment, (int) directoryId, id, 0, 0);
                                                    }
                                                    strEmployeeService.deleteRalationEmployeeAndDepart(Employeeid);
                                                    strEmployeeService.maintainRelationGroupEmployee(strEmpl, departmentEnt, (int) directoryId, updateTime, (int) directoryId, 0);
                                                }
                                            }else {
                                                DepartmentEntity departmentEnt=null;
                                                DepartmentEntity departInfo=departmentAOService.getDepartInfo(id, ADdepartment);
                                                if (departInfo != null) {
                                                    departmentEnt=departInfo;
                                                } else {
                                                    departmentEnt=departmentAOService.createByInfo(ADdepartment, (int) directoryId, id, 0, 0);
                                                }
                                                strEmployeeService.maintainRelationGroupEmployee(strEmpl, departmentEnt, (int) directoryId, updateTime, (int) directoryId, 0);
                                            }
                                        }
                                    }
                                }
                            } else {
                                //扩展表中有
                                for (StrEmployeeExtend strEmployeeExtend : strEmployeeExtends) {
                                    Long beforeHashValue=strEmployeeExtend.getHashValue();
                                    if (!hash .equals(beforeHashValue)) {//公司 有变动，更新扩展表 和 组织表  雇员表
                                        strEmployeeExtend.setHashValue(hash);
                                        strEmployeeExtend.setUpdateTime(updateTime);
                                        strEmployeeExtend.save();
                                        if (StringUtils.isNotBlank(company)) {
                                            StrOrganize[] strOrganizes=ao.find(StrOrganize.class, String.format(" NAME = '%s' ", company));
                                            if (strOrganizes.length == 0) {
                                                StrOrganize strOrg=strOrganizeService.createByInfo(company, 10000, directoryId, updateTime, 0, 0, "0", "0");
                                                strEmpl.setStrOrganize(strOrg);
                                                strEmpl.save();
                                                if (StringUtils.isNotBlank(ADdepartment)) {
                                                    int idOrg=strOrg.getID();
                                                    DepartmentEntity departmentEnt=departmentAOService.createByInfo(ADdepartment, (int) directoryId, idOrg, 0, 0);
                                                    strEmployeeService.deleteRalationEmployeeAndDepart(Employeeid);
                                                    strEmployeeService.maintainRelationGroupEmployee(strEmpl, departmentEnt, (int) directoryId, updateTime, (int) directoryId, 0);
                                                }
                                            } else {
                                                StrOrganize strOrganize=strOrganizes[0];
                                                strEmpl.setStrOrganize(strOrganize);
                                                strEmpl.save();
                                                int Orgid=strOrganize.getID();
                                                if (StringUtils.isNotBlank(ADdepartment)) {
                                                    //StruGroupOfEmployee depart=strEmployeeService.getDepart(Employeeid);
                                                    StrEmployee strEmployee=ao.get(StrEmployee.class, Employeeid);
                                                    List<DepartmentEntity> deptsByOrgId=departmentAOService.getDirectDeptsByOrgId(Orgid);
                                                    Map<String, Integer> DepartmentName=new HashMap<>();
                                                    for (int u=0; u < deptsByOrgId.size(); u++) {
                                                        DepartmentName.put(deptsByOrgId.get(u).getGroupName(), deptsByOrgId.get(u).getID());
                                                    }
                                                    Set<String> strings=DepartmentName.keySet();
                                                    if (strings.contains(ADdepartment)) {
                                                        Integer departId=DepartmentName.get(ADdepartment);
                                                        DepartmentEntity departmentEntity=null;
                                                        for (DepartmentEntity departmentEntity1 : deptsByOrgId) {
                                                            if (departmentEntity1.getID() == departId) {
                                                                departmentEntity=departmentEntity1;
                                                            }
                                                        }
                                                        strEmployeeService.deleteRalationEmployeeAndDepart(Employeeid);
                                                        strEmployeeService.maintainRelationGroupEmployee(strEmployee, departmentEntity, (int) directoryId, updateTime, (int) directoryId, 0);
                                                    } else {
                                                        //DepartmentEntity byInfo=departmentAOService.createByInfo(ADdepartment, (int) directoryId, Orgid, 0, 0);
                                                        DepartmentEntity departmentEnt=departmentAOService.createByInfo(ADdepartment, (int) directoryId, Orgid, 0, 0);
                                                        StrEmployee strEmploy=ao.get(StrEmployee.class, Employeeid);
                                                        strEmployeeService.deleteRalationEmployeeAndDepart(Employeeid);
                                                        strEmployeeService.maintainRelationGroupEmployee(strEmploy, departmentEnt, (int) directoryId, updateTime, (int) directoryId, 0);
                                                    }
                                                }
                                            }
                                        } else {
                                            //    company为空 删除雇员和公司关系，删除雇员和组织关系
                                            strEmpl.setStrOrganize(null);
                                            strEmpl.save();
                                            strEmployeeService.deleteRalationEmployeeAndDepart(Employeeid);
                                        }
                                    } else {//公司没变动  查看jira用户的部门 情况 和  AD域中的部门情况 对比  维护 部门和公司关系，雇员和部门关系
                                        if(StringUtils.isNotBlank(company)){
                                            if (StringUtils.isNotBlank(ADdepartment)) {
                                                StruGroupOfEmployee depart=strEmployeeService.getDepart(Employeeid);
                                                if (depart != null) {
                                                    String groupName=depart.getGroup().getGroupName();
                                                    if (!groupName.equals(ADdepartment)) {
                                                        int id=strEmpl.getStrOrganize().getID();
                                                        DepartmentEntity departmentEntity=null;
                                                        departmentEntity=departmentAOService.getDepartInfo(id, ADdepartment);
                                                        if (departmentEntity == null) {
                                                            departmentEntity=departmentAOService.createByInfo(ADdepartment, (int) directoryId, id, 0, 0);
                                                        }
                                                        strEmployeeService.deleteRalationEmployeeAndDepart(Employeeid);
                                                        strEmployeeService.maintainRelationGroupEmployee(strEmpl, departmentEntity, (int) directoryId, updateTime, (int) directoryId, 0);
                                                    }
                                                } else {
                                                    int id=strEmpl.getStrOrganize().getID();
                                                    DepartmentEntity departmentEntity;
                                                    departmentEntity=departmentAOService.getDepartInfo(id, ADdepartment);
                                                    if (departmentEntity == null) {
                                                        departmentEntity=departmentAOService.createByInfo(ADdepartment, (int) directoryId, id, 0, 0);
                                                    }
                                                    strEmployeeService.maintainRelationGroupEmployee(strEmpl, departmentEntity, (int) directoryId, updateTime, (int) directoryId, 0);
                                                }
                                            }else {
                                                strEmployeeService.deleteRalationEmployeeAndDepart(Employeeid);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
//获取差集 在获取AD域中没有关联的sAMAccountName    同步 扩展表和 雇员表--------------------------------
                    ArrayList differlist=new ArrayList(differSet);
                    //同步组织表
                    //根据jirauserkey 过去AD域中全部信息 从中取出company 放到set集合中
                    createNotExitOrg(differlist,stringMapMap,OrganazitionAttrNameOfAd,directoryId,updateTime);
                    createNotExitDepartment(differlist,directoryId,stringMapMap,OrganazitionAttrNameOfAd,departmentAttrNameOfAd);
//                    Set<String> ADOrganizeSet=new HashSet<String>();
//                    for (int i=0; i < differlist.size(); i++) {
//                        String TableNoExsitOfsAMAccountName=(String) differlist.get(i);
//                        //Attributes attr=new BasicAttributes(LDAP_UniqueName, TableNoExsitOfsAMAccountName);
//                        // Map<String, Object> stringStringMap=SyncLdapImpl.searchByAttribute(init, groupdn + "," + searchBase, attr);
//                        Map<String, Object> stringStringMap=stringMapMap.get(TableNoExsitOfsAMAccountName);
//                        String company=(String) stringStringMap.get(OrganazitionAttrNameOfAd);
//                        //String department=(String) stringStringMap.get(departmentAttrNameOfAd);
//                        if (StringUtils.isNotBlank(company)) {
//                            ADOrganizeSet.add(company);
//                        }
//                    }
//                    //获取组织表中的所有名字 set 集合
//                    Set<String> StrOrganizeSet=new HashSet<String>();
//                    StrOrganize[] strOrganizes=ao.find(StrOrganize.class);
//                    int length=strOrganizes.length;
//                    for (int x=0; x < length; x++) {
//                        StrOrganizeSet.add(strOrganizes[x].getName());
//                    }
//                    //获取最终没有在表中的组织名字
//                    List list=new ArrayList();
//                    for (int a=0; a < ADOrganizeSet.size(); a++) {
//                        ArrayList ADOrganizelsit=new ArrayList(ADOrganizeSet);
//                        String s=String.valueOf(ADOrganizelsit.get(a));
//                        if (StrOrganizeSet.add(s)) {
//                            //不存在则创建
//                            strOrganizeService.createByInfo(s, (int) directoryId,directoryId, updateTime, 1, 0, "0", "0");
//                        }
//                    }
//同步到雇员表  根据差集 jirauserkey 去AD域中获取信息 每循环一个同步一个
                    for (int i=0; i < differlist.size(); i++) {
                        String TableNoExsitOfsAMAccountName=(String) differlist.get(i);
                        // Attributes attr=new BasicAttributes(LDAP_UniqueName, TableNoExsitOfsAMAccountName);
                        //获取每个没有同步的jira 用户信息
                        //Map<String, Object> stringStringMap=SyncLdapImpl.searchByAttribute(init, groupdn + "," + searchBase, attr);
                        Map<String, Object> stringStringMap=stringMapMap.get(TableNoExsitOfsAMAccountName);
                        String company1=(String) stringStringMap.get(OrganazitionAttrNameOfAd);
                        String department=(String) stringStringMap.get(departmentAttrNameOfAd);
                        String objectGUID=(String) stringStringMap.get(UserUniqueIDAttribute);
                        Long hash = returnHash(company1, TableNoExsitOfsAMAccountName);
                        String ADmail=(String) stringStringMap.get("mail");
                        StrEmployee strEmployee2 = strEmployeeService.getByEmail(ADmail);
                        DepartmentEntity DepbyInfo;
                        if (StringUtils.isNotBlank(company1)&&StringUtils.isNotBlank(department)){
                            DepbyInfo=departmentAOService.getDepartInfo(strOrganizeService.getByOrgName(company1).getID(),department);
                        }else {
                            DepbyInfo =null;
                        }
                        if (strEmployee2 != null) {
                            emialIsExit(company1,strEmployee2,directoryId,department,updateTime,TableNoExsitOfsAMAccountName);
                        } else {
                            emialIsNotExit(UserCreatTimeOfAd,stringStringMap,ADmail,directoryId,TableNoExsitOfsAMAccountName,UserChangeTimeOfAd,company1,updateTime,DepbyInfo);
                        }
//                            if (company1!=null) {
//                                StrOrganize OrgbyInfo=null;
//                                StrOrganize byOrgName=strOrganizeService.getByOrgName(company1);
//                                int Organizeid=0;
//                                if (byOrgName != null) {
//                                    OrgbyInfo=byOrgName;
//                                    Organizeid=byOrgName.getID();
//                                } else {
//                                    OrgbyInfo=strOrganizeService.createByInfo(company1,10000,directoryId, updateTime, 1, 0, "0", "0");
//                                    Organizeid=OrgbyInfo.getID();
//                                }
//                                if (StringUtils.isNotBlank(department)) {
//                                    DepartmentEntity departmentEntity=departmentAOService.getDepartInfo(Organizeid, department);
//                                    if (departmentEntity != null) {
//                                        DepbyInfo=departmentEntity;
//                                    } else {
//                                        DepbyInfo=departmentAOService.createByInfo(department, (int) directoryId, Organizeid, 0, 0);
//                                    }
//                                }
//                            }
//                            StrEmployee strEmployee=ao.create(StrEmployee.class);
//                            String whenCreated=(String) stringStringMap.get(UserCreatTimeOfAd);
//                            String s=this.parseTime(whenCreated);
//                            strEmployee.setCreateDate(s);
//                            strEmployee.setEmployeeSex("1");
//                            strEmployee.setEmail(ADmail);
//                            strEmployee.setEmployeeName((String) stringStringMap.get("cn"));
//                            strEmployee.setEmploymentStatus("1");
//                            strEmployee.setLdapId(directoryId);
//                            strEmployee.setJiraUserKey(TableNoExsitOfsAMAccountName);
//                            String whenChanged=(String) stringStringMap.get(UserChangeTimeOfAd);
//                            String s1=this.parseTime(whenChanged);
//                            strEmployee.setModifierDate(s1);
//                            strEmployee.setPhone((String) stringStringMap.get("telephoneNumber"));
//                            StrOrganize[] strOrganizes1=ao.find(StrOrganize.class, String.format(" NAME = '%s' ", company1));
//                            for (StrOrganize strOrganize : strOrganizes1) {
//                                if (strOrganize.getName().equalsIgnoreCase(company1)) {
//                                    strEmployee.setStrOrganize(strOrganize);
//                                }
//                            }
//                            strEmployee.save();
//                            strEmployeeService.maintainRelationGroupEmployee(strEmployee, DepbyInfo, (int) directoryId, updateTime, (int) directoryId, 0);
//                        }
//                    再同步到 扩展表
                        syncExtendTable( TableNoExsitOfsAMAccountName, directoryId, hash, updateTime,objectGUID);
//                        int id=0;
//                        StrEmployee[] strEmployees = strEmployeeService.getEmployeeByJiraUserKeyAndDirectoryId(TableNoExsitOfsAMAccountName, directoryId);
//                        if (strEmployees != null) {
//                            for (StrEmployee strEmployee1 : strEmployees) {
//                                id=strEmployee1.getID();
//                            }
//                        }
//                        StrEmployeeExtend strEmployeeExtend1=ao.create(StrEmployeeExtend.class);
//                        strEmployeeExtend1.setEmployeeId(id);
//                        strEmployeeExtend1.setHashValue(hash);
//                        strEmployeeExtend1.setLoginName(TableNoExsitOfsAMAccountName);
//                        strEmployeeExtend1.setUpdateTime(updateTime);
//                        strEmployeeExtend1.setSourcePart(objectGUID);
//                        strEmployeeExtend1.save();
                    }
                }

            }
        }
    }

    @Override
    public long getHashCode(String loginName) {
        StrEmployeeExtend[] strEmployeeExtends=ao.find(StrEmployeeExtend.class, MessageFormat.format(" LOGIN_NAME = {0} ", loginName));
        for (StrEmployeeExtend strEmployeeExtend : strEmployeeExtends) {
            return strEmployeeExtend.getHashValue();
        }
        return 0;
    }

    @Override
    public String getUniqueCode(int employeeId) {
        StrEmployeeExtend[] strEmployeeExtends=ao.find(StrEmployeeExtend.class, String.format(" EMPLOYEE_ID = '%d' ", employeeId));
        for (StrEmployeeExtend strEmployeeExtend:strEmployeeExtends){
            return strEmployeeExtend.getSourcePart();
        }
        return null;
    }

    @Override
    public void delDirectoryInfo(long directoryId) {
        StrEmployee[] strEmployees = ao.find(StrEmployee.class, String.format("LDAP_ID = '%s' ", directoryId));
        for (StrEmployee strEmployee:strEmployees){
            StrEmployeeExtend[] strEmployeeExtends = ao.find(StrEmployeeExtend.class, String.format("EMPLOYEE_ID = '%s' ", strEmployee.getID()));
            ao.delete(strEmployeeExtends[0]);
        }
    }

    @Override
    public void create(Long hash,int Employeeid,String updateTime,String ExsitJiraUserKey,String objectGUID) {
        StrEmployeeExtend strEmployeeExtend=ao.create(StrEmployeeExtend.class);
        strEmployeeExtend.setEmployeeId(Employeeid);
        strEmployeeExtend.setUpdateTime(updateTime);
        strEmployeeExtend.setHashValue(hash);
        strEmployeeExtend.setLoginName(ExsitJiraUserKey);
        strEmployeeExtend.setSourcePart(objectGUID);
        strEmployeeExtend.save();
    }

    public String parseTime(String toParseTime) {
        if (StringUtils.isNotBlank(toParseTime)) {
            String toparseTime = toParseTime;
            if (toParseTime.contains(".")) {
                String[] parts = toParseTime.split("[.]");
                String dateTimePart = parts[0];
                String timeZonePart = "+0" + parts[1].substring(0, parts[1].length() - 1) + "00";
                toparseTime = dateTimePart + timeZonePart;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            Date theDate = null;
            try {
                theDate = sdf.parse(toparseTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String time = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(theDate);
            return time;
        }else {
            return "";
        }
    }

    private LdapContext getInit(String LDAP_URL,String adminName,String adminPassword ){
        LdapContext init = SyncLdapImpl.init(LDAP_URL, adminName, adminPassword);
        return init;
    }

    private String getNowTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        return simpleDateFormat.format(new Date());
    }

    private List<Directory> getActiveDirectory() {
        List<Directory> allDirectories = new ArrayList<>();
        List<Directory> allActiveDirectories = crowdDirectoryService.findAllDirectories();
        for (Directory directory : allActiveDirectories) {
            if (directory.isActive()) {
                allDirectories.add(directory);
            }
        }
        return allDirectories;
    }

    private void checkStatusOfDirectory(List<Directory> allDirectories){
        Set<Long> longs= ScheduleWay.scheduleMap.keySet();
        int size=longs.size();
        int AdDirectorySize=allDirectories.size() - 1;
        if (AdDirectorySize == 0) { //del
            if (size !=0){
                for (long ls:longs){
                    if (ls!=-1){ //  System.out.println("删除了一个AD");
                        ScheduleWay.scheduleMap.get(ls).cancel();
                        ScheduleWay.scheduleMap.remove(ls);
                        ldapSyncTimeService.delete(ls);
                        delDirectoryInfo(ls);
                        strEmployeeService.setJiraUserKeyNull(ls);
                        strEmployeeService.setLdapToNull(ls);
                    }
                }
            }
        } else {
            if (AdDirectorySize > size) {//add
                for (Directory directory : allDirectories) {
                    if (directory.isActive()){
                        String implementationClass=directory.getImplementationClass();
                        Long a_id=directory.getId();
                        if (a_id != 1&& (implementationClass.equals("com.atlassian.crowd.directory.OpenLDAP") ||implementationClass.equals("com.atlassian.crowd.directory.MicrosoftActiveDirectory"))) {
                            if (!longs.contains(a_id)) {// System.out.println("增加了一个AD");
                                SchedulerTask schedulerTask=new SchedulerTask(a_id);
                                Timer timer=new Timer();
                                ScheduleWay.scheduleMap.put(a_id, schedulerTask);
                                int timeOfOriginal=Integer.parseInt(directory.getValue("directory.cache.synchronise.interval")) / 60;
                                ldapSyncTimeService.create(a_id, timeOfOriginal);
                                timer.schedule(schedulerTask, timeOfOriginal * 60*1000, timeOfOriginal * 60000);
                            }
                        }
                    }}
            } else {//del
                Set<Long> existIdSet = new HashSet<>();
                Set<Long> deleteIdSet = new HashSet<>();
                for (Directory directory : allDirectories) {
                    if (directory.isActive()) {
                        String implementationClass = directory.getImplementationClass();
                        Long a_id = directory.getId();
                        if (a_id != 1 && (implementationClass.equals("com.atlassian.crowd.directory.OpenLDAP") || implementationClass.equals("com.atlassian.crowd.directory.MicrosoftActiveDirectory"))) {
                            existIdSet.add(a_id);
                        }
                    }
                }
                deleteIdSet.addAll(longs);
                deleteIdSet.removeAll(existIdSet);
                ArrayList arrayList = new ArrayList(deleteIdSet);
                for (int o = 0; o < arrayList.size(); o++) {// System.out.println("del one AD ");
                    long o1 = (long) arrayList.get(o);
                    ScheduleWay.scheduleMap.get(o1).cancel();
                    ldapSyncTimeService.delete(o1);
                    delDirectoryInfo(o1);
                    strEmployeeService.setJiraUserKeyNull(o1);
                    strEmployeeService.setLdapToNull(o1);
                    ScheduleWay.scheduleMap.remove(o1);
                }
            }
        }
    }

    private  Map<String, Map<String, Object>> getDomainInfo(String groupdn,String searchBase,String userList,LdapContext init,String searchObject,String[] returnedAtts ){
        Map<String, Map<String, Object>> stringMapMap= new HashMap<>();
        try {
            String searchB = "";
            searchB =searchBase;
            if (StringUtils.isNotBlank(userList)) {
                String userl= userList+","+searchB;
                Map<String, Map<String, Object>> stringMapMaps = SyncLdapImpl.searchInfoByFilter(init, userl, searchObject, returnedAtts);
                stringMapMap.putAll(stringMapMaps);
            } else {
                stringMapMap = SyncLdapImpl.searchInfoByFilter(init, searchB, searchObject, returnedAtts);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return stringMapMap;
    }

    private void createNotExitOrg(ArrayList differlist,Map<String, Map<String, Object>> stringMapMap,String OrganazitionAttrNameOfAd,long directoryId ,String updateTime){
        //同步组织表
        //根据jirauserkey 过去AD域中全部信息 从中取出company 放到set集合中
        Set<String> ADOrganizeSet=new HashSet<String>();
        for (int i=0; i < differlist.size(); i++) {
            String TableNoExsitOfsAMAccountName=(String) differlist.get(i);
            //Attributes attr=new BasicAttributes(LDAP_UniqueName, TableNoExsitOfsAMAccountName);
            // Map<String, Object> stringStringMap=SyncLdapImpl.searchByAttribute(init, groupdn + "," + searchBase, attr);
            Map<String, Object> stringStringMap=stringMapMap.get(TableNoExsitOfsAMAccountName);
            String company=(String) stringStringMap.get(OrganazitionAttrNameOfAd);
            //String department=(String) stringStringMap.get(departmentAttrNameOfAd);
            if (StringUtils.isNotBlank(company)) {
                ADOrganizeSet.add(company);
            }
        }
        //获取组织表中的所有名字 set 集合
        Set<String> StrOrganizeSet=new HashSet<String>();
        StrOrganize[] strOrganizes=ao.find(StrOrganize.class);
        int length=strOrganizes.length;
        for (int x=0; x < length; x++) {
            StrOrganizeSet.add(strOrganizes[x].getName());
        }
        //获取最终没有在表中的组织名字
        List list=new ArrayList();
        for (int a=0; a < ADOrganizeSet.size(); a++) {
            ArrayList ADOrganizelsit=new ArrayList(ADOrganizeSet);
            String s=String.valueOf(ADOrganizelsit.get(a));
            if (StrOrganizeSet.add(s)) {
                //不存在则创建
                strOrganizeService.createByInfo(s, (int) directoryId,directoryId, updateTime, 1, 0, "0", "0");
            }
        }
    }

    private void  createNotExitDepartment(ArrayList differlist,long directoryId ,Map<String, Map<String, Object>> stringMapMap,String OrganazitionAttrNameOfAd,String departmentAttrNameOfAd){
        for (int i=0; i < differlist.size(); i++) {
            String TableNoExsitOfsAMAccountName=(String) differlist.get(i);
            // Attributes attr=new BasicAttributes(LDAP_UniqueName, TableNoExsitOfsAMAccountName);
            //获取每个没有同步的jira 用户信息
            //Map<String, Object> stringStringMap=SyncLdapImpl.searchByAttribute(init, groupdn + "," + searchBase, attr);
            Map<String, Object> stringStringMap=stringMapMap.get(TableNoExsitOfsAMAccountName);
            String company1=(String) stringStringMap.get(OrganazitionAttrNameOfAd);
            String department=(String) stringStringMap.get(departmentAttrNameOfAd);
            if (StringUtils.isNotBlank(company1)&&StringUtils.isNotBlank(department)){
                StrOrganize strOrganize=strOrganizeService.getByOrgName(company1);
                DepartmentEntity departInfo = departmentAOService.getDepartInfo(strOrganize.getID(), department);
                if (departInfo == null){
                    departmentAOService.createByInfo(department,(int)directoryId,strOrganize.getID(),0,0);
                }
            }
        }
    }

    private Long returnHash(String company1,String TableNoExsitOfsAMAccountName){
        long hash=0;
        for (int a=0; a < (company1 + TableNoExsitOfsAMAccountName).length(); a++) {
            hash=33 * hash + (company1 + TableNoExsitOfsAMAccountName).charAt(a);
        }
        return hash;
    }

    private void emialIsExit(String company1,StrEmployee strEmployee2,long directoryId,String department,String updateTime,String TableNoExsitOfsAMAccountName){
        strEmployee2.setJiraUserKey(TableNoExsitOfsAMAccountName.toLowerCase());
        strEmployee2.setLdapId(directoryId);
        strEmployee2.save();
        //在jira中关联的记录必有组织，部门可以有或无
        // int empId=strEmployee.getID();
//        StrOrganize strOrganize=strEmployee2.getStrOrganize();
//        if (strOrganize != null) {
//            int orgId = strOrganize.getID();
//            if (StringUtils.isNotBlank(department)) {
//                //改变了 雇员和部门的关系  删除了原先的雇员和部门关系
//                strEmployeeService.deleteRalationEmployeeAndDepart(strEmployee2.getID());
//                DepartmentEntity departmentEntity = departmentAOService.getDepartInfo(orgId, department);
//                if (departmentEntity == null) {
//                    departmentEntity = departmentAOService.createByInfo(department, (int) directoryId, orgId, 0, 0);
//                }
//                strEmployeeService.maintainRelationGroupEmployee(strEmployee2, departmentEntity, (int) directoryId, updateTime, (int) directoryId, 0);
//            }
//
//        }else {
//           if (StringUtils.isNotBlank(company1)){
//               strEmployee2.setStrOrganize(strOrganizeService.getByOrgName(company1));
//               strEmployee2.save();
//           }
//        }
    }

    private void emialIsNotExit(String UserCreatTimeOfAd,Map<String, Object> stringStringMap,String ADmail,long directoryId,String TableNoExsitOfsAMAccountName,String UserChangeTimeOfAd,String company1,String updateTime,DepartmentEntity departmentEntity){
        StrEmployee strEmployee=ao.create(StrEmployee.class);
        String whenCreated=(String) stringStringMap.get(UserCreatTimeOfAd);
        String s=this.parseTime(whenCreated);
        strEmployee.setCreateDate(s);
        strEmployee.setEmployeeSex("1");
        strEmployee.setEmail(ADmail);
        strEmployee.setEmployeeName((String) stringStringMap.get("cn"));
        strEmployee.setEmploymentStatus("1");
        strEmployee.setLdapId(directoryId);
        strEmployee.setJiraUserKey(TableNoExsitOfsAMAccountName.toLowerCase());
        String whenChanged=(String) stringStringMap.get(UserChangeTimeOfAd);
        String s1=this.parseTime(whenChanged);
        strEmployee.setModifierDate(s1);
        strEmployee.setPhone((String) stringStringMap.get("telephoneNumber"));
        StrOrganize byOrgName = strOrganizeService.getByOrgName(company1);
        if (byOrgName != null){
            strEmployee.setStrOrganize(byOrgName);
        }
        strEmployee.save();
        strEmployeeService.maintainRelationGroupEmployee(strEmployee, departmentEntity, (int) directoryId, updateTime, (int) directoryId, 0);
    }

    private void  syncExtendTable(String TableNoExsitOfsAMAccountName,long directoryId,long hash,String updateTime,String objectGUID){
        StrEmployee[] strEmployees = strEmployeeService.getEmployeeByJiraUserKeyAndDirectoryId(TableNoExsitOfsAMAccountName, directoryId);
        if (strEmployees.length != 0) {
            int  id=strEmployees[0].getID();
            StrEmployeeExtend strEmployeeExtend1=ao.create(StrEmployeeExtend.class);
            strEmployeeExtend1.setEmployeeId(id);
            strEmployeeExtend1.setHashValue(hash);
            strEmployeeExtend1.setLoginName(TableNoExsitOfsAMAccountName);
            strEmployeeExtend1.setUpdateTime(updateTime);
            strEmployeeExtend1.setSourcePart(objectGUID);
            strEmployeeExtend1.save();
        }
    }

    private  void checkRelation(int employeeId,String company,String department,long directoryId,String updateTime){
        if (StringUtils.isNotBlank(company) && StringUtils.isNotBlank(department)) {
            StrEmployee employee = strEmployeeService.getEmployee(employeeId);
            StrOrganize byOrgName = strOrganizeService.getByOrgName(company);
            DepartmentEntity departInfo = departmentAOService.getDepartInfo(byOrgName.getID(), department);
            if (departInfo == null) {
                departInfo = departmentAOService.createByInfo(department, (int) directoryId, byOrgName.getID(), 0, 0);
            }
            int id = employee.getStrOrganize().getID();
            if (id != byOrgName.getID()) {
                employee.setStrOrganize(byOrgName);
                employee.save();
            }
            StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, String.format("EMPLOYEE_ID = '%d' ", employeeId));
            if (struGroupOfEmployees.length != 0) {
                int id1 = struGroupOfEmployees[0].getGroup().getID();
                if (departInfo.getID() != id1) {
                    strEmployeeService.deleteRalationEmployeeAndDepart(employeeId);
                    strEmployeeService.maintainRelationGroupEmployee(employee, departInfo, (int) directoryId, updateTime, (int) directoryId, 0);
                }
            }
        }
    }

    private void  checkExtendEmployee(){
        StrEmployeeExtend[] strEmployeeExtends = ao.find(StrEmployeeExtend.class);
        Arrays.stream(strEmployeeExtends).forEach(s ->{
            StrEmployee employee = strEmployeeService.getEmployee(s.getEmployeeId());
            if (employee != null){
                if (!s.getLoginName().equals(employee.getJiraUserKey())){
                    ao.delete(s);
                }
            }else {
                ao.delete(s);
            }
        });
    }
}