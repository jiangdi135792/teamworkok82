package web.contextproviders;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.work.plugin.util.ToolsDataHelper;
import com.work.plugin.view.ViewStrEmployeeBean;
import com.work.plugin.ao.*;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * Created by mark on 2021/6/28.
 */

@RequiredArgsConstructor
public class GetOgranizations extends AbstractJiraContextProvider {

    private final StrEmployeeService strEmployeeService;
    private final StrOrganizeService strOrganizeService;
    private final DepartmentAOService departmentAOService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private String mUser;
    private String mGroup;
    private int majorVersion;
    private int minorVersion;
/*
    public GetOgranizations(ApplicationProperties applicationProperties) {

        String versionString = applicationProperties.getVersion();
        String versionRegex = "^(\\d+)\\.(\\d+)";
        Pattern versionPattern = Pattern.compile(versionRegex);
        Matcher versionMatcher = versionPattern.matcher(versionString);
        versionMatcher.find();
        majorVersion = Integer.decode(versionMatcher.group(1));
        minorVersion = Integer.decode(versionMatcher.group(2));


    }
    */
    /*
    public Map getContextMap(GadgetRequestContext.User user, JiraHelper jiraHelper)
    {
        int historyIssues = 0;
        if (jiraHelper != null && jiraHelper.getRequest() != null)
        {
            UserHistory history = (UserHistory) jiraHelper.getRequest().getSession().getAttribute(SessionKeys.USER_ISSUE_HISTORY);
            if (history != null)
            {
                historyIssues = history.getIssues().size();
            }
        }
        int logoHeight = TextUtils.parseInt(applicationProperties.getDefaultBackedString(APKeys.JIRA_LF_LOGO_HEIGHT));
        String historyHeight = String.valueOf(80 + logoHeight + (25 * historyIssues));
        String filterHeight = String.valueOf(205 + logoHeight);
        return EasyMap.build("historyWindowHeight", historyHeight,
                "filtersWindowHeight", filterHeight);
    }

    public void init(final Map<String, String> paramMap) throws PluginParseException {
        mUser = paramMap.get("user");
        mGroup = paramMap.get("group");
    }
*/

    private String[] getOrgPath(CustomFieldManager customFieldManager, Issue currIssue, String OrganizationPath) {
        String currAssigneeOrganizationPath="";
        String currAssigneeOrganizationPath1="";
        try {
            CustomField cfNameCustomField=customFieldManager.getCustomFieldObjectByName(OrganizationPath);


            if (cfNameCustomField.hasValue(currIssue)) {
                Collection<Object> customFieldValue=(Collection) cfNameCustomField.getValue(currIssue);
                String[] str=new String[5];
                int i=0;

/* 将Set集合转为List,这样获得的list并不能有序排�?*/
                //List list = Arrays.asList(array);

                List<Object> customFieldValueList=Arrays.asList((Object[]) customFieldValue.toArray());


/*将list有序排列*/
                Collections.sort(customFieldValueList, new Comparator<Object>() {
                    public int compare(Object arg0, Object arg1) {
                        return (arg0.toString().split(":")[0].compareTo(arg1.toString().split(":")[0])); // 按照id排列
                    }
                });

                for (Object a : customFieldValueList
                        ) {
                    //i=Integer.parseInt(a.toString().split(":")[0]);

                    currAssigneeOrganizationPath1=currAssigneeOrganizationPath1 + a.toString().substring(a.toString().split(":")[0].length() + 1) + " > ";
                    if (i < 1)
                        str[i]=a.toString().substring(a.toString().split(":")[0].length() + 1);
                    else if (i < 4)
                        str[i]=a.toString().substring(a.toString().split(":")[0].length() + 1);
                    else {
                        str[1]=str[2];
                        str[2]=a.toString().substring(a.toString().split(":")[0].length() + 1);
                    }
                    i++;

                }
                // for(int m=0;m<i;m++)currAssigneeOrganizationPath1=currAssigneeOrganizationPath1+" ";

                for (int j=0; j < i && j < 3; j++) {
                    if (i < 4)
                        currAssigneeOrganizationPath=currAssigneeOrganizationPath + str[j] + " > ";
                    else {
                        if (j == 1) {
                            currAssigneeOrganizationPath=currAssigneeOrganizationPath + "... > ";
                        }
                        currAssigneeOrganizationPath=currAssigneeOrganizationPath + str[j] + " > ";
                    }
                }
            }
        } catch (Exception e) {


        }

        return new String[]{currAssigneeOrganizationPath, currAssigneeOrganizationPath1};

    }

