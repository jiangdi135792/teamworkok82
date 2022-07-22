package com.work.plugin.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by work on 2022/6/10.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowerIssuesChildBean {

    @XmlElement
    private String name;

    @XmlElement
    private List<LowerIssuesChildBean> childrens;

    @XmlElement
    private LowerIssuesBean lowerIssuesBean;

    @XmlElement
    private int type;

    @XmlElement
    private int pid;
}
