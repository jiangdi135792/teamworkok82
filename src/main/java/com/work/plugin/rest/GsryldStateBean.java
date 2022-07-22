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
public class GsryldStateBean {

//    @XmlElement
//    private String orgName;
//
    @XmlElement
    private String name;

    @XmlElement
    private int entNum;

    @XmlElement
    private int dimNum;

    @XmlElement
    private double dimRate;

    @XmlElement
    private int entTotalNum;

    @XmlElement
    private int dimTotalNum;

    @XmlElement
    private double dimTotalRate;

}
