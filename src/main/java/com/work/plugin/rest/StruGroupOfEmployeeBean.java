package com.work.plugin.rest;

import com.work.plugin.ao.DepartmentEntity;
import com.work.plugin.ao.StrEmployee;
import com.work.plugin.ao.StrEmployeeServiceImpl;
import com.work.plugin.ao.StruGroupOfEmployee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by admin on 2021/7/5.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StruGroupOfEmployeeBean {

    @XmlElement
    private int id;

    @XmlElement
    private int groupId;

    @XmlElement
    private String groupName; // 部门名称

    @XmlElement
    private int employeeId;
    @XmlElement
    private String email;//电邮
    @XmlElement
    private String employeeName;//姓名
    @XmlElement
    private String employeeNo;//工号
    @XmlElement
    private String jiraUserName;//Jira用户�?
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
    private String employmentStatus;//在职状�?
    @XmlElement
    private String entryTime;//入职时间
    @XmlElement
    private String leaveTime;//离职时间
    @XmlElement
    private String memo;//备注

    @XmlElement
    private int teamId;
    @XmlElement
    private String teamName;
    @XmlElement
    private String roleNames;
    @XmlElement
    private String roleIds;

//    @XmlElement
//    private int owner;
//
//    @XmlElement
//    private String createDate;
//
//    @XmlElement
//    private int modifier;
//
//    @XmlElement
//    private String modifierDate;

    public  StruGroupOfEmployeeBean(StruGroupOfEmployee struGroupOfEmployee) {
        try {
            StrEmployee employee = struGroupOfEmployee.getEmployee();
            BeanUtils.copyProperties(this, employee);
            DepartmentEntity team = struGroupOfEmployee.getGroup();
            BeanUtils.copyProperties(this, team);
            BeanUtils.copyProperties(this, struGroupOfEmployee);
            this.setTeamId(team.getID());
            this.setTeamName(team.getGroupName());
            this.setEmployeeId(employee.getID());
            StruGroupOfEmployee groupMember = StrEmployeeServiceImpl.getGroupMember(employee.getStruGroupOfEmployee());
            if (groupMember != null) {
                DepartmentEntity group = groupMember.getGroup();
                this.setGroupId(group.getID());
                this.setGroupName(group.getGroupName());
            }

            this.setId(struGroupOfEmployee.getID());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
