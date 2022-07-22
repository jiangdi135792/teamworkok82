package com.work.plugin.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by admin on 2022/3/28.
 * 设置展示报表所需要的条件
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetReportBean {

    @XmlElement
    private String reportName;//报表名称

    @XmlElement
    private String reportKey;//报表的Key

    @XmlElement
    private String jira_user_key;//创建人的jira用户key

    @XmlElement
    private String group_one;//报表的第一分组

    @XmlElement
    private String group_two;//报表的第二分组

    @XmlElement
    private String modelName;//图表名称

    @XmlElement
    private int modelType;//图表类型

    @XmlElement
    private int model_show;//图表是否显示

    @XmlElement
    private int startLine;//开始行

    @XmlElement
    private int endLine;//结束行

    @XmlElement
    private String createDate;//创建时间

    @XmlElement
    private String initType;//判断是否为初始化数据

    public SetReportBean(SetReport entity){
        this.reportName = entity.getReportName();
        this.reportKey = entity.getReportKey();
        this.jira_user_key = entity.getJira_user_key();
        this.group_one = entity.getGroup_one();
        this.group_two = entity.getGroup_two();
        this.modelName = entity.getModelName();
        this.modelType = entity.getModelType();
        this.model_show = entity.getShow();
        this.startLine = entity.getStartLine();
        this.endLine = entity.getEndLine();
        this.createDate = entity.getCreateDate();
        this.initType = entity.getInitType();
    }

}
