package com.work.plugin.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by work on 2022/3/29.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraUserBean {
    @XmlElement
    private String userName;
    @XmlElement
    private String password;
    @XmlElement
    private String displayName;
    @XmlElement
    private String emailAddress;
}
