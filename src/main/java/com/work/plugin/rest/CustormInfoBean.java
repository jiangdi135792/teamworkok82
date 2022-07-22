package com.work.plugin.rest;

import lombok.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by work on 2022/3/29.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustormInfoBean {
    @XmlElement
    private String email;

    @XmlElement
    private String name;
    @XmlElement
    private Integer customId;
}
