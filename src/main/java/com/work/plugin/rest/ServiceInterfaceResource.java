package com.work.plugin.rest;

import com.atlassian.annotations.security.XsrfProtectionExcluded;
import com.atlassian.crowd.embedded.api.*;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.crowd.embedded.ofbiz.OfBizUser;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserDetails;
import com.atlassian.jira.user.util.UserManager;
import com.work.plugin.util.Encryption;
import com.work.plugin.util.ObjectGUID_Tools;
import com.work.plugin.util.ToolsDataHelper;
import com.work.plugin.util.exception.DuplicateKeyException;
import com.work.plugin.util.license.license.GlobalConfig;
import com.work.plugin.ao.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by work on 2022/3/27.
 */
@Path("/SerInterface")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class ServiceInterfaceResource {
    private final UserManager userManager;
    private final CrowdService crowdService;
    private final GroupManager groupManager;
    private final StrEmployeeService strEmployeeService;
    private final CrowdDirectoryService crowdDirectoryService;
    private final WeChatAndJiraUserService weChatAndJiraUserService;
    private final StrOrganizeService strOrganizeService;
    private final DepartmentAOService departmentAOService;
    private static  LdapContext  init=null;

    @GET
    @Path("JiraGroup")
    public Response getAllJiraGroup(){
        Set<String> groupName=new HashSet();
        Set<Group> allGroups=userManager.getAllGroups();
        for (Group group : allGroups) {
            groupName.add(group.getName());
        }
        return Response.ok(groupName).build();
    }
    @POST
    @Path("addGroup")
    @XsrfProtectionExcluded
    public Response addJiraGroup(GroupBean groupBean){
        if (!groupManager.groupExists(groupBean.getGroupName())){
            try {
                groupManager.createGroup(groupBean.getGroupName());
            } catch (OperationNotPermittedException e) {
                e.printStackTrace();
            } catch (InvalidGroupException e) {
                e.printStackTrace();
            }
        }
        return Response.ok().build();
    }
    @POST
    @Path("addJiraUesrToGroup")
    @XsrfProtectionExcluded
    public Response addJiraUserToGroup(JiraUserGroupBean jiraUserGroupBean){
        ApplicationUser userByName=userManager.getUserByName(jiraUserGroupBean.getUserName());
        Group group=groupManager.getGroup(jiraUserGroupBean.getGroupName());
        if (userByName!=null&&group!=null){
            try {
                if (!groupManager.isUserInGroup(userByName,group)){
                    groupManager.addUserToGroup(userByName,group);
                }
            } catch (GroupNotFoundException e) {
                e.printStackTrace();
            } catch (UserNotFoundException e) {
                e.printStackTrace();
            } catch (OperationNotPermittedException e) {
                e.printStackTrace();
            } catch (OperationFailedException e) {
                e.printStackTrace();
            }

        }
        return Response.ok().build();
    }
    @POST
    @Path("rmJiraUserFromGroup")
    @XsrfProtectionExcluded
    public Response rmJiraUserFromGroup(JiraUserGroupBean jiraUserGroupBean){
        String groupName=jiraUserGroupBean.getGroupName();
        String userName=jiraUserGroupBean.getUserName();
        if (StringUtils.isNotBlank(userName)&&StringUtils.isNotBlank(groupName)){
            ApplicationUser userByName=userManager.getUserByName(userName);
            if (groupManager.groupExists(groupName)&&userByName!=null){
                if (groupManager.isUserInGroup(userByName,groupManager.getGroup(groupName))){
                    try {
                        crowdService.removeUserFromGroup(userByName.getDirectoryUser(),groupManager.getGroup(groupName));
                    } catch (OperationNotPermittedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return Response.ok().build();
    }
    @POST
    @Path("createOrgUser")
    @XsrfProtectionExcluded
    public Response createOrgUser(StrEmployeeBean model){
        try {
            strEmployeeService.create(model);
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }
    @POST
    @Path("creatJiraUser")
    @XsrfProtectionExcluded
    //@Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Response creatJiraUser(JiraUserBean jiraUserBean) {
        String displayName=jiraUserBean.getDisplayName();
        String emailAddress=jiraUserBean.getEmailAddress();
        String password=jiraUserBean.getPassword();
        String userName=jiraUserBean.getUserName();
        if (StringUtils.isNotBlank(displayName) && StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password) && StringUtils.isNotBlank(password)) {
            UserDetails userDetai=new UserDetails(userName, displayName);
            userDetai.withDirectory((long) 1);
            userDetai.withPassword(password);
            userDetai.withEmail(emailAddress);
            try {
                ApplicationUser user=userManager.createUser(userDetai);
            } catch (CreateException e) {
                e.printStackTrace();
            } catch (PermissionException e) {
                e.printStackTrace();
            }
        }
        return Response.ok().build();
    }

    @GET
    @Path("search/{tel}")
    public  Response isBind(@PathParam("tel") String tel){
        Map map = new HashMap();
        String changeTime = "";
        WeChatAndJiraUserEntity byTel = weChatAndJiraUserService.getByTel(tel);
        if (byTel != null){
            // try {
            String userName = byTel.getUserName();
            User user = crowdService.getUser(userName);
            long directoryId = user.getDirectoryId();
            Map<String, String> changeTime1 = getChangeTime(userName, crowdService, crowdDirectoryService, userManager);
            changeTime=  changeTime1.get("time");
            String location = changeTime1.get("location");
            if (!byTel.getUpdateTime().equals(changeTime)) {
                map.put("status", "504");
                return Response.ok(map).build();
            }else {
                if (directoryId == 1){
                    try {
                        User authenticate = crowdService.authenticate(byTel.getUserName(), Encryption.decrypt(byTel.getPassword()));
                    } catch (Exception e) {
                        map.put("status", "504");
                        return Response.ok(map).build();
                    }
                }else {
                    map.put("userName", userName);
                    map.put("location", location);
                    try {
                        map.put("updateTime", changeTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return Response.ok(map).build();
                }
            }
            map.put("userName", userName);
            map.put("location", location);
            map.put("updateTime", changeTime);
            return  Response.ok(map).build();
        }else {
            map.put("status","503");
            return  Response.ok(map).build();
        }
    }
    /*    @GET
        @Path("searching/{tel}")
        public  Response isBinded(@PathParam("tel") String tel){
            Map map = new HashMap();
            String changeTime = "";
            WeChatAndJiraUserEntity byTel = weChatAndJiraUserService.getByTel(tel);
            if (byTel != null){
                // try {
                Map<String, String> changeTime1 = getChangeTime(byTel.getUserName(), crowdService, crowdDirectoryService, userManager);
                changeTime=  changeTime1.get("time");
                if (!byTel.getUpdateTime().equals(changeTime)) {
                    map.put("status", "504");
                    return Response.ok(map).build();
                }else {
                    map.put("userName", byTel.getUserName());
                    try {
                        map.put("password", Encryption.decrypt(byTel.getPassword()));
                        map.put("updateTime", changeTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return Response.ok(map).build();
                }
            }else {
                map.put("status","503");
                return  Response.ok(map).build();
            }
        }*/
    @GET
    @Path("bind/{tel}/{userName}/{passwd}")
    public Response bind(@PathParam("tel") String tel,@PathParam("userName") String userName,@PathParam("passwd") String passwd){
        String decode ="";
        try {
            String  decodeS = passwd.replace("jwclcc", "%");
            decode = URLDecoder.decode(decodeS, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map map = new HashMap();
        try {
            User authenticate = crowdService.authenticate(userName, decode);
            Map<String, String> changeTime1 = getChangeTime(userName, crowdService, crowdDirectoryService, userManager);
            String  changeTime=  changeTime1.get("time");
            String  location=  changeTime1.get("location");
            Boolean aBoolean = weChatAndJiraUserService.bindInfo(tel, userName, decode,changeTime);
            if (aBoolean) {
                map.put("userName",userName);
                map.put("location", location);
                return Response.ok(map).build();
            } else {
                map.put("status", "502");
                return Response.ok(map).build();
            }
        } catch (Exception e) {
            map.put("status", "503");
            return Response.ok(map).build();
        }
    }
    @GET
    @Path("unbind/{tel}")
    public Response unbind(@PathParam("tel") String tel){
        Map map = new HashMap();
        Boolean booleans = weChatAndJiraUserService.unbindInfo(tel);
        if (booleans){
            map.put("status","200");
            return  Response.ok(map).build();
        }else {
            map.put("status","503");
            return Response.ok(map).build();
        }
    }
    @POST
    @Path("Alternative/{orgName}/{depaName}/{username}")
    public Response alternative(@PathParam("username") String username,@PathParam("depaName") String depaName ,@PathParam("orgName") String orgName){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        String maintaintime = simpleDateFormat.format(new Date());
        StrOrganize byInfo = strOrganizeService.createByInfo(orgName, 0,0,maintaintime ,1 ,0 , "0", "0");
        DepartmentEntity byInfo1 = departmentAOService.createByInfo(depaName, 0, byInfo.getID(), 0, 0);
       /* StrEmployee orgUser = strEmployeeService.createOrgUser(orgName, username);
        strEmployeeService.maintainRelationGroupEmployee(orgUser,byInfo1,0,maintaintime,0,0);*/
        return Response.ok().build();
    }
    /*    @GET
        @Path("test")
        public Response test(){
            List<Directory> allDirectories=crowdDirectoryService.findAllDirectories();
            Map<String,Object>  map = new HashMap<>();
            for (Directory directory : allDirectories) {
                if (directory.getId() == 10200) {
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
                    String[] returnedAtts=null;
                    map.put(LDAP_URL+adminName+adminPassword,adminPassword);
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
                    map.put(adminName+"1",adminPassword);
                    LdapContext init = SyncLdapImpl.init(LDAP_URL, adminName, adminPassword);
                    map.put(adminName+"2",adminPassword);
                    Map<String, String> map1 = SyncLdapImpl.map;
                    map.putAll(map1);
                }
            }
            return Response.ok(map).build();
        }*/
/*
    @GET
    @Path("binding/{tel}/{userName}/{passwd}")
    public Response binding(@PathParam("tel") String tel,@PathParam("userName") String userName,@PathParam("passwd") String passwd){
        Map map = new HashMap();
        Boolean aBoolean = weChatAndJiraUserService.bindInfo(tel, userName, passwd);
        if (aBoolean) {
            map.put("userName",userName);
            map.put("passwd",passwd);
            return Response.ok(map).build();
        } else {
            map.put("status", "502");
            return Response.ok(map).build();
        }
    }*/
/*
    @GET
    @Path("binded/{domain}/{tel}/{userName}/{passwd}")
    public Response binded(@PathParam("domain") String domain,@PathParam("tel") String tel,@PathParam("userName") String userName,@PathParam("passwd") String passwd){
        Map map = new HashMap();
        List<Directory> allDirectories=crowdDirectoryService.findAllDirectories();//ldap://SHHHQDC03S.wilmar.cn:389
        for (Directory directory : allDirectories) {
            domain ="ldap://"+domain+":389";
            String LDAP_URL=directory.getValue("ldap.url");
            if (LDAP_URL.equals(domain)) {
                LdapContext init = SyncLdapImpl.init(LDAP_URL, userName, passwd);
                if (init !=null){
                     Boolean aBoolean = weChatAndJiraUserService.bindInfo(tel, userName, passwd);
                    if (aBoolean) {
                        map.put("userName",userName);
                        map.put("passwd",passwd);
                        try {
                            init.close();
                        } catch (NamingException e) {
                        }
                        return Response.ok(map).build();
                    } else {
                        map.put("status", "502");
                        try {
                            init.close();
                        } catch (NamingException e) {
                        }
                        return Response.ok(map).build();
                    }
                }
            }
        }
       return Response.ok().build();
    }*/
/*    @GET
    @Path("PBind/{tel}/{userName}/{passwd}")
    public Response PrecisionBinded(@PathParam("tel") String tel,@PathParam("userName") String userName,@PathParam("passwd") String passwd){
        Map map1 = new HashMap();
        List<Directory> allDirectories=crowdDirectoryService.findAllDirectories();//ldap://SHHHQDC03S.wilmar.cn:389
        for (Directory directory : allDirectories) {
            Map map = new HashMap();
            String LDAP_URL=directory.getValue("ldap.url");
            if (LDAP_URL.equals("ldap://SHHHQDC03S.wilmar.cn:389")) {
                LdapContext init = SyncLdapImpl.init(LDAP_URL, userName, passwd);
                if (init !=null){
                    Boolean aBoolean = weChatAndJiraUserService.bindInfo(tel, userName, passwd);
                    if (aBoolean) {
                        map.put("userName",userName);
                        map.put("passwd",passwd);
                        try {
                            init.close();
                        } catch (NamingException e) {
                        }
                        return Response.ok(map).build();
                    } else {
                        map.put("status", "502");
                        try {
                            init.close();
                        } catch (NamingException e) {
                        }
                        return Response.ok(map).build();
                    }
                }
            }
        }
        map1.put("status", "503");
        return Response.ok(map1).build();
    }*/
    @GET
    @Path("getAll")
    public Response getAll(){
        WeChatAndJiraUserEntity[] all = weChatAndJiraUserService.getAll();
        Map map = new HashMap();
        for (WeChatAndJiraUserEntity weChatAndJiraUserEntity:all){
            map.put(weChatAndJiraUserEntity.getTel(),weChatAndJiraUserEntity.getUserName());
            map.put(weChatAndJiraUserEntity.getUserName(),weChatAndJiraUserEntity.getUpdateTime());
        }
        return Response.ok(map).build();
    }
    @GET
    @Path("getTime/{userNmae}")
    public Response getTime(@PathParam("userNmae") String userName){
        Map map = new HashMap();
        Map<String, String> changeTime1 = getChangeTime(userName, crowdService, crowdDirectoryService, userManager);
        String changeTime = changeTime1.get("time");
        map.put("time",changeTime);
        return Response.ok(map).build();
    }
    private  List<Directory> getActiveDirectory() {
        List<Directory> allDirectories = new ArrayList<>();
        List<Directory> allActiveDirectories = crowdDirectoryService.findAllDirectories();
        for (Directory directory : allActiveDirectories) {
            if (directory.isActive()) {
                allDirectories.add(directory);
            }
        }
        return allDirectories;
    }
    private static LdapContext getInit(String LDAP_URL, String adminName, String adminPassword ){
        LdapContext init = SyncLdapImpl.init(LDAP_URL, adminName, adminPassword);
        return init;
    }
    private  static   Map<String, Map<String, Object>> getDomainInfo(String groupdn,String searchBase,String userList,LdapContext init,String searchObject,String[] returnedAtts ){
        Map<String, Map<String, Object>> stringMapMap= new HashMap<>();
//        try {
//            String searchB = "";
//            if (StringUtils.isBlank(groupdn)){
//                searchB =searchBase;
//            }else {
//                searchB =groupdn + "," + searchBase;
//            }
//            if (userList.contains(",")){
//                String[]  split = userList.split(",");
//                int length = split.length;
//                for (int c =0;c <length;c++){
//                    String searcha="";
//                    searcha =searchB;
//                    String searchc = split[c] + "," + searcha;
//                    Map<String, Map<String, Object>> stringMapMaps=SyncLdapImpl.searchInfoByFilter(init, searchc, searchObject, returnedAtts);
//                    stringMapMap.putAll(stringMapMaps);
//                }
//            }else {
//                //stringMapMap=SyncLdapImpl.searchInfoByFilter(init, searchB, searchObject, returnedAtts);
//                stringMapMap=SyncLdapImpl.searchInfoByFilter(init, searchB, searchObject, returnedAtts);
//            }
//
//        }
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
    private static Map<String, Map<String, Object>> searchInfoByFilter(LdapContext ldapCtx, String searchBase, String searchFilter, String[] returnedAtts) throws NamingException {
        Map<String,Map<String,Object>> hashmap=new HashMap<String, Map<String, Object>>();
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(returnedAtts);
        NamingEnumeration<SearchResult> answer = ldapCtx.search(searchBase, searchFilter, searchCtls);
        while (answer.hasMoreElements()) {
            Map<String,Object> map=new HashMap<String, Object>();
            SearchResult sr = answer.next();
            javax.naming.directory.Attributes Attrs = sr.getAttributes();
            if (Attrs != null) {
                NamingEnumeration<?> ne = Attrs.getAll();
                while (ne.hasMore()) {
                    Attribute Attr = (Attribute) ne.next();
                    String name = Attr.getID();
                    Enumeration<?> values = Attr.getAll();
                    if (values != null) { // 迭代
                        while (values.hasMoreElements()) {
                            String value = "";
                            //String UniqueUUid=returnedAtts[1];
                            //if (UniqueUUid.equals(name)&&UniqueUUid.equals("objectGUID")) {
                            if ("objectGUID".equals(name)){
                                //value = UUID.nameUUIDFromBytes((byte[]) values.nextElement()).toString();
                                value = ObjectGUID_Tools.convertToDashedString((byte[]) values.nextElement()) ;
                            } else {
                                value = (String) values.nextElement();
                            }
                            //  System.out.println(name+""+value);
                            map.put(name,value);
                            if (name.equals(returnedAtts[0])){
                                hashmap.put(value,map);
                            }
                        }
                    }
                }
            }
        }
        return hashmap;
    }
    private   Map<String,String> getChangeTime(String substring,CrowdService crowdService,CrowdDirectoryService crowdDirectoryService,UserManager userManager){
        Map<String,String> returnMap=new HashMap<>();
        String time ="";
        Map<String, Map<String, Object>> stringMapMap =new HashMap<>();
        User user = crowdService.getUser(substring);
        long directoryId = user.getDirectoryId();
        if (directoryId != 1){
            LdapContext init=null;
            List<Directory> allDirectories = new ArrayList<>();
            List<Directory> allActiveDirectories = crowdDirectoryService.findAllDirectories();
            for (Directory directory : allActiveDirectories) {
                if (directory.isActive()) {
                    allDirectories.add(directory);
                }
            }
            for (Directory directory:allDirectories){
                if (directory.getId().equals(directoryId)){
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
                    if (init != null){
                        //stringMapMap = getDomainInfo(groupdn, searchBase, userList, init, searchObject, returnedAtts);
                        try {
                            stringMapMap = searchInfoByFilter(init, searchBase, "(&"+searchObject+"("+LDAP_UniqueName+"="+substring+"))", new String[]{LDAP_UniqueName,UserChangeTimeOfAd,OrganazitionAttrNameOfAd});
                            init.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Map<String, Object> stringObjectMap = stringMapMap.get(substring);
                        time = (String) stringObjectMap.get(UserChangeTimeOfAd);
                        String  locaion = (String) stringObjectMap.get(OrganazitionAttrNameOfAd);
                        returnMap.put("time",time);
                        if (StringUtils.isBlank(locaion)){
                            StrEmployee byJiraUserKey = strEmployeeService.getByJiraUserKey(substring);
                            if (byJiraUserKey != null){
                                StrOrganize strOrganize = byJiraUserKey.getStrOrganize();
                                if (strOrganize != null) {
                                    locaion = strOrganize.getName();
                                }else {
                                    locaion ="";
                                }
                            }else {
                                locaion ="";
                            }
                        }
                        returnMap.put("location",locaion);
                        return returnMap;
                    }else {
                        return null;
                    }
                }
            }
        }else {
            Date updatedDate = ((OfBizUser) user).getUpdatedDate();
            time =updatedDate.toString();
            String locations ="";
            StrEmployee byJiraUserKey = strEmployeeService.getByJiraUserKey(substring);
            if (byJiraUserKey != null) {
                StrOrganize strOrganize = byJiraUserKey.getStrOrganize();
                if (strOrganize != null) {
                    String name = strOrganize.getName();
                    locations = name;
                    returnMap.put("time",time);
                    returnMap.put("location",locations);
                    return returnMap;
                } else {
                    Set<String> OrganizationName = new HashSet();
                    //OrganizationName.add("中国建设银行");
                    Set<String> DepartmentName = new HashSet();
                    //DepartmentName.add("开发部");
                    Set<String> OrganizationPathName = new HashSet();
                    Set<String> StaffIDName = new HashSet();
                    // StaffIDName.add("李玟");
                    Set<String> ReporterStaffId = new HashSet();
                    //ReporterStaffId.add("1");
                    Set<String> ReporterDepartmentId = new HashSet();
                    //ReporterDepartmentId.add("1");
                    Set<String> ReporterOrganizationId = new HashSet();
                    // ReporterOrganizationId.add("1");

                    List<StrOrganizaitonBean> m_strOrganizaiton = strOrganizeService.getOrgAndDept();
                    int f = byJiraUserKey.getID();

                    int g = strEmployeeService.getByEmployeeId(Integer.toString(f));
                    boolean isfirstcompany = setParent(OrganizationPathName, OrganizationName, DepartmentName, ReporterDepartmentId, ReporterOrganizationId, m_strOrganizaiton, "d_" + Integer.toString(g));
                    locations = String.valueOf(OrganizationName.toArray()[0]);
                    returnMap.put("time",time);
                    returnMap.put("location",locations);
                    return returnMap;
                }

            }else {
                return null;
            }

        }
        return null;
    }
    private static boolean setParent(Set<String> Organizationpathname, Set<String> OrganizationName, Set<String> DepartmentName,
                                     Set<String> ReporterDepartmentId, Set<String> ReporterOrganizationId,
                                     List<StrOrganizaitonBean> lstOrganizaiton, String id) {
        String pranetId = "";
        for (StrOrganizaitonBean o : lstOrganizaiton) {
            if (id.equals(o.getId())) {
                pranetId = o.getParent();
                Organizationpathname.add(ToolsDataHelper.setRepalaceString((Integer.toString(Organizationpathname.size() + 1) + ":" + o.getName())));
                if (Organizationpathname.size() == 1 && o.getId().length() > 2 && o.getId().substring(0, 1).equals("d"))//department ,first
                {
                    ReporterDepartmentId.add(o.getId().substring(2));
                    DepartmentName.add(ToolsDataHelper.setRepalaceString(o.getName()));
                }
                if (o.getId().length() > 2 && o.getId().substring(0, 1).equals("o"))//Organization last
                {
                    ReporterOrganizationId.clear();
                    ReporterOrganizationId.add(o.getId().substring(2));
                    OrganizationName.clear();
                    OrganizationName.add(ToolsDataHelper.setRepalaceString(o.getName()));
                }

                if (pranetId != null && pranetId.length() > 2 && ((!"0".equals(pranetId.substring(2)))) && !(pranetId.equals(o.getId()))) {
                    setParent(Organizationpathname, OrganizationName, DepartmentName,
                            ReporterDepartmentId, ReporterOrganizationId,
                            lstOrganizaiton, pranetId);
                    return true;
                }
                break;
            }
        }
        return false;
    }
    @GET
    @Path("{id}")
    public Response getInfo(@PathParam("id")String id){
        StrEmployee employee = strEmployeeService.getEmployee(Integer.parseInt(id));
        Map map = new HashMap();
        map.put(employee.getID(),employee.getStrOrganize().getID());
        map.put(employee.getLdapId(),employee.getJiraUserKey());
        return Response.ok(map).build();
    }
    @GET
    @Path("info")
    public Response getInfog(){
        String varsss = GlobalConfig.varsss;
       /* StrEmployee employee = strEmployeeService.getemployee(Integer.parseInt(id));*/
        Map map = new HashMap();
        map.put("info",varsss);
        return Response.ok(map).build();
    }
}
