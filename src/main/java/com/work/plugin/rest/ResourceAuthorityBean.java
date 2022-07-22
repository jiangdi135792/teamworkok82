package com.work.plugin.rest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Administrator on 2021/7/4.
 */
@XmlRootElement
@NoArgsConstructor
public class ResourceAuthorityBean {
    @XmlElement
    @Getter
    @Setter
    private int id;

    @XmlElement
    @Getter @Setter private int FunctionId;

    @XmlElement
    @Getter @Setter private int UserId;

    @XmlElement
    @Getter @Setter private int Type;

    public ResourceAuthorityBean(int id, int FunctionId, int UserId, int Type){
        this.id=id;
        this.FunctionId=FunctionId;
        this.UserId=UserId;
        this.Type=Type;

    }
}
