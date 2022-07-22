package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.work.plugin.rest.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin on 2021/7/5.
 */
@RequiredArgsConstructor
public class BmryfbStateServiceImpl implements BmryfbStateService {
    private static final Logger log = LoggerFactory.getLogger(BmryfbStateServiceImpl.class);
    private final SearchService searchService = ComponentAccessor.getComponentOfType(SearchService.class);
    private final ActiveObjects ao;
    private final IssueManager issueManager;
    private final ProjectManager projectManager;
    private final UserManager userManager;
    private final GsryldStateServiceImpl service;
    private final JiraAuthenticationContext jiraAuthenticationContext;
//    private final SearchProvider provider = ComponentAccessor.getComponentOfType(SearchProvider.class);

    public List<StrOrganize> orgs = new ArrayList<StrOrganize>();
    public List<String> deptKeys = new ArrayList<>();
    public List<String> orgKeys = new ArrayList<>();

    /**
     * 获取所有人员发布数据集合，返回到前台
     * @return
     */
    public List<ReportJoinChildrenBean> getAllOfBm(String sign){
        orgs = Arrays.asList(ao.find(StrOrganize.class));//获取所有机构集合
        List<ReportJoinChildrenBean> reportJoinChildrenBean = new ArrayList<ReportJoinChildrenBean>();
        for(StrOrganize org : orgs){
            if(org.getParent() == null || org.getParent() == 0 || org.getID()==org.getParent()){
                reportJoinChildrenBean.add(buildChildrenOrg(org.getID(),sign));
            }
        }
        return reportJoinChildrenBean;
    }

    /**
     * 层级递归机构及其所有子机构,获取此机构下的数据集
     * @param orgNo
     */
    public ReportJoinChildrenBean buildChildrenOrg(int orgNo,String sign){
        ReportJoinChildrenBean orgChild = new ReportJoinChildrenBean();
        orgChild.setName(service.getOrgName(orgNo));
        orgChild.setBmryfb(getBmryfbByOrgNo(orgNo,sign));
        orgChild.setType(999);
        orgChild.setPid(orgNo);

        StrOrganize[] strOrganize = ao.find(StrOrganize.class, String.format("PARENT = '%s'  and ID <> %d", orgNo, orgNo));//此机构下的所有机构
        //判断机构下是否有子机构
        if(strOrganize.length != 0){
            List<ReportJoinChildrenBean> temps = new ArrayList<ReportJoinChildrenBean>();
            for(StrOrganize org : strOrganize){
                temps.add(buildChildrenOrg(org.getID(),sign));
            }
            orgChild.setChildrens(temps);
        }

        StrOrganizeGroup[] organizeGroups = ao.find(StrOrganizeGroup.class, MessageFormat.format("ORG_ID = {0}", orgNo));//机构与部门的对应关系表对象
        //判断机构下是否有直属部门
        if(organizeGroups.length != 0){
            DepartmentEntity[] depts;
            if("bmryfb".equals(sign)){
                depts = Lists.newArrayList(organizeGroups).stream().map(e -> e.getGroup()).filter(s -> s.getType() == 0).toArray(DepartmentEntity[]::new);
            } else{
                depts = Lists.newArrayList(organizeGroups).stream().map(e -> e.getGroup()).filter(s -> s.getType() == 1).toArray(DepartmentEntity[]::new);
            }
            List<ReportJoinChildrenBean> lists = new ArrayList<ReportJoinChildrenBean>();
            for(DepartmentEntity dept : depts){
                lists.add(buildChildrenDept(dept.getID()));
            }
            if(orgChild.getChildrens() != null){
                lists.addAll(orgChild.getChildrens());
            }
            orgChild.setChildrens(lists);
        }

        //判断此机构有没有直属雇员，即在此机构下找到的雇员没有在部门下
        StrEmployee[] employees = ao.find(StrEmployee.class,MessageFormat.format("STR_ORGANIZE_ID = {0}", orgNo));
//        if("bmryfb".equals(sign)){
            if(employees.length != 0){
                List<ReportJoinChildrenBean> emplists = new ArrayList<>();
                for(StrEmployee employee : employees){
                    StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("EMPLOYEE_ID = {0}", employee.getID()));
                    if(groupemps.length == 0){
                        emplists.add(getEmployeeBmryfb(employee.getID()));
                    }
                }
                if(orgChild.getChildrens() != null){
                    emplists.addAll(orgChild.getChildrens());
                }
                orgChild.setChildrens(emplists);
            }
//        }


