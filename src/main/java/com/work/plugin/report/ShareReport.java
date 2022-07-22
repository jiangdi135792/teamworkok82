package com.work.plugin.report;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

/**
 * Created by admin on 2022/3/28.
 */
@Table("SHAREREPORT")
public interface ShareReport extends Entity{

	String getReportKey();
	void setReportKey(String reportKey);

	String getJira_user_key();
	void setJira_user_key(String jira_user_key);

	int getType();
	void setType(int type);

	int getEmp_org_id();
	void setEmp_org_id(int emp_org_id);

	String getInitType();
	void setInitType(String initType);
}
