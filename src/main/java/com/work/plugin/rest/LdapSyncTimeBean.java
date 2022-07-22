package com.work.plugin.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by work on 2022/2/3.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LdapSyncTimeBean {
    @XmlElement
    private int id;
    @XmlElement
    private long directoryID;
    @XmlElement
    private int syncTime;
}
