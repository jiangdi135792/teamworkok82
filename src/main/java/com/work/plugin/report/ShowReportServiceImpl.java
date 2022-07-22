package com.work.plugin.report;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.status.SimpleStatusImpl;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.work.plugin.rest.StrEmployeeBean;
import com.work.plugin.ao.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by admin on 2021/7/5.
 */
@RequiredArgsConstructor
public class ShowReportServiceImpl implements ShowReportService {
    private static final Logger log = LoggerFactory.getLogger(ShowReportServiceImpl.class);
    private final ActiveObjects ao;
    private final SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
    private final UserManager userManager;
    private final IssueManager issueManager;
    private final WorklogManager worklogManager;
    private final SetReportService service;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
    private final ProjectManager projectManager;
    private final StrEmployeeService strEmployeeService;
    private final String TODO_COLOR = "blue-gray";
    private final String IN_PROGRESS_COLOR = "yellow";
    private final String DONE_COLOR = "green";

    private List<StrOrganize> orgs = new ArrayList<>();//所有机构集合
    private ArrayList<HashMap<String, String>> result = new ArrayList<>();//返回的查询结果

    /**
     * 根据条件查询报表结果
     */
    @Override
    public ReportInfoBean all(){
        if(result.size() != 0){
            result.clear();
        }
        n = 1;
        orgs = Arrays.asList(ao.find(StrOrganize.class));//获取所有机构集合
        ReportInfoBean bean = new ReportInfoBean();
        SetReportBean bean1 = service.getReportByReportkey(GlobalReport.jira_user_key,GlobalReport.reportKey);//获取当前报表设置

        //判断单选框是选择是机构还是团队
        if("teamRadio".equals(GlobalReport.radioType)){
            if(!"".equals(GlobalReport.teamids)){
                ArrayList<Integer> teams = new ArrayList<>();//所有的团队id集合
                for(String str : GlobalReport.teamids.split(",")){
                    if(isStrToNum(str)){
                        int id = Integer.parseInt(str);
                        TeamProjectEntity[] entity = ao.find(TeamProjectEntity.class,String.format(" TEAM_ID = '%d' ",(id-20000)));
                        if(entity.length == 0){
                            Project project = projectManager.getProjectObj((long)id);
                            if(project == null){
                                DepartmentEntity departmentEntity = ao.get(DepartmentEntity.class,id-20000);
                                if(departmentEntity.getParent() != null && !"".equals(departmentEntity.getParent())){
                                    int teamid = id - 20000;//团队的id
                                    teams.add(teamid);
                                    continue;
                                }
                            }
                            List<Issue> issues = getIssueByJql("project = " + project.getName());
                            ArrayList<HashMap<String,String>> issuemaps = new ArrayList<>();//此项目下所有issue的map集合
                            int ProjectTreeId = n++;//此项目用于生成树的id
                            for(Issue issue : issues){
                                issuemaps.add(formatMap(issue,ProjectTreeId,""));
                            }
                            HashMap<String,String> proMap = sumMapResult(project.getName(),issuemaps,"project");//此项目对应的map
                            proMap.put("id",""+ProjectTreeId);
                            result.add(proMap);
                        } else {
                            int teamid = id - 20000;//团队的id
                            teams.add(teamid);
                        }
                    }
                }
                ArrayList<Integer> teamsTemp = new ArrayList<>();
                teamsTemp.addAll(teams);
                ArrayList<Integer> teamids = changeTeamIds(teams);//经过去除子集处理的团队id集合
                if(teamids.size() != 0){
                    for(Integer teamid : teamids){
                        TeamProjectEntity[] entity = ao.find(TeamProjectEntity.class,String.format(" TEAM_ID = '%d' ",getParentTeam(teamid)));
                        if(entity.length == 0){
                            continue;
                        }
                        Project project = projectManager.getProjectObj((long)entity[0].getProjectId());//此团队的父集根团队对应的项目
                        GlobalReport.proname = project.getName();
                        int ProjectTreeId = n;//此项目用于生成树的id
                        n++;
                        DepartmentEntity teamEntity = ao.get(DepartmentEntity.class,teamid);//团队的实体
                        ArrayList<HashMap<String,String>> teamMaps = buildChildrenTeam(teamid,teamEntity.getGroupName(),ProjectTreeId,teamsTemp);
                        HashMap<String,String> proMap = sumMapResult(project.getName(),teamMaps,"project");//此项目对应的map
                        proMap.put("id",""+ProjectTreeId);
                        result.add(proMap);
                    }
                }
                formatResult(result);
                bean.setLists(result);
                return bean;
            }
            return new ReportInfoBean();
        }

        //如果报表的第一分组为project，则执行另外的查询方法
        if("project".equals(bean1.getGroup_one())){
            ReportInfoBean reportInfoBean = getReportByIssue();
            return reportInfoBean;
        } else {
            //如果选择了机构或部门，则不遍历所有机构部门
            if(GlobalReport.orgtype != null && !"".equals(GlobalReport.orgtype)){
                int id = n++;//用于生成树的id
                if("org_type".equals(GlobalReport.orgtype)){
                    buildChildrenOrg(GlobalReport.orgid,GlobalReport.orgname,id,1);
                    for(HashMap<String, String> map : result){
                        if("1".equals(map.get("_parentId"))){
                            map.remove("_parentId");
                            break;
                        }
                    }
                    formatResult(result);//格式化输出结果
                    bean.setLists(result);
                    return bean;
                } else if("dept_type".equals(GlobalReport.orgtype)){
                    buildChildrenDept(GlobalReport.orgid,GlobalReport.orgname,id);
                    for(HashMap<String, String> map : result){
                        if("1".equals(map.get("_parentId"))){
                            map.remove("_parentId");
                            break;
                        }
                    }
                    formatResult(result);//格式化输出结果
                    bean.setLists(result);
                    return bean;
                }
            }
        }


        //如果第一分组不是project，且没有选择机构或部门，则遍历所有机构部门
        for(StrOrganize org : orgs){
            int id = n++;//此机构的id，用于生成树
            //判断是否为根组织,即没有上级,是根组织则继续向下遍历
            if(org.getParent() == null || org.getParent() == 0 || org.getID()==org.getParent()){
                buildChildrenOrg(org.getID(),org.getName(),id,1);
            }
        }
        formatResult(result);
        bean.setLists(result);
        return bean;
    }

    /**
     * 层级递归机构及其所有子机构
     * @param orgNo 机构编号
     * @param parentId 机构上级树的id
     */
    public ArrayList<HashMap<String,String>> buildChildrenOrg(int orgNo,String name,int parentId,int type){
        ArrayList<HashMap<String,String>> lists = new ArrayList<>();//此机构下所有信息之和,包括子机构和子部门
        StrOrganize[] strOrganize = ao.find(StrOrganize.class, String.format("PARENT = '%s'  and ID <> %d", orgNo, orgNo));//此机构下的所有子机构
        int id = n++;
        //判断机构下是否有子机构
        if(strOrganize.length != 0){//有子机构
            ArrayList<HashMap<String,String>> templists = new ArrayList<>();
            for(StrOrganize org : strOrganize){
                templists.addAll(buildChildrenOrg(org.getID(),org.getName(),id,2));//回调执行查询子机构函数
            }
            lists.addAll(templists);
        }

        StrOrganizeGroup[] organizeGroups = ao.find(StrOrganizeGroup.class, MessageFormat.format("ORG_ID = {0}", orgNo));//机构与部门的对应关系表对象
        //判断机构下是否有直属部门
        if(organizeGroups.length != 0){//有直属部门
            DepartmentEntity[] depts = Lists.newArrayList(organizeGroups).stream().map(e -> e.getGroup()).filter(s -> s.getType() == 0).toArray(DepartmentEntity[]::new);//type为0表示部门
            ArrayList<HashMap<String,String>> templists = new ArrayList<>();
            for(DepartmentEntity dept : depts){
                templists.addAll(buildChildrenDept(dept.getID(),dept.getGroupName(),id));
            }
            lists.addAll(templists);
        }

        boolean flag = true;
        ;       //判断此机构有没有直属雇员，即在此机构下找到的雇员没有在部门下
        StrEmployee[] employees = ao.find(StrEmployee.class,MessageFormat.format("STR_ORGANIZE_ID = {0}", orgNo));
        if(employees.length != 0){
            ArrayList<HashMap<String,String>> templists = new ArrayList<>();
            for(StrEmployee emp : employees){
                StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("EMPLOYEE_ID = {0}", emp.getID()));
                //为0则表示此雇员不在部门下
                if(groupemps.length == 0){
                    flag = false;
                    int temp_id = n++;//当前雇员的树的id
                    ArrayList<HashMap<String,String>> temp = getEmployeeReport(emp.getID(),emp.getEmployeeName(),temp_id);
                    templists.addAll(temp);
                    HashMap<String,String> temp_map = sumMapResult(emp.getEmployeeName(),temp,"employee");
                    temp_map.put("_parentId",""+id);
                    temp_map.put("id",""+temp_id);
                    result.add(temp_map);//添加雇员信息汇总map
                }
            }
            lists.addAll(templists);
        }


