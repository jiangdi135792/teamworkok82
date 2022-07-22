package com.work.plugin.rest;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by admin on 2021/7/21.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportJoinChildrenBean {

    @XmlElement
    private String name;

    @XmlElement
    private List<ReportJoinChildrenBean> childrens;

    @XmlElement
    private ReportJoinBean bmryfb;

    @XmlElement
    private int type;

    @XmlElement
    private int pid;

}
