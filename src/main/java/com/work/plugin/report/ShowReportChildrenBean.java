package com.work.plugin.report;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;

/**
 * Created by admin on 2021/7/21.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowReportChildrenBean {

    @XmlElement
    private HashMap<String,String> map;

    @XmlElement
    private List<ShowReportChildrenBean> children;


}