        //如果此机构下没有子机构也没有子部门也没有直属雇员
        if(strOrganize.length == 0 && organizeGroups.length == 0 && flag){
            HashMap<String,String> map = new HashMap<>();
            map.put("id",""+id);
            map.put("name",name);
            map.put("_parentId",""+parentId);
            map.put("iconCls", "icon-organization");
            result.add(map);
        } else {
            HashMap<String,String> map = sumMapResult(name,lists,"organization");
            if(type == 2){
                map.put("_parentId",""+parentId);
            }
            map.put("id",""+id);
            result.add(map);
        }
        return lists;
    }


    /**
     * 层级递归部门及其所有子部门
     * @param deptNo 此部门编号
     * @param name 此部门的上级机构或上级部门的名称
     * @param parentId 此部门的上级机构或上级部门的树的id
     * @return
     */
    public ArrayList<HashMap<String,String>> buildChildrenDept(int deptNo,String name,int parentId){
        ArrayList<HashMap<String,String>> lists = new ArrayList<>();//此部门下所有信息之和
        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", deptNo, deptNo));//获取此部门下的部门
        //判断部门下是否有部门
        int id = n++;
        if(depts.length != 0){//表示有部门
            ArrayList<HashMap<String,String>> templists = new ArrayList<>();
            for(DepartmentEntity dept : depts){
                templists.addAll(buildChildrenDept(dept.getID(),dept.getGroupName(),id));
            }
            lists.addAll(templists);
        }

        StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0}", deptNo));
        //判断此部门下是否有雇员
        if(groupemps.length != 0){//表示有雇员
            StrEmployee[] employees = Lists.newArrayList(groupemps).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new);//获取部门下的雇员
            ArrayList<HashMap<String,String>> templists = new ArrayList<>();
            for(StrEmployee emp : employees){
                int temp_id = n++;//当前雇员的树的id
                ArrayList<HashMap<String,String>> temp = getEmployeeReport(emp.getID(),emp.getEmployeeName(),temp_id);
                templists.addAll(temp);
                HashMap<String,String> temp_map = sumMapResult(emp.getEmployeeName(),temp,"employee");
                temp_map.put("_parentId",""+id);
                temp_map.put("id",""+temp_id);
                result.add(temp_map);//添加雇员信息汇总map
            }