    public Map getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        CustomFieldManager customFieldManager=ComponentAccessor.getCustomFieldManager();
        Issue currIssue=(Issue) jiraHelper.getContextParams().get("issue");
        String currReporterId=currIssue.getReporterId();
        String currAssigneeId=currIssue.getAssigneeId();
        // #set($customFieldObj = $customFieldManager.getCustomFieldObject("customfield_10056"))
        // $cutomFieldObj.getValue($issue))
        ;
        String currReporterOrganization="";
        String currReporterDepartment="";
        String currReporterOrganizationPath="";
        String currReporterOrganizationPathTooltip="";
        String currReporterStaffName="";
        String currAssigneeOrganization="";
        String currAssigneeDepartment="";
        String currAssigneeOrganizationPath="";
        String currAssigneeOrganizationPathTooltip="";
        String currAssigneeStaffName="";
        String reporterStaffId="";
        String reporterDepartmentId="";
        String reporterOrganizationId="";
        String assigneeStaffId="";
        String assigneeDepartmentId="";
        String assigneeOrganizationId="";
      //  String contextPaht =jiraHelper.getRequest().getContextPath();
        CustomField cf=null;;
        String assigneeSubId="";
        String reporterSubId="";
        //ViewStrOrganizeBean entityassigneeStrOrganize = new ViewStrOrganizeBean();
     //   ViewStrOrganizeBean entityreporterStrOrganize = new ViewStrOrganizeBean();
      //  ViewStrEmployeeBean entityassigneeStrEmployee = new ViewStrEmployeeBean();
     //   ViewStrEmployeeBean entityassigneeStrEmployeeDirector = new ViewStrEmployeeBean();
     //   ViewStrEmployeeBean entityreporterStrEmployee = new ViewStrEmployeeBean();
      //  ViewStrEmployeeBean entityreporterStrEmployeeDirector = new ViewStrEmployeeBean();
    //    ViewDepartmentBean assigneeDepartmentEntity = new ViewDepartmentBean();
    //    ViewDepartmentBean reporterDepartmentEntity = new ViewDepartmentBean();
        //cf = customFieldManager.getCustomFieldObjectByName("AssigneeOrganizationPath");
        try {


            cf = customFieldManager.getCustomFieldObjectByName("ReporterOrganization");
            if (cf != null && cf.getValue(currIssue) != null) {
                currReporterOrganization = ToolsDataHelper.getRepalaceString(cf.getValue(currIssue).toString());
            }
            cf = customFieldManager.getCustomFieldObjectByName("ReporterDepartment");
            if (cf != null && cf.getValue(currIssue) != null) {
                currReporterDepartment = ToolsDataHelper.getRepalaceString(cf.getValue(currIssue).toString());
            }
            //cf = customFieldManager.getCustomFieldObjectByName("ReporterOrganizationPath");
            //if(cf!=null&&cf.getValue(currIssue)!=null){
            //   currReporterOrganizationPath = cf.getValue(currIssue).toString();
            //}
            String[] currReporterOrganizationPaths = getOrgPath(customFieldManager, currIssue, "ReporterOrganizationPath");
            currReporterOrganizationPath = ToolsDataHelper.getRepalaceString(currReporterOrganizationPaths[0]);
            currReporterOrganizationPathTooltip = ToolsDataHelper.getRepalaceString(currReporterOrganizationPaths[1]);
            cf = customFieldManager.getCustomFieldObjectByName("ReporterStaffName");
            if (cf != null && cf.getValue(currIssue) != null) {
                currReporterStaffName = ToolsDataHelper.getRepalaceString(cf.getValue(currIssue).toString());
            }
            cf = customFieldManager.getCustomFieldObjectByName("AssigneeOrganization");
            if (cf != null && cf.getValue(currIssue) != null) {
                currAssigneeOrganization = ToolsDataHelper.getRepalaceString(cf.getValue(currIssue).toString());
            }
            cf = customFieldManager.getCustomFieldObjectByName("AssigneeDepartment");
            if (cf != null && cf.getValue(currIssue) != null) {
                currAssigneeDepartment = ToolsDataHelper.getRepalaceString(cf.getValue(currIssue).toString());
            }
            // cf = customFieldManager.getCustomFieldObjectByName("AssigneeOrganizationPath");
            // if(cf!=null&&cf.getValue(currIssue)!=null) {
            //     currAssigneeOrganizationPath = cf.getValue(currIssue).toString();
            // }
            String[] currAssigneeOrganizationPaths = getOrgPath(customFieldManager, currIssue, "AssigneeOrganizationPath");
            currAssigneeOrganizationPath = ToolsDataHelper.getRepalaceString(currAssigneeOrganizationPaths[0]);

            currAssigneeOrganizationPathTooltip = ToolsDataHelper.getRepalaceString(currAssigneeOrganizationPaths[1]);
            cf = customFieldManager.getCustomFieldObjectByName("AssigneeStaffName");
            if (cf != null && cf.getValue(currIssue) != null) {
                currAssigneeStaffName = ToolsDataHelper.getRepalaceString(cf.getValue(currIssue).toString());
            }

            cf = customFieldManager.getCustomFieldObjectByName("ReporterStaffId");
            if (cf != null && cf.getValue(currIssue) != null) {

                //  Collection<Object> customFieldValue = (Collection) cf.getValue(currIssue);
                for (Object a : (Collection) cf.getValue(currIssue)) {
                    reporterStaffId = a.toString();
                }

                // reporterStaffId = cf.getValue(currIssue).toString();
            }
            cf = customFieldManager.getCustomFieldObjectByName("ReporterDepartmentId");
            StrOrganize entityassigneeStrOrganize_s = null;
            if (cf != null && cf.getValue(currIssue) != null) {
                // reporterDepartmentId = cf.getValue(currIssue).toString();
                for (Object a : (Collection) cf.getValue(currIssue)) {
                    reporterDepartmentId = a.toString();
                }
            }
            cf = customFieldManager.getCustomFieldObjectByName("ReporterOrganizationId");
            if (cf != null && cf.getValue(currIssue) != null) {
                // reporterOrganizationId = cf.getValue(currIssue).toString();
                for (Object a : (Collection) cf.getValue(currIssue)) {
                    reporterOrganizationId = a.toString();
                }
            }
            cf = customFieldManager.getCustomFieldObjectByName("AssigneeStaffId");
            if (cf != null && cf.getValue(currIssue) != null) {
                //assigneeStaffId = cf.getValue(currIssue).toString();
                for (Object a : (Collection) cf.getValue(currIssue)) {
                    assigneeStaffId = a.toString();
                }
            }
            cf = customFieldManager.getCustomFieldObjectByName("AssigneeDepartmentId");
            if (cf != null && cf.getValue(currIssue) != null) {
                // assigneeDepartmentId = cf.getValue(currIssue).toString();
                for (Object a : (Collection) cf.getValue(currIssue)) {
                    assigneeDepartmentId = a.toString();
                }
            }
            cf = customFieldManager.getCustomFieldObjectByName("AssigneeOrganizationId");
            if (cf != null && cf.getValue(currIssue) != null) {
                // assigneeOrganizationId = cf.getValue(currIssue).toString();
                for (Object a : (Collection) cf.getValue(currIssue)) {
                    assigneeOrganizationId = a.toString();
                }
            }

           // I18nHelper i18nHelper = jiraAuthenticationContext.getI18nHelper();
     /*    GetDicValue.getDictionaryValue(DicTypeEnum.SEX,"0",i18nHelper,"workorg.property.StrEmployee.");
        GetDicValue.getDictionaryValue(DicTypeEnum.BRANCHCHARACTER,"0",i18nHelper,"workorg.property.organization.character.");
        GetDicValue.getDictionaryValue(DicTypeEnum.STATUS,"0",i18nHelper,"workorg.property.common.status.");
        GetDicValue.getDictionaryValue(DicTypeEnum.ORGANIZATIONTYPE,"0",i18nHelper,"workorg.property.organization.type.");
*/

            if (assigneeOrganizationId != null && !assigneeOrganizationId.isEmpty())
                entityassigneeStrOrganize_s = strOrganizeService.get(Integer.parseInt(assigneeOrganizationId));
           /* if (Objects.nonNull(entityassigneeStrOrganize_s)) {
                entityassigneeStrOrganize.setCharacter(GetDicValue.getDictionaryValue(DicTypeEnum.BRANCHCHARACTER, entityassigneeStrOrganize_s.getCharacter().toString(), i18nHelper, "workorg.property.organization.character."));
                entityassigneeStrOrganize.setStatus(GetDicValue.getDictionaryValue(DicTypeEnum.STATUS, entityassigneeStrOrganize_s.getStatus().toString(), i18nHelper, "workorg.property.common.status."));
                entityassigneeStrOrganize.setType(GetDicValue.getDictionaryValue(DicTypeEnum.ORGANIZATIONTYPE, entityassigneeStrOrganize_s.getType().toString(), i18nHelper, "workorg.property.organization.type."));
                entityassigneeStrOrganize.setName(entityassigneeStrOrganize_s.getName());
            }*/

            StrOrganize entityreporterStrOrganize_s = null;

            if (reporterOrganizationId != null && !reporterOrganizationId.isEmpty())
                entityreporterStrOrganize_s = strOrganizeService.get(Integer.parseInt(reporterOrganizationId));
        /*    if (Objects.nonNull(entityreporterStrOrganize_s)) {
                entityreporterStrOrganize.setCharacter(GetDicValue.getDictionaryValue(DicTypeEnum.BRANCHCHARACTER, entityreporterStrOrganize_s.getCharacter().toString(), i18nHelper, "workorg.property.organization.character."));
                entityreporterStrOrganize.setStatus(GetDicValue.getDictionaryValue(DicTypeEnum.STATUS, entityreporterStrOrganize_s.getStatus().toString(), i18nHelper, "workorg.property.common.status."));
                entityreporterStrOrganize.setType(GetDicValue.getDictionaryValue(DicTypeEnum.ORGANIZATIONTYPE, entityreporterStrOrganize_s.getType().toString(), i18nHelper, "workorg.property.organization.type."));
                entityreporterStrOrganize.setName(entityreporterStrOrganize_s.getName());
            }*/
            StrEmployee entityassigneeStrEmployee_s = null;


            if (assigneeStaffId != null && !assigneeStaffId.isEmpty())
                entityassigneeStrEmployee_s = strEmployeeService.getEmployee(Integer.parseInt(assigneeStaffId));
            if (Objects.nonNull(entityassigneeStrEmployee_s)) {
/*
                String stemp = GetDicValue.getDictionaryValue(DicTypeEnum.SEX, entityassigneeStrEmployee_s.getEmployeeSex(), i18nHelper, "workorg.property.StrEmployee.");
                entityassigneeStrEmployee.setEmployeeSex(stemp);

                entityassigneeStrEmployee.setEmploymentStatus(GetDicValue.getDictionaryValue(DicTypeEnum.EMPLOYMENTSTATUS, entityassigneeStrEmployee_s.getEmploymentStatus(), i18nHelper, "workorg.property.StrEmployee."));
                //entityreporterStrOrganize.setType(GetDicValue.getDictionaryValue(DicTypeEnum.ORGANIZATIONTYPE, entityassigneeStrEmployee_s.getType().toString(), i18nHelper, "workorg.property.organization.type."));
                entityassigneeStrEmployee.setEmployeeName(entityassigneeStrEmployee_s.getEmployeeName());
                entityassigneeStrEmployee.setEmail(entityassigneeStrEmployee_s.getEmail());
                entityassigneeStrEmployee.setEmployeeNo(entityassigneeStrEmployee_s.getEmployeeNo());
                entityassigneeStrEmployee.setEntryTime(entityassigneeStrEmployee_s.getEntryTime());
                entityassigneeStrEmployee.setLeaveTime(entityassigneeStrEmployee_s.getLeaveTime());
                entityassigneeStrEmployee.setPhone(entityassigneeStrEmployee_s.getPhone());
                entityassigneeStrEmployee.setOtherPhone(entityassigneeStrEmployee_s.getOtherPhone());
*/

                Integer supervisor = entityassigneeStrEmployee_s.getSupervisor();
                if (supervisor != null) {
                    assigneeSubId = String.valueOf(supervisor);
                    /*StrEmployee employee = strEmployeeService.getEmployee(supervisor.intValue());
                    entityassigneeStrEmployeeDirector.setEmail(employee.getEmail());
                    entityassigneeStrEmployeeDirector.setEmployeeName(employee.getEmployeeName());
                    entityassigneeStrEmployeeDirector.setPhone(employee.getPhone());*/
                }

            }

            StrEmployee entityreporterStrEmployee_s = null;


            if (reporterStaffId != null && !reporterStaffId.isEmpty())
                entityreporterStrEmployee_s = strEmployeeService.getEmployee(Integer.parseInt(reporterStaffId));
            if (Objects.nonNull(entityreporterStrEmployee_s)) {
              //  String stemp = GetDicValue.getDictionaryValue(DicTypeEnum.SEX, entityreporterStrEmployee_s.getEmployeeSex(), i18nHelper, "workorg.property.StrEmployee.");
             /*   entityreporterStrEmployee.setEmployeeSex(stemp);

                entityreporterStrEmployee.setEmploymentStatus(GetDicValue.getDictionaryValue(DicTypeEnum.EMPLOYMENTSTATUS, entityreporterStrEmployee_s.getEmploymentStatus(), i18nHelper, "workorg.property.StrEmployee."));
                //entityreporterStrOrganize.setType(GetDicValue.getDictionaryValue(DicTypeEnum.ORGANIZATIONTYPE, entityassigneeStrEmployee_s.getType().toString(), i18nHelper, "workorg.property.organization.type."));
                entityreporterStrEmployee.setEmployeeName(entityreporterStrEmployee_s.getEmployeeName());
                entityreporterStrEmployee.setEmail(entityreporterStrEmployee_s.getEmail());
                entityreporterStrEmployee.setEmployeeNo(entityreporterStrEmployee_s.getEmployeeNo());
                entityreporterStrEmployee.setEntryTime(entityreporterStrEmployee_s.getEntryTime());
                entityreporterStrEmployee.setLeaveTime(entityreporterStrEmployee_s.getLeaveTime());
                entityreporterStrEmployee.setPhone(entityreporterStrEmployee_s.getPhone());
                entityreporterStrEmployee.setOtherPhone(entityreporterStrEmployee_s.getOtherPhone());
*/
                Integer supervisor = entityreporterStrEmployee_s.getSupervisor();
                if (supervisor != null) {
                    reporterSubId= String.valueOf(supervisor);
                   /* StrEmployee employee = strEmployeeService.getEmployee(supervisor.intValue());
                    entityreporterStrEmployeeDirector.setEmail(employee.getEmail());
                    entityreporterStrEmployeeDirector.setEmployeeName(employee.getEmployeeName());
                    entityreporterStrEmployeeDirector.setPhone(employee.getPhone());*/
                }

            }


           /* DepartmentEntity assigneeDepartmentEntity_s = null;

            if (assigneeDepartmentId != null && !assigneeDepartmentId.isEmpty())
                assigneeDepartmentEntity_s = departmentAOService.get(Integer.parseInt(assigneeDepartmentId));*/
       /*     if (Objects.nonNull(assigneeDepartmentEntity_s)) {
                assigneeDepartmentEntity.setStatus(GetDicValue.getDictionaryValue(DicTypeEnum.STATUS, Integer.toString(assigneeDepartmentEntity_s.getStatus()), i18nHelper, "workorg.property.common.status."));
                assigneeDepartmentEntity.setType(GetDicValue.getDictionaryValue(DicTypeEnum.GROUPTYPE, Integer.toString(assigneeDepartmentEntity_s.getType()), i18nHelper, "workorg.property.Department."));
                assigneeDepartmentEntity.setGroupName(assigneeDepartmentEntity_s.getGroupName());
                assigneeDepartmentEntity.setGroupNo(assigneeDepartmentEntity_s.getGroupNo());
            }*/
            /*DepartmentEntity reporterDepartmentEntity_s = null;*/

            /*if (reporterDepartmentId != null && !reporterDepartmentId.isEmpty())
                reporterDepartmentEntity_s = departmentAOService.get(Integer.parseInt(reporterDepartmentId));*/
/*            if (Objects.nonNull(reporterDepartmentEntity_s)) {
                reporterDepartmentEntity.setStatus(GetDicValue.getDictionaryValue(DicTypeEnum.STATUS, Integer.toString(reporterDepartmentEntity_s.getStatus()), i18nHelper, "workorg.property.common.status."));
                reporterDepartmentEntity.setType(GetDicValue.getDictionaryValue(DicTypeEnum.GROUPTYPE, Integer.toString(reporterDepartmentEntity_s.getType()), i18nHelper, "workorg.property.Department."));
                reporterDepartmentEntity.setGroupName(reporterDepartmentEntity_s.getGroupName());
                reporterDepartmentEntity.setGroupNo(reporterDepartmentEntity_s.getGroupNo());
            }*/
        } catch (Exception e) {
           // System.out.println(e.getCause()+"2222222222222222222222222222222");
        }
/*-------------------------------------------------------------------------------------------*/
        IssueImpl newissue = (IssueImpl) jiraHelper.getContextParams().get("issue");


