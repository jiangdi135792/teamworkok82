package com.work.plugin.ao;

import net.java.ao.Entity;

/**
 * Created by admin on 2021/7/5.
 */
public interface ReportJoin extends Entity{

	int getProNum();
	void setProNum(int proNum);

	int getIssNum();
	void setIssNum(int issNum);

	int getRepNum();
	void setRepNum(int repNum);

	int getRepNum1();
	void setRepNum1(int repNum1);

	String getName();
	void setName(String name);

}
