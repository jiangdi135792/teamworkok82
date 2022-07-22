package com.work.plugin.report;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

/**
 * Created by admin on 2022/3/28.
 */
@Table("SETREPORTCOLUMN")
public interface SetReportColumn extends Entity{

	String getJira_user_key();
	void setJira_user_key(String jira_user_key);

	String getColumnName();
	void setColumnName(String columnName);

	String getReportKey();
	void setReportKey(String reportKey);

	String getColumnWidth();
	void setColumnWidth(String columnWidth);

	String getSequence();
	void setSequence(String getSequence);

	String getStatistics();
	void setStatistics(String statistics);

	String getInitType();
	void setInitType(String initType);
}
