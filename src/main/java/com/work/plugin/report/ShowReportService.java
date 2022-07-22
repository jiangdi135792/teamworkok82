package com.work.plugin.report;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.jira.issue.Issue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2022/3/28.
 */
@Transactional
public interface ShowReportService {

//    ReportInfoBean all(String startTime,String endTime,String startTime_plan,String endTime_plan,String reportKey);
    ReportInfoBean all();

    /**
     *  以 项目为根  树状图
     * @param proName  项目 name  可以为空   demo: 项目1,项目2
     * @param argFiled  assignee 或者 reporter   默认为 reporter
     * @return
     */
    ReportInfoBean getNewTypeAll(String proName,String argFiled);

    HashMap<String,ArrayList<SelectBean>> getProName();
    Map<String, List> getIssueStatuses(List<Issue> issueList);

}
