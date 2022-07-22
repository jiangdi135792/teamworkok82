package com.work.plugin.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by admin on 2021/7/4.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BmryfbInfoBean {

    @XmlElement
    private String proName;

    @XmlElement
    private String issName;

    @XmlElement
    private String repName;

    @XmlElement
    private String repName1;

    @XmlElement
    private String undoName;

    @XmlElement
    private String doingName;

    @XmlElement
    private String doneName;

    @XmlElement
    private String issKey;

    @XmlElement
    private long proId;

    @XmlElement
    private long issId;

    @XmlElement
    private long repId;

    @XmlElement
    private long repId1;

    @XmlElement
    private long undoId;

    @XmlElement
    private long doingId;

    @XmlElement
    private long doneId;
    @XmlElement
    private String isKey;
}
