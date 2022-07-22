package com.work.plugin.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by admin on 2021/9/25.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComboTreeModel {

    
    @XmlElement private int id;

    @XmlElement private String text;

    @XmlElement private String type;

    @XmlElement private List<ComboTreeModel> children;

}
