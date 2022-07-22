package com.work.plugin.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by admin on 2021/9/13.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LdapImportBean {
    //必须-------------------------------------------------------
    @XmlElement
    private String employeeName;//员工名
    @XmlElement
    private String email;       //邮箱
    @XmlElement
    private String orgName; //公司名

    //不必须-------------------------------------------------------
    @XmlElement
    private Integer orgId;  //导入数据中的公司id
    @XmlElement
    private Integer parentOrgId;    //导入数据中的父公司id
    @XmlElement
    private String departmentName;      //部门名
    @XmlElement
    private Integer departmentId;       //导入数据中的部门id
    @XmlElement
    private Integer parentDepartmentId; //导入数据中的父部门id

    //其他-----------------员工----------------------
    @XmlElement
    private String employeeStatus = "1"; //员工状态 默认为1 在职 0 不在职
    @XmlElement
    private String employeeSex;//性别 1男2女
    @XmlElement
    private String phone;//员工手机
    @XmlElement
    private String entryTime;//入职时间
    @XmlElement
    private String jiraUserKey;

}
