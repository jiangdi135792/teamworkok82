package com.work.plugin.rest;

import com.work.plugin.ao.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by work on 2022/3/26.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleBean {

    @XmlElement
    private int id;
    @XmlElement
        private int type;

    @XmlElement
    private int status;

    @XmlElement
    private int parentId;

    @XmlElement
    private String name;

    @XmlElement
    private String desc;

    @XmlElement
    private int order;

    public RoleBean(RoleEntity roleEntity) {
        try {
            BeanUtils.copyProperties(this, roleEntity);
            this.id=roleEntity.getID();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public RoleBean(RoleBean entity) {
        try {
            BeanUtils.copyProperties(this, entity);
            this.id = entity.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public RoleBean(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
