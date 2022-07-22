package com.work.plugin.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by work on 2022/3/28.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupBean {
    @XmlElement
    private String groupName;
}
