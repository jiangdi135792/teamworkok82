package com.work.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.schema.StringLength;

/**
 * Created by admin on 2021/6/21.
 */
public interface StrOrganize extends Entity {
	String getName();
	void setName(String name);

	@StringLength(1)
	String getType();
	void setType(String type);

	Integer getParent();
	void setParent(Integer parent);

	String getStatus();
	void setStatus(String status);

	Integer getOrgLevel();
	void setOrgLevel(Integer orgLevel);

	String getMappingCode();
	void setMappingCode(String mappingCode);

	Integer getCharacter();
	void setCharacter(Integer character);

	String getMemo();
	void setMemo(String memo);

	Integer getOwner();
	void setOwner(Integer owner);
	/*2018年2月26日17:57:24*/
	long getLdapId();
	void setLdapId(long LdapId);
/*2018年2月26日17:57:24*/

	@StringLength(20)
	String getCreateDate();
	void setCreateDate(String createDate);

	Integer getModifier();
	void setModifier(int modifier);

	@StringLength(20)
	String getModifierDate();
	void setModifierDate(String modifierDate);

	@OneToMany
	StrOrganizeGroup[] getStrOrganizeGroup();

	@OneToMany
	StrEmployee[] getStrEmployee();

	enum COLUMN {
		ID, NAME,TYPE,PARENT,STATUS,ORG_LEVEL,CHARACTER,
		CREATE_DATE,MAPPING_CODE,MEMO,MODIFIER,MODIFIER_DATE,OWNER;

		COLUMN(){}
	}
}
