package com.work.plugin.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by admin on 2021/3/28.
 * 设置展示报表的issue展示的列
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetReportColumnBean {

    @XmlElement
    private String columnName;//issue所展示的列

    @XmlElement
    private String reportKey;//报表的Key

    @XmlElement
    private String jira_user_key;//创建人的jira用户key

    @XmlElement
    private String columnWidth;//此列的宽度

    @XmlElement
    private String sequence;//顺序，列按照此顺序显示

    @XmlElement
    private String statistics;//统计，0不做操作，1计数即count，2合计即sum

    @XmlElement
    private String initType;//判断是否为初始化数据

    public SetReportColumnBean(SetReportColumn entity){
        this.jira_user_key = entity.getJira_user_key();
        this.columnName = entity.getColumnName();
        this.reportKey = entity.getReportKey();
        this.columnWidth = entity.getColumnWidth();
        this.sequence = entity.getSequence();
        this.statistics = entity.getStatistics();
        this.initType = entity.getInitType();
    }


}
