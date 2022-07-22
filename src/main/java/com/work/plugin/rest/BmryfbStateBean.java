package com.work.plugin.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by admin on 2021/7/4.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BmryfbStateBean {

    @XmlElement
    private int proNum;

    @XmlElement
    private int issNum;

    @XmlElement
    private int repNum;

    @XmlElement
    private int repNum1;

    @XmlElement
    private int undoNum;

    @XmlElement
    private int doingNum;

    @XmlElement
    private int doneNum;

    @XmlElement
    private List<BmryfbInfoBean> infos;
}
