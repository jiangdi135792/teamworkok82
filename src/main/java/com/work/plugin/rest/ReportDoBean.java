package com.work.plugin.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by admin on 2021/7/4.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDoBean {

    @XmlElement
    private int proNum;

    @XmlElement
    private int undoNum;

    @XmlElement
    private int doingNum;

    @XmlElement
    private int doneNum;

    @XmlElement
    private int otherNum;

}