//            if(lists.size() != 0){
//                templists.addAll(lists);
//            }
            lists.addAll(templists);
        }
        //如果此部门下没有子部门也没有雇员
        if(depts.length == 0 && groupemps.length == 0){
            HashMap<String,String> map = new HashMap<>();
            map.put("id",""+id);
            map.put("name",name);
            map.put("_parentId",""+parentId);
            map.put("iconCls", "icon-department");
            result.add(map);
        } else {
            HashMap<String,String> map = sumMapResult(name,lists,"department");
            map.put("_parentId",""+parentId);
            map.put("id",""+id);
            result.add(map);
        }
        return lists;
    }

    /**
     * 层级递归团队及其所有子团队
     * @param teamId 此团队编号
     * @param name 此团队的上级机构或上级团队的名称
     * @param parentId 此团队的上级机构或上级团队的树的id
     * @return
     */
    public ArrayList<HashMap<String,String>> buildChildrenTeam(int teamId,String name,int parentId,ArrayList<Integer> teams){
        ArrayList<HashMap<String,String>> lists = new ArrayList<>();//此团队下所有信息之和
        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", teamId, teamId));//获取此团队下的团队
        //判断团队下是否有团队
        int id = n++;
        if(depts.length != 0){//表示有团队
            ArrayList<HashMap<String,String>> templists = new ArrayList<>();
            for(DepartmentEntity dept : depts){
                if(teams.contains(dept.getID())){
                    templists.addAll(buildChildrenTeam(dept.getID(),dept.getGroupName(),id,teams));
                }
            }
            lists.addAll(templists);
        }

        StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0}", teamId));
        //判断此团队下是否有雇员
        if(groupemps.length != 0){//表示有雇员
            StrEmployee[] employees = Lists.newArrayList(groupemps).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new);//获取团队下的雇员
            ArrayList<HashMap<String,String>> templists = new ArrayList<>();
            for(StrEmployee emp : employees){
                int temp_id = n++;//当前雇员的树的id
                ArrayList<HashMap<String,String>> temp = getEmployeeReport1(emp.getID(),emp.getEmployeeName(),temp_id);
                templists.addAll(temp);
                HashMap<String,String> temp_map = sumMapResult(emp.getEmployeeName(),temp,"employee");
                temp_map.put("_parentId",""+id);
                temp_map.put("id",""+temp_id);
                result.add(temp_map);//添加雇员信息汇总map
            }
            lists.addAll(templists);
        }

        //如果此团队下没有子团队也没有雇员
        if(depts.length == 0 && groupemps.length == 0){
            HashMap<String,String> map = new HashMap<>();
            map.put("id",""+id);
            map.put("name",name);
            map.put("_parentId",""+parentId);
            result.add(map);
        } else {
            HashMap<String,String> map = sumMapResult(name,lists,"project");
            map.put("_parentId",""+parentId);
            map.put("id",""+id);
            result.add(map);
        }
        return lists;
    }

    /**
     * 根据雇员id获取其对应的issue数据并转化为map
     * @param empId 雇员id
     * @param name
     * @param id 雇员树的id
     * @return
     */
    public ArrayList<HashMap<String,String>> getEmployeeReport1(int empId,String name,int id){
        ArrayList<HashMap<String,String>> maps = new ArrayList<>();
        String userKey = getJiraUserKeyByEmpNo(empId);//雇员对应的jira用户的key
        List<Issue> issues = getIssueByJiraUserKey(userKey);//该雇员下所有的issue的集合
        for(Issue issue : issues){
            maps.add(formatMap(issue,id,userKey));
        }
        return maps;
    }

    /**
     * 根据雇员id获取其对应的issue数据并转化为map
     * @param empId 雇员id
     * @param name
     * @param id 雇员树的id
     * @return
     */
    public ArrayList<HashMap<String,String>> getEmployeeReport(int empId,String name,int id){
        ArrayList<HashMap<String,String>> maps = new ArrayList<>();
//        GlobalReport.testKey.add("雇员名称："+name+",");
        String userKey = getJiraUserKeyByEmpNo(empId);//雇员对应的jira用户的key
//        GlobalReport.testKey.add("雇员对应的jira用户的key："+userKey+",");
        List<Issue> issues = getIssueByJiraUserKey(userKey);//该雇员下所有的issue的集合
//        GlobalReport.testKey.add("该雇员下所有的issue的数量："+issues.size()+"---------");
        SetReportBean bean = service.getReportByReportkey(GlobalReport.jira_user_key,GlobalReport.reportKey);
        if(!"project".equals(bean.getGroup_one())){
            //将issue集合根据不同的项目分类,一个项目对应其项目下的issue的集合
            HashMap<Project,List<Issue>> proToIss = new HashMap<>();
            for(Issue issue : issues){
                Project project = issue.getProjectObject();
                List<Issue> tempIss = proToIss.get(project);
                if(tempIss == null){
                    tempIss = new ArrayList<>();
                }
                tempIss.add(issue);
                proToIss.put(project,tempIss);
            }
            Iterator iter = proToIss.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Project project = (Project) entry.getKey();//获取键
                int ProjectTreeId = n;//此项目用于生成树的id
                n++;
                List<Issue> issuesTemp = (List<Issue>)entry.getValue();//获取值
                ArrayList<HashMap<String,String>> mapsTemp = new ArrayList<>();//此项目下所有issue的map集合
                for(Issue issue : issuesTemp){
                    mapsTemp.add(formatMap(issue,ProjectTreeId,userKey));
                }
                HashMap<String,String> proMap = sumMapResult(project.getName(),mapsTemp,"project");//此项目对应的map
                proMap.put("_parentId",""+id);
                proMap.put("id",""+ProjectTreeId);
                maps.add(proMap);
                result.add(proMap);
            }
        } else {
            for(Issue issue : issues){
                maps.add(formatMap(issue,id,userKey));
            }
        }
        return maps;
    }


    /**
     * 根据雇员id找到对应的jira用户的key
     * @param empNo 雇员id
     * @return 对应的jira用户的key
     */
    public String getJiraUserKeyByEmpNo(int empNo){
        StrEmployee emp = ao.get(StrEmployee.class, empNo);
        String userKey = emp.getJiraUserKey();
        if(userKey == null || "".equals(userKey) || " ".equals(userKey)){
            return null;
        }
        return userKey;
    }


    /**
     * 查询根据条件拼接成jql语句查询issue集合
     * @param userKey
     */
    public List<Issue> getIssueByJiraUserKey(String userKey){
        String jqlQuery = getJqlQuery(userKey);
//        GlobalReport.testKey.add("执行的jql语句："+jqlQuery+",");
        List<Issue> issues = new ArrayList<>();
        final SearchService.ParseResult parseResult = searchService.parseQuery(userManager.getUserByKey(userKey), jqlQuery);
        if (parseResult.isValid()){
            try {
                SearchResults results = searchService.search(userManager.getUserByKey(userKey), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                issues = results.getIssues();
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        return issues;
    }

    int n = 1;
    /**
     * 将所查询到的map转化为对应树形列表的map
     * @param issue 需要转化为map的issue
     * @param id 此issue的父节点的id
     * @param userKey 雇员对应的jira用户的key
     * @return
     */
    public HashMap<String,String> formatMap(Issue issue,int id,String userKey){
        HashMap<String,String> map = new HashMap<>();

        map.put("id",""+n);//为当前issue的id
        n++;
        map.put("issuekey",issue.getKey());
        map.put("name",issue.getSummary());
        map.put("_parentId",""+id);//id为此issue的父集雇员的id
        HashMap<String,String> map1 = changMap(map,issue,userKey);
        result.add(map1);
        return map1;
    }

    /**
     * 将传入的issue按照选择的列转化为map
     * @param map
     * @param issue
     * @param userKey 雇员对应的jira用户的key
     * @return
     */
    public HashMap<String,String> changMap(HashMap<String,String> map,Issue issue,String userKey){
        for(String column : getShowColumn()){
            switch (column){
                case "name": map.put(column,issue.getSummary());break;
                case "workingHours": map.put(column,(issue.getTimeSpent()==null?"0":formatToLong(""+getLogWorkByIssue(userKey,issue))));break;
                case "countProject": map.put(column,issue.getProjectObject().getName());break;
                case "todo": map.put(column,getIssueStatus(issue,column));break;
                case "doing": map.put(column,getIssueStatus(issue,column));break;
                case "done": map.put(column,getIssueStatus(issue,column));break;
                case "Key": map.put(column,issue.getKey());break;
                case "Summary": map.put(column,issue.getSummary());break;
                case "Assignee": map.put(column,(issue.getAssignee()==null?"":issue.getAssignee().getName()));break;
                case "Report": map.put(column,(issue.getReporterId()==null?"":issue.getReporter().getName()));break;
                case "Status": map.put(column,issue.getStatus().getName());break;
                case "Created": map.put(column,(issue.getCreated()==null?"":format.format(issue.getCreated().getTime())));break;
                case "Updated": map.put(column,(issue.getUpdated()==null?"":format.format(issue.getUpdated().getTime())));break;
                case "Due": map.put(column,(issue.getDueDate()==null?"":format.format(issue.getDueDate().getTime())));break;
                case "Environment": map.put(column,issue.getEnvironment());break;
                case "Time Spent": map.put(column,(issue.getTimeSpent()==null?"0":formatToLong(""+issue.getTimeSpent()/3600.0)));break;
                case "Votes": map.put(column,""+issue.getVotes());break;
                case "Watchers": map.put(column,""+issue.getWatches());break;
                case "Creator": map.put(column,issue.getCreator().getName());break;
                case "Security": map.put(column,(issue.getSecurityLevelId()==null?"":""+issue.getSecurityLevelId()));break;
                case "Description": map.put(column,issue.getDescription());break;
                case "issueType": map.put(column,issue.getIssueType().getName());break;
                case "estimate": map.put(column,(issue.getEstimate()==null?"0":formatToLong(""+issue.getEstimate()/3600.0)));break;
                case "originalEstimate": map.put(column,(issue.getOriginalEstimate()==null?"0":formatToLong(""+issue.getOriginalEstimate()/3600.0)));break;
                case "components": map.put(column,getComponentsByIssue(issue));break;
                case "affectedVersions": map.put(column,getAffectedVersionsByIssue(issue));break;
                case "fixVersions": map.put(column,getFixVersionsByIssue(issue));break;
                case "number": map.put(column,issue.getNumber().toString());break;
                case "resolutionDate": map.put(column,(issue.getResolutionDate()==null?"":format.format(issue.getResolutionDate().getTime())));break;
                case "priority": map.put(column,issue.getPriority().getName());break;
                case "parentObject": map.put(column,(issue.getParentObject()==null?"":""+issue.getParentObject().getSummary()));break;
                case "iconCls":map.put(column, "icon-issue");break;
                default:map.put(column,(customFieldManager.getCustomFieldObject(column)==null?"":""+customFieldManager.getCustomFieldObject(column)));
            }
        }
        return map;
    }

    /**
     * 对字符串格式化，获取小数点后1位
     * @param str
     * @return
     */
    public String formatToLong(String str){
        try {
            double x = Double.parseDouble(str);
            return ""+((double)((int)(x*100)))/100.00;
        } catch (Exception e){
            return str;
        }
    }

    public String getAffectedVersionsByIssue(Issue issue){
        String str_result = "";
        for(Version version : issue.getAffectedVersions()){
            str_result = str_result + version.getName();
        }
        return str_result;
    }

    public String getFixVersionsByIssue(Issue issue){
        String str_result = "";
        for(Version version : issue.getFixVersions()){
            str_result = str_result + version.getName();
        }
        return str_result;
    }

    public String getComponentsByIssue(Issue issue){
        String str_result = "";
        for(ProjectComponent projectComponent : issue.getComponents()){
            str_result = str_result + projectComponent.getName();
        }
        return str_result;
    }

    /**
     * 获取表格中所有需要展示的列
     * @return
     */
    public ArrayList<String> getShowColumn(){
        ArrayList<String> lists = new ArrayList<>();
        List<SetReportColumnBean> columnbeans = service.getColumn(GlobalReport.jira_user_key,GlobalReport.reportKey,"show");
        for(SetReportColumnBean bean : columnbeans){
            lists.add(bean.getColumnName());
        }
        lists.add("iconCls");
        return lists;
    }

    /**
     * 将map集合合并成一个map
     * @param name 机构名称或部门名称或雇员名称
     * @param lists 需要合并的集合，例如雇员下的所有issue
     * statistics 统计状态，0不统计，1计数，2合计
     * @return 合并后的map
     */
    public HashMap<String,String> sumMap(String name,ArrayList<HashMap<String,String>> lists){
        HashMap<String,String> mapReault = new HashMap<>();
        List<SetReportColumnBean> columnbeans = service.getColumn(GlobalReport.jira_user_key,GlobalReport.reportKey,"show");
        mapReault.put("name",name);
        //根据统计值对此列进行操作，0-不操作 1-统计 sum 2-计数 count
        for(SetReportColumnBean bean : columnbeans){
            double sumnum = 0;
            if(bean.getColumnName().equals("name")){
                continue;
            }
            if(bean.getStatistics().equals("2")){
                for(HashMap<String,String> map : lists) {
                    Iterator iter = map.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        String key = (String) entry.getKey();//获取键
                        if (key.equals(bean.getColumnName())) {
                            if("countProject".equals(bean.getColumnName())){
                                mapReault.put(key, getCountProjects(lists));
                                break;
                            }
                            mapReault.put(key, "" + lists.size());
                            break;
                        }
                    }
                }
            } else if(bean.getStatistics().equals("1")){
                for(HashMap<String,String> map : lists){
                    Iterator iter = map.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        String key = (String)entry.getKey();//获取键
                        String value = (String)entry.getValue();//获取值
                        if(key.equals(bean.getColumnName())){
                            if(key.equals("workingHours") || key.equals("Time Spent") || key.equals("'estimate'") || key.equals("'originalEstimate'")){
                                if(isStrToLong(value)){
                                    sumnum = sumnum +  Double.parseDouble(value);
                                }
                            } else {
                                if(isStrToNum(value)){
                                    sumnum = sumnum + Integer.parseInt(value);
                                    sumnum = (int)sumnum;
                                }
                            }
                        }
                    }
                }
                String str = "";
                if("0".equals((""+sumnum).split("\\.")[1])){
                    str = (""+sumnum).split("\\.")[0];
                } else {
                    str = ""+sumnum;
                }
                mapReault.put(bean.getColumnName(),str);
            } else {
                for(HashMap<String,String> map : lists){
                    Iterator iter = map.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        String key = (String)entry.getKey();//获取键
                        if(key.equals(bean.getColumnName())){
                            mapReault.put(key,"");
                            break;
                        }
                    }
                }
            }
        }
        return mapReault;
    }

    /**
     * 将map集合合并成一个map
     * @param name 机构名称或部门名称或雇员名称
     * @param lists 需要合并的集合，例如雇员下的所有issue
     * statistics 统计状态，0不统计，1计数，2合计
     * @param type  需要统计的类型，如机构、部门、项目、雇员
     * @return 合并后的map
     */
    public HashMap<String,String> sumMapResult(String name,ArrayList<HashMap<String,String>> lists,String type){
        HashMap<String,String> mapReault = new HashMap<>();
        List<SetReportColumnBean> columnbeans = service.getColumn(GlobalReport.jira_user_key,GlobalReport.reportKey,"show");
        mapReault.put("name",name);
        //根据不同的类型修改不同的图标
        switch (type){
            case "organization":mapReault.put("iconCls", "icon-organization");break;
            case "department":mapReault.put("iconCls", "icon-department");break;
            case "project":mapReault.put("iconCls", "icon-project");break;
            case "employee":mapReault.put("iconCls", "icon-employee");break;
            default:break;
        }
        if(lists == null || lists.size() == 0){
            return mapReault;
        }
        //根据统计值对此列进行操作，0-不操作 1-统计 sum 2-计数 count
        for(SetReportColumnBean bean : columnbeans){
            double sumnum = 0;
            if(bean.getColumnName().equals("name")){
                continue;
            }
            if(bean.getStatistics().equals("2")){//列属性为2--计数
                if(lists.get(0).get("issuekey") != null && !"".equals(lists.get(0).get("issuekey"))){
                    for(HashMap<String,String> map : lists) {
                        Iterator iter = map.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry entry = (Map.Entry) iter.next();
                            String key = (String) entry.getKey();//获取键
                            if (key.equals(bean.getColumnName())) {
                                if("countProject".equals(bean.getColumnName())){
                                    mapReault.put(key, getCountProjects(lists));
                                    break;
                                }
                                mapReault.put(key, "" + lists.size());
                                break;
                            }
                        }
                    }
                } else {
                    int countNum = 0;
                    for(HashMap<String,String> map : lists) {
                        Iterator iter = map.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry entry = (Map.Entry) iter.next();
                            String key = (String) entry.getKey();//获取键
                            String value = (String)entry.getValue();//获取值
                            if (key.equals(bean.getColumnName())) {
                                if("countProject".equals(bean.getColumnName())){
                                    mapReault.put(key, getCountProjects(lists));
                                    break;
                                }
                                if(isStrToNum(value)){
                                    countNum = countNum + Integer.parseInt(value);
                                }
                            }
                        }
                    }
                    if(!"countProject".equals(bean.getColumnName())){
                        mapReault.put(bean.getColumnName(), "" + countNum);
                    }
                }
            } else if(bean.getStatistics().equals("1")){//列属性为1--统计
                for(HashMap<String,String> map : lists){
                    Iterator iter = map.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        String key = (String)entry.getKey();//获取键
                        String value = (String)entry.getValue();//获取值
                        if(key.equals(bean.getColumnName())){
                            if(key.equals("workingHours") || key.equals("Time Spent") || key.equals("estimate") || key.equals("originalEstimate")){
                                if(isStrToLong(value)){
                                    sumnum = sumnum +  Double.parseDouble(value);
                                }
                            } else {
                                if(isStrToNum(value)){
                                    sumnum = sumnum + Integer.parseInt(value);
                                    sumnum = (int)sumnum;
                                }
                            }
                        }
                    }
                }
                String str = "";
                if("0".equals((""+sumnum).split("\\.")[1])){
                    str = (""+sumnum).split("\\.")[0];
                } else {
                    str = formatToLong(""+sumnum);
                }
                mapReault.put(bean.getColumnName(),str);
            } else {//列属性为0--空
                for(HashMap<String,String> map : lists){
                    Iterator iter = map.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        String key = (String)entry.getKey();//获取键
                        if(key.equals(bean.getColumnName())){
//                            if(key.equals("workingHours") || key.equals("Time Spent") || key.equals("'estimate'") || key.equals("'originalEstimate'")){
//                                mapReault.put(key,"0");
//                            } else {
                            mapReault.put(key,"");
//                            }
                            break;
                        }
                    }
                }
            }
        }
        return mapReault;
    }

    public String getCountProjects(ArrayList<HashMap<String,String>> lists){
        List<String> pros = new ArrayList<>();
        String[] proArr;
        for(HashMap<String,String> map : lists){
            proArr = map.get("countProject").split(",");
            for(String name : proArr){
                if(!pros.contains(name)){
                    pros.add(name);
                }
            }
        }
        String str = "";
        for(String name : pros){
            str = str + name + ",";
        }
        return str.substring(0,str.length()-1);
    }


    /**
     * 判断一个字符串是否可以转换为整数
     * @param str 字符串
     * @return true 可以; false 不可以
     */
    public static boolean isStrToNum(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 判断一个字符串是否可以转换为long型
     * @param str 字符串
     * @return true 可以; false 不可以
     */
    public static boolean isStrToLong(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 根据报表设置找出要执行的jql语句
     * @return
     */
    public String getJqlQuery(String userKey){
        SetReportBean bean = service.getReportByReportkey(GlobalReport.jira_user_key,GlobalReport.reportKey);
        String groupone = bean.getGroup_one();
//        String grouptwo = bean.getGroup_two();
        String jqlQuery = "";
        if("project".equals(groupone)){
            switch (groupone){
//                case "issue": jqlQuery = " assignee = " + userKey + " OR reporter = " + userKey;break;
                case "worklogAuthor":jqlQuery = " worklogAuthor = '" + userKey+"'";break;
                case "assignee":jqlQuery = " assignee = '" + userKey+"'";break;
                case "reporter":jqlQuery = " reporter = '" + userKey+"'";break;
                default:jqlQuery = " reporter = " + userKey;break;//当第一分组为project，第二分组未分配时，默认为报告人
            }
            if(!"teamRadio".equals(GlobalReport.radioType)){
                jqlQuery = jqlQuery + " AND " + jqlQuery_issue;
            }
        } else {
            switch (groupone){
                case "issue": jqlQuery = " (assignee = '" + userKey + "' OR reporter = '" + userKey + "') ";break;
                case "worklogAuthor":jqlQuery = " worklogAuthor = '" + userKey+"'";break;
                case "assignee":jqlQuery = " assignee = '" + userKey+"'";break;
                case "reporter":jqlQuery = " reporter = '" + userKey+"'";break;
                default:jqlQuery = " assignee = '" + userKey + "' OR reporter = '" + userKey+"'";break;
            }
            if(!"".equals(GlobalReport.proname) && !(GlobalReport.proname == null)){
                jqlQuery = jqlQuery + " AND project in " + changeToString(GlobalReport.proname) ;
            }
        }
        if(!"".equals(GlobalReport.startTime) && !(GlobalReport.startTime == null)){
            jqlQuery = jqlQuery + " AND createdDate >= '" + changeTime(GlobalReport.startTime) + "'";
        }
        if(!"".equals(GlobalReport.endTime) && !(GlobalReport.endTime == null)){
            jqlQuery = jqlQuery + " AND createdDate <= '" + changeTime(GlobalReport.endTime) + "'";
        }
        return jqlQuery;
    }

    /**
     * 将前台传过来的查询条件变为("项目一","项目二")的形式，以符合jql查询语句
     * @param str 查询条件
     * @return 变换后的字符串
     */
    public String changeToString(String str){
        if("".equals(str)){
            return str;
        } else {
            if(str.contains(",")){
                str = str.replace(",","\",\"");
                return "(\""+str+"\")";
            } else {
                return "(\""+str+"\")";
            }
        }
    }

    /**
     * 将时间从MM/dd/yyyy转化为jql语句可以操作的yyyy/MM/dd
     * @param time
     * @return
     */
    public String changeTime(String time){
        String resultTime = "";
        String[] arr = time.split("/");
        resultTime = resultTime + arr[2] + "/" + arr[0] + "/" + arr[1];
        return resultTime;
    }

    /**
     * 根据雇员编号计算出此雇员参与的项目数
     * @param empNo
     * @return
     */
    public int getProjectCount(int empNo){
        String userKey = getJiraUserKeyByEmpNo(empNo);
        String jqlQuery = " assignee = '" + userKey + "' OR reporter = '" + userKey+"'";
        List<Issue> issues = new ArrayList<>();
        final SearchService.ParseResult parseResult = searchService.parseQuery(userManager.getUserByKey(userKey), jqlQuery);
        if (parseResult.isValid()){
            try {
                SearchResults results = searchService.search(userManager.getUserByKey(userKey), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                issues = results.getIssues();
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        List<Long> projects = new ArrayList<>();
        for(Issue issue : issues){
            if(!projects.contains(issue.getProjectId())){
                projects.add(issue.getProjectId());
            }
        }
        return projects.size();
    }

    /**
     * 获取用户在此issue中logwork的时间
     * @param jira_user_key 用户的key
     * @param issue 用户logwork的issue
     * @return logwork的时间，转化成小时，比如1.5h
     */
    public String getLogWorkByIssue(String jira_user_key,Issue issue){
        List<Worklog> worklogs = worklogManager.getByIssue(issue);
        long sumTimeSpent = 0;
        for(Worklog worklog : worklogs){
            if(worklog.getAuthorKey().equals(jira_user_key)){
                sumTimeSpent += worklog.getTimeSpent();
            }
        }
        return ""+sumTimeSpent/3600.0;
    }

    /**
     * 获取所有项目名称
     * @return 所有项目名称
     */
    public HashMap<String,ArrayList<SelectBean>> getProName(){
        ArrayList<ComboboxBean> proNames = new ArrayList<>();
        List<Project> projects = projectManager.getProjects();
        for(Project project : projects){
            ComboboxBean bean = new ComboboxBean();
            bean.setComboboxValue(project.getName());
            proNames.add(bean);
        }
        return changeToSelect2(proNames);
    }

    /**
     * 将查询出的结果转化为select2的数据格式
     * @param lists
     * @return
     */
    public HashMap<String,ArrayList<SelectBean>> changeToSelect2(ArrayList<ComboboxBean> lists){
        HashMap<String,ArrayList<SelectBean>> map = new HashMap<>();
        ArrayList<SelectBean> list = new ArrayList<>();
        SelectBean bean1 = new SelectBean();
        for(ComboboxBean bean : lists){
            bean1 = new SelectBean();
            bean1.setId(bean.getComboboxValue());
            bean1.setText(bean.getComboboxValue());
            list.add(bean1);
        }
        map.put("results",list);
        return map;
    }

    /**
     * 传入一个issue的集合，返回TODO，IN_PROGRESS，DONE状态的统计数据，与每个issue的状态
     * @param issueList
     * @return
     */
    public Map<String, List> getIssueStatuses(List<Issue> issueList) {
        int todoSum = 0, progressSum = 0, doneSum = 0;
        Map<String, List> result = Maps.newHashMap();

        for (Issue issue : issueList) {
            Status status = issue.getStatus();
            SimpleStatusImpl simpleStatus = new SimpleStatusImpl(status);
            StatusCategory statusCategory = simpleStatus.getStatusCategory();
            String colorName = statusCategory.getColorName();
            String key = issue.getKey();
            boolean isTodo = false, isPregress = false, isDone = false;
            switch (colorName) {
                case TODO_COLOR:
                    todoSum++;
                    isTodo = true;
                    break;
                case IN_PROGRESS_COLOR:
                    progressSum++;
                    isPregress = true;
                    break;
                case DONE_COLOR:
                    doneSum++;
                    isDone = true;
                    break;
                default:
                    //
            }
            ArrayList<Boolean> booleans = Lists.newArrayList(isTodo, isPregress, isDone);
            result.put(key, booleans);
        }

        result.put("stats", Lists.newArrayList(todoSum, progressSum, doneSum));

        return result;
    }

    /**
     * 传入issue与状态，判断issue是否属于此状态
     * @param issue
     * @param type
     * @return
     */
    public String getIssueStatus(Issue issue,String type){
        Status status = issue.getStatus();
        SimpleStatusImpl simpleStatus = new SimpleStatusImpl(status);
        StatusCategory statusCategory = simpleStatus.getStatusCategory();
        String colorName = statusCategory.getColorName();
        switch (colorName) {
            case TODO_COLOR:
                if("todo".equals(type)){
                    return ""+1;
                }
                break;
            case IN_PROGRESS_COLOR:
                if("doing".equals(type)){
                    return ""+1;
                }
                break;
            case DONE_COLOR:
                if("done".equals(type)){
                    return ""+1;
                }
                break;
            default:return ""+0;
        }
        return ""+0;
    }

    /**
     * 把返回的结果中todo、doing、done三列格式化格式
     * */
    public void formatResult(ArrayList<HashMap<String, String>> result){
        for(HashMap<String, String> map : result){
            if(map.get("countProject") != null && !"".equals(map.get("countProject") != null)){
                int pronameSize = map.get("countProject").split(",").length;
                map.put("countProject",""+pronameSize);
            } else {
                map.put("countProject","0");
            }
            if(map.get("issuekey") != null){
                if("0".equals(map.get("todo"))){
                    map.put("todo","");
                } else {
                    map.put("todo","true");
                }
                if("0".equals(map.get("doing"))){
                    map.put("doing","");
                } else {
                    map.put("doing","true");
                }
                if("0".equals(map.get("done"))){
                    map.put("done","");
                } else {
                    map.put("done","true");
                }
            }
        }
    }


    private String jqlQuery_issue = "";//当第一分组为project时且选择了机构或部门时，执行的jql语句
    /**
     * 当第一分组为project时执行的查询方法
     * @return
     */
    public ReportInfoBean getReportByIssue(){
        ReportInfoBean bean = new ReportInfoBean();
        List<Project> projects = new ArrayList<>();//所有需要展示的项目
        if(!"".equals(GlobalReport.proname)){
            for(String name : GlobalReport.proname.split(",")){
                projects.add(projectManager.getProjectObjByName(name));
            }
        } else {
            projects.addAll(projectManager.getProjects());
        }
        n = 1;
        ArrayList<HashMap<String,String>> maps = new ArrayList<>();
        for(Project project : projects){
            int id = n++;//用于生成树的id
            HashMap<String,String> proMap;//此项目对应的map
            jqlQuery_issue = "project = '" + project.getName() + "'";

            if("org_type".equals(GlobalReport.orgtype)){//选择的是机构
                buildChildrenOrg(GlobalReport.orgid,GlobalReport.orgname,id,1);
            } else if("dept_type".equals(GlobalReport.orgtype)){//选择的是部门
                buildChildrenDept(GlobalReport.orgid,GlobalReport.orgname,id);
            } else {
                //没有选择机构或部门，展示所有的机构
                ReportInfoBean bean1 = getNewTypeAll(GlobalReport.proname,"reporter");
                return bean1;
            }
            ArrayList<HashMap<String,String>> proChildLists = new ArrayList<>();//项目直属子节点集合
            for(HashMap<String, String> map : result){
                if(map.get("_parentId") == null){
                    map.put("_parentId",""+id);
                }
                if((""+id).equals(map.get("_parentId"))){
                    proChildLists.add(map);
                }
            }
            formatResult(result);//格式化输出结果
            proMap = sumMapResult(project.getName(),proChildLists,"project");
            proMap.put("id",""+id);
            maps.add(proMap);

            maps.addAll(result);
            jqlQuery_issue = "";
            result.clear();
        }
        n =1;
        bean.setLists(maps);
        return bean;
    }

    /**
     * 对后台传过来的团队id集合进行处理，将有子团队且子团队的id在集合中的所有子团队id全部移除
     * @param teamids 团队id集合
     * @return 团队id集合，保证每个团队在此集合中找不到父团队和子团队
     */
    public ArrayList<Integer> changeTeamIds(ArrayList<Integer> teamids){
        ArrayList<Integer> temps = new ArrayList<>();
        temps.addAll(teamids);
        for(Integer teamid : teamids){
            ArrayList<Integer> childTeamids = getChildrenTeamids(teamid);//该团队的子团队id集合
            if(childTeamids.size() == 0){
                continue;
            }
            if(childTeamids.contains(teamid)){
                childTeamids.remove(teamid);
            }
            for(Integer childTeamid : childTeamids){
                if(temps.contains(childTeamid)){
                    temps.remove(childTeamid);
                }
            }
        }
        return temps;
    }

    /**
     * 根据团队的id，获取其所有子团队的集合
     * @param teamid 团队的id
     * @return
     */
    public ArrayList<Integer> getChildrenTeamids(int teamid){
        ArrayList<Integer> lists = new ArrayList<>();
        lists.add(teamid);
        DepartmentEntity[] teamEntities = ao.find(DepartmentEntity.class,String.format(" PARENT = '%s' ",teamid));
        if(teamEntities.length != 0){
            for(DepartmentEntity entity : teamEntities){
                lists.addAll(getChildrenTeamids(entity.getID()));
            }
        }
        return lists;
    }

    /**
     * 根据团队id获取其根团队的id
     * @param teamid
     * @return
     */
    public Integer getParentTeam(int teamid){
        DepartmentEntity teamEntity = ao.get(DepartmentEntity.class,teamid);
        if(teamEntity.getParent() == null || "".equals(teamEntity.getParent())){
            return teamid;
        } else {
            return getParentTeam(Integer.parseInt(teamEntity.getParent()));
        }
    }


    /*------------------------------------------------王清林-----------------------------------------------------------------------*/
    ArrayList<HashMap<String,String>> newResult = new ArrayList<>();
    Map<String,Map<Integer,Integer>> idAndTreeId =new HashMap<>();//String pro org  dep emp  Map<id,treeId>
    List<Integer> treeIdList=new LinkedList();
    Map<String,Map<Integer,ArrayList<HashMap<String,String>>>> proEmpIssuesMap= new HashMap<>();//pro empId issueId
    /**
     * 当第一分组为project，且没有选择机构或部门时，调用此方法
     * @param projectKey 选择的项目，为  项目一,项目二,项目三 的结构
     * @param argFiled  assignee 或者 reporter   默认为 reporter
     * @return
     */
    @Override
    public ReportInfoBean getNewTypeAll(String projectKey,String argFiled) {
        Set<String> projectName = new HashSet<>();
        List<Project> projects = projectManager.getProjects();
        if (StringUtils.isBlank(projectKey)){
            Set<String> finalProjectName = projectName;
            projects.stream().forEach(project -> finalProjectName.add(project.getName()));
            projectName=finalProjectName;
        }else {
            projectName = getProjectName(projectKey);
        }
        ReportInfoBean bean = new ReportInfoBean();
        newResult.clear();
        treeIdList.clear();
        idAndTreeId.clear();
        treeIdList.add(0);
        Map<String,Integer> projectNameNum = new HashMap<>();
        List<String> collect = projectName.stream().collect(Collectors.toList());
        Map<Integer, Integer> map = new HashMap<>();
        for (int i =1;i<collect.size()+1;i++){
            projectNameNum.put(collect.get(i-1),i);
            map.put(i, 0);
        }
        if (idAndTreeId.containsKey("pro")) {
            idAndTreeId.get("pro").putAll(map);
        } else {
            idAndTreeId.put("pro", map);
        }
        Set<String> finalProjectName1 = projectName;
        projects.stream()
                .filter(project -> {
                    if (finalProjectName1.contains(project.getName())) {
                        return true;
                    } else {
                        return false;
                    }
                })
                .forEach(project -> {
                    proEmpIssuesMap.clear();
                    idAndTreeId.clear();
                    Integer integer = treeIdList.stream().max(Integer::compareTo).get() + 2;
                    int treeRootId = integer;
                    treeIdList.add(treeRootId);
                    HashMap<String, String> temp_map = new HashMap<>();
                    temp_map.put("id", "" + treeRootId);
                    temp_map.put("name", project.getName());
                    temp_map.put("iconCls", "icon-project");
                    newResult.add(temp_map);//添加项目对应的map
                    String jqlQuery = "project = '" + project.getName() + "'";
                    if(!"".equals(GlobalReport.startTime) && !(GlobalReport.startTime == null)){
                        jqlQuery = jqlQuery + " AND createdDate >= '" + changeTime(GlobalReport.startTime) + "'";
                    }
                    if(!"".equals(GlobalReport.endTime) && !(GlobalReport.endTime == null)){
                        jqlQuery = jqlQuery + " AND createdDate <= '" + changeTime(GlobalReport.endTime) + "'";
                    }
                    List<Issue> issueByJql = getIssueByJql(jqlQuery);
                    Map<Integer, Integer> map1 = new HashMap<>();
                    map1.put(projectNameNum.get(project.getName()), treeRootId);
                    if (idAndTreeId.containsKey("pro")) {
                        idAndTreeId.get("pro").putAll(map1);
                    } else {
                        idAndTreeId.put("pro", map1);
                    }
                    issueByJql.stream().forEach(issue -> {
                        StrEmployeeBean employeeByJiraKey = getEmployeeByJiraKey(issue,argFiled);
                        if (employeeByJiraKey != null){
                            int subordinateOrgTreeId = buildSuperiorRelation(idAndTreeId.get("pro").get(projectNameNum.get(project.getName())), employeeByJiraKey, "org");
                            buildDepEmpRelation(subordinateOrgTreeId, employeeByJiraKey, issue, "dep");
                        }
                    });
                    formatProInfo(proEmpIssuesMap,idAndTreeId);
                });
        formatResult(newResult);
        bean.setLists(newResult);
        return bean;
    }

    /**
     * 构建项目 ---组织 关系  返回最下级 组织 树id(关系id 非 实际)
     * @param treeRootId      根ID
     * @param strEmployeeBean
     * @param type  类型
     * @return 最下级组织 树 ID
     */
    private Integer buildSuperiorRelation(int treeRootId, StrEmployeeBean strEmployeeBean, String type) {
        Integer orgId = strEmployeeBean.getOrgId();
        if (orgId != null || orgId != 0) {
            if (idAndTreeId.get(type) != null && idAndTreeId.get(type).containsKey(orgId)) {
                //idAndTreeId.get(type).put(orgId,treeRootId);
                treeIdList.add(idAndTreeId.get(type).get(orgId));
                int i = buildOrgR(orgId, treeRootId, type);
                treeIdList.add(i);
                return idAndTreeId.get(type).get(orgId);
            } else {
                int i = buildOrgR(orgId, treeRootId, type);
                treeIdList.add(i);
                return i;
            }
        } else {//兼容 老版本
            String groupId = strEmployeeBean.getGroupId();
            int orgID = 0;
            if (StringUtils.isNotBlank(groupId)) {
                DepartmentEntity departmentEntity = ao.get(DepartmentEntity.class, Integer.valueOf(groupId));
                boolean flag = true;
                while (flag) {
                    String parent = departmentEntity.getParent();
                    if (StringUtils.isNotBlank(parent)) {
                        continue;
                    } else {
                        int id = departmentEntity.getID();
                        orgID = ao.find(StrOrganizeGroup.class, String.format("GROUP_ID", id))[0].getOrg().getID();
                        flag = false;
                    }
                }
            }
            int i = 0;
            if (orgID != 0) {
                if (idAndTreeId.get(type).containsKey(orgID)) {
                    i = idAndTreeId.get(type).get(orgId);
                } else {
                    i = buildOrgR(orgID, treeRootId, type);
                }
            }
            treeIdList.add(i);
            return i;
        }
    }

    /**
     *  建立 组织间关系
     * @param orgId
     * @param treeRootId
     * @param type
     * @return
     */
    private int buildOrgR(int orgId, int treeRootId, String type) {
        List<Integer> orgList = new ArrayList<>();
        List<Integer> orgIDList = new ArrayList<>();
        List<String> orgNameList = new ArrayList<>();
        orgList.add(orgId);
        StrOrganize strOrganize = ao.get(StrOrganize.class, orgId);
        boolean falg = true;
        int parentId = 0;
        parentId = strOrganize.getParent();
        orgNameList.add(strOrganize.getName());
        orgIDList.add(strOrganize.getID());
        while (falg) {
            if (parentId != 0) {
                orgList.add(parentId);
                StrOrganize strOrganize1 = ao.get(StrOrganize.class, parentId);
                orgNameList.add(strOrganize1.getName());
                orgIDList.add(strOrganize1.getID());
                parentId = strOrganize1.getParent();
            } else {
                falg = false;
            }
        }
        long count = orgIDList.stream().filter(integer -> idAndTreeId.get(type) != null && idAndTreeId.get(type).containsKey(integer)).count();
        int i = 0;
        if (count == 0) {
            i = addNewResult(orgIDList, orgList, orgNameList, treeRootId, type);
        } else {
            int an = 0;
            int depTreeId = 0;
            for (int n = 0; n < orgIDList.size(); n++) {
                if (idAndTreeId.get(type) != null && idAndTreeId.get(type).containsKey(orgIDList.get(n))) {
                    an = n;
                    depTreeId = idAndTreeId.get(type).get(orgIDList.get(n));
                    break;
                }
            }
            if (orgIDList.subList(0, an).size() == 0 && orgIDList.size() == 0) {
                i = depTreeId;
            } else {
                i = addNewResult(orgIDList.subList(0, an+1), orgList.subList(0, an+1), orgNameList.subList(0, an+1), depTreeId, type);
            }
        }
        return i;
    }

    /**
     *  产生新的结果
     * @param realIdList
     * @param list
     * @param depNameList
     * @param subordinateOrgTreeId
     * @param type
     * @return
     */
    private int addNewResult(List<Integer> realIdList, List<Integer> list, List<String> depNameList, int subordinateOrgTreeId, String type) {
        int returnTreeId = 0;
        for (int a = list.size(); a > 0; a--) {
            HashMap<String, String> temp_map = new HashMap<>();
            temp_map.put("_parentId", "" + subordinateOrgTreeId);
            treeIdList.add(subordinateOrgTreeId);
            Integer integer = treeIdList.stream().max(Integer::compareTo).get() + 1;
            subordinateOrgTreeId = integer;
            temp_map.put("id", "" + subordinateOrgTreeId);
            Map<Integer, Integer> map = new HashMap<>();
            map.put(realIdList.get(a - 1), subordinateOrgTreeId);
            temp_map.put("name", depNameList.get(a - 1));
            if(type.equals("org")){
                temp_map.put("iconCls", "icon-organization");
            }else {
                temp_map.put("iconCls", "icon-department");
            }
            if (idAndTreeId.containsKey(type)) {
                if (!idAndTreeId.get(type).keySet().contains(realIdList.get(a-1))){
                    newResult.add(temp_map);
                    idAndTreeId.get(type).putAll(map);
                }else {
                    subordinateOrgTreeId= idAndTreeId.get(type).get(realIdList.get(a-1));
                }
            } else {
                idAndTreeId.put(type, map);
                newResult.add(temp_map);
            }

            //idAndTreeId.put(type,map);
            if (a == 1) {
                returnTreeId = subordinateOrgTreeId;
                break;
            }
        }
        treeIdList.add(returnTreeId);
        return returnTreeId;
    }

    /**
     * 构建 组织 ----(部门)----雇员 ----- issue id (树ID) 关系
     *
     * @param subordinateOrgTreeId
     * @param strEmployeeBean
     * @param issue
     * @param type
     */
    private void buildDepEmpRelation(int subordinateOrgTreeId, StrEmployeeBean strEmployeeBean, Issue issue, String type) {
        int id = strEmployeeBean.getId();
        if (StringUtils.isNotBlank(strEmployeeBean.getGroupId())) {//org --dep -- emp --issue
            DepartmentEntity departmentEntity = ao.get(DepartmentEntity.class, Integer.valueOf(strEmployeeBean.getGroupId()));
            String parent = departmentEntity.getParent();
            List<Integer> depList = new ArrayList<>();
            List<Integer> depRealList = new ArrayList<>();
            depList.add(departmentEntity.getID());
            List<String> depNameList = new ArrayList<>();
            depNameList.add(departmentEntity.getGroupName());
            depRealList.add(departmentEntity.getID());
            boolean flag = true;
            while (flag) {
                if (StringUtils.isNotBlank(parent) && !"null".equals(parent)) {
                    depList.add(Integer.valueOf(parent));
                    DepartmentEntity departmentEntity1 = ao.get(DepartmentEntity.class, Integer.valueOf(parent));
                    depNameList.add(departmentEntity1.getGroupName());
                    depRealList.add(departmentEntity1.getID());
                    parent = String.valueOf(departmentEntity1.getParent());
                } else {
                    flag = false;
                }
            }
            long count = 0;
            if (idAndTreeId.containsKey(type)) {
                count = depRealList.stream().filter(integer -> idAndTreeId.get(type).containsKey(integer)).count();
            } else {
                count = 0;
            }
            int i = 0;
            if (count == 0) {
                i = addNewResult(depRealList, depList, depNameList, subordinateOrgTreeId, type);
            } else {
                int an = 0;
                int depTreeId = 0;
                for (int n = 0; n < depRealList.size(); n++) {
                    if (idAndTreeId.get(type).containsKey(depRealList.get(n))) {
                        an = n;
                        //depTreeId = idAndTreeId.get(type).get(depRealList.get(n));
                        depTreeId = treeIdList.stream().max(Integer::compareTo).get();
                        break;
                    }
                }
                if (depRealList.size() == 0) {
                    i = depTreeId;
                } else {
                    i = addNewResult(depRealList.subList(0, an+1), depList.subList(0, an+1), depNameList.subList(0, an+1), depTreeId, type);
                }
            }
            treeIdList.add(i);
            int empTreeId = buildDepEmpRaltion(i, strEmployeeBean);
            treeIdList.add(empTreeId);

            Integer integer = buildEmpIssueRelation(empTreeId, id, issue);
            treeIdList.add(integer);
        } else {//org --emp---issue
            HashMap<String, String> temp_map = new HashMap<>();
            temp_map.put("_parentId", "" + subordinateOrgTreeId);
            subordinateOrgTreeId = treeIdList.stream().max(Integer::compareTo).get()+1;
            temp_map.put("id", "" + subordinateOrgTreeId);
            temp_map.put("name", strEmployeeBean.getEmployeeName());
            temp_map.put("iconCls", "icon-employee");
            if (idAndTreeId.containsKey("emp")) {
                if (idAndTreeId.get("emp").containsKey(id)) {
                    subordinateOrgTreeId = idAndTreeId.get("emp").get(id);
                } else {
                    Map<Integer, Integer> map = new HashMap<>();
                    map.put(id, subordinateOrgTreeId);
                    idAndTreeId.get("emp").putAll(map);
                    newResult.add(temp_map);
                }
            } else {
                Map<Integer, Integer> map = new HashMap<>();
                map.put(id, subordinateOrgTreeId);
                idAndTreeId.put("emp", map);
                newResult.add(temp_map);
            }
            treeIdList.add(subordinateOrgTreeId);
            HashMap<String, String> temp_map1 = new HashMap<>();
            temp_map1.put("_parentId", "" + subordinateOrgTreeId);
            Integer empTreeId1 = treeIdList.stream().max(Integer::compareTo).get();
            temp_map1.put("id", "" + (empTreeId1 + 1));
            temp_map1.put("name", issue.getSummary());
            temp_map1.put("Summary", issue.getSummary());
            HashMap<String, String> map = changMap(temp_map1, issue,GlobalReport.jira_user_key);
            if (proEmpIssuesMap.keySet().contains(issue.getProjectObject().getName())){
                Map<Integer, ArrayList<HashMap<String, String>>> integerListMap = proEmpIssuesMap.get(issue.getProjectObject().getName());//collection user
                if (integerListMap.keySet().contains(id)){
                    integerListMap.get(id).add(map);
                }else {
                    ArrayList<HashMap<String, String>> mapList= new ArrayList<>();
                    mapList.add(map);
                    integerListMap.put(id,mapList);
                }
            }else {
                Map<Integer,ArrayList<HashMap<String, String>>> userIssueList =new HashMap<>();
                ArrayList<HashMap<String, String>> mapList= new ArrayList<>();
                mapList.add(map);
                userIssueList.put(id,mapList);
                proEmpIssuesMap.put(issue.getProjectObject().getName(),userIssueList);
            }
            newResult.add(map);
            treeIdList.add(empTreeId1 + 1);
        }
    }

    /**
     *  建立部门 雇员关系 （内）
     * @param depTreeId
     * @param strEmployeeBean
     * @return
     */
    private int buildDepEmpRaltion(int depTreeId, StrEmployeeBean strEmployeeBean) {
        HashMap<String, String> temp_map = new HashMap<>();
        temp_map.put("_parentId", "" + depTreeId);
        depTreeId = treeIdList.stream().max(Integer::compareTo).get() + 1;
        temp_map.put("id", "" + depTreeId);
        temp_map.put("name", strEmployeeBean.getEmployeeName());
        temp_map.put("iconCls", "icon-employee");
        if (idAndTreeId.containsKey("emp")) {
            if (idAndTreeId.get("emp").containsKey(strEmployeeBean.getId())) {
                depTreeId = idAndTreeId.get("emp").get(strEmployeeBean.getId());
                return depTreeId;
            } else {
                Map<Integer, Integer> map = new HashMap<>();
                map.put(strEmployeeBean.getId(), depTreeId);
                idAndTreeId.get("emp").putAll(map);
                newResult.add(temp_map);
                return depTreeId;
            }
        } else {
            Map<Integer, Integer> map = new HashMap<>();
            map.put(strEmployeeBean.getId(), depTreeId);
            idAndTreeId.put("emp", map);
            newResult.add(temp_map);
            return depTreeId;
        }
    }

    /**
     *  构建 雇员 issue 关系
     * @param empTreeId
     * @param issue
     * @return
     */
    private Integer buildEmpIssueRelation(int empTreeId,int employeeId, Issue issue) {
        // Map<String,Map<Integer,List<Map<String,String>>>> proEmpIssuesMap= new HashMap<>();//pro empId issueId
        HashMap<String, String> temp_map = new HashMap<>();
        temp_map.put("_parentId", "" + empTreeId);
        Integer empTreeId1 = treeIdList.stream().max(Integer::compareTo).get() + 1;
        temp_map.put("id", "" + empTreeId1);
        temp_map.put("name", issue.getSummary());
        temp_map.put("issuekey",issue.getKey());
        HashMap<String, String> map = changMap(temp_map, issue,GlobalReport.jira_user_key);
        newResult.add(map);
        if (proEmpIssuesMap.keySet().contains(issue.getProjectObject().getName())){
            Map<Integer, ArrayList<HashMap<String, String>>> integerListMap = proEmpIssuesMap.get(issue.getProjectObject().getName());//collection user
            if (integerListMap.keySet().contains(employeeId)){
                integerListMap.get(employeeId).add(map);
            }else {
                ArrayList<HashMap<String, String>> mapList= new ArrayList<>();
                mapList.add(map);
                integerListMap.put(employeeId,mapList);
            }
        }else {
            Map<Integer,ArrayList<HashMap<String, String>>> userIssueList =new HashMap<>();
            ArrayList<HashMap<String, String>> mapList= new ArrayList<>();
            mapList.add(map);
            userIssueList.put(employeeId,mapList);
            proEmpIssuesMap.put(issue.getProjectObject().getName(),userIssueList);
        }
        return empTreeId1;
    }

    /**
     *  获取 issue 集合
     * @param jqlQuery
     * @return
     */
    public List<Issue> getIssueByJql(String jqlQuery) {//执行jqlQuery查询语句获取issue集合
        List<Issue> issues = new ArrayList<>();
        final SearchService.ParseResult parseResult = searchService.parseQuery(userManager.getUserByKey(GlobalReport.jira_user_key), jqlQuery);
        if (parseResult.isValid()) {
            try {
                SearchResults results = searchService.search(userManager.getUserByKey(GlobalReport.jira_user_key), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                issues = results.getIssues();
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        return issues;
    }

    /***
     *  获取当前 issue 的 StrEmployeeBean
     * @param issue
     * @return
     */
    public StrEmployeeBean getEmployeeByJiraKey(Issue issue,String argFiled) {//根据issue获取其对应的雇员信息
        StrEmployeeBean bean = new StrEmployeeBean();
        //StrEmployee byJiraUserKey = strEmployeeService.getByJiraUserKey(issue.getReporter().getKey());
        String jira_user_key ="";
        if (argFiled.equals("assignee")){
            ApplicationUser assignee = issue.getAssignee();
            if (assignee == null){
                return null;
            }
            jira_user_key= assignee.getKey();
        }else {
            ApplicationUser reporter = issue.getReporter();
            if (reporter == null){
                return null;
            }
            jira_user_key= reporter.getKey();
        }
        // StrEmployee[] employees = ao.find(StrEmployee.class, MessageFormat.format(" JIRA_USER_KEY = {0}", jira_user_key));
        StrEmployee byJiraUserKey1 = strEmployeeService.getByJiraUserKey(jira_user_key);
       /* if(employees.length != 0){
            bean = new StrEmployeeBean(employees[0]);
            return bean;
        }*/
        StrEmployeeBean strEmployeeBean = new StrEmployeeBean(byJiraUserKey1);
        return byJiraUserKey1 == null ? null : strEmployeeBean;
        //  return null;
    }

    /**
     * 把传入的项目参数变为项目名称集合
     * @param proStr 项目一,项目二,项目三
     * @return
     */
    private Set<String>  getProjectName(String proStr){
        Set<String> stringSet =new HashSet<>();
        if("".equals(proStr)){
            stringSet.add(proStr);
            return stringSet;
        } else {
            if(proStr.contains(",")){
                String[] split = proStr.split(",");
                Arrays.stream(split).forEach(s -> stringSet.add(s));
                return stringSet;
            } else {
                stringSet.add(proStr);
                return stringSet;
            }
        }
    }

    /**
     *  把 issue 信息 补充到 user dep  org pro
     * @param proEmpIssuesMap
     * @param idAndTreeId
     */
    private void formatProInfo(Map<String,Map<Integer,ArrayList<HashMap<String,String>>>> proEmpIssuesMap,Map<String,Map<Integer,Integer>> idAndTreeId){
        Set<String> stringSet = proEmpIssuesMap.keySet();
        stringSet.stream().forEach(s -> {
            /*-------*/
            Map<Integer,HashMap<String,String>> userMap= new HashMap<>();
            Set<Integer> currentProUserIdSet = proEmpIssuesMap.get(s).keySet();
            currentProUserIdSet.stream().forEach(integer -> {
                ArrayList<HashMap<String, String>> mapList = proEmpIssuesMap.get(s).get(integer);
                HashMap<String, String> map = sumMap(ao.get(StrEmployee.class, integer).getEmployeeName(), mapList);
                userMap.put(integer,map);
                Integer empTreeId = idAndTreeId.get("emp").get(integer);
                newResult.stream().filter(stringStringHashMap ->{
                    if (Integer.valueOf(stringStringHashMap.get("id")) == empTreeId){
                        return true;
                    }else {
                        return false;
                    }
                } ).forEach(stringStringHashMap -> stringStringHashMap.putAll(map));
            });
            /*-------*/
            Map<Integer,HashMap<String,String>> depMap= new HashMap<>();
            if(idAndTreeId.keySet().contains("dep")){
                Map<Integer, Integer> currentProDep = idAndTreeId.get("dep");//depId,depTreeId
                Set<Integer> depIdSet = currentProDep.keySet();
                depIdSet.stream().forEach(integer1 -> {
                    List<Integer> oneDepUserList= new ArrayList<>();
                    getDeplist(integer1).stream().forEach(integer2 -> {
                        List<Integer> userlist = getUserlist(integer2);
                        oneDepUserList.addAll(userlist);
                    });
                    Set<Integer> userSet = oneDepUserList.stream().collect(Collectors.toSet());
                    Set<Integer> toAddInfo= new HashSet<>();
                    Set<Integer> currentProUserIdS =currentProUserIdSet;
                    Set<Integer> collect = currentProUserIdS.stream().collect(Collectors.toSet());
                    toAddInfo.addAll(userSet);
                    toAddInfo.addAll(collect);
                    toAddInfo.removeAll(collect);
                    userSet.removeAll(toAddInfo);
                    ArrayList<HashMap<String, String>> map = new ArrayList<>();
                    userSet.stream().forEach(integer -> {
                        if (userMap.keySet().contains(integer)){
                            map.addAll(proEmpIssuesMap.get(s).get(integer));
                        }
                    });
                    HashMap<String, String> map1 = sumMap(ao.get(DepartmentEntity.class, integer1).getGroupName(), map);
                    depMap.put(integer1,map1);
                    newResult.stream().filter(stringStringHashMap -> {
                        if (Integer.valueOf(stringStringHashMap.get("id")) == currentProDep.get(integer1)){
                            return true;
                        }else {
                            return false;
                        }
                    }).forEach(stringStringHashMap -> {
                        stringStringHashMap.putAll(map1);
                    });
                });}
            /*-------*/
            Map<Integer, Integer> currentProOrg = idAndTreeId.get("org");

            Set<Integer> orgIdSet = currentProOrg.keySet();
            orgIdSet.stream().forEach(integer -> {
                Set<Integer> currentProUserIdSets = currentProUserIdSet;
                Set<Integer> collect = currentProUserIdSets.stream().collect(Collectors.toSet());
                Set<Integer> oneOrgDepList= new HashSet<>();
                Set<Integer> list = new HashSet<>();
                Set<Integer> subUserListByOneOrgAllSet = getSubUserListByOneOrg(integer, list);
                oneOrgDepList.addAll(collect);
                oneOrgDepList.addAll(subUserListByOneOrgAllSet);
                oneOrgDepList.removeAll(subUserListByOneOrgAllSet);
                collect.removeAll(oneOrgDepList);
                ArrayList<HashMap<String, String>> map = new ArrayList<>();
                collect.stream().forEach(integer1 -> {
                    if (userMap.keySet().contains(integer1)){
                        map.addAll(proEmpIssuesMap.get(s).get(integer1));
                    }
                });
                HashMap<String, String> map1 = sumMap(ao.get(StrOrganize.class, integer).getName(), map);
                newResult.stream().filter(stringStringHashMap -> {
                    if (Integer.valueOf(stringStringHashMap.get("id")) == currentProOrg.get(integer)){
                        return true;
                    }else {
                        return false;
                    }
                }).forEach(stringStringHashMap -> {
                    stringStringHashMap.putAll(map1);
                });
            });
            /*-----*/
            Map<Integer, Integer> map = idAndTreeId.get("pro");
            ArrayList<HashMap<String, String>> map2 = new ArrayList<>();
            final int[] num = {0};
            map.keySet().stream().forEach(integer -> {
                Set<Integer> currentProUserIdSets = currentProUserIdSet;
                Set<Integer> collect = currentProUserIdSets.stream().collect(Collectors.toSet());
                collect.stream().forEach(integer1 -> {
                    map2.addAll(proEmpIssuesMap.get(s).get(integer1));
                    num[0] =integer;
                });
            });
            HashMap<String, String> map1 = sumMap(s, map2);
            newResult.stream().filter(stringStringHashMap -> {
                if (Integer.valueOf(stringStringHashMap.get("id")) == map.get(num[0])){
                    return true;
                }else {
                    return false;
                }
            }).forEach(stringStringHashMap -> {
                stringStringHashMap.putAll(map1);
            });
        });

    }

    /**
     *  返回所有部门ID
     * @param depId
     * @return
     */
    private  List<Integer> getDeplist(int depId){
        List<Integer>  list = new ArrayList<>();
        int depno  = depId;
        list.add(depno);
        boolean flage = true;
        Set<Integer> listToCycl = new HashSet<>();
        listToCycl.add(depno);
        while (flage) {
            Iterator<Integer> iterator = listToCycl.iterator();
            if (iterator.hasNext()) {
                depno = iterator.next();
            }
            DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", depno, depno));
            listToCycl.remove(depno);
            if (depts.length != 0) {
                Arrays.stream(depts).forEach(departmentEntity1 -> listToCycl.add(departmentEntity1.getID()));
                Arrays.stream(depts).forEach(departmentEntity1 -> list.add(departmentEntity1.getID()));
            }
            if (listToCycl.size() == 0) {
                flage = false;
                break;
            }
        }
        return list;
    }

    /**
     *  返回 部门下雇员id 集合
     * @param id
     * @return
     */
    private List<Integer> getUserlist(int id){
        List<Integer> list = new ArrayList<>();
        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, String.format("GROUP_ID = '%s' ", id));
        if (struGroupOfEmployees.length != 0){
            Arrays.stream(struGroupOfEmployees).forEach(struGroupOfEmployee -> list.add(struGroupOfEmployee.getEmployee().getID()));
        }
        return list;
    }

    private Set<Integer> getSubUserListByOneOrg(int orgId,Set<Integer> list){
        StrOrganize[] strOrganizes = ao.find(StrOrganize.class, String.format(" PARENT = '%s' and ID <> %d", orgId, orgId));
        for (StrOrganize organize : strOrganizes) {
            if (organize.getParent() != null) {
                getSubUserListByOneOrg(organize.getID(), list);
            }
        }
        StrEmployee[] strEmployees = ao.find(StrEmployee.class, String.format("STR_ORGANIZE_ID  = '%d' ", orgId));
        if (strEmployees.length != 0){
            Arrays.stream(strEmployees).forEach(strEmployee -> {
                list.add(strEmployee.getID());
            });
        }
        return list;
    }
/*------------------------------------------------王清林----------------------------------------------------------------------*/

}
