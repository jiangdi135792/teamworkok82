package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

/**
 * Created by admin on 2021/6/21.
 * @see DepartmentEntity
 */
@Deprecated
@Table("STR_GROUPS")
public interface StrGroups extends Entity {
	int getGroupNo();
	void setGroupNo(int groupNo);

	@StringLength(50)
	String getGroupName();
	void setGroupName(String groupName);

	int getParent();
	void setParent(int parent);

	int getStatus();
	void setStatus(int status);

	int getType();
	void setType(int type);

	String getMappingCode();
	void setMappingCode(String mappingCode);

	String getMemo();
	void setMemo(String memo);

	int getOwner();
	void setOwner(int owner);

	@StringLength(20)
	String getCreateDate();
	void setCreateDate(String createDate);

	int getStamodifiertus();
	void setStamodifiertus(int stamodifiertus);

	@StringLength(20)
	String getModifierDate();
	void setModifierDate(String modifierDate);
}
