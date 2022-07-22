package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

/**
 * Created by admin on 2021/7/6.
 */
@Table("STR_GROUP_EMPLOYEE")
public interface StruGroupOfEmployee extends Entity {

	DepartmentEntity getGroup();

	void setGroup(DepartmentEntity group);

	StrEmployee getEmployee();

	void setEmployee(StrEmployee e);

	String getEmployeePostLevel();

	void setEmployeePostLevel(String employeePostLevel);

	int getGroupAdmin();

	void setGroupAdmin(int GroupAdmin);

	int getPostType();// type = 0 dept ;type = 1 team

	void setPostType(int postType);

	String getMemo();

	void setMemo(String memo);

	int getOwner();

	void setOwner(int owner);

	String getCreateDate();

	void setCreateDate(String createDate);

	int getModifier();

	void setModifier(int modifier);

	String getModifierDate();

	void setModifierDate(String modifierDate);


}