        //如果此机构下没有子机构,没有子部门,没有直属雇员
        if(strOrganize.length == 0 && organizeGroups.length == 0 && employees.length == 0){
            orgChild.setChildrens(new ArrayList<>());
        }
        return orgChild;
    }


    /**
     * 层级递归部门及其所有子部门,获取此部门下的数据集
     * @param deptNo
     */
    public ReportJoinChildrenBean buildChildrenDept(int deptNo){
        ReportJoinChildrenBean deptChild = new ReportJoinChildrenBean();
        deptChild.setName(service.getDeptName(deptNo));
        deptChild.setBmryfb(getBmryfbByDeptNo(deptNo));
        deptChild.setType(888);
        deptChild.setPid(deptNo);

        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", deptNo, deptNo));//获取此部门下的部门
        //判断部门下是否有部门
        if(depts.length != 0){
            List<ReportJoinChildrenBean> temps = new ArrayList<ReportJoinChildrenBean>();
            for(DepartmentEntity dept : depts){
                temps.add(buildChildrenDept(dept.getID()));
            }
            deptChild.setChildrens(temps);
        }

        StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0}", deptNo));
        //判断此部门下是否有雇员
        if(groupemps.length != 0){
            StrEmployee[] employees = Lists.newArrayList(groupemps).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new);//获取部门下的雇员
            List<ReportJoinChildrenBean> lists = new ArrayList<ReportJoinChildrenBean>();
            for(StrEmployee emp : employees){
                lists.add(getEmployeeBmryfb(emp.getID()));
            }
            if(deptChild.getChildrens() != null){
                lists.addAll(deptChild.getChildrens());
            }
            deptChild.setChildrens(lists);
        }
        //如果此部门下没有子部门也没有雇员
        if(depts.length == 0 && groupemps.length == 0){
            deptChild.setChildrens(new ArrayList<ReportJoinChildrenBean>());
        }
        return deptChild;
    }

    /**
     * 根据雇员id获取此雇员对应的的数据集
     * @param empNo
     * @return
     */
    public ReportJoinChildrenBean getEmployeeBmryfb(int empNo){
        ReportJoinChildrenBean empChild = new ReportJoinChildrenBean();
        StrEmployee employee = ao.get(StrEmployee.class,empNo);
        empChild.setName(employee.getEmployeeName());
        empChild.setBmryfb(getBmryfbByEmpNo(empNo));
        empChild.setChildrens(new ArrayList<ReportJoinChildrenBean>());
        empChild.setType(777);
        empChild.setPid(empNo);
        return empChild;
    }

    /**
     * 根据雇员id找到其对应的参与的issue的相关数据
     * @param empNo 雇员id（主键）
     * @return
     */
    public ReportJoinBean getBmryfbByEmpNo(int empNo){
        List<Long> proLists = new ArrayList<Long>();//存储项目id的集合
        int issNum = 0;//分配次数
        int repNum = 0;//报告次数
        int repNum1 = 0;//已报告未分配的issue数
        StrEmployee emp = ao.get(StrEmployee.class, empNo);
        String userKey = emp.getJiraUserKey();
        if(userKey == null || "".equals(userKey) || " ".equals(userKey)){
            return new ReportJoinBean(proLists.size(),issNum,repNum,repNum1);
        }
        String jqlQuery = "reporter = " + userKey + " or assignee = " + userKey + "";
        final SearchService.ParseResult parseResult = searchService.parseQuery(userManager.getUserByKey(userKey), jqlQuery);
        if (parseResult.isValid()){
            try {
                SearchResults results = searchService.search(userManager.getUserByKey(userKey), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                List<Issue> issues = results.getIssues();
                for(Issue issue : issues){
                    if(!proLists.contains(issue.getProjectId())){
                        proLists.add(issue.getProjectId());
                    }
                    if(userKey.equals(issue.getAssigneeId())){
                        issNum++;
                    }
                    if(userKey.equals(issue.getReporterId())){
                        repNum++;
                        if(issue.getAssigneeId() == null || "".equals(issue.getAssigneeId())){
                            repNum1++;
                        }
                    }
                }
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        return new ReportJoinBean(proLists.size(),issNum,repNum,repNum1);
    }


    /**
     * 根据机构id找到其对应的机构人员发布相关数据
     * @param orgNo 机构编号
     * @return
     */
    public ReportJoinBean getBmryfbByOrgNo(int orgNo,String sign){
        if(orgKeys != null || orgKeys.size() != 0){
            orgKeys.clear();
        }
        buildBmryfbOrg(orgNo,sign);//调用机构回调函数
        return CountBmryfb(orgKeys);
    }

    /**
     * 层级递归机构下的所有子机构
     * @param orgNo
     */
    public void buildBmryfbOrg(int orgNo,String sign){
        StrOrganize[] strOrganize = ao.find(StrOrganize.class, String.format("PARENT = '%s'  and ID <> %d", orgNo, orgNo));//此机构下的所有机构
        //判断机构下是否有子机构
        if(strOrganize.length != 0){
            for(StrOrganize org : strOrganize){
                buildBmryfbOrg(org.getID(),sign);
            }
        }

        StrOrganizeGroup[] organizeGroups = ao.find(StrOrganizeGroup.class, MessageFormat.format("ORG_ID = {0}", orgNo));//机构与部门的对应关系表对象
        //判断机构下是否有直属部门
        if(organizeGroups.length != 0){
            DepartmentEntity[] depts;
            if("bmryfb".equals(sign)){
                depts = Lists.newArrayList(organizeGroups).stream().map(e -> e.getGroup()).filter(s -> s.getType() == 0).toArray(DepartmentEntity[]::new);
            } else{
                depts = Lists.newArrayList(organizeGroups).stream().map(e -> e.getGroup()).filter(s -> s.getType() == 1).toArray(DepartmentEntity[]::new);
            }
            for(DepartmentEntity dept : depts){
                getBmryfbByDeptNo(dept.getID());
                orgKeys.addAll(deptKeys);
            }
        }

        //判断此机构有没有直属雇员，即在此机构下找到的雇员没有在部门下
        StrEmployee[] employees = ao.find(StrEmployee.class,MessageFormat.format("STR_ORGANIZE_ID = {0}", orgNo));
        if(employees.length != 0){
            List<ReportJoinChildrenBean> emplists = new ArrayList<>();
            for(StrEmployee employee : employees){
                StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("EMPLOYEE_ID = {0}", employee.getID()));
                //为0表示此雇员为机构直属雇员
                if(groupemps.length == 0){
                    orgKeys.add(employee.getJiraUserKey());
                }
            }

        }


    }



    /**
     * 根据部门id找到其对应的部门人员发布相关数据
     * @param deptNo 部门编号
     * @return
     */
    public ReportJoinBean getBmryfbByDeptNo(int deptNo){
        if(deptKeys != null || deptKeys.size() != 0){
            deptKeys.clear();
        }
        buildBmryfbDept(deptNo);//调用部门回调函数
        return CountBmryfb(deptKeys);
    }

    /**
     * 层级递归部门下的所有子部门
     * @param deptNo
     */
    public void buildBmryfbDept(int deptNo){
        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", deptNo, deptNo));//获取此部门下的部门
        //判断部门下是否有部门
        if(depts.length != 0){
            for(DepartmentEntity dept : depts){
                buildBmryfbDept(dept.getID());
            }
        }

        StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0}", deptNo));
        //判断此部门下是否有雇员
        if(groupemps.length != 0){
            StrEmployee[] employees = Lists.newArrayList(groupemps).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new);//获取部门下的雇员
            for(StrEmployee emp : employees){
                if(!"".equals(emp.getJiraUserKey()) && !" ".equals(emp.getJiraUserKey()) && emp.getJiraUserKey() != null){
                    deptKeys.add(emp.getJiraUserKey());
                }
            }
        }
    }

    /**
     * 根据传入的雇员key值集合获取对应的信息集合
     * @param deptKeys 雇员key值集合
     * @return 部门或机构的信息集合
     */
    public ReportJoinBean CountBmryfb(List<String> deptKeys){
        List<Long> proLists = new ArrayList<Long>();//存储项目id的集合
        int issNum = 0;//分配次数
        int repNum = 0;//报告次数
        int repNum1 = 0;//已报告未分配的issue数

        String str = "(";
        for(String s : deptKeys){
            if(s != null && !"".equals(s) && !" ".equals(s) ){
                str = str + s + ",";
            }
        }
        if(str.length() == 1){
            return new ReportJoinBean(proLists.size(),issNum,repNum,repNum1);
        }
        String userKey = str.substring(0,str.length()-1) + ")";
        String jqlQuery = "reporter in " + userKey + " or assignee in " + userKey + "";


        final SearchService.ParseResult parseResult = searchService.parseQuery(jiraAuthenticationContext.getLoggedInUser(), jqlQuery);
        if (parseResult.isValid()){
            try {
                SearchResults results = searchService.search(jiraAuthenticationContext.getLoggedInUser(), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                List<Issue> issues = results.getIssues();
                for(Issue issue : issues){
                    if(!proLists.contains(issue.getProjectId())){
                        proLists.add(issue.getProjectId());
                    }
                    if(deptKeys.contains(issue.getAssigneeId())){
                        issNum++;
                    }
                    if(deptKeys.contains(issue.getReporterId())){
                        repNum++;
                        if(issue.getAssigneeId() == null || "".equals(issue.getAssigneeId())){
                            repNum1++;
                        }
                    }
                }
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        return new ReportJoinBean(proLists.size(),issNum,repNum,repNum1);
    }













//-----------------------------------------------------------------------------------
//-----------------------------分割线------------------------------------------------
//-----------------------------------------------------------------------------------









    /**
     * 获取所有人员工作数据集合，返回到前台
     * @return
     */
    public List<ReportDoChildrenBean> getAllOfTd(String sign){
        orgs = Arrays.asList(ao.find(StrOrganize.class));//获取所有机构集合
        List<ReportDoChildrenBean> reportDoChildrenBean = new ArrayList<ReportDoChildrenBean>();
        for(StrOrganize org : orgs){
            if(org.getParent() == null || org.getParent() == 0 || org.getID()==org.getParent()){
                reportDoChildrenBean.add(buildChildrenOrg1(org.getID(),sign));
            }
        }

        return reportDoChildrenBean;
    }

    /**
     * 层级递归机构及其所有子机构,获取此机构下的数据集
     * @param orgNo
     */
    public ReportDoChildrenBean buildChildrenOrg1(int orgNo,String sign){
        ReportDoChildrenBean orgChild = new ReportDoChildrenBean();
        orgChild.setName(service.getOrgName(orgNo));
        orgChild.setBmryfb(getBmryfbByOrgNo1(orgNo,sign));
        orgChild.setType(999);
        orgChild.setPid(orgNo);

        StrOrganize[] strOrganize = ao.find(StrOrganize.class, String.format("PARENT = '%s'  and ID <> %d", orgNo, orgNo));//此机构下的所有机构
        //判断机构下是否有子机构
        if(strOrganize.length != 0){
            List<ReportDoChildrenBean> temps = new ArrayList<ReportDoChildrenBean>();
            for(StrOrganize org : strOrganize){
                temps.add(buildChildrenOrg1(org.getID(),sign));
            }
            orgChild.setChildrens(temps);
        }

        StrOrganizeGroup[] organizeGroups = ao.find(StrOrganizeGroup.class, MessageFormat.format("ORG_ID = {0}", orgNo));//机构与部门的对应关系表对象
        //判断机构下是否有直属部门
        if(organizeGroups.length != 0){
            DepartmentEntity[] depts;
            if("bmrygz".equals(sign)){
                depts = Lists.newArrayList(organizeGroups).stream().map(e -> e.getGroup()).filter(s -> s.getType() == 0).toArray(DepartmentEntity[]::new);
            } else{
                depts = Lists.newArrayList(organizeGroups).stream().map(e -> e.getGroup()).filter(s -> s.getType() == 1).toArray(DepartmentEntity[]::new);
            }
            List<ReportDoChildrenBean> lists = new ArrayList<ReportDoChildrenBean>();
            for(DepartmentEntity dept : depts){
                lists.add(buildChildrenDept1(dept.getID()));
            }
            if(orgChild.getChildrens() != null){
                lists.addAll(orgChild.getChildrens());
            }
            orgChild.setChildrens(lists);
        }

        //判断此机构有没有直属雇员，即在此机构下找到的雇员没有在部门下
        StrEmployee[] employees = ao.find(StrEmployee.class,MessageFormat.format("STR_ORGANIZE_ID = {0}", orgNo));//机构下所有雇员
//        if("bmrygz".equals(sign)){
            if(employees.length != 0){
                List<ReportDoChildrenBean> emplists = new ArrayList<>();//机构直属雇员对应信息集合
                for(StrEmployee employee : employees){
                    StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("EMPLOYEE_ID = {0}", employee.getID()));
                    //判断所有雇员是否属于部门或团队，若不属于，则属于机构直属雇员
                    if(groupemps.length == 0){
                        emplists.add(getEmployeeBmryfb1(employee.getID()));
                    }
                }
                if(orgChild.getChildrens() != null){
                    emplists.addAll(orgChild.getChildrens());
                }
                orgChild.setChildrens(emplists);
            }
//        }

        //如果此机构下没有子机构也没有子部门
        if(strOrganize.length == 0 && organizeGroups.length == 0 && employees.length == 0){
            orgChild.setChildrens(new ArrayList<>());
        }
        return orgChild;
    }


    /**
     * 层级递归部门及其所有子部门,获取此部门下的数据集
     * @param deptNo
     */
    public ReportDoChildrenBean buildChildrenDept1(int deptNo){
        ReportDoChildrenBean deptChild = new ReportDoChildrenBean();
        deptChild.setName(service.getDeptName(deptNo));
        deptChild.setBmryfb(getBmryfbByDeptNo1(deptNo));
        deptChild.setType(888);
        deptChild.setPid(deptNo);

        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", deptNo, deptNo));//获取此部门下的部门
        //判断部门下是否有部门
        if(depts.length != 0){
            List<ReportDoChildrenBean> temps = new ArrayList<ReportDoChildrenBean>();
            for(DepartmentEntity dept : depts){
                temps.add(buildChildrenDept1(dept.getID()));
            }
            deptChild.setChildrens(temps);
        }

        StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0}", deptNo));
        //判断此部门下是否有雇员
        if(groupemps.length != 0){
            StrEmployee[] employees = Lists.newArrayList(groupemps).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new);//获取部门下的雇员
            List<ReportDoChildrenBean> lists = new ArrayList<>();
            for(StrEmployee emp : employees){
                lists.add(getEmployeeBmryfb1(emp.getID()));
            }
            if(deptChild.getChildrens() != null){
                lists.addAll(deptChild.getChildrens());
            }
            deptChild.setChildrens(lists);
        }
        //如果此部门下没有子部门也没有雇员
        if(depts.length == 0 && groupemps.length == 0){
            deptChild.setChildrens(new ArrayList<ReportDoChildrenBean>());
        }
        return deptChild;
    }

    /**
     * 根据雇员id获取此雇员对应的的数据集
     * @param empNo
     * @return
     */
    public ReportDoChildrenBean getEmployeeBmryfb1(int empNo){
        ReportDoChildrenBean empChild = new ReportDoChildrenBean();
        StrEmployee employee = ao.get(StrEmployee.class,empNo);
        empChild.setName(employee.getEmployeeName());
        empChild.setBmryfb(getBmryfbByEmpNo1(empNo));
        empChild.setChildrens(new ArrayList<ReportDoChildrenBean>());
        empChild.setType(777);
        empChild.setPid(empNo);
        return empChild;
    }

    /**
     * 根据雇员id找到其对应的参与的issue的相关数据
     * @param empNo 雇员id（主键）
     * @return
     */
    public ReportDoBean getBmryfbByEmpNo1(int empNo){
        List<Long> proLists = new ArrayList<Long>();//存储项目id的集合
        int undoNum = 0;//待办事项数
        int doingNum = 0;//进行中事项数
        int doneNum = 0;//已完成事项数
        int otherNum = 0;//其他事项数
        StrEmployee emp = ao.get(StrEmployee.class, empNo);
        String userKey = emp.getJiraUserKey();
        if(userKey == null || "".equals(userKey) || " ".equals(userKey)){
            return new ReportDoBean(proLists.size(),undoNum,doingNum,doneNum,otherNum);
        }
        String jqlQuery = "reporter = " + userKey + " or assignee = " + userKey + "";
        final SearchService.ParseResult parseResult = searchService.parseQuery(userManager.getUserByKey(userKey), jqlQuery);
        if (parseResult.isValid()){
            try {
                SearchResults results = searchService.search(userManager.getUserByKey(userKey), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                List<Issue> issues = results.getIssues();
                for(Issue issue : issues){
                    if(!proLists.contains(issue.getProjectId())){
                        proLists.add(issue.getProjectId());
                    }
                    String status = issue.getStatusId();
                    if(status != null && !"".equals(status)){
                        if(issue.getResolutionId() != null){
                            doneNum++;
                        } else if("10000".equals(status)){
                            undoNum++;
                        } else if("3".equals(status) || "4".equals(status)){
                            doingNum++;
                        } else {
                            otherNum++;
                        }
                    }
                }
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        return new ReportDoBean(proLists.size(),undoNum,doingNum,doneNum,otherNum);
    }


    /**
     * 根据机构id找到其对应的机构人员发布相关数据
     * @param orgNo 机构编号
     * @return
     */
    public ReportDoBean getBmryfbByOrgNo1(int orgNo,String sign){
        if(orgKeys != null || orgKeys.size() != 0){
            orgKeys.clear();
        }
        buildBmryfbOrg1(orgNo,sign);//调用机构回调函数
        return CountBmryfb1(orgKeys);
    }

    /**
     * 层级递归机构下的所有子机构
     * @param orgNo
     */
    public void buildBmryfbOrg1(int orgNo,String sign){
        StrOrganize[] strOrganize = ao.find(StrOrganize.class, String.format("PARENT = '%s'  and ID <> %d", orgNo, orgNo));//此机构下的所有机构
        //判断机构下是否有子机构
        if(strOrganize.length != 0){
            for(StrOrganize org : strOrganize){
                buildBmryfbOrg1(org.getID(),sign);
            }
        }

        StrOrganizeGroup[] organizeGroups = ao.find(StrOrganizeGroup.class, MessageFormat.format("ORG_ID = {0}", orgNo));//机构与部门的对应关系表对象
        //判断机构下是否有直属部门
        if(organizeGroups.length != 0){
            DepartmentEntity[] depts;
            if("bmrygz".equals(sign)){
                depts = Lists.newArrayList(organizeGroups).stream().map(e -> e.getGroup()).filter(s -> s.getType() == 0).toArray(DepartmentEntity[]::new);
            } else{
                depts = Lists.newArrayList(organizeGroups).stream().map(e -> e.getGroup()).filter(s -> s.getType() == 1).toArray(DepartmentEntity[]::new);
            }
            for(DepartmentEntity dept : depts){
                getBmryfbByDeptNo1(dept.getID());
                orgKeys.addAll(deptKeys);
            }
        }

        //判断此机构有没有直属雇员，即在此机构下找到的雇员没有在部门下
        StrEmployee[] employees = ao.find(StrEmployee.class,MessageFormat.format("STR_ORGANIZE_ID = {0}", orgNo));
        if(employees.length != 0){
            List<ReportJoinChildrenBean> emplists = new ArrayList<>();
            for(StrEmployee employee : employees){
                StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("EMPLOYEE_ID = {0}", employee.getID()));
                //为0表示此雇员为机构直属雇员
                if(groupemps.length == 0){
                    orgKeys.add(employee.getJiraUserKey());
                }
            }

        }
    }



    /**
     * 根据部门id找到其对应的部门人员发布相关数据
     * @param deptNo 部门编号
     * @return
     */
    public ReportDoBean getBmryfbByDeptNo1(int deptNo){
        if(deptKeys != null || deptKeys.size() != 0){
            deptKeys.clear();
        }
        buildBmryfbDept1(deptNo);//调用部门回调函数
        return CountBmryfb1(deptKeys);
    }

    /**
     * 层级递归部门下的所有子部门
     * @param deptNo
     */
    public void buildBmryfbDept1(int deptNo){
        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", deptNo, deptNo));//获取此部门下的部门
        //判断部门下是否有部门
        if(depts.length != 0){
            for(DepartmentEntity dept : depts){
                buildBmryfbDept1(dept.getID());
            }
        }

        StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0}", deptNo));
        //判断此部门下是否有雇员
        if(groupemps.length != 0){
            StrEmployee[] employees = Lists.newArrayList(groupemps).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new);//获取部门下的雇员
            for(StrEmployee emp : employees){
                if(!"".equals(emp.getJiraUserKey()) && !" ".equals(emp.getJiraUserKey()) && emp.getJiraUserKey() != null){
                    deptKeys.add(emp.getJiraUserKey());
                }
            }
        }
    }

    /**
     * 根据传入的雇员key值集合获取对应的信息集合
     * @param deptKeys 雇员key值集合
     * @return 部门或机构的信息集合
     */
    public ReportDoBean CountBmryfb1(List<String> deptKeys){
        List<Long> proLists = new ArrayList<Long>();//存储项目id的集合
        int undoNum = 0;//待办事项数
        int doingNum = 0;//进行中事项数
        int doneNum = 0;//已完成事项数
        int otherNum = 0;//其他事项数

        String str = "(";
        for(String s : deptKeys){
            if(s != null && !"".equals(s) && !" ".equals(s) ){
                str = str + s + ",";
            }
        }
        if(str.length() == 1){
            return new ReportDoBean(proLists.size(),undoNum,doingNum,doneNum,otherNum);
        }
        String userKey = str.substring(0,str.length()-1) + ")";
        String jqlQuery = "reporter in " + userKey + " or assignee in " + userKey + "";

        final SearchService.ParseResult parseResult = searchService.parseQuery(jiraAuthenticationContext.getLoggedInUser(), jqlQuery);
        if (parseResult.isValid()){
            try {
                SearchResults results = searchService.search(jiraAuthenticationContext.getLoggedInUser(), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                List<Issue> issues = results.getIssues();
                for(Issue issue : issues){
                    if(!proLists.contains(issue.getProjectId())){
                        proLists.add(issue.getProjectId());
                    }
                    String status = issue.getStatusId();
                    if(status != null && !"".equals(status)){
//                        String summary = issue.getSummary();
//                        String id = issue.getResolutionId();
                        if(issue.getResolutionId() != null){
                            doneNum++;
                        } else if("10000".equals(status)){
                            undoNum++;
                        } else if("3".equals(status) || "4".equals(status)){
                            doingNum++;
                        } else {
                            otherNum++;
                        }
                    }
                }
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        return new ReportDoBean(proLists.size(),undoNum,doingNum,doneNum,otherNum);
    }



//-----------------------------------------------------------------------------------
//-----------------------------分割线------------------------------------------------
//-----------------------------------------------------------------------------------


    /**
     * 根据传入的信息获取对应的详细信息
     * @param sign bmryfb、tdryfb、bmrygz、tdrygz
     * @param type 999表示机构，888表示部门或团队，777表示雇员
     * @param pid 机构、团队、部门、雇员的id
     * @return 详细信息
     */
    public List<BmryfbInfoBean> getInfos(String sign,int type,int pid){
        if(type == 999){
            if(orgKeys != null || orgKeys.size() != 0){
                orgKeys.clear();
            }
            if("bmryfb".equals(sign) || "tdryfb".equals(sign)){
                buildBmryfbOrg(pid,sign);//调用机构回调函数
            } else if("bmrygz".equals(sign) || "tdrygz".equals(sign)){
                buildBmryfbOrg1(pid,sign);//调用机构回调函数
            }
            return sumBmryfb(getInfosByKeys(orgKeys));
        } else if(type == 888){
            if(deptKeys != null || deptKeys.size() != 0){
                deptKeys.clear();
            }
            buildBmryfbDept(pid);//调用部门回调函数
            return sumBmryfb(getInfosByKeys(deptKeys));
        } else if(type == 777){
            ArrayList<String> empkey = new ArrayList<>();
            empkey.add(ao.get(StrEmployee.class, pid).getJiraUserKey());
            return filteBean(getInfosByKeys(empkey));
        }
        return null;
    }

    /**
     * 根据传入的雇员key值集合获取对应的信息集合
     * @param deptKeys 雇员key值集合
     * @return 部门或机构的信息集合
     */
    public ArrayList<BmryfbInfoBean> getInfosByKeys(List<String> deptKeys){
        ArrayList<BmryfbInfoBean> allinfo = new ArrayList<>();
        List<Long> proLists = new ArrayList<Long>();//存储项目id的集合
        int issNum = 0;
        int repNum = 0;
        int repNum1 = 0;
        int undoNum = 0;
        int doingNum = 0;
        int doneNum = 0;
        String str = "(";
        for(String s : deptKeys){
            if(s != null && !"".equals(s) && !" ".equals(s) ){
                str = str + s + ",";
            }
        }
        if(str.length() == 1){
            return allinfo;
        }
        String userKey = str.substring(0,str.length()-1) + ")";
        String jqlQuery = "reporter in " + userKey + " or assignee in " + userKey + "";

        final SearchService.ParseResult parseResult = searchService.parseQuery(jiraAuthenticationContext.getLoggedInUser(), jqlQuery);
        if (parseResult.isValid()){
            try {
                SearchResults results = searchService.search(jiraAuthenticationContext.getLoggedInUser(), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                List<Issue> issues = results.getIssues();
                for(Issue issue : issues){
                    String status = issue.getStatusId();
                    BmryfbInfoBean bmryfbInfo = new BmryfbInfoBean();
                    bmryfbInfo.setProName(projectManager.getProjectObj(issue.getProjectId()).getName());
                    bmryfbInfo.setProId(projectManager.getProjectObj(issue.getProjectId()).getId());
                    bmryfbInfo.setIssKey(projectManager.getProjectObj(issue.getProjectId()).getKey());
                    bmryfbInfo.setIsKey(issue.getKey());
                    if(!proLists.contains(issue.getProjectId())){
                        proLists.add(issue.getProjectId());
                    }
                    if(deptKeys.contains(issue.getAssigneeId())){
                        bmryfbInfo.setIssName(issue.getSummary());
                        bmryfbInfo.setIssId(issue.getId());
                    }
                    if(deptKeys.contains(issue.getReporterId())){
                        bmryfbInfo.setRepName(issue.getSummary());
                        bmryfbInfo.setRepId(issue.getId());
                        if(issue.getAssigneeId() == null || "".equals(issue.getAssigneeId())){
                            bmryfbInfo.setRepName1(issue.getSummary());
                            bmryfbInfo.setRepId1(issue.getId());
                        }
                    }
                    if(status != null && !"".equals(status)){
                        if("10000".equals(status)){
                            bmryfbInfo.setUndoName(issue.getSummary());
                            bmryfbInfo.setUndoId(issue.getId());
                        } else if("3".equals(status) || "4".equals(status)){
                            bmryfbInfo.setDoingName(issue.getSummary());
                            bmryfbInfo.setDoingId(issue.getId());
                        } else if("5".equals(status) || "6".equals(status) || "8".equals(status)){
                            bmryfbInfo.setDoneName(issue.getSummary());
                            bmryfbInfo.setDoneId(issue.getId());
                        }
                    }
                    allinfo.add(bmryfbInfo);
                }
            } catch (SearchException e) {
                log.error("Error running search", e);
            }
        } else {
            log.error("Error parsing jqlQuery: " + parseResult.getErrors());
        }
        return allinfo;
    }

    /**
     * 将传入的部门人员发布信息集合相加
     * @param beans 部门人员发布信息集合
     * @return
     */
    public List<BmryfbInfoBean> sumBmryfb(List<BmryfbInfoBean> beans){
        List<BmryfbInfoBean> infos = new ArrayList<BmryfbInfoBean>();
        List<Long> proLists = new ArrayList<Long>();
        List<Long> issLists = new ArrayList<Long>();
        List<Long> repLists = new ArrayList<Long>();
        List<Long> repLists1 = new ArrayList<Long>();
        List<Long> undoLists = new ArrayList<Long>();
        List<Long> doingLists = new ArrayList<Long>();
        List<Long> doneLists = new ArrayList<Long>();

        if(beans != null && beans.size() != 0){
            for(BmryfbInfoBean info : beans){
                if(info.getProId() != 0 && !proLists.contains(info.getProId())){
                    proLists.add(info.getProId());
                }
                if(info.getIssId() != 0 && !issLists.contains(info.getIssId())){
                    issLists.add(info.getIssId());
                }
                if(info.getRepId() != 0 && !repLists.contains(info.getRepId())){
                    repLists.add(info.getRepId());
                }
                if(info.getRepId1() != 0 && !repLists1.contains(info.getRepId1())){
                    repLists1.add(info.getRepId1());
                }
                if(info.getUndoId() != 0 && !undoLists.contains(info.getUndoId())){
                    undoLists.add(info.getUndoId());
                }
                if(info.getDoingId() != 0 && !doingLists.contains(info.getDoingId())){
                    doingLists.add(info.getDoingId());
                }
                if(info.getDoneId() != 0 && !doneLists.contains(info.getDoneId())){
                    doneLists.add(info.getDoneId());
                }
            }

            for(long prolist : proLists){
                int issName = 0;
                int repName = 0;
                int repName1 = 0;
                int undoName = 0;
                int doingName = 0;
                int doneName = 0;
                BmryfbInfoBean info = new BmryfbInfoBean();
                Project pro = projectManager.getProjectObj(prolist);
                info.setProName(pro.getName());
                for(long isslists : issLists){
                    if(issueManager.getIssueObject(isslists).getProjectId() == prolist){
                        issName++;
                    }
                }
                for(long replists : repLists){
                    if(issueManager.getIssueObject(replists).getProjectId() == prolist){
                        repName++;
                    }
                }
                for(long replists1 : repLists1){
                    if(issueManager.getIssueObject(replists1).getProjectId() == prolist){
                        repName1++;
                    }
                }
                for(long undolists : undoLists){
                    if(issueManager.getIssueObject(undolists).getProjectId() == prolist){
                        undoName++;
                    }
                }
                for(long doinglists : doingLists){
                    if(issueManager.getIssueObject(doinglists).getProjectId() == prolist){
                        doingName++;
                    }
                }
                for(long donelists : doneLists){
                    if(issueManager.getIssueObject(donelists).getProjectId() == prolist){
                        doneName++;
                    }
                }

                info.setIssName(""+issName);
                info.setRepName(""+repName);
                info.setRepName1(""+repName1);
                info.setUndoName(""+undoName);
                info.setDoingName(""+doingName);
                info.setDoneName(""+doneName);
                infos.add(info);
            }
        }
        return infos;
    }

    public ArrayList<BmryfbInfoBean> filteBean(ArrayList<BmryfbInfoBean> beans){
        for(BmryfbInfoBean bean : beans){
            if(bean.getProName() == null){
                bean.setProName("");
            }
            if(bean.getIssName() == null){
                bean.setIssName("");
            }
            if(bean.getRepName() == null){
                bean.setRepName("");
            }
            if(bean.getRepName1() == null){
                bean.setRepName1("");
            }
            if(bean.getUndoName() == null){
                bean.setUndoName("");
            }
            if(bean.getDoingName() == null){
                bean.setDoingName("");
            }
            if(bean.getDoneName() == null){
                bean.setDoneName("");
            }
        }

        return beans;
    }

}
