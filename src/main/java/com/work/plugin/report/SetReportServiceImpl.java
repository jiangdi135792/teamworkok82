package com.work.plugin.report;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.work.plugin.ao.RoleEntity;
import com.work.plugin.ao.StrEmployee;
import com.work.plugin.ao.StrEmployeeOfRole;
import com.work.plugin.util.license.license.GlobalConfig;
import lombok.RequiredArgsConstructor;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by admin on 2021/7/5.
 */
@RequiredArgsConstructor
public class SetReportServiceImpl implements SetReportService {
    private static final Logger log = LoggerFactory.getLogger(SetReportServiceImpl.class);
    private final ActiveObjects ao;
    private final CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserManager userManager;

    /**
     * 获取此jira用户对应的展示的issue的列及列对应数据
     * @param jira_user_key
     * @param reportKey
     * @param type "show"为前台获取，则不展示手动增加的列
     * @return
     */
    @Override
    public List<SetReportColumnBean> getColumn(String jira_user_key,String reportKey,String type) {
//        SetReportColumn[] columns = ao.find(SetReportColumn.class,String.format(" %s = ? AND %s = ? ","JIRA_USER_KEY","REPORT_KEY"),jira_user_key,reportKey);
        SetReportColumn[] columns = ao.find(SetReportColumn.class,String.format(" REPORT_KEY = '%s' ",reportKey));
        List<SetReportColumnBean> beans = new ArrayList<>();
        SetReportColumnBean bean;
        for(SetReportColumn column : columns){
            //无论如何将name列排在第一列
            if(column.getColumnName().equals("name")){
                bean = new SetReportColumnBean(column);
                beans.add(bean);
                break;
            }
        }

//        if("show".equals(type)){
//            //将新增的统计列排在第二列
//            SetReportBean bean1 = getReportByReportkey(jira_user_key,reportKey);
//            String groupone = bean1.getGroup_one();
//            String grouptwo = bean1.getGroup_two();
//            SetReportColumnBean bean2 = new SetReportColumnBean();
//            switch (groupone){
//                case "issue": break;
//                case "worklogAuthor":
//                    bean2 = new SetReportColumnBean();
//                    bean2.setColumnName("workingHours");
//                    bean2.setColumnWidth("100");
//                    bean2.setJira_user_key(jira_user_key);
//                    bean2.setReportKey(reportKey);
//                    bean2.setStatistics("1");
//                    bean2.setSequence("2");
//                    beans.add(bean2);
//                    break;
//                case "project":
//                    bean2 = new SetReportColumnBean();
//                    bean2.setColumnName("countProject");
//                    bean2.setColumnWidth("100");
//                    bean2.setJira_user_key(jira_user_key);
//                    bean2.setReportKey(reportKey);
//                    bean2.setStatistics("1");
//                    bean2.setSequence("2");
//                    beans.add(bean2);
//                    break;
//                case "assignee":break;
//                case "reporter":break;
//            }
//            //增加待办todo 进行中doing 已完成done 三列
//            SetReportColumnBean bean3 = new SetReportColumnBean();
//            bean3.setColumnName("todo");
//            bean3.setColumnWidth("100");
//            bean3.setJira_user_key(jira_user_key);
//            bean3.setReportKey(reportKey);
//            bean3.setStatistics("1");
//            bean3.setSequence("2");
//            beans.add(bean3);
//            bean3 = new SetReportColumnBean();
//            bean3.setColumnName("doing");
//            bean3.setColumnWidth("100");
//            bean3.setJira_user_key(jira_user_key);
//            bean3.setReportKey(reportKey);
//            bean3.setStatistics("1");
//            bean3.setSequence("2");
//            beans.add(bean3);
//            bean3 = new SetReportColumnBean();
//            bean3.setColumnName("done");
//            bean3.setColumnWidth("100");
//            bean3.setJira_user_key(jira_user_key);
//            bean3.setReportKey(reportKey);
//            bean3.setStatistics("1");
//            bean3.setSequence("2");
//            beans.add(bean3);
//        }

        //剩下的非name列按照顺序排列
        List<SetReportColumnBean> temps = new ArrayList<>();
        for(SetReportColumn column : columns){
            if(!column.getColumnName().equals("name")){
                bean = new SetReportColumnBean(column);
                temps.add(bean);
            }
        }
        //按照数据库中的列的顺序进行排序
        Collections.sort(temps, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                SetReportColumnBean bean1=(SetReportColumnBean)o1;
                SetReportColumnBean bean2=(SetReportColumnBean)o2;
                if(Integer.parseInt(bean1.getSequence()) > Integer.parseInt(bean2.getSequence())){
                    return 1;
                }else if(Integer.parseInt(bean1.getSequence())==Integer.parseInt(bean2.getSequence())){
                    return 0;
                }else{
                    return -1;
                }
            }
        });

        beans.addAll(temps);
        return beans;
    }

    /**
     * 通过jira用户的key和报表的key获取对应报表设置信息
     * @param jira_user_key jira用户的key
     * @param reportKey 报表的key
     * @return
     */
    @Override
    public SetReportBean getReportByReportkey(String jira_user_key,String reportKey){
//        SetReport[] reports = ao.find(SetReport.class,String.format(" %s = ? AND  %s = ? ","JIRA_USER_KEY","REPORT_KEY"),jira_user_key,reportKey);
        SetReport[] reports = ao.find(SetReport.class,String.format(" REPORT_KEY = '%s' ",reportKey));
        SetReportBean bean = new SetReportBean();
        for(SetReport report : reports){
            bean = new SetReportBean(report);
        }
        return bean;
    }

    /**
     * 初始化issue的列
     * @return
     */
    public ArrayList<ComboboxBean> getInitColumn(){
        ArrayList<ComboboxBean> lists = new ArrayList<>();
        ComboboxBean bean = new ComboboxBean();
        //系统自带的字段
        String[] customarr = {"workingHours","countProject","todo","doing","done","Key",
                "Summary","Assignee","Report","Status","Resolution",
                "Created","Updated","Due","Environment","Time Spent",
                "Votes","Watchers","Creator","Security","Description",
                "issueType","estimate","originalEstimate","components","affectedVersions",
                "fixVersions","number","resolutionDate","priority","parentObject"};
        for(String str : customarr){
            bean = new ComboboxBean();
            bean.setComboboxValue(str);
            lists.add(bean);
        }
        List<CustomField> coustomFields = customFieldManager.getCustomFieldObjects();//获取jira中的自定义字段
        if(coustomFields != null && coustomFields.size() != 0){
            for(CustomField customField : coustomFields){
                bean = new ComboboxBean();
                bean.setComboboxValue(customField.getName());
                lists.add(bean);
            }
        }
        return lists;
    }

    /**
     * 获取此jira用户的所有报表设置
     * @param jira_user_key 用户的key
     * @param type 获取类型，set为获取用户自己设置的报表，show为获取展示的报表，可能包括别人分享给自己的
     * @return
     */
    @Override
    public List<SetReportBean> getSetReport(String jira_user_key,String type) {
        SetReport[] reports = ao.find(SetReport.class,String.format(" JIRA_USER_KEY = '%s' ",jira_user_key));
        List<SetReportBean> beans = new ArrayList<>();
        SetReportBean bean;
        for(SetReport report : reports){
            bean = new SetReportBean(report);
            beans.add(bean);
        }
        if("set".equals(type)){
            return beans;
        }

        StrEmployee[] strEmployee = ao.find(StrEmployee.class,String.format(" %s = ? ","JIRA_USER_KEY"),jira_user_key);//通过jira用户key找到雇员
        if(strEmployee == null || strEmployee.length == 0){
            return beans;
        }
        StrEmployeeOfRole[] strEmployeeOfRoles = strEmployee[0].getStrEmployeeOfRole();//雇员的角色
        if(strEmployeeOfRoles == null || strEmployeeOfRoles.length == 0){
            return beans;
        }
        //获取别人分享给此用户角色的报表
        for(StrEmployeeOfRole strEmployeeOfRole : strEmployeeOfRoles){
            ShareReport[] shareReports = ao.find(ShareReport.class,String.format(" %s = ? AND TYPE = 0 ","EMP_ORG_ID"),strEmployeeOfRole.getRole().getID());
            for(ShareReport report : shareReports){
                SetReport[] reports1 = ao.find(SetReport.class,String.format(" %s = ? AND  %s = ? ","JIRA_USER_KEY","REPORT_KEY"),report.getJira_user_key(),report.getReportKey());
                for(SetReport report1 : reports1){
                    bean = new SetReportBean(report1);
                    if(!beans.contains(bean)){
                        beans.add(bean);
                    }
                }
            }
        }

        //获取别人分享给此用户的报表
        ShareReport[] shareReports1 = ao.find(ShareReport.class,String.format(" %s = ? AND TYPE = 1 ","EMP_ORG_ID"),strEmployee[0].getID());
        for(ShareReport report : shareReports1){
            SetReport[] reports1 = ao.find(SetReport.class,String.format(" %s = ? AND  %s = ? ","JIRA_USER_KEY","REPORT_KEY"),report.getJira_user_key(),report.getReportKey());
            for(SetReport report1 : reports1){
                bean = new SetReportBean(report1);
                if(!beans.contains(bean)){
                    beans.add(bean);
                }
            }
        }
        return beans;
    }

    /**
     * 创建报表设置
     */
    @Override
    public void createReport(SetReportBean bean,String jira_user_key) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        final SetReport entity = ao.create(SetReport.class);
        entity.setJira_user_key(jira_user_key);
        entity.setReportKey(bean.getReportKey());
        entity.setReportName(bean.getReportName());
        entity.setGroup_one(bean.getGroup_one());
        entity.setGroup_two(bean.getGroup_two());
        entity.setModelName(bean.getModelName());
        entity.setModelType(bean.getModelType());
        entity.setShow(bean.getModel_show());
        entity.setStartLine(bean.getStartLine());
        entity.setEndLine(bean.getEndLine());
        entity.setCreateDate(format.format(new Date()));
        entity.setInitType(bean.getInitType());
        entity.save();
    }

    /**
     * 创建报表issue展示的列
     * @param lists
     */
    @Override
    public void createColumn(ArrayList<SetReportColumnBean> lists,String jira_user_key,String reportKey) {
        for(SetReportColumnBean bean : lists){
            final SetReportColumn entity = ao.create(SetReportColumn.class);
            entity.setJira_user_key(jira_user_key);
            entity.setColumnName(bean.getColumnName());
            if("".equals(bean.getColumnWidth())){
                entity.setColumnWidth("100");
            } else {
                entity.setColumnWidth(bean.getColumnWidth());
            }
            entity.setJira_user_key(jira_user_key);
            entity.setStatistics(bean.getStatistics());
            if("".equals(bean.getSequence())){
                entity.setSequence("1");
            } else {
                entity.setSequence(bean.getSequence());
            }
            entity.setInitType(bean.getInitType());
            entity.setReportKey(reportKey);
            entity.save();
        }
    }

    /**
     * 创建分享的报表信息
     * @param orgRole 组织角色
     * @param employee 雇员
     * @param jira_user_key 当前jira用户key，即分享人
     * @param reportKey 报表key
     */
    public void createShare(String orgRole,String employee,String jira_user_key,String reportKey,String initType){
        if("null".equals(orgRole) && "null".equals(employee)){
            return;
        }
        for(String str : orgRole.split(",")){
            if(!isStrToNum(str)){
                continue;
            }
            ShareReport entity = ao.create(ShareReport.class);
            entity.setEmp_org_id(Integer.parseInt(str));
            entity.setJira_user_key(jira_user_key);
            entity.setReportKey(reportKey);
            entity.setType(0);
            entity.setInitType(initType);
            entity.save();
        }
        for(String str : employee.split(",")){
            if(!isStrToNum(str)){
                continue;
            }
            ShareReport entity = ao.create(ShareReport.class);
            entity.setEmp_org_id(Integer.parseInt(str));
            entity.setJira_user_key(jira_user_key);
            entity.setReportKey(reportKey);
            entity.setType(1);
            entity.setInitType(initType);
            entity.save();
        }
    }

    /**
     * 修改报表设置
     * @param bean
     * @param jira_user_key
     */
    public void updateReport(SetReportBean bean,String jira_user_key){
        SetReport[] reports = ao.find(SetReport.class,String.format(" %s = ? AND  %s = ? ","JIRA_USER_KEY","REPORT_KEY"),jira_user_key,bean.getReportKey());
        for(SetReport report : reports){
            report.setReportName(bean.getReportName());
            report.setGroup_one(bean.getGroup_one());
            report.setGroup_two(bean.getGroup_two());
            report.setModelName(bean.getModelName());
            report.setModelType(bean.getModelType());
            report.setShow(bean.getModel_show());
            report.setStartLine(bean.getStartLine());
            bean.setEndLine(bean.getEndLine());
            report.save();
        }
    }


    /**
     * 修改报表issue展示的列
     * @param lists
     * @param jira_user_key
     * @param reportKey
     */
    public void updateColumn(ArrayList<SetReportColumnBean> lists,String jira_user_key,String reportKey){
        SetReportColumn[] reports = ao.find(SetReportColumn.class,String.format(" %s = ? AND  %s = ? ","JIRA_USER_KEY","REPORT_KEY"),jira_user_key,reportKey);
        for(SetReportColumn report : reports){
            ao.delete(report);
        }
        createColumn(lists,jira_user_key,reportKey);
    }

    /**
     * 修改报表分享
     * @param orgRole 组织角色id
     * @param employee 雇员id
     * @param jira_user_key jira用户的key
     * @param reportKey 报表的key
     */
    public void updateShare(String orgRole,String employee,String jira_user_key,String reportKey){
        ShareReport[] shareReports = ao.find(ShareReport.class,String.format(" %s = ? AND %s = ? ","REPORT_KEY","JIRA_USER_KEY"),reportKey,jira_user_key);
        for(ShareReport report : shareReports){
            ao.delete(report);
        }
        createShare(orgRole,employee,jira_user_key,reportKey,null);
    }

    /**
     * 根据报表key和jira用户key删除对应报表数据
     * @param reportKey
     * @param jira_user_key
     */
    public void deleteReport(String reportKey,String jira_user_key){
        SetReport[] reports = ao.find(SetReport.class,String.format(" %s = ? AND  %s = ? ","JIRA_USER_KEY","REPORT_KEY"),jira_user_key,reportKey);
        ShareReport[] shareReports = ao.find(ShareReport.class,String.format(" %s = ? AND %s = ? ","REPORT_KEY","JIRA_USER_KEY"),reportKey,jira_user_key);
        SetReportColumn[] reportColumns = ao.find(SetReportColumn.class,String.format(" %s = ? AND  %s = ? ","JIRA_USER_KEY","REPORT_KEY"),jira_user_key,reportKey);
        for(SetReport report : reports){
            ao.delete(report);
        }
        for(SetReportColumn report : reportColumns){
            ao.delete(report);
        }
        for(ShareReport report : shareReports){
            ao.delete(report);
        }
    }

    /**
     * 判断报表名称是否存在
     * @param reportName 报表名称
     * @param jira_user_key 用户key
     * @return true-存在 false-不存在
     */
    public boolean reportNameIsExist(String reportName,String jira_user_key){
        SetReport[] reports = ao.find(SetReport.class,String.format(" %s = ? AND  %s = ? ","JIRA_USER_KEY","REPORT_NAME"),jira_user_key,reportName);
        if(reports.length != 0){
            return true;
        }
        return false;
    }

    /**
     * 判断报表key是否存在
     * @param reportKey 报表名称
     * @param jira_user_key 用户key
     * @return true-存在 false-不存在
     */
    public boolean reportKeyIsExist(String reportKey,String jira_user_key){
        SetReport[] reports = ao.find(SetReport.class,String.format(" %s = ? AND  %s = ? ","JIRA_USER_KEY","REPORT_KEY"),jira_user_key,reportKey);
        if(reports.length != 0){
            return true;
        }
        return false;
    }

    /**
     * 二级关联根据第一分组获取第二分组的值
     * @param groupone
     * @return
     */
    public ArrayList<GroupTwoBean> getGrouptwo(String groupone){
        ArrayList<GroupTwoBean> lists = new ArrayList<>();
        if("project".equals(groupone)){
//            GroupTwoBean bean = new GroupTwoBean();
//            bean.setValue("issue");
//            bean.setText("issue(assignee + reporter)");
//            lists.add(bean);
            GroupTwoBean bean = new GroupTwoBean();
            bean.setValue("assignee");
            bean.setText("assignee");
            lists.add(bean);
            bean = new GroupTwoBean();
            bean.setValue("reporter");
            bean.setText("reporter");
            lists.add(bean);
//            bean = new GroupTwoBean();
//            bean.setValue("worklogAuthor");
//            bean.setText("worklogAuthor");
//            lists.add(bean);
        } else {
            GroupTwoBean bean = new GroupTwoBean();
            bean.setValue("project");
            bean.setText("project");
            lists.add(bean);
        }
        return lists;
    }

    /**
     * 判断该数据是否存在  true-不存在  false-存在
     * @param jira_user_key
     * @param reportkey
     * @return
     */
    public boolean setIsexit(String jira_user_key,String reportkey){
//        SetReport[] reports = ao.find(SetReport.class,String.format(" %s = ? AND  %s = ? ","JIRA_USER_KEY","REPORT_KEY"),jira_user_key,reportkey);
        SetReport[] reports = ao.find(SetReport.class,String.format(" REPORT_KEY = '%s' ",reportkey));

        if(reports.length == 0){
            return true;
        }
        return false;
    }

    /**
     * 初始化报表设置表
     */
    public void initReport(){
        String jira_user_key = "admin";
        Set<ApplicationUser> lstu = userManager.getAllUsers();
        for (ApplicationUser u : lstu) {
            if (u.getId() == 10000 || u.getId() == 1) {
                jira_user_key = u.getKey();
                break;
            }
        }
        ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        if (!GlobalConfig.getIsDebug()) {
            if (loggedInUser == null) return;
            jira_user_key = loggedInUser.getKey();
        }
        if (1==1){
           // jira_user_key = loggedInUser.getKey();
//        if(!setIsexit(jira_user_key,"工时表")){
//            deleteReport("工时表",jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"项目数统计表")){
//            deleteReport("项目数统计表",jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"issue状态统计表")){
//            deleteReport("issue状态统计表",jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"参与项目的机构")){
//            deleteReport("参与项目的机构",jira_user_key);
//        }
//
//        if(!setIsexit(jira_user_key,"time")){
//            deleteReport("time",jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"countProject")){
//            deleteReport("countProject",jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"issueStatus")){
//            deleteReport("issueStatus",jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"projectRoot")){
//            deleteReport("projectRoot",jira_user_key);
//        }
//
//        if(!setIsexit(jira_user_key,"工时表_"+jira_user_key)){
//            deleteReport("工时表_"+jira_user_key,jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"项目数统计表_"+jira_user_key)){
//            deleteReport("项目数统计表_"+jira_user_key,jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"issue状态统计表_"+jira_user_key)){
//            deleteReport("issue状态统计表_"+jira_user_key,jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"参与项目的机构_"+jira_user_key)){
//            deleteReport("参与项目的机构_"+jira_user_key,jira_user_key);
//        }
//
//        if(!setIsexit(jira_user_key,"time_"+jira_user_key)){
//            deleteReport("time_"+jira_user_key,jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"countProject_"+jira_user_key)){
//            deleteReport("countProject_"+jira_user_key,jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"issueStatus_"+jira_user_key)){
//            deleteReport("issueStatus_"+jira_user_key,jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"projectRoot_"+jira_user_key)){
//            deleteReport("projectRoot_"+jira_user_key,jira_user_key);
//        }
//
//        if(!setIsexit(jira_user_key,"timesheet_"+jira_user_key)){
//            deleteReport("timesheet_"+jira_user_key,jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"project count_"+jira_user_key)){
//            deleteReport("project count_"+jira_user_key,jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"issueStatus_"+jira_user_key)){
//            deleteReport("issueStatus_"+jira_user_key,jira_user_key);
//        }
//        if(!setIsexit(jira_user_key,"group by project_"+jira_user_key)){
//            deleteReport("group by project_"+jira_user_key,jira_user_key);
//        }

            SetReport[] reports = ao.find(SetReport.class);
            for(SetReport report : reports){
                if(report.getInitType() != null && "init".equals(report.getInitType())){
                    ao.delete(report);
                }
            }

            if(setIsexit(jira_user_key,"timesheet_"+jira_user_key)){
                SetReportBean bean = new SetReportBean();
                bean.setJira_user_key(jira_user_key);//用户的key
                bean.setReportKey("timesheet_"+jira_user_key);//报表key
                bean.setReportName("统计工时报表");//报表名称
                bean.setGroup_one("worklogAuthor");//第一分组
                bean.setGroup_two("project");//第二分组，999表示没有选择，界面展示为空白
    //            bean.setCreateDate("2018-07-25 10:07:43");//报表创建时间
                bean.setModelName("工时图");//图表名称
                bean.setModel_show(9);//图表是否展示   9-展示  8-不展示
                bean.setModelType(0);//图表类型   0-折线图   1-柱状图
                bean.setStartLine(0);//图表开始行,默认为0,即从第一行开始查
                bean.setEndLine(0);//图表结束行,默认为0,即查询到最后一行
                bean.setInitType("init");
                createReport(bean,jira_user_key);
            }
//            if(setIsexit(jira_user_key,"project count_"+jira_user_key)){
//                SetReportBean bean = new SetReportBean();
//                bean.setJira_user_key(jira_user_key);
//                bean.setReportKey("project count_"+jira_user_key);
//                bean.setReportName("project count");
//                bean.setGroup_one("reporter");
//                bean.setGroup_two("project");
//    //            bean.setCreateDate("2018-04-25 10:07:43");
//                bean.setModelName("project count chart");
//                bean.setModel_show(9);
//                bean.setModelType(0);
//                bean.setStartLine(0);
//                bean.setEndLine(0);
//                bean.setInitType("init");
//                createReport(bean,jira_user_key);
//            }
            if(setIsexit(jira_user_key,"issueStatus_"+jira_user_key)){
                SetReportBean bean = new SetReportBean();
                bean.setJira_user_key(jira_user_key);
                bean.setReportKey("issueStatus_"+jira_user_key);
                bean.setReportName("issue状态");
                bean.setGroup_one("issue");
                bean.setGroup_two("project");
    //            bean.setCreateDate("2018-04-25 10:07:43");
                bean.setModelName("issueStatus");
                bean.setModel_show(9);
                bean.setModelType(0);
                bean.setStartLine(0);
                bean.setEndLine(0);
                bean.setInitType("init");
                createReport(bean,jira_user_key);
            }
            if(setIsexit(jira_user_key,"group by project_"+jira_user_key)){
                SetReportBean bean = new SetReportBean();
                bean.setJira_user_key(jira_user_key);
                bean.setReportKey("group by project_"+jira_user_key);
                bean.setReportName("group by project");
                bean.setGroup_one("project");
                bean.setGroup_two("reporter");
    //            bean.setCreateDate("2018-04-25 10:07:43");
                bean.setModelName("group by project cahrt");
                bean.setModel_show(9);
                bean.setModelType(0);
                bean.setStartLine(0);
                bean.setEndLine(0);
                bean.setInitType("init");
                createReport(bean,jira_user_key);
            }
        }
    }

    /**
     * 初始化报表列设置表
     */
    public void initReportColumn(){
        String jira_user_key = "admin";
        Set<ApplicationUser> lstu = userManager.getAllUsers();
        for (ApplicationUser u : lstu) {
            if (u.getId() == 10000 || u.getId() == 1) {
                jira_user_key = u.getKey();
                break;
            }
        }
        ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();

        if (!GlobalConfig.getIsDebug()) {
            if (loggedInUser == null) return;
            jira_user_key = loggedInUser.getKey();
        }

        if(1==1){
           // jira_user_key = loggedInUser.getKey();
            String reportkey = "timesheet_"+jira_user_key;
            SetReportColumn[] reportColumns = ao.find(SetReportColumn.class);
            for(SetReportColumn report : reportColumns){
                if(report.getInitType() != null && "init".equals(report.getInitType())){
                    ao.delete(report);
                }
            }

            if(columnIsexit(jira_user_key,reportkey)){
                ArrayList<SetReportColumnBean> lists = new ArrayList<>();
                SetReportColumnBean bean = getBean(jira_user_key,reportkey,"name");
                bean.setSequence("1");
                bean.setColumnWidth("200");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"workingHours");
                bean.setStatistics("1");
                bean.setSequence("1");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Key");
                bean.setSequence("3");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Time Spent");
                bean.setStatistics("1");
                bean.setSequence("6");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Report");
                bean.setSequence("2");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Votes");
                bean.setStatistics("1");
                bean.setSequence("4");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Watchers");
                bean.setStatistics("1");
                bean.setSequence("5");
                lists.add(bean);
                createColumn(lists,jira_user_key,reportkey);
            }
            reportkey = "project count_"+jira_user_key;
            if(columnIsexit(jira_user_key,reportkey)){
                ArrayList<SetReportColumnBean> lists = new ArrayList<>();
                SetReportColumnBean bean = getBean(jira_user_key,reportkey,"name");
                bean.setColumnWidth("200");
                bean.setSequence("1");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"countProject");
                bean.setStatistics("2");
                bean.setSequence("2");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Summary");
                bean.setSequence("6");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Key");
                bean.setStatistics("2");
                bean.setSequence("4");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Report");
                bean.setStatistics("2");
                bean.setSequence("5");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Time Spent");
                bean.setStatistics("1");
                bean.setSequence("3");
                lists.add(bean);
                createColumn(lists,jira_user_key,reportkey);
            }
            reportkey = "issueStatus_"+jira_user_key;
            if(columnIsexit(jira_user_key,reportkey)){
                ArrayList<SetReportColumnBean> lists = new ArrayList<>();
                SetReportColumnBean bean = getBean(jira_user_key,reportkey,"name");
                bean.setColumnWidth("200");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Summary");
                bean.setSequence("5");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"todo");
                bean.setStatistics("1");
                bean.setSequence("2");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Status");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"doing");
                bean.setStatistics("1");
                bean.setSequence("3");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"done");
                bean.setStatistics("1");
                bean.setSequence("4");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Resolution");
                bean.setSequence("6");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Created");
                bean.setSequence("7");
                lists.add(bean);
                createColumn(lists,jira_user_key,reportkey);
            }
            reportkey = "group by project_"+jira_user_key;
            if(columnIsexit(jira_user_key,reportkey)){
                ArrayList<SetReportColumnBean> lists = new ArrayList<>();
                SetReportColumnBean bean = getBean(jira_user_key,reportkey,"name");
                bean.setColumnWidth("200");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Summary");
                bean.setSequence("2");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Assignee");
                bean.setSequence("6");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Status");
                bean.setSequence("4");
                bean.setStatistics("2");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Due");
                bean.setSequence("5");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Resolution");
                bean.setSequence("7");
                lists.add(bean);
                bean = getBean(jira_user_key,reportkey,"Created");
                bean.setSequence("3");
                lists.add(bean);
                createColumn(lists,jira_user_key,reportkey);
            }
        }
    }

    /**
     * 初始化报表分享表
     */
    public void initShareReport(){
        String jira_user_key = "admin";
        Set<ApplicationUser> lstu = userManager.getAllUsers();
        for (ApplicationUser u : lstu) {
            if (u.getId() == 10000 || u.getId() == 1) {
                jira_user_key = u.getKey();
                break;
            }
        }
        ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        if (!GlobalConfig.getIsDebug()) {
            if (loggedInUser == null) return;
            jira_user_key = loggedInUser.getKey();
        }
        if(1==1){
            //jira_user_key = loggedInUser.getKey();
            String id1 = "";
            String id2 = "";
            RoleEntity[] entitys = ao.find(RoleEntity.class);
            for(RoleEntity entity : entitys){
                if("System Administrator".equals(entity.getName())){
                    id1 = ""+entity.getID();
                    continue;
                }
                if("Top Executives".equals(entity.getName())){
                    id2 = ""+entity.getID();
                    continue;
                }
            }
            ShareReport[] shareReports = ao.find(ShareReport.class);
            for(ShareReport report : shareReports){
                if(report.getInitType() != null && "init".equals(report.getInitType())){
                    ao.delete(report);
                }
            }
            if(shareIsexit(jira_user_key,"timesheet_"+jira_user_key,id1)){
                createShare(id1,"",jira_user_key,"timesheet_"+jira_user_key,"init");
            }
            if(shareIsexit(jira_user_key,"timesheet_"+jira_user_key,id2)){
                createShare(id2,"",jira_user_key,"timesheet_"+jira_user_key,"init");
            }
            if(shareIsexit(jira_user_key,"project count_"+jira_user_key,id1)){
                createShare(id1,"",jira_user_key,"project count_"+jira_user_key,"init");
            }
            if(shareIsexit(jira_user_key,"project count_"+jira_user_key,id2)){
                createShare(id2,"",jira_user_key,"project count_"+jira_user_key,"init");
            }
            if(shareIsexit(jira_user_key,"issueStatus_"+jira_user_key,id1)){
                createShare(id1,"",jira_user_key,"issueStatus_"+jira_user_key,"init");
            }
            if(shareIsexit(jira_user_key,"issueStatus_"+jira_user_key,id2)){
                createShare(id2,"",jira_user_key,"issueStatus_"+jira_user_key,"init");
            }
            if(shareIsexit(jira_user_key,"group by project_"+jira_user_key,id1)){
                createShare(id1,"",jira_user_key,"group by project_"+jira_user_key,"init");
            }
            if(shareIsexit(jira_user_key,"group by project_"+jira_user_key,id2)){
                createShare(id2,"",jira_user_key,"group by project_"+jira_user_key,"init");
            }
        }
    }

    /**
     * 判断该数据是否存在  true-不存在  false-存在
     * @param jira_user_key
     * @param reportkey
     * @return
     */
    public boolean columnIsexit(String jira_user_key,String reportkey){
//        SetReportColumn[] columns = ao.find(SetReportColumn.class,String.format(" %s = ? AND  %s = ? ","JIRA_USER_KEY","REPORT_KEY"),jira_user_key,reportkey);
        SetReportColumn[] columns = ao.find(SetReportColumn.class,String.format(" REPORT_KEY = '%s' ",reportkey));
        if(columns.length == 0){
            return true;
        }
        return false;
    }

    /**
     * 判断该分享报表是否存在  true-不存在  false-存在
     * @param jira_user_key
     * @param reportkey
     * @param orgRole
     * @return
     */
    public boolean shareIsexit(String jira_user_key,String reportkey,String orgRole){
//        ShareReport[] shareReports = ao.find(ShareReport.class,String.format(" %s = ? AND  %s = ? AND  %s = ? AND TYPE = 0","JIRA_USER_KEY","REPORT_KEY","EMP_ORG_ID"),jira_user_key,reportkey,orgRole);
        ShareReport[] shareReports = ao.find(ShareReport.class,String.format(" JIRA_USER_KEY = '%s' AND  REPORT_KEY = '%s' AND  EMP_ORG_ID = '%s' AND TYPE = 0",jira_user_key,reportkey,orgRole));
        if(shareReports.length == 0){
            return true;
        }
        return false;
    }


    public SetReportColumnBean getBean(String jira_user_key,String reportkey,String columnName){
        SetReportColumnBean bean = new SetReportColumnBean();
        bean.setJira_user_key(jira_user_key);
        bean.setReportKey(reportkey);
        bean.setColumnName(columnName);
        bean.setColumnWidth("100");
        bean.setStatistics("0");
        bean.setSequence("1");
        bean.setInitType("init");
        return bean;
    }


    /**
     * 获取组织角色
     * @return
     */
    public HashMap<String,ArrayList<SelectBean>> getOrgRole(){
        RoleEntity[] entities = ao.find(RoleEntity.class,String.format(" STATUS = 1 AND TYPE = 1 "));
        HashMap<String,ArrayList<SelectBean>> map = new HashMap<>();
        ArrayList<SelectBean> list = new ArrayList<>();
        SelectBean bean = new SelectBean();
        for(RoleEntity entity : entities){
            bean = new SelectBean();
            bean.setId(""+entity.getID());
            bean.setText(entity.getName());
            list.add(bean);
        }
        map.put("results",list);
        return map;
    }

    /**
     * 获取雇员
     * @return
     */
    public HashMap<String,ArrayList<SelectBean>> getEmployee(){
//        StrEmployee[] entities = ao.find(StrEmployee.class,String.format(" JIRA_USER_KEY <> %s "),GlobalReport.jira_user_key);
        StrEmployee[] entities = ao.find(StrEmployee.class);
        HashMap<String,ArrayList<SelectBean>> map = new HashMap<>();
        ArrayList<SelectBean> list = new ArrayList<>();
        SelectBean bean = new SelectBean();
        for(StrEmployee entity : entities){
            if(GlobalReport.jira_user_key.equals(entity.getJiraUserKey())){
                continue;
            }
            bean = new SelectBean();
            bean.setId(""+entity.getID());
            bean.setText(entity.getEmployeeName() + "(" + entity.getJiraUserKey() + ")");
            list.add(bean);
        }
        map.put("results",list);
        return map;
    }

    /**
     * 根据报表的key获取报表对应的分享数据
     * @param reportKey 报表的key
     * @return
     */
    public ArrayList<ShareReportBean> getShareReport(String reportKey){
        ShareReport[] entities = ao.find(ShareReport.class, Query.select().where(String.format(" REPORT_KEY = '%s' AND JIRA_USER_KEY =  '%s' ", reportKey,GlobalReport.jira_user_key )));
//        ShareReport[] entities = ao.find(ShareReport.class,String.format("REPORT_KEY = '%s'", reportKey));
        ArrayList<ShareReportBean> lists = new ArrayList<>();
        for(ShareReport entity : entities){
            ShareReportBean bean = new ShareReportBean();
            bean.setEmp_org_id(entity.getEmp_org_id());
            bean.setJira_user_key(entity.getJira_user_key());
            bean.setReportKey(entity.getReportKey());
            bean.setType(entity.getType());
            lists.add(bean);
        }
        return lists;
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

}
