package com.work.plugin.ao;

/**
 * Created by work on 2021/6/21.
 */

import net.java.ao.Entity;

/**
 * Created by work on 2021/6/21.
 */
public interface StrOrganizeGroup extends Entity {

//	Integer getGroupId();
//
//	void setGroupId(Integer groupId);
	StrOrganize getOrg();

	void setOrg(StrOrganize org);

	DepartmentEntity getGroup();
	void setGroup(DepartmentEntity group);

//	Integer getOrgId();
//
//	void setOrgId(Integer orgId);

	Integer getType();

	void setType(Integer type);

	String getStatus();

	void setStatus(String status);

	String getMemo();

	void setMemo(String memo);

	Integer getOwner();

	void setOwner(Integer owner);

	String getCreateDate();

	void setCreateDate(String createDate);

	Integer getModifier();

	void setModifier(Integer modifier);

    enum COLUMN {
    	ID,  ORG_ID,GROUP_ID, TYPE, STATUS, MEMO, MODIFIER, MODIFIER_DATE, OWNER;

        COLUMN() {}
    }
}