        List<List<ViewStrEmployeeBean>> collectInfo = new LinkedList<>();
        ApplicationUser assigneeUser = currIssue.getAssigneeUser();
        if (assigneeUser != null) {
            List<ViewStrEmployeeBean>  collectOne = new LinkedList<>();
            ViewStrEmployeeBean viewStrEmployeeBean = new ViewStrEmployeeBean();
            viewStrEmployeeBean.setType("Assignee");
            collectOne.add(viewStrEmployeeBean);
            String jiraUserKey = assigneeUser.getKey();
            StrEmployee empByJiraUserKey = strEmployeeService.getByJiraUserKey(jiraUserKey);
            if (empByJiraUserKey != null){
            collectOne = getSelfInfoAndSupervisorInfo(empByJiraUserKey, collectOne);
            collectInfo.add(collectOne);
            }
        }
        List<ViewStrEmployeeBean>  collectOne = new LinkedList<>();
        ViewStrEmployeeBean viewStrEmployeeBean = new ViewStrEmployeeBean();
        viewStrEmployeeBean.setType("Reporter");
        collectOne.add(viewStrEmployeeBean);
        String reporterKey = currIssue.getReporter().getKey();
        StrEmployee byJiraUserKey1 = strEmployeeService.getByJiraUserKey(reporterKey);
        if (byJiraUserKey1 != null){
            collectOne = getSelfInfoAndSupervisorInfo(byJiraUserKey1, collectOne);
            collectInfo.add(collectOne);
        }
            List<CustomField> customFieldObjects = customFieldManager.getCustomFieldObjects(currIssue);
            customFieldObjects.stream()
                    .filter(customField -> customField.getCustomFieldType().getKey().equals("com.atlassian.jira.plugin.system.customfieldtypes:userpicker"))
                    .forEach(customField -> {
                        String value = String.valueOf(customField.getValue(currIssue));
                        if (org.apache.commons.lang3.StringUtils.isNotBlank(value)&& !"null".equals(value)){
                            StrEmployee byJiraUserKey = strEmployeeService.getByJiraUserKey(((ApplicationUser) customField.getValue(currIssue)).getKey());
                            ViewStrEmployeeBean viewStrEmployeeBean1 = new ViewStrEmployeeBean();
                            List<ViewStrEmployeeBean>  collectOn = new LinkedList<>();
                            viewStrEmployeeBean1.setType(customField.getName());
                            collectOn.add(viewStrEmployeeBean1);
                            if (byJiraUserKey!=null){
                            collectOn = getSelfInfoAndSupervisorInfo(byJiraUserKey, collectOn);
                            }
                            collectInfo.add(collectOn);
                        }
                    });

/*-------------------------------------------------------------------------------------------*/
        Random random=new Random();
        return EasyMap.build("currReporterId", currReporterId, "currAssigneeId", currAssigneeId,
                "currReporterOrganization", currReporterOrganization,
                "currReporterDepartment", currReporterDepartment,
                "currReporterOrganizationPath", currReporterOrganizationPath,
                "currReporterOrganizationPathTooltip", currReporterOrganizationPathTooltip,
                "currReporterStaffName", currReporterStaffName,
                "currAssigneeOrganization", currAssigneeOrganization,
                "currAssigneeDepartment", currAssigneeDepartment,
                "currAssigneeOrganizationPath", currAssigneeOrganizationPath,
                "currAssigneeOrganizationPathTooltip", currAssigneeOrganizationPathTooltip,
                "currAssigneeStaffName", currAssigneeStaffName,
                "reporterStaffId", reporterStaffId,
                "reporterDepartmentId", reporterDepartmentId,
                "reporterOrganizationId", reporterOrganizationId,
                "assigneeStaffId", assigneeStaffId,
                "assigneeDepartmentId", assigneeDepartmentId,
                "assigneeOrganizationId", assigneeOrganizationId,
                "list",collectInfo,
                /*"assigneeOrganization", entityassigneeStrOrganize,
                "reporterOrganization", entityreporterStrOrganize,
                "assigneeStrEmployee", entityassigneeStrEmployee,
                "reporterStrEmployee", entityreporterStrEmployee,
                "assigneeDepartment", assigneeDepartmentEntity,
                "reporterDepartment", reporterDepartmentEntity,*/
                //"assigneeStrEmployeeDirector", entityassigneeStrEmployeeDirector,
              //  "reporterStrEmployeeDirector", entityreporterStrEmployeeDirector,
                "assigneeSubId",assigneeSubId,
                "reporterSubId",reporterSubId,
                "randomStr", String.valueOf(random.nextInt(10000))
              //  "contextPath",contextPaht
        );

    }
    private List<ViewStrEmployeeBean> getSelfInfoAndSupervisorInfo(StrEmployee strEmployee,List<ViewStrEmployeeBean>  collectOn){
        ViewStrEmployeeBean viewStrEmployeeBea = new ViewStrEmployeeBean();
        viewStrEmployeeBea.setEmployeeName(strEmployee.getEmployeeName());
        viewStrEmployeeBea.setStrEmpId(String.valueOf(strEmployee.getID()));
       // viewStrEmployeeBea.setEmail(StringUtils.isBlank(strEmployee.getEmail()) ? "" : strEmployee.getEmail());
       // viewStrEmployeeBea.setPhone(StringUtils.isBlank(strEmployee.getPhone()) ? "" : strEmployee.getPhone());
      //  StruGroupOfEmployee depart = strEmployeeService.getDepart(strEmployee.getID());
      //  viewStrEmployeeBea.setDepartment(depart == null ? "" : (StringUtils.isBlank(depart.getGroup().getGroupName()) ? "" : depart.getGroup().getGroupName()));
      //  viewStrEmployeeBea.setOrganization(strEmployee.getStrOrganize() == null ? "" : (StringUtils.isBlank(strEmployee.getStrOrganize().getName()) ? "" : strEmployee.getStrOrganize().getName()));
        collectOn.add(viewStrEmployeeBea);
        ViewStrEmployeeBean viewStrEmployeeBea3 = new ViewStrEmployeeBean();
        if (strEmployee.getSupervisor() != null) {
            StrEmployee employee = strEmployeeService.getEmployee(strEmployee.getSupervisor());
            viewStrEmployeeBea3.setEmployeeName(strEmployee.getSupervisor() == null ? "" : employee.getEmployeeName());
            viewStrEmployeeBea3.setStrEmpId(String.valueOf(employee.getID()));
           // viewStrEmployeeBea3.setEmail(StringUtils.isBlank(employee.getEmail()) ? "" : employee.getEmail());
           // StruGroupOfEmployee depart1 = strEmployeeService.getDepart(employee.getID());
          //  viewStrEmployeeBea3.setDepartment(depart1 == null ? "" : (StringUtils.isBlank(depart1.getGroup().getGroupName()) ? "" : depart1.getGroup().getGroupName()));
           // viewStrEmployeeBea3.setPhone(StringUtils.isBlank(employee.getPhone()) ? "" : employee.getPhone());
          //  viewStrEmployeeBea3.setOrganization(employee.getStrOrganize() == null ? "" : (StringUtils.isBlank(employee.getStrOrganize().getName()) ? "" : employee.getStrOrganize().getName()));
            collectOn.add(viewStrEmployeeBea3);
        }
        return collectOn;
    }
}