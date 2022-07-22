package com.work.plugin.rest;

import com.work.plugin.ao.DepartmentEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.beanutils.BeanUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by work on 2021/6/21.
 */
@XmlRootElement
@NoArgsConstructor
public class DepartmentBean {
    @XmlElement
    @Getter @Setter private int id;

    @XmlElement
    @Getter @Setter
    private String groupNo;

    @XmlElement
    @Getter @Setter private String groupName;

    @XmlElement
    @Getter @Setter private String parent;

    @XmlElement
    @Getter @Setter private int status;

    @XmlElement
    @Getter @Setter private int type;

    @XmlElement
    @Getter @Setter private String mappingCode;

    @XmlElement
    @Getter @Setter private String memo;

    @XmlElement
    @Getter @Setter private String owner;

    @XmlElement
    @Getter @Setter private String createDate;


    @XmlElement
    @Getter @Setter private String modifier;

    @XmlElement
    @Getter @Setter private String modifierDate;

    @XmlElement
    @Getter @Setter private String statuss;
    @XmlElement
    @Getter @Setter private String types;

    public DepartmentBean (int id,String groupNo,String groupName,String parent,int status,int type,String mappingCode,
                           String memo,String createDate,String owner,String modifier, String modifierDate){

        this.id=id;
        this.groupNo=groupNo;
        this.groupName=groupName;
        this.parent=parent;
        this.status=status;
        this.type=type;
        this.mappingCode=mappingCode;
        this.memo=memo;
        this.owner=owner;
        this.createDate=createDate;
        this.modifier=modifier;
        this.modifierDate=modifierDate;
    }
    public DepartmentBean(DepartmentEntity entity) {
        try {
            BeanUtils.copyProperties(this, entity);
            this.id = entity.getID();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public DepartmentBean(int id,String groupNo,String groupName, String type, String status) {
        this.id=id;
        this.groupNo = groupNo;
        this.groupName = groupName;
        this.types = type;
        this.statuss = status;
    }
}
