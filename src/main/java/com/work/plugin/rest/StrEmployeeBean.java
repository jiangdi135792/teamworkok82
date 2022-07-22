package com.work.plugin.rest;

import com.work.plugin.ao.*;
import lombok.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by admin on 2021/6/21.
 * Update by admin on 2021/6/28.
 */
@XmlRootElement
@Data
@NoArgsConstructor
//@AllArgsConstructor
public class StrEmployeeBean {
	@XmlElement
	private int id;//编号
	@XmlElement
	private int struGroupEmployeeId;

	@XmlElement
	private String email;//电邮
	@XmlElement
	private String employeeName;//姓名
	@XmlElement
	private String employeeNo;//工号
	@XmlElement
	private String jiraUserName;//Jira用户名
	@XmlElement
	private int jiraId;//Jira用户编号
	@XmlElement
	private String jiraUserKey;//Jira用户key
	@XmlElement
	private String employeeSex;//性别
	@XmlElement
	private String phone;//联系电话
	@XmlElement
	private String otherPhone;//其它联系电话
	@XmlElement
	private String employmentStatus;//在职状态
	@XmlElement
	private String entryTime;//入职时间
	@XmlElement
	private String leaveTime;//离职时间
	@XmlElement
	private String memo;//备注
	@XmlElement
	private String owner;//数据拥有者，指第一次的创建人
	@XmlElement
	private String createDate;//创建时间
	@XmlElement
	private String modifier;//修改人员
	@XmlElement
	private String modifierDate;//最后修改时间
	@XmlElement
	private String groupId; // 部门id

	@XmlElement
	private String groupName; // 部门名称
	@XmlElement
	private Integer orgId; // 组织id
	@XmlElement
	private String orgName; // 组织名称

	@XmlElement
	private Integer supervisor; // 直接上司

	@XmlElement
	private String roleId; // 角色ID

	@XmlElement
	private String supName; // 上级名字

	public void StrEmployeeBean(){}

	public StrEmployeeBean(int id, int struGroupEmployeeId, String email, String employeeName, String employeeNo, String jiraUserName, int jiraId, String jiraUserKey, String employeeSex, String phone, String otherPhone, String employmentStatus, String entryTime, String leaveTime, String memo, String owner, String createDate, String modifier, String modifierDate, String groupId, String groupName, Integer orgId, String orgName, Integer supervisor, String roleId, String supName) {
		this.id = id;
		this.struGroupEmployeeId = struGroupEmployeeId;
		this.email = email;
		this.employeeName = employeeName;
		this.employeeNo = employeeNo;
		this.jiraUserName = jiraUserName;
		this.jiraId = jiraId;
		this.jiraUserKey = jiraUserKey;
		this.employeeSex = employeeSex;
		this.phone = phone;
		this.otherPhone = otherPhone;
		this.employmentStatus = employmentStatus;
		this.entryTime = entryTime;
		this.leaveTime = leaveTime;
		this.memo = memo;
		this.owner = owner;
		this.createDate = createDate;
		this.modifier = modifier;
		this.modifierDate = modifierDate;
		this.groupId = groupId;
		this.groupName = groupName;
		this.orgId = orgId;
		this.orgName = orgName;
		this.supervisor = supervisor;
		this.roleId = roleId;
		this.supName = supName;
	}

	public StrEmployeeBean(StrEmployee strEmployee) {
		this.id = strEmployee.getID();
		this.email = strEmployee.getEmail();
		this.employeeName = strEmployee.getEmployeeName();
		this.employeeNo = strEmployee.getEmployeeNo();
		this.jiraUserName = strEmployee.getJiraUserName();
		this.jiraId = strEmployee.getJiraId();
		this.jiraUserKey = strEmployee.getJiraUserKey();
		this.employeeSex = strEmployee.getEmployeeSex();
		this.phone = strEmployee.getPhone();
		this.otherPhone = strEmployee.getOtherPhone();
		this.employmentStatus = strEmployee.getEmploymentStatus();
		this.entryTime = strEmployee.getEntryTime();
		this.leaveTime = strEmployee.getLeaveTime();
		this.memo = strEmployee.getMemo();
		this.owner = strEmployee.getOwner();
		this.createDate = strEmployee.getCreateDate();
		this.modifier = strEmployee.getModifier();
		this.modifierDate = strEmployee.getModifierDate();
		StruGroupOfEmployee struGroupOfEmployee = StrEmployeeServiceImpl.getGroupMember(strEmployee.getStruGroupOfEmployee());
		if (struGroupOfEmployee != null) {
			this.struGroupEmployeeId = struGroupOfEmployee.getID();
			DepartmentEntity group = struGroupOfEmployee.getGroup();
			if (group != null) {
				this.groupId = String.valueOf(group.getID());
				this.groupName = group.getGroupName();
			}
		}
		StrOrganize strOrganize = strEmployee.getStrOrganize();
		if (strOrganize != null) {
			this.orgId = strOrganize.getID();
			this.orgName = strOrganize.getName();
		}
		this.supervisor = strEmployee.getSupervisor();
        this.roleId =StrEmployeeServiceImpl.getRoleId(strEmployee.getStrEmployeeOfRole());
	}
	public StrEmployeeBean(int id,String employeeName,String employeeNo,String email,String employeeSex,String phone,String employmentStatus,String entryTime,String orgName,String groupName,String supName,Integer supervisor) {
		this.id=id;
		this.employeeName=employeeName;
		this.employeeNo=employeeNo;
		this.email=email;
		this.employeeSex=employeeSex;
		this.phone=phone;
		this.employmentStatus=employmentStatus;
		this.entryTime=entryTime;
		this.orgName=orgName;
		this.groupName=groupName;
		this.supName=supName;
		this.supervisor=supervisor;
	}

}

