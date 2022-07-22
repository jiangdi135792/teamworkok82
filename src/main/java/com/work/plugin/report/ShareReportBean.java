package com.work.plugin.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by admin on 2022/3/28.
 * 将报表分享给某人和某角色可以看
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareReportBean {

    @XmlElement
    private String reportKey;//报表的Key

    @XmlElement
    private String jira_user_key;//jira用户的Key

    @XmlElement
    private int type;//类型，表示分享给人(雇员 - 0)还是角色(组织角色 - 1)

    @XmlElement
    private int emp_org_id;//雇员或组织角色的id

    @XmlElement
    private String initType;//判断是否为初始化数据

    public ShareReportBean(ShareReport entity){
        this.reportKey = entity.getReportKey();
        this.jira_user_key = entity.getJira_user_key();
        this.type = entity.getType();
        this.emp_org_id = entity.getEmp_org_id();
        this.initType = entity.getInitType();
    }

}
