package com.work.plugin.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationBean {
    @XmlElement
    @Getter @Setter private int id;

    @XmlElement
    @Getter @Setter private String function1;

    @XmlElement
    @Getter @Setter private String name;

    @XmlElement
    @Getter @Setter private String menuNameid;

    @XmlElement
    @Getter @Setter private int roleCount;
    @XmlElement
    @Getter @Setter private int memberCount;
}