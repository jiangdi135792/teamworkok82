package com.work.plugin.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by admin on 2022/3/28.
 * 返回报表信息的对象
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportInfoBean {

    @XmlElement
    private ArrayList<HashMap<String,String>> lists;

}
