package com.work.plugin.listener;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Option;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.GlobalPermissionType;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
//import com.atlassian.scheduler.compat.CompatibilityPluginScheduler;
import com.work.plugin.customfield.OrganizationCustomFieldsService;
import com.work.plugin.report.SetReportService;
import com.work.plugin.rest.StrOrganizaitonBean;
import com.work.plugin.util.ToolsDataHelper;
import com.work.plugin.util.license.license.GlobalConfig;
import com.work.plugin.util.license.license.LicenseService;
import com.work.plugin.util.license.license.PRLInfo;
import com.work.plugin.ao.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.var;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class IssueCreatedEventListener implements LifecycleAware,InitializingBean, DisposableBean {

    private final EventPublisher eventPublisher;
    private final IssueManager issueManager;
    private final LabelManager labelManager;
    private final GroupManager groupManager;
    private final UserManager userManager;
    private final StrEmployeeService strEmployeeService;
    //private final OrganizationCustomFieldService organizationCustomFieldService;
    private final OrganizationCustomFieldsService organizationCustomFieldsService;
    private final StrOrganizeService strOrganizeService;
    private final StrEmployeeService struGroupOfEmployeeService;
    private final CustomFieldManager customFieldManager;
    private final OrganizationAOService orgAOService;
    private final MemberAOService memberAOService;
    private final LSDataAOService lSDataAOService;
    private final LicenseService licenseService;
    private final SetReportService setReportService;
    private final StrPowerRoleService strPowerRoleService;
    private final GlobalPermissionManager globalPermissionManager;
    private final ProjectRoleManager projectRoleManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final CrowdService crowdService;
    /* private final GroupService groupService;
   private final JiraServiceContext jiraServiceContext;*/
    private final ActiveObjects ao;
    private  boolean setParent(Set<String> Organizationpathname,Set<String> OrganizationName,Set<String> DepartmentName,
                               Set<String> ReporterDepartmentId,Set<String> ReporterOrganizationId,
                               List<StrOrganizaitonBean> lstOrganizaiton,String id){
        String pranetId="";
        for (StrOrganizaitonBean o:lstOrganizaiton) {
            if(id.equals(o.getId())){
                pranetId=o.getParent();
                Organizationpathname.add(ToolsDataHelper.setRepalaceString((Integer.toString(Organizationpathname.size()+1)+":"+o.getName())));
                if(Organizationpathname.size()==1&&o.getId().length()>2&&o.getId().substring(0,1).equals("d"))//department ,first
                {
                    ReporterDepartmentId.add(o.getId().substring(2));
                    DepartmentName.add(ToolsDataHelper.setRepalaceString(o.getName()));
                }
                if(o.getId().length()>2&&o.getId().substring(0,1).equals("o"))//Organization last
                {
                    ReporterOrganizationId.clear();
                    ReporterOrganizationId.add(o.getId().substring(2));
                    OrganizationName.clear();
                    OrganizationName.add(ToolsDataHelper.setRepalaceString(o.getName()));
                }

                if(pranetId!=null&&pranetId.length()>2&&( (!"0".equals(pranetId.substring(2))) )&&!(pranetId.equals(o.getId())) ) {
                    setParent(Organizationpathname,OrganizationName,DepartmentName,
                            ReporterDepartmentId,ReporterOrganizationId,
                            lstOrganizaiton,pranetId);
                    return true;
                }
                break;
            }
        }
        return false;
    }



    @Override
    public void onStop()
    {
        scheduleWay.ShutDownAllTask();
        scheduleWay.shutDownInitTask();
    }

    @EventListener
    public void onUserEvent(UserEvent event) {
        int eventTypeId = event.getEventType();

        if(  eventTypeId== UserEventType.USER_CREATED){
            String admin_user = "admin";
            String admin_group = "jira-administrators";
            Collection<Group> lstg = groupManager.getAllGroups();
            for (Group g : lstg
            ) {
                if (g.getName() != null && !"".equals(g.getName())) {
                    if (g.getName().toLowerCase().indexOf("administ") >= 0) {
                        admin_group = g.getName();

                        if (g.getName().toLowerCase().indexOf("jira-administrators") >= 0)
                            break;
                    }
                }

            }

            Set<ApplicationUser> lstu = userManager.getAllUsers();
            for (ApplicationUser u : lstu
            ) {
                if (u.getId() == 10000 || u.getId() == 1) {
                    admin_user = u.getKey();
                    break;
                }

            }
            Object oldValue = null;
            ApplicationUser u=event.getUser();
            StrEmployee otherUser = strEmployeeService.creatUser(u.getDisplayName());
            GlobalConfig.printDebug("1111111111111111111111111111111111111111111_4");
            StrOrganize all = strOrganizeService.getByOrgId(1);
            if(all!=null) {
                otherUser.setStrOrganize(all);
            }
            otherUser.setJiraUserKey(u.getKey());
            otherUser.save();
            Collection<ApplicationUser> us =groupManager.getUsersInGroup(admin_group);

            if(us.contains(u)) {
                RoleEntity systemAdministrator = roleService.creatRole("System Administrator", 1, "System Administrator Des", 10000);
                roleService.setRoleToEmployee(systemAdministrator, otherUser);
            }else{
                RoleEntity GeneralStaff=roleService.creatRole("General Staff",1,"General Staff Des",10800);
                roleService.setRoleToEmployee(GeneralStaff, otherUser);
            }
        }
    }

    @Override
    public void onStart() {
        String admin_user = "admin";
        String admin_group = "jira-administrators";
        Collection<Group> lstg = groupManager.getAllGroups();
        for (Group g : lstg
        ) {
            if (g.getName() != null && !"".equals(g.getName())) {
                if (g.getName().toLowerCase().indexOf("administ") >= 0) {
                    admin_group = g.getName();

                    if (g.getName().toLowerCase().indexOf("jira-administrators") >= 0)
                        break;
                }
            }

        }

        Set<ApplicationUser> lstu = userManager.getAllUsers();
        for (ApplicationUser u : lstu
        ) {
            if (u.getId() == 10000 || u.getId() == 1) {
                admin_user = u.getKey();
                break;
            }

        }
        if (!GlobalConfig.getIsDebug()) {
            ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
            if (loggedInUser == null) return;
            admin_user = loggedInUser.getKey();
        }
        //roleService.maintainData("Project developers","Project Deputy Leader","Project Testers","Manager","Ordinary Members","Chief Financial Officer","Accounting","Cashier","Guest","Project leader");
        //组织雇员 映射 jira
        //初始化角色表 TODO  更改为英文初始化
        RoleEntity systemAdministrator = roleService.creatRole("System Administrator", 1, "System Administrator Des", 10000);
        StrOrganize other = strOrganizeService.getByOrgName("all");
        StrOrganize all = strOrganizeService.getByOrgName("all");
        if (all == null ){
            all = strOrganizeService.createByInfo("all", 0, 1, "", 0, 0, "0", "1");
            GlobalConfig.printDebug("1111111111111111111111111111111111111111111_3");
        }
        StrEmployee initAdmin = strEmployeeService.creatUserOnly("Admin");
        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_4");
        initAdmin.setStrOrganize(all);
        initAdmin.setJiraUserKey(admin_user);
        initAdmin.save();
        roleService.setRoleToEmployee(systemAdministrator,initAdmin);
        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_5");

        RoleEntity GeneralStaff=roleService.creatRole("General Staff",1,"General Staff Des",10800);

        if (other == null ){
            Set<ApplicationUser> lstuOther = userManager.getAllUsers();
            for (ApplicationUser u : lstuOther
            ) {
                if (u.getId() == 10000 || u.getId() == 1) {

                } else {
                    StrEmployee otherUser = strEmployeeService.creatUser(u.getDisplayName());
                    GlobalConfig.printDebug("1111111111111111111111111111111111111111111_4");
                    otherUser.setStrOrganize(all);
                    otherUser.setJiraUserKey(u.getKey());
                    otherUser.save();
                    Collection<ApplicationUser> us =groupManager.getUsersInGroup(admin_group);
                    if(us.contains(u)) {
                        roleService.setRoleToEmployee(systemAdministrator, otherUser);
                    }else{
                        roleService.setRoleToEmployee(GeneralStaff, otherUser);
                    }

                }
            }
        }





        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_6");
//        RoleEntity ItOperationDimension=roleService.creatRole("It Operation",1,"It Operation Des",10100);
        RoleEntity TopExecutives=roleService.creatRole("Top Executives",1,"Top Executives Des",10200);
//        RoleEntity DepartmentManager=roleService.creatRole("Department Manager",1,"Department Manager Des",10300);
//        RoleEntity ProjectManagerSupervisor=roleService.creatRole("Project Managers",1,"Project Managers Des",10400);
//        RoleEntity ProductManager=roleService.creatRole("Product Manager",2,"Product Manager Des",20000);
//        RoleEntity ProjectDirector=roleService.creatRole("Project Manager",2,"Project Manager Des",20001);
//        RoleEntity Testsupervisor=roleService.creatRole("Test supervisor",2,"Test supervisor Des",20002);
//        RoleEntity Developer=roleService.creatRole("Developer",2,"Developer Des",20003);
//        RoleEntity ProductTesters=roleService.creatRole("Product Tester",2,"Product Tester Des",20004);
//        List<RoleEntity> projectRoleLists= new LinkedList<>();
//        projectRoleLists.add(ProductManager);
//        projectRoleLists.add(ProjectDirector);
//        projectRoleLists.add(Testsupervisor);
//        projectRoleLists.add(Developer);
//        projectRoleLists.add(ProductTesters);
//        projectRoleLists.stream()
//                .filter(roleEntity -> projectRoleManager.isRoleNameUnique(roleEntity.getName()))
//                .forEach(roleEntity -> roleService.createProjectRoleOfJiraInnerByCustomRole(roleEntity));

        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_8");
//        SetReport[] reports3 = ao.find(SetReport.class," INIT_TYPE = 'init'");
//        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_8_0");
//        SetReport[] reports = ao.find(SetReport.class,String.format(" INIT_TYPE = '%s'","init"));
//        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_8-1");
//        SetReport[] reports1 = ao.find(SetReport.class,String.format(" %s = ? ","INIT_TYPE"),"init");
//
//        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_8-2");
        //初始化报表设�?
        setReportService.initReport();
        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_8-3");
        setReportService.initReportColumn();
        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_8-4");
        setReportService.initShareReport();
        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_9");
        // userManager;
//        if (!orgAOService.isExistpermissionMgr("gsryld")) {
//            val entity=orgAOService.create("Reports", "company staff incumbency", "gsryld");
//            memberAOService.create(entity.getID(), admin_user, entity.getMenuNameid(), 1);
//            memberAOService.create(entity.getID(), admin_group, entity.getMenuNameid(), 2);
//        }
//        if (!orgAOService.isExistpermissionMgr("bmryfb")) {
//            val entity=orgAOService.create("Reports", "department staff distribution", "bmryfb");
//            memberAOService.create(entity.getID(), admin_user, entity.getMenuNameid(), 1);
//            memberAOService.create(entity.getID(), admin_group, entity.getMenuNameid(), 2);
//        }
        if (!orgAOService.isExistpermissionMgr("zzjggl")) {
            val entity=orgAOService.create("management", "organization manage", "zzjggl");
            memberAOService.create(entity.getID(), admin_user, entity.getMenuNameid(), 1);
            memberAOService.create(entity.getID(), admin_group, entity.getMenuNameid(), 2);
        }
/*        if (!orgAOService.isExistpermissionMgr("permissionMgr")) {
            val entity=orgAOService.create("management", "permission  manage", "permissionMgr");
            memberAOService.create(entity.getID(), admin_user, entity.getMenuNameid(), 1);
            memberAOService.create(entity.getID(), admin_group, entity.getMenuNameid(), 2);
        }*/

//        if (!orgAOService.isExistpermissionMgr("tdryfb")) {
//            val entity=orgAOService.create("Reports", "team staff distribution", "tdryfb");
//            memberAOService.create(entity.getID(), admin_user, entity.getMenuNameid(), 1);
//            memberAOService.create(entity.getID(), admin_group, entity.getMenuNameid(), 2);
//        }
//        if (!orgAOService.isExistpermissionMgr("tdrygz")) {
//            val entity=orgAOService.create("Reports", "team staff work", "tdrygz");
//            memberAOService.create(entity.getID(), admin_user, entity.getMenuNameid(), 1);
//            memberAOService.create(entity.getID(), admin_group, entity.getMenuNameid(), 2);
//        }
//        if (!orgAOService.isExistpermissionMgr("bmrygz")) {
//            val entity=orgAOService.create("Reports", "department staff work", "bmrygz");
//            memberAOService.create(entity.getID(), admin_user, entity.getMenuNameid(), 1);
//            memberAOService.create(entity.getID(), admin_group, entity.getMenuNameid(), 2);
//        }
//        if (!orgAOService.isExistpermissionMgr("adsjtb")) {
//            val entity=orgAOService.create("management", "synchronous data", "adsjtb");
//            memberAOService.create(entity.getID(), admin_user, entity.getMenuNameid(), 1);
//            memberAOService.create(entity.getID(), admin_group, entity.getMenuNameid(), 2);
//        }
        if (!orgAOService.isExistpermissionMgr("setReport")) {
            val entity=orgAOService.create("management", "set report", "setReport");
            memberAOService.create(entity.getID(), admin_user, entity.getMenuNameid(), 1);
            memberAOService.create(entity.getID(), admin_group, entity.getMenuNameid(), 2);
        }
        if (!orgAOService.isExistpermissionMgr("showReport")) {
            val entity=orgAOService.create("management", "show report", "showReport");
            memberAOService.create(entity.getID(), admin_user, entity.getMenuNameid(), 1);
            memberAOService.create(entity.getID(), admin_group, entity.getMenuNameid(), 2);
        }
        if (!orgAOService.isExistpermissionMgr("qxkznew")) {
            val entity=orgAOService.create("management", "qxkz new ", "qxkznew");
            memberAOService.create(entity.getID(), admin_user, entity.getMenuNameid(), 1);
            memberAOService.create(entity.getID(), admin_group, entity.getMenuNameid(), 2);
        }
//        if (!orgAOService.isExistpermissionMgr("lowerissues")) {
//            val entity=orgAOService.create("Reports", "lower issues", "lowerissues");
//            memberAOService.create(entity.getID(), admin_user, entity.getMenuNameid(), 1);
//            memberAOService.create(entity.getID(), admin_group, entity.getMenuNameid(), 2);
//        }
//        String dd2 = licenseService.getReleaseData("comments");
//        if(dd2!=null&&!dd2.equals("")){
//            List<PRLInfo> lstPRLInfo = licenseService.getPRLInfofromByte(dd2);
//
//            try {
//                saveValidLicense(dd2);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        }

        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_10");
//        List<Directory> allDirectories=crowdDirectoryService.findAllDirectories();
//        scheduleWay.startAllTask(taskMap, allDirectories, ldapSyncTimeService);

        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_11");
        //Get the appropriate menu and authorize the initialized role ;Initialize permissions to Roles
//        OrganizationEntity gsryld=orgAOService.get("gsryld");
//        OrganizationEntity tdryfb=orgAOService.get("tdryfb");
//        OrganizationEntity adsjtb=orgAOService.get("adsjtb");
//        OrganizationEntity tdrygz=orgAOService.get("tdrygz");
        //OrganizationEntity permissionMgr=orgAOService.get("permissionMgr");
        OrganizationEntity zzjggl=orgAOService.get("zzjggl");
//        OrganizationEntity bmryfb=orgAOService.get("bmryfb");
//        OrganizationEntity bmrygz=orgAOService.get("bmrygz");
        OrganizationEntity setReport=orgAOService.get("setReport");
        OrganizationEntity showReport=orgAOService.get("showReport");
        OrganizationEntity qxkznew=orgAOService.get("qxkznew");
//        OrganizationEntity lowerissues=orgAOService.get("lowerissues");
//        int id=gsryld.getID();
        if (systemAdministrator != null){
            String name=systemAdministrator.getName();
            Integer type=2;
//            memberAOService.create(id, name,gsryld.getMenuNameid(), type);
//            memberAOService.create(tdryfb.getID(), name,tdryfb.getMenuNameid(), type);
//            memberAOService.create(adsjtb.getID(), name,adsjtb.getMenuNameid(), type);
//            memberAOService.create(tdrygz.getID(), name,tdrygz.getMenuNameid(), type);
            memberAOService.create(zzjggl.getID(), name,zzjggl.getMenuNameid(), type);
//            memberAOService.create(bmryfb.getID(), name,bmryfb.getMenuNameid(), type);
//            memberAOService.create(bmrygz.getID(), name,bmrygz.getMenuNameid(), type);
            //memberAOService.create(permissionMgr.getID(), name,permissionMgr.getMenuNameid(), type);
            memberAOService.create(setReport.getID(), name,setReport.getMenuNameid(), type);
            memberAOService.create(showReport.getID(), name,showReport.getMenuNameid(), type);
            memberAOService.create(qxkznew.getID(), name,qxkznew.getMenuNameid(), type);
//            memberAOService.create(lowerissues.getID(), name,lowerissues.getMenuNameid(), type);
        }
//        if (ItOperationDimension != null){
//            Integer managerType=2;
//            String managerName=ItOperationDimension.getName();
//            memberAOService.create(id, managerName,gsryld.getMenuNameid(), managerType);
//            memberAOService.create(adsjtb.getID(), managerName,adsjtb.getMenuNameid(), managerType);
//            memberAOService.create(bmrygz.getID(), managerName,bmrygz.getMenuNameid(), managerType);
//            memberAOService.create(tdrygz.getID(), managerName,tdrygz.getMenuNameid(), managerType);
            //memberAOService.create(permissionMgr.getID(), managerName,permissionMgr.getMenuNameid(), managerType);
//            memberAOService.create(bmryfb.getID(), managerName,bmryfb.getMenuNameid(), managerType);
//            memberAOService.create(tdryfb.getID(), managerName,tdryfb.getMenuNameid(), managerType);
//            memberAOService.create(zzjggl.getID(), managerName,zzjggl.getMenuNameid(), managerType);
//            memberAOService.create(qxkznew.getID(), managerName,qxkznew.getMenuNameid(), managerType);
           // memberAOService.create(lowerissues.getID(), managerName,lowerissues.getMenuNameid(), managerType);
//        }
        if (TopExecutives != null) {
            Integer chiefFinancialOfficertype = 2;
            String chiefFinancialOfficerName = TopExecutives.getName();
//            memberAOService.create(id, chiefFinancialOfficerName, gsryld.getMenuNameid(), chiefFinancialOfficertype);
//            memberAOService.create(bmrygz.getID(), chiefFinancialOfficerName, bmrygz.getMenuNameid(), chiefFinancialOfficertype);
//            memberAOService.create(bmryfb.getID(), chiefFinancialOfficerName, bmryfb.getMenuNameid(), chiefFinancialOfficertype);
            memberAOService.create(zzjggl.getID(), chiefFinancialOfficerName, zzjggl.getMenuNameid(), chiefFinancialOfficertype);
            memberAOService.create(setReport.getID(), chiefFinancialOfficerName, setReport.getMenuNameid(), chiefFinancialOfficertype);
            memberAOService.create(showReport.getID(), chiefFinancialOfficerName, showReport.getMenuNameid(), chiefFinancialOfficertype);
        }
//        if (DepartmentManager!=null){
//            Integer accountingtype=2;
//            String accountingName=DepartmentManager.getName();
           // memberAOService.create(id, accountingName,gsryld.getMenuNameid(), accountingtype);
   //         memberAOService.create(zzjggl.getID(), accountingName,zzjggl.getMenuNameid(), accountingtype);
           // memberAOService.create(bmryfb.getID(), accountingName,bmryfb.getMenuNameid(), accountingtype);
          //  memberAOService.create(bmrygz.getID(), accountingName,bmrygz.getMenuNameid(), accountingtype);
    //    }
//        if(ProjectManagerSupervisor!=null){
//            Integer cashierType=2;
//            String cashierName=ProjectManagerSupervisor.getName();
       //     memberAOService.create(zzjggl.getID(), cashierName,zzjggl.getMenuNameid(), cashierType);
//            memberAOService.create(id, cashierName,gsryld.getMenuNameid(), cashierType);
//            memberAOService.create(tdryfb.getID(), cashierName,tdryfb.getMenuNameid(), cashierType);
//            memberAOService.create(tdrygz.getID(), cashierName,tdrygz.getMenuNameid(), cashierType);
      //  }
       /* if (ProjectDirector!=null){
        String projectTestersName=ProjectDirector.getName();
        Integer projectTestersType=2;
        memberAOService.create(bmrygz.getID(), projectTestersName,bmrygz.getMenuNameid(), projectTestersType);
        memberAOService.create(tdrygz.getID(), projectTestersName,tdrygz.getMenuNameid(), projectTestersType);
        memberAOService.create(zzjggl.getID(), projectTestersName,zzjggl.getMenuNameid(), projectTestersType);
        memberAOService.create(bmryfb.getID(), projectTestersName,bmryfb.getMenuNameid(), projectTestersType);
        memberAOService.create(tdryfb.getID(), projectTestersName,tdryfb.getMenuNameid(), projectTestersType);
        }
        if (ProductTesters!=null){
        String projectDevelopersName=ProductTesters.getName();
        Integer projectDevelopersType=2;
        memberAOService.create(bmrygz.getID(), projectDevelopersName,bmrygz.getMenuNameid(), projectDevelopersType);
        memberAOService.create(tdrygz.getID(), projectDevelopersName,tdrygz.getMenuNameid(), projectDevelopersType);
        memberAOService.create(zzjggl.getID(), projectDevelopersName,zzjggl.getMenuNameid(), projectDevelopersType);
        memberAOService.create(bmryfb.getID(), projectDevelopersName,bmryfb.getMenuNameid(), projectDevelopersType);
        memberAOService.create(tdryfb.getID(), projectDevelopersName,tdryfb.getMenuNameid(), projectDevelopersType);
        }
        if (ProductManager!=null){
        String projectleaderName=ProductManager.getName();
        Integer projectleaderType=2;
        memberAOService.create(bmrygz.getID(), projectleaderName,bmrygz.getMenuNameid(), projectleaderType);
        memberAOService.create(tdrygz.getID(), projectleaderName,tdrygz.getMenuNameid(), projectleaderType);
        memberAOService.create(zzjggl.getID(), projectleaderName,zzjggl.getMenuNameid(), projectleaderType);
        memberAOService.create(bmryfb.getID(), projectleaderName,bmryfb.getMenuNameid(), projectleaderType);
        memberAOService.create(tdryfb.getID(), projectleaderName,tdryfb.getMenuNameid(), projectleaderType);

        }
        if (Developer!=null){
        String projectDeputyLeaderName=Developer.getName();
        Integer projectDeputyLeaderType=2;
        memberAOService.create(bmrygz.getID(), projectDeputyLeaderName,bmrygz.getMenuNameid(), projectDeputyLeaderType);
        memberAOService.create(tdrygz.getID(), projectDeputyLeaderName,tdrygz.getMenuNameid(), projectDeputyLeaderType);
        memberAOService.create(zzjggl.getID(), projectDeputyLeaderName,zzjggl.getMenuNameid(), projectDeputyLeaderType);
        memberAOService.create(bmryfb.getID(), projectDeputyLeaderName,bmryfb.getMenuNameid(), projectDeputyLeaderType);
        memberAOService.create(tdryfb.getID(), projectDeputyLeaderName,tdryfb.getMenuNameid(), projectDeputyLeaderType);
        }*/
        if (GeneralStaff!=null){
//            memberAOService.create(bmrygz.getID(),GeneralStaff.getName(),bmrygz.getMenuNameid(),2);
//            memberAOService.create(bmryfb.getID(),GeneralStaff.getName(),bmryfb.getMenuNameid(),2);
        }
        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_12");

        //分配给角色每个菜单的详细权限
        RoleEntity[] roleEntities = {systemAdministrator, TopExecutives,GeneralStaff};
        setOrgMenuPower(zzjggl,Arrays.asList(roleEntities));
//        setBmryfbPower(bmryfb,Arrays.asList(roleEntities));
//        setBmrygzPower(bmrygz,Arrays.asList(roleEntities));
        setSetReportPower(setReport,Arrays.asList(roleEntities));
        setShowReport(showReport,Arrays.asList(roleEntities));
//        setAdsjtb(adsjtb,Arrays.asList(roleEntities));
//        setGsryld(gsryld,Arrays.asList(roleEntities));
//        setTdryfb(tdryfb,Arrays.asList(roleEntities));
//        setTdrygz(tdrygz,Arrays.asList(roleEntities));
        setQxkznew(qxkznew,Arrays.asList(roleEntities));
       // setLowerissues(lowerissues,Arrays.asList(roleEntities));
        //初始化项目的权限，以及相应的角色权限
        //PowerEntity allpermissions = initProPower("allpermissions");
//        PowerEntity allpermissionsproject = initProPower("allpermissionsproject");
//        PowerEntity subpermissions = initProPower("subpermissions");
//        PowerEntity suppermissions = initProPower("suppermissions");
        //   setPermissionsPower(allpermissions,ProductManager,ProjectDirector);
//        setPermissionsPower(allpermissionsproject,ProductManager,ProjectDirector);
//        setPermissionsPower(subpermissions,ProductManager,ProjectDirector,Testsupervisor);
//        setPermissionsPower(suppermissions,ProductManager,ProjectDirector,Testsupervisor,Developer,ProductTesters);
        System.out.println("it 9 ok ");
        GlobalConfig.printDebug("1111111111111111111111111111111111111111111_13");
        /*删除systemAdministrator GeneralStaff  group  取消其全局权限*/
//        String delGroupName1 = "System Administrator";
//        String delGroupName2 = "General Staff";
//        delGroup(delGroupName1);
//        delGroup(delGroupName2);
        /*删除systemAdministrator GeneralStaff  group  取消其全局权限*/
    }
    @EventListener
    public void onIssueEvent(IssueEvent event) {
        val eventTypeId = event.getEventTypeId();
        val issue = issueManager.getIssueByCurrentKey(event.getIssue().getKey());

        if( (eventTypeId.equals(EventType.ISSUE_CREATED_ID))|| (eventTypeId.equals(EventType.ISSUE_ASSIGNED_ID)||eventTypeId.equals(EventType.ISSUE_UPDATED_ID)) ){

            if(!(issue==null||issue.getReporterId()==null||issue.getReporterId().isEmpty())) {
                val customFields = organizationCustomFieldsService.getOrganizationCustomFields("Reporter");
                //  val names = strEmployeeService.getByJiraId(issue.getReporter().getId().toString());
                //  if (names.length == 0)
                //      return;
                Set<String> OrganizationName=  new HashSet();
                //OrganizationName.add("中国建设银行");
                Set<String> DepartmentName=  new HashSet();
                //DepartmentName.add("开发部");
                Set<String> OrganizationPathName=  new HashSet();
                Set<String> StaffIDName=  new HashSet();
                // StaffIDName.add("李玟");
                Set<String> ReporterStaffId=  new HashSet();
                //ReporterStaffId.add("1");
                Set<String> ReporterDepartmentId=  new HashSet();
                //ReporterDepartmentId.add("1");
                Set<String> ReporterOrganizationId=  new HashSet();
                // ReporterOrganizationId.add("1");

                List<StrOrganizaitonBean> m_strOrganizaiton=strOrganizeService.getOrgAndDept();
                var m_strEmployee= strEmployeeService.getByJiraId(issue.getReporterId()/*Long.toString(issue.getReporter().getId())*/);
                if(null!=m_strEmployee&&m_strEmployee.length>0)
                {
                    int f=m_strEmployee[0].getID();
                    ReporterStaffId.add(Integer.toString(f));
                    StaffIDName.add(ToolsDataHelper.setRepalaceString(m_strEmployee[0].getEmployeeName()));

                    int g=struGroupOfEmployeeService.getByEmployeeId(Integer.toString(f));
                    if(g==0) {
                        StrEmployee[]  _strEmploye=strEmployeeService.getByJiraId(issue.getReporterId());
                        if(_strEmploye!=null&&_strEmploye.length>0&&_strEmploye[0].getStrOrganize()!=null) {

                            boolean isfirstcompany = setParent(OrganizationPathName, OrganizationName, DepartmentName, ReporterDepartmentId, ReporterOrganizationId, m_strOrganizaiton, "o_" + Integer.toString(_strEmploye[0].getStrOrganize().getID()));
                         /*  int ccc=OrganizationPathName.size()+1;
                            Object [] ss=OrganizationPathName.toArray();
                            OrganizationPathName.clear();
                            for (int ii=0;ii<ccc-1;ii++)
                                  {
                                //if(!ss[ii].toString().substring(0,1).equals("1"))
                                { String dd=Integer.toString(ccc-Integer.parseInt(ss[ii].toString().substring(0,1)))+ss[ii].toString().substring(1);
                                    OrganizationPathName.add(dd);
                                }
                            }*/
                        }
                        ;
                    }else
                    {
                        boolean isfirstcompany = setParent(OrganizationPathName, OrganizationName, DepartmentName, ReporterDepartmentId, ReporterOrganizationId, m_strOrganizaiton, "d_" + Integer.toString(g));
                       /* int ccc=OrganizationPathName.size()+1;
                        Object [] ss=OrganizationPathName.toArray();
                        OrganizationPathName.clear();
                        for (int ii=0;ii<ccc-1;ii++)
                        {

                            { String dd=Integer.toString(ccc-Integer.parseInt(ss[ii].toString().substring(0,1)))+ss[ii].toString().substring(1);
                                OrganizationPathName.add(dd);
                            }
                        }*/

                    }
                }

                /*
                OrganizationPathName.add("1:中国建设银行");
                OrganizationPathName.add("2:四川建行");
                OrganizationPathName.add("3:成华分理�???");
                OrganizationPathName.add("4:开发部");
                OrganizationPathName.add("5:二组");
           */

                Set<String> OrganizationPathName1=  new HashSet();
                int maxO=OrganizationPathName.size();
                int iOrganizationPath=0;
                for (String OrganizationPath:OrganizationPathName
                ) {
                    iOrganizationPath=Integer.parseInt( OrganizationPath.split(":")[0]);
                    OrganizationPathName1.add( Integer.toString(1+maxO-iOrganizationPath)+":"+OrganizationPath.split(":")[1]);

                }
                OrganizationPathName.clear();
                OrganizationPathName=OrganizationPathName1;

                //names[0].getEmployeeName()
                for (CustomField customField:customFields) {
                    // ,Department,OrganizationPath,StaffID
                    if("ReporterOrganization".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),OrganizationName, false, true);
                    else if("ReporterDepartment".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),DepartmentName, false, true);
                    else if("ReporterOrganizationPath".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),OrganizationPathName, false, true);
                    else if("ReporterStaffName".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),StaffIDName, false, true);
                    else if("ReporterStaffId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),ReporterStaffId, false, true);
                    else if("ReporterDepartmentId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),ReporterDepartmentId, false, true);
                    else if("ReporterOrganizationId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),ReporterOrganizationId, false, true);
                }
            }else if(issue==null|| issue.getReporterId()==null || issue.getReporterId().isEmpty())
            {
                val customFields = organizationCustomFieldsService.getOrganizationCustomFields("Reporter");
                Set<String> OrganizationName=  new HashSet();
                Set<String> DepartmentName=  new HashSet();
                Set<String> OrganizationPathName=  new HashSet();
                Set<String> StaffIDName=  new HashSet();
                Set<String> ReporterStaffId=  new HashSet();
                Set<String> ReporterDepartmentId=  new HashSet();
                Set<String> ReporterOrganizationId=  new HashSet();
                Set<String> AssigneeStaffId=  new HashSet();
                Set<String> AssigneeDepartmentId=  new HashSet();
                Set<String> AssigneeOrganizationId=  new HashSet();
                for (CustomField customField:customFields) {
                    if("AssigneeOrganization".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),OrganizationName, false, true);
                    else if("AssigneeDepartment".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),DepartmentName, false, true);
                    else if("AssigneeOrganizationPath".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),OrganizationPathName, false, true);
                    else if("AssigneeStaffName".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),StaffIDName, false, true);
                    else if("AssigneeStaffId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeStaffId, false, true);
                    else if("AssigneeDepartmentId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeDepartmentId, false, true);
                    else if("AssigneeOrganizationId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeOrganizationId, false, true);
                }

            }
            if(!(issue==null||issue.getAssigneeId()==null||issue.getAssigneeId().isEmpty())) {
                val customFields = organizationCustomFieldsService.getOrganizationCustomFields("Assignee");

                CustomField cf = customFieldManager.getCustomFieldObjectByName("AssigneeStaffId");
                if(cf!=null&&cf.getValue(issue)!=null){

                    //  Collection<Object> customFieldValue = (Collection) cf.getValue(currIssue);
                    for (Object a:(Collection)cf.getValue(issue)){
                        if( issue.getAssigneeId().equals(struGroupOfEmployeeService.getEmployee(Integer.parseInt(a.toString())).getJiraUserKey())) {
                            return;
                        }
                    }

                    // reporterStaffId = cf.getValue(currIssue).toString();
                }


                Set<String> OrganizationName=  new HashSet();
                //OrganizationName.add("上海图书�???");
                Set<String> DepartmentName=  new HashSet();
                //DepartmentName.add("技术部");
                Set<String> OrganizationPathName=  new HashSet();
                //OrganizationPathName.add("上海图书�??? > "+"徐汇图书�??? > "+"大浦桥营业部 > "+"技术部");
               /* OrganizationPathName.add("1:上海图书�???");
                OrganizationPathName.add("2:徐汇图书�???");
                OrganizationPathName.add("3:大浦桥营业部");
                OrganizationPathName.add("4:技术部");*/
                Set<String> StaffIDName=  new HashSet();
                //StaffIDName.add("刘涛");
                Set<String> AssigneeStaffId=  new HashSet();
                //AssigneeStaffId.add("2");
                Set<String> AssigneeDepartmentId=  new HashSet();
                //AssigneeDepartmentId.add("2");
                Set<String> AssigneeOrganizationId=  new HashSet();
                //  AssigneeOrganizationId.add("2");

                List<StrOrganizaitonBean> m_strOrganizaiton=strOrganizeService.getOrgAndDept();
                var m_strEmployee= strEmployeeService.getByJiraId(issue.getAssigneeId()/*Long.toString(issue.getAssignee().getId())*/);
                if(null!=m_strEmployee&&m_strEmployee.length>0)
                {
                    int f=m_strEmployee[0].getID();
                    AssigneeStaffId.add(Integer.toString(f));
                    StaffIDName.add(ToolsDataHelper.setRepalaceString(m_strEmployee[0].getEmployeeName()));
                    int g=struGroupOfEmployeeService.getByEmployeeId(Integer.toString(f));



                    if(g==0) {
                        StrEmployee[]  _strEmploye=strEmployeeService.getByJiraId(issue.getAssigneeId());
                        if(_strEmploye!=null&&_strEmploye.length>0&&_strEmploye[0].getStrOrganize()!=null) {

                            boolean isfirstcompany = setParent(OrganizationPathName, OrganizationName, DepartmentName, AssigneeDepartmentId, AssigneeOrganizationId, m_strOrganizaiton, "o_" + Integer.toString(_strEmploye[0].getStrOrganize().getID()));
                           /* int ccc=OrganizationPathName.size()+1;
                            Object [] ss=OrganizationPathName.toArray();
                            OrganizationPathName.clear();
                            for (int ii=0;ii<ccc-1;ii++)
                            {
                               // if(!ss[ii].toString().substring(0,1).equals("1"))
                                { String dd=Integer.toString(ccc-Integer.parseInt(ss[ii].toString().substring(0,1)))+ss[ii].toString().substring(1);
                                    OrganizationPathName.add(dd);
                                }
                            }*/
                        }
                        ;
                    }else {
                        boolean isfirstcompany = setParent(OrganizationPathName, OrganizationName, DepartmentName, AssigneeDepartmentId, AssigneeOrganizationId, m_strOrganizaiton, "d_" + Integer.toString(g));
                       /* int ccc=OrganizationPathName.size()+1;
                        Object [] ss=OrganizationPathName.toArray();
                        OrganizationPathName.clear();
                        for (int ii=0;ii<ccc-1;ii++)
                        {
                            { String dd=Integer.toString(ccc-Integer.parseInt(ss[ii].toString().substring(0,1)))+ss[ii].toString().substring(1);
                                OrganizationPathName.add(dd);
                            }
                        }*/
                    }
                }
                Set<String> OrganizationPathName1=  new HashSet();
                int maxO=OrganizationPathName.size();
                int iOrganizationPath=0;
                for (String OrganizationPath:OrganizationPathName
                ) {
                    iOrganizationPath=Integer.parseInt( OrganizationPath.split(":")[0]);
                    OrganizationPathName1.add( Integer.toString(1+maxO-iOrganizationPath)+":"+OrganizationPath.split(":")[1]);

                }
                OrganizationPathName.clear();
                OrganizationPathName=OrganizationPathName1;


                //names[0].getEmployeeName()
                for (CustomField customField:customFields) {
                    // ,Department,OrganizationPath,StaffID
                    if("AssigneeOrganization".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),OrganizationName, false, true);
                    else if("AssigneeDepartment".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),DepartmentName, false, true);
                    else if("AssigneeOrganizationPath".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),OrganizationPathName, false, true);
                    else if("AssigneeStaffName".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),StaffIDName, false, true);
                    else if("AssigneeStaffId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeStaffId, false, true);
                    else if("AssigneeDepartmentId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeDepartmentId, false, true);
                    else if("AssigneeOrganizationId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeOrganizationId, false, true);
                }
            } if(issue==null|| issue.getAssigneeId()==null || issue.getAssigneeId().isEmpty())
            {
                val customFields = organizationCustomFieldsService.getOrganizationCustomFields("Assignee");
                Set<String> OrganizationName=  new HashSet();
                Set<String> DepartmentName=  new HashSet();
                Set<String> OrganizationPathName=  new HashSet();
                Set<String> StaffIDName=  new HashSet();
                Set<String> AssigneeStaffId=  new HashSet();
                Set<String> AssigneeDepartmentId=  new HashSet();
                Set<String> AssigneeOrganizationId=  new HashSet();

                //names[0].getEmployeeName()
                for (CustomField customField:customFields) {
                    // ,Department,OrganizationPath,StaffID
                    if("AssigneeOrganization".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),OrganizationName, false, true);
                    else if("AssigneeDepartment".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),DepartmentName, false, true);
                    else if("AssigneeOrganizationPath".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),OrganizationPathName, false, true);
                    else if("AssigneeStaffName".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),StaffIDName, false, true);
                    else if("AssigneeStaffId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeStaffId, false, true);
                    else if("AssigneeDepartmentId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeDepartmentId, false, true);
                    else if("AssigneeOrganizationId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeOrganizationId, false, true);
                }

            }


        }
       /* if (eventTypeId.equals(EventType.ISSUE_ASSIGNED_ID)||eventTypeId.equals(EventType.ISSUE_UPDATED_ID)) {

            val customFields = organizationCustomFieldsService.getOrganizationCustomFields("Assignee");
            if(issue==null|| issue.getAssigneeId()==null || issue.getAssigneeId().isEmpty()) {
                // val names = strEmployeeService.getByJiraId(issue.getAssignee().getId().toString());
                //  if (names.length == 0)
                //      return;
                Set<String> OrganizationName=  new HashSet();

                Set<String> DepartmentName=  new HashSet();

                Set<String> OrganizationPathName=  new HashSet();

                Set<String> StaffIDName=  new HashSet();
                Set<String> AssigneeStaffId=  new HashSet();

                Set<String> AssigneeDepartmentId=  new HashSet();

                Set<String> AssigneeOrganizationId=  new HashSet();


                //names[0].getEmployeeName()
                for (CustomField customField:customFields) {
                    // ,Department,OrganizationPath,StaffID
                    if("AssigneeOrganization".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),OrganizationName, false, true);
                    else if("AssigneeDepartment".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),DepartmentName, false, true);
                    else if("AssigneeOrganizationPath".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),OrganizationPathName, false, true);
                    else if("AssigneeStaffName".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),StaffIDName, false, true);
                    else if("AssigneeStaffId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeStaffId, false, true);
                    else if("AssigneeDepartmentId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeDepartmentId, false, true);
                    else if("AssigneeOrganizationId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeOrganizationId, false, true);
                }
            }else
            {

               // val names = strEmployeeService.getByJiraId(issue.getAssignee().getId().toString());
                //  if (names.length == 0)
                //      return;
                Set<String> OrganizationName=  new HashSet();
                //OrganizationName.add("上海图书�???");
                Set<String> DepartmentName=  new HashSet();
               // DepartmentName.add("技术部");
                Set<String> OrganizationPathName=  new HashSet();
               // OrganizationPathName.add("上海图书�??? > "+"徐汇图书�??? > "+"大浦桥营业部 > "+"技术部");
               //OrganizationPathName.add("1:上海图书�???");
               // OrganizationPathName.add("2:徐汇图书�???");
               // OrganizationPathName.add("3:大浦桥营业部");
               // OrganizationPathName.add("4:技术部");
                Set<String> StaffIDName=  new HashSet();
               // StaffIDName.add("刘涛");
                Set<String> AssigneeStaffId=  new HashSet();
               // AssigneeStaffId.add("2");
                Set<String> AssigneeDepartmentId=  new HashSet();
               // AssigneeDepartmentId.add("2");
                Set<String> AssigneeOrganizationId=  new HashSet();
               // AssigneeOrganizationId.add("2");


                List<StrOrganizaitonBean> m_strOrganizaiton=strOrganizeService.getOrgAndDept();
                var m_strEmployee= strEmployeeService.getByJiraId(Long.toString(issue.getAssignee().getId()));
                if(null!=m_strEmployee&&m_strEmployee.length>0)
                {
                    int f=m_strEmployee[0].getID();
                    AssigneeStaffId.add(Integer.toString(f));
                    StaffIDName.add(ToolsDataHelper.setRepalaceString(m_strEmployee[0].getEmployeeName()));
                    int g=struGroupOfEmployeeService.getByEmployeeId(Integer.toString(f));
                    boolean isfirstcompany=setParent(OrganizationPathName,OrganizationName,DepartmentName,AssigneeDepartmentId,AssigneeOrganizationId,m_strOrganizaiton,"d_"+Integer.toString(g));
                }
                //names[0].getEmployeeName()
                for (CustomField customField:customFields) {
                    // ,Department,OrganizationPath,StaffID
                    if("AssigneeOrganization".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),OrganizationName, false, true);
                    else if("AssigneeDepartment".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),DepartmentName, false, true);
                    else if("AssigneeOrganizationPath".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),OrganizationPathName, false, true);
                    else if("AssigneeStaffName".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),StaffIDName, false, true);
                    else if("AssigneeStaffId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeStaffId, false, true);
                    else if("AssigneeDepartmentId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeDepartmentId, false, true);
                    else if("AssigneeOrganizationId".equals(customField.getFieldName()))
                        labelManager.setLabels(event.getUser(), issue.getId(), customField.getIdAsLong(),AssigneeOrganizationId, false, true);
                }

            }

        }*/
    }

    public void destroy() throws Exception
    {
        this.eventPublisher.unregister(this);
    }

    public void afterPropertiesSet() throws Exception
    {
        this.eventPublisher.register(this);
    }


    public boolean saveValidLicense(String keyBytes) throws UnsupportedEncodingException
    {
        List<PRLInfo>  lstPRLInfo =licenseService.getPRLInfofromByte(keyBytes);

        if(1==1)
        {

            for (PRLInfo ppinput :lstPRLInfo
            ) {
                LSDataEntity var1= lSDataAOService.getByPP(ppinput.getP());
                if(var1==null) {
                    lSDataAOService.update(ppinput.getP(), ppinput.getCompany(), ppinput.getV(), keyBytes);
                }else
                {
                    List<PRLInfo>  lstPRLInfoOld =licenseService.getPRLInfofromByte(var1.getKeykey());
                    if(lstPRLInfoOld!=null&&lstPRLInfoOld.size()>0)
                    {

                        if(ppinput.getAppend().compareTo( lstPRLInfoOld.get(0).getAppend())>0)
                        {
                            lSDataAOService.update(ppinput.getP(), ppinput.getCompany(), ppinput.getV(), keyBytes);
                        }
                    }

                }

            }


            GlobalConfig.glstPRLInfo.removeAll(GlobalConfig.glstPRLInfo);
            GlobalConfig.glstPRLInfo.addAll(licenseService.getLicenseInfos());
            return true;
        }
        else
        {
            return false;
        }

    }

    /*SYNC*/
