package com.work.plugin.report;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

/**
 * Created by admin on 2022/3/28.
 */
@Table("SETREPORT")
public interface SetReport extends Entity{

	String getReportName();
	void setReportName(String reportName);

	String getReportKey();
	void setReportKey(String reportKey);

	String getJira_user_key();
	void setJira_user_key(String jira_user_key);

	String getGroup_one();
	void setGroup_one(String group_one);

	String getGroup_two();
	void setGroup_two(String group_two);

	String getModelName();
	void setModelName(String modelName);

	int getModelType();
	void setModelType(int modelType);

	int getShow();
	void setShow(int show);

	int getStartLine();
	void setStartLine(int startLine);

	int getEndLine();
	void setEndLine(int endLine);

	String getCreateDate();
	void setCreateDate(String createDate);

	String getInitType();
	void setInitType(String initType);

}
