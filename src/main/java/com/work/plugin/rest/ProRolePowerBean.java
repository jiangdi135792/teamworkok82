package com.work.plugin.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by work on 2022/7/16.
 */
@XmlRootElement
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProRolePowerBean {
    @XmlElement
    private int id;

    @XmlElement
    private boolean exit;

    @XmlElement
    private String name;
}