//    private final CompatibilityPluginScheduler compatibilityPluginScheduler;
    private final CrowdDirectoryService crowdDirectoryService;
    private final LdapSyncTimeService ldapSyncTimeService;
    private final RoleService roleService;
    private final PowerService powerService;
    private final OrganizationAOService organizationAOService;
    private static HashMap<Long,TimerTask> taskMap=new HashMap<Long,TimerTask>();
    ScheduleWay scheduleWay=new ScheduleWay();

    //power
    private void setOrgMenuPower(OrganizationEntity zzjggl, List<RoleEntity> roleEntityList){
        PowerEntity edit = powerService.setPower("/latest/orgstr/", "edit", true, 1);
        PowerEntity del = powerService.setPower("/latest/orgstr/", "del", true, 1);
        PowerEntity create = powerService.setPower("/latest/orgstr/", "creorg", true, 1);
        PowerEntity createD = powerService.setPower("/latest/orgstr/", "credep", true, 1);
        PowerEntity createE = powerService.setPower("/latest/orgstr/", "creemp", true, 1);
        PowerEntity upload = powerService.setPower("/latest/orgstr/", "upload", true, 1);
        PowerEntity creteam = powerService.setPower("/latest/orgstr/", "creteam", true, 1);
        PowerEntity cretegs = powerService.setPower("/latest/orgstr/", "cretegs", true, 1);
        organizationAOService.mainPowerAndMenu(edit,zzjggl);
        organizationAOService.mainPowerAndMenu(del,zzjggl);
        organizationAOService.mainPowerAndMenu(create,zzjggl);
//        organizationAOService.mainPowerAndMenu(createD,zzjggl);
//        organizationAOService.mainPowerAndMenu(createE,zzjggl);
//        organizationAOService.mainPowerAndMenu(upload,zzjggl);
//        organizationAOService.mainPowerAndMenu(creteam,zzjggl);
//        organizationAOService.mainPowerAndMenu(cretegs,zzjggl);
        Map<PowerEntity,Boolean> map =new HashMap();
        map.put(edit,true);
        map.put(del,true);
        map.put(create,true);
//        map.put(createD,true);
//        map.put(createE,true);
//        map.put(upload,true);
//        map.put(creteam,true);
//        map.put(cretegs,true);
        Map<PowerEntity,Boolean> map1 =new HashMap();
        map1.put(edit,false);
        map1.put(del,false);
        map1.put(create,false);
//        map1.put(createD,false);
//        map1.put(createE,false);
//        map1.put(upload,false);
//        map1.put(creteam,false);
//        map1.put(cretegs,false);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),zzjggl,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),zzjggl,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),zzjggl,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),zzjggl,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),zzjggl,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),zzjggl,map,-3);
//        roleService.setOrgRoleMenuPower(roleEntityList.get(2),zzjggl,map1,-1);
//        roleService.setOrgRoleMenuPower(roleEntityList.get(2),zzjggl,map1,-2);
//        roleService.setOrgRoleMenuPower(roleEntityList.get(2),zzjggl,map,-3);
//        roleService.setOrgRoleMenuPower(roleEntityList.get(3),zzjggl,map1,-1);
//        roleService.setOrgRoleMenuPower(roleEntityList.get(3),zzjggl,map1,-2);
//        roleService.setOrgRoleMenuPower(roleEntityList.get(3),zzjggl,map,-3);
//        roleService.setOrgRoleMenuPower(roleEntityList.get(4),zzjggl,map1,-1);
//        roleService.setOrgRoleMenuPower(roleEntityList.get(4),zzjggl,map1,-2);
//        roleService.setOrgRoleMenuPower(roleEntityList.get(4),zzjggl,map,-3);
    }

    private void  setBmryfbPower(OrganizationEntity organizationEntity, List<RoleEntity> roleEntityList){
        PowerEntity detail = powerService.setPower("/1/bmryfbState/bmryfb", "detail", true, 1);
        organizationAOService.mainPowerAndMenu(detail,organizationEntity);
        Map<PowerEntity,Boolean> map =new HashMap();
        map.put(detail,true);
        Map<PowerEntity,Boolean> map1 =new HashMap();
        map1.put(detail,false);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(2),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(2),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(2),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(3),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(3),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(3),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(5),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(5),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(5),organizationEntity,map,-3);
    }

    private void setBmrygzPower(OrganizationEntity organizationEntity, List<RoleEntity> roleEntityList){
        PowerEntity detail = powerService.setPower("/1/bmryfbState/bmrygz", "detail1", true, 1);
        organizationAOService.mainPowerAndMenu(detail,organizationEntity);
        Map<PowerEntity,Boolean> map =new HashMap();
        map.put(detail,true);
        Map<PowerEntity,Boolean> map1 =new HashMap();
        map1.put(detail,false);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(2),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(2),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(2),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(3),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(3),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(3),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(5),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(5),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(5),organizationEntity,map,-3);
    }

    private void setSetReportPower(OrganizationEntity organizationEntity, List<RoleEntity> roleEntityList){
        PowerEntity addreport = powerService.setPower("/1/setreport/", "addReport", true, 1);
        PowerEntity savereport = powerService.setPower("/1/setreport/", "saveReport", true, 1);
        PowerEntity editreport = powerService.setPower("/1/setreport/", "updateReport", true, 1);
        PowerEntity delreport = powerService.setPower("/1/setreport/", "delReport", true, 1);
        PowerEntity shareDemo = powerService.setPower("/1/setreport/", "shareDemo", true, 1);
        organizationAOService.mainPowerAndMenu(addreport,organizationEntity);
        organizationAOService.mainPowerAndMenu(savereport,organizationEntity);
        organizationAOService.mainPowerAndMenu(editreport,organizationEntity);
        organizationAOService.mainPowerAndMenu(delreport,organizationEntity);
        organizationAOService.mainPowerAndMenu(shareDemo,organizationEntity);
        Map<PowerEntity,Boolean> map =new HashMap();
        map.put(addreport,true);
        map.put(savereport,true);
        map.put(editreport,true);
        map.put(delreport,true);
        map.put(shareDemo,true);
        Map<PowerEntity,Boolean> map1 =new HashMap();
        map1.put(addreport,false);
        map1.put(savereport,false);
        map1.put(editreport,false);
        map1.put(delreport,false);
        map1.put(shareDemo,false);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map,-3);
        map.put(shareDemo,false);
        checkAllMenuPower(map,organizationEntity);
    }

    private void setShowReport(OrganizationEntity organizationEntity, List<RoleEntity> roleEntityList){
        PowerEntity query = powerService.setPower("/latest/showReport", "query", true, 1);
        organizationAOService.mainPowerAndMenu(query,organizationEntity);
        Map<PowerEntity,Boolean> map =new HashMap();
        map.put(query,true);
        Map<PowerEntity,Boolean> map1 =new HashMap();
        map1.put(query,false);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map,-3);
    }
    private void setAdsjtb(OrganizationEntity organizationEntity, List<RoleEntity> roleEntityList){
        PowerEntity edited = powerService.setPower("/latest/info/", "edited", true, 1);
        PowerEntity pause = powerService.setPower("/latest/info/", "pause", true, 1);
        PowerEntity sync = powerService.setPower("/latest/info/", "sync", true, 1);
        organizationAOService.mainPowerAndMenu(edited,organizationEntity);
        organizationAOService.mainPowerAndMenu(pause,organizationEntity);
        organizationAOService.mainPowerAndMenu(sync,organizationEntity);
        Map<PowerEntity,Boolean> map =new HashMap();
        map.put(edited,true);
        map.put(pause,true);
        map.put(sync,true);
        Map<PowerEntity,Boolean> map1 =new HashMap();
        map1.put(edited,false);
        map1.put(pause,false);
        map1.put(sync,false);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map,-3);
    }

    private void setGsryld(OrganizationEntity organizationEntity, List<RoleEntity> roleEntityList){
        PowerEntity viewed = powerService.setPower("/1/gsryldState/", "viewed", true, 1);
        organizationAOService.mainPowerAndMenu(viewed,organizationEntity);
        Map<PowerEntity,Boolean> map =new HashMap();
        map.put(viewed,true);
        Map<PowerEntity,Boolean> map1 =new HashMap();
        map1.put(viewed,false);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(2),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(2),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(2),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(3),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(3),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(3),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(4),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(4),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(4),organizationEntity,map,-3);

    }
    private void setTdryfb(OrganizationEntity organizationEntity, List<RoleEntity> roleEntityList){
        PowerEntity Tdetail = powerService.setPower("/1/bmryfbState/", "Tdetail", true, 1);
        organizationAOService.mainPowerAndMenu(Tdetail,organizationEntity);
        Map<PowerEntity,Boolean> map =new HashMap();
        map.put(Tdetail,true);
        Map<PowerEntity,Boolean> map1 =new HashMap();
        map1.put(Tdetail,false);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(4),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(4),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(4),organizationEntity,map,-3);
    }
    private void setTdrygz(OrganizationEntity organizationEntity, List<RoleEntity> roleEntityList){
        PowerEntity TdetailZ = powerService.setPower("/1/bmryfbState/tdrygz", "TdetailZ", true, 1);
        organizationAOService.mainPowerAndMenu(TdetailZ,organizationEntity);
        Map<PowerEntity,Boolean> map =new HashMap();
        map.put(TdetailZ,true);
        Map<PowerEntity,Boolean> map1=new HashMap();
        map1.put(TdetailZ,false);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(4),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(4),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(4),organizationEntity,map,-3);
    }
    private void setQxkznew(OrganizationEntity organizationEntity, List<RoleEntity> roleEntityList){
        PowerEntity addRole = powerService.setPower("/latest/power/", "addRole", true, 1);
        PowerEntity delRole = powerService.setPower("/latest/power/", "delRole", true, 1);
        PowerEntity changePower = powerService.setPower("/latest/power/", "changePower", true, 1);
        organizationAOService.mainPowerAndMenu(addRole,organizationEntity);
        organizationAOService.mainPowerAndMenu(delRole,organizationEntity);
        organizationAOService.mainPowerAndMenu(changePower,organizationEntity);
        Map<PowerEntity,Boolean> map =new HashMap();
        map.put(addRole,true);
        map.put(delRole,true);
        map.put(changePower,true);
        Map<PowerEntity,Boolean> map1 =new HashMap();
        map1.put(addRole,false);
        map1.put(delRole,false);
        map1.put(changePower,false);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map,-3);
