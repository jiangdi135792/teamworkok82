package com.work.plugin.rest;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by admin on 2021/7/4.
 */
@XmlRootElement
@Data

public class ReportJoinBean {

    @XmlElement
    private int proNum;

    @XmlElement
    private int issNum;

    @XmlElement
    private int repNum;

    @XmlElement
    private int repNum1;
    public void StrEmployeeBean(){}
    public ReportJoinBean(int proNum, int issNum, int repNum, int repNum1) {
        this.proNum = proNum;
        this.issNum = issNum;
        this.repNum = repNum;
        this.repNum1 = repNum1;
    }
}
