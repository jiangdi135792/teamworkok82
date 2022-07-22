package com.work.plugin.report;

import com.atlassian.activeobjects.tx.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by admin on 2022/3/28.
 */
@Transactional
public interface SetReportService {

    ArrayList<ComboboxBean> getInitColumn();

    List<SetReportColumnBean> getColumn(String jira_user_key,String reportKey,String type);

    List<SetReportBean> getSetReport(String jira_user_key,String type);

    void createReport(SetReportBean bean,String jira_user_key);

    void createColumn(ArrayList<SetReportColumnBean> lists,String jira_user_key,String reportKey);

    void createShare(String orgRole,String employee,String jira_user_key,String reportKey,String initType);

    void updateReport(SetReportBean bean,String jira_user_key);

    void updateColumn(ArrayList<SetReportColumnBean> lists,String jira_user_key,String reportKey);

    void updateShare(String orgRole,String employee,String jira_user_key,String reportKey);

    void deleteReport(String reportKey,String jira_user_key);

    boolean reportNameIsExist(String reportName,String jira_user_key);

    boolean reportKeyIsExist(String reportKey,String jira_user_key);

    ArrayList<GroupTwoBean> getGrouptwo(String groupone);

    SetReportBean getReportByReportkey(String jira_user_key,String reportKey);

    void initReport();

    void initReportColumn();

    void initShareReport();

    HashMap<String,ArrayList<SelectBean>> getOrgRole();

    HashMap<String,ArrayList<SelectBean>> getEmployee();

    ArrayList<ShareReportBean> getShareReport(String reportKey);

}