//        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-1);
//        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-2);
//        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map,-3);
    }
    private void setLowerissues(OrganizationEntity organizationEntity, List<RoleEntity> roleEntityList){
        PowerEntity showLowerIssues = powerService.setPower("/latest/lowerissues/getAll", "showLowerIssues", true, 1);
        organizationAOService.mainPowerAndMenu(showLowerIssues,organizationEntity);
        Map<PowerEntity,Boolean> map =new HashMap();
        map.put(showLowerIssues,true);
        Map<PowerEntity,Boolean> map1 =new HashMap();
        map1.put(showLowerIssues,false);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(0),organizationEntity,map,-3);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-1);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map1,-2);
        roleService.setOrgRoleMenuPower(roleEntityList.get(1),organizationEntity,map,-3);
    }

    /**
     * 添加全局的权限-->自定义的 角色(用户组)
     * @param globalPermissionKey
     * @param roleEntity
     */
    private void addGloblePermissionToCustomRole(GlobalPermissionKey globalPermissionKey,RoleEntity roleEntity){
        Option<GlobalPermissionType> globalPermission = globalPermissionManager.getGlobalPermission(globalPermissionKey);
        GlobalPermissionType globalPermissionType = globalPermission.get();
        globalPermissionManager.addPermission(globalPermissionType,roleEntity.getName());
    }

    /**
     * 创建 自定义 用户组  也就是 角色
     * @param roleEntity
     */
    private void createCustomGroup(RoleEntity roleEntity){

        if (!groupManager.groupExists(roleEntity.getName())){
            try {
                groupManager.createGroup(roleEntity.getName());
            } catch (OperationNotPermittedException e) {
                e.printStackTrace();
            } catch (InvalidGroupException e) {
                e.printStackTrace();
            }
        }
    }
    private PowerEntity initProPower(String desc){
        return powerService.setPower("", desc, true, 2);
    }
    private void setPermissionsPower(PowerEntity powerEntity,RoleEntity... roleEntities){
        Arrays.stream(roleEntities).forEach(roleEntity -> {strPowerRoleService.createRelationOfPowerAndRole(roleEntity,powerEntity);});
    }

    private  void  checkAllMenuPower(Map<PowerEntity,Boolean> map,OrganizationEntity organizationEntity){
        MemberEntity[] memberByOrganization = memberAOService.getMemberByOrganization(organizationEntity);
        Arrays.stream(memberByOrganization).filter(memberEntity -> {
            String powerList = memberEntity.getPowerList();
            if (StringUtils.isNotBlank(powerList)){
                return true;
            }else {
                return false;
            }
        }).forEach(memberEntity -> {
            String powerList = memberEntity.getPowerList();
            int length = powerList.split("\\|")[0].split(";").length;
            int size = map.size();
            if (length != size){
                String userKey = memberEntity.getUserKey();
                String[] split = powerList.split("\\|")[0].split(";");
                Set<String> collect = Arrays.stream(split).map(s -> s.split(":")[0]).collect(Collectors.toSet());
                Set<Integer> collect1 = map.keySet().stream().map(powerEntity -> powerEntity.getID()).collect(Collectors.toSet());
                Set<Integer> collect2 = collect.stream().map(s -> Integer.valueOf(s)).collect(Collectors.toSet());
                collect1.removeAll(collect2);
                String[] split1 = powerList.split("\\|");
                collect1.stream().forEach(integer -> {
                    IntStream.range(0,split1.length).forEach(sn ->{
                        PowerEntity byId = powerService.getById(integer);
                        split1[sn] += (integer+":"+(map.get(byId) == true? 1:0));
                        split1[sn] +=";";
                    });
                });
                final String[] sss = {""};
                Arrays.stream(split1).forEach(s -> {
                    sss[0] +=s;
                    sss[0] +="|";});
                String substring = sss[0].substring(0, sss[0].lastIndexOf("|"));
                memberEntity.setPowerList(substring);
                memberEntity.save();
            }
        });
    }


    private void delGroup(String groupName){
        if (groupManager.groupExists(groupName)){
            globalPermissionManager.removePermissions(groupName);
            Group group = groupManager.getGroup(groupName);
            try {
                crowdService.removeAllGroupAttributes(group);
                crowdService.removeGroup(group);
            } catch (OperationNotPermittedException e) {
                e.printStackTrace();
            }
        }
    }
}
