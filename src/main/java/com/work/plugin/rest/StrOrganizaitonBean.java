package com.work.plugin.rest;

import com.work.plugin.ao.DepartmentEntity;
import com.work.plugin.ao.StrOrganize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by admin on 2021/6/22.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrOrganizaitonBean {
	@XmlElement
	private String id;

	@XmlElement private String name;
	@XmlElement
	private String type;
	@XmlElement
	private String parent;
	@XmlElement
	private String status;
	@XmlElement
	private int orgLevel;
	@XmlElement
	private String mappingCode;
	@XmlElement
	private int character;
	@XmlElement
	private String memo;
	@XmlElement
	private int owner;
	@XmlElement
	private String createDate;
	@XmlElement
	private int modifier;
	@XmlElement
	private String modifierDate;

	@XmlElement
	private Integer teamId;
	@XmlElement
	private String projectKey;
	@XmlElement
	private Integer projectId;
	@XmlElement
	private String projectName;
	@XmlElement
	private String characters;
	public StrOrganizaitonBean(String id, String name, String parent) {
		this.id = id;
		this.name = name;
		this.parent = parent;
	}
	public StrOrganizaitonBean(int id, String name, int parent) {
		this.id = String.valueOf(id);
		this.name = name;
		this.parent = String.valueOf(parent);
	}
	public StrOrganizaitonBean(String id, String name, String parent, String type) {
		this(id, name, parent);
		this.type = type;
	}
	public StrOrganizaitonBean(String id, String name, String parent, String type,String characters,String test) {
		this(id, name, parent);
		this.type = type;
		this.characters = characters;
	}

	public StrOrganizaitonBean(StrOrganize entity) {
		try {
			BeanUtils.copyProperties(this, entity);
			this.id = String.valueOf(entity.getID());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public StrOrganizaitonBean(DepartmentEntity entity) {
		try {
			BeanUtils.copyProperties(this, entity);
			this.id = String.valueOf(entity.getID());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public StrOrganizaitonBean(String id,String type,String status, String name, String characters) {
		this.id=id;
		this.type = type;
		this.status = status;
		this.name = name;
		this.characters = characters;
	}

}
