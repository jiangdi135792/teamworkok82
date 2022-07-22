package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

/**
 * Created by admin on 2021/6/21.
 * Update by admin on 2021/6/28.
 */
@Table("EMPLOYEE_ENTITY")
public interface StrEmployee extends Entity{

	String getEmail();
	void setEmail(String email);

	String getEmployeeName();
	void setEmployeeName(String employeeName);

	@StringLength(50)
	String getEmployeeNo();
	void setEmployeeNo(String employeeNo);

	String getJiraUserName();
	void setJiraUserName(String jiraUserName);

	String getJiraUserKey();
	void setJiraUserKey(String jiraUserKey);

	int getJiraId();
	void setJiraId(int JiraId);

	@StringLength(1)
	String getEmployeeSex();
	void setEmployeeSex(String employeeSex);

	@StringLength(50)
	String getPhone();
	void setPhone(String phone);

	@StringLength(50)
	String getOtherPhone();
	void setOtherPhone(String otherPhone);

	@StringLength(1)
	String getEmploymentStatus();
	void setEmploymentStatus(String employmentStatus);
/*2018年2月26日17:57:24*/
	long getLdapId();
	void setLdapId(long LdapId);
/*2018年2月26日17:57:24*/
	@StringLength(20)
	String getEntryTime();
	void setEntryTime(String entryTime);

	@StringLength(20)
	String getLeaveTime();
	void setLeaveTime(String leaveTime);

	String getMemo();
	void setMemo(String memo);

	String getOwner();
	void setOwner(String owner);

	@StringLength(20)
	String getCreateDate();
	void setCreateDate(String createDate);

	String getModifier();
	void setModifier(String modifier);

	@StringLength(20)
	String getModifierDate();
	void setModifierDate(String modifierDate);

	Integer getSupervisor(); //直接上司
	void setSupervisor(Integer supervisor);

	StrOrganize getStrOrganize(); // 员工 所属的组织机构
	void setStrOrganize(StrOrganize strOrganize);

	@OneToMany
	StruGroupOfEmployee[] getStruGroupOfEmployee();

	@OneToMany
	StrEmployeeOfRole[] getStrEmployeeOfRole();
}
