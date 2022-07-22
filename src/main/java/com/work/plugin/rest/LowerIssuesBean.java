package com.work.plugin.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by work on 2022/6/10.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowerIssuesBean {
    @XmlElement
    private int proNum;

    @XmlElement
    private int issNum;

    @XmlElement
    private int repNum;

    @XmlElement
    private int repNum1;

}
