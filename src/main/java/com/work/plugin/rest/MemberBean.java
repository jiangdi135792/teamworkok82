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
public class MemberBean {

    @XmlElement
    @Getter @Setter private int id;

    @XmlElement
    @Getter @Setter private String userKey;

    @XmlElement
    @Getter @Setter private String menu_id;

    @XmlElement
    @Getter @Setter private int type;

}