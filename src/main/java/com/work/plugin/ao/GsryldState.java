package com.work.plugin.ao;

import net.java.ao.Entity;

/**
 * Created by admin on 2021/7/5.
 */
public interface GsryldState extends Entity{

	int getEntNum();
	void setEntNum(int entNum);

	int getDimNum();
	void setDimNum(int dimNum);

	double getDimRate();
	void setDimRate(double dimRate);

	int getEntTotalNum();
	void setEntTotalNum(int entTotalNum);

	int getDimTotalNum();
	void setDimTotalNum(int dimTotalNum);

	double getDimTotalRate();
	void setDimTotalRate(double dimTotalRate);

	String getName();
	void setName(int Name);
//
//	String getOrgName();
//	void setOrgName(int orgName);

}
