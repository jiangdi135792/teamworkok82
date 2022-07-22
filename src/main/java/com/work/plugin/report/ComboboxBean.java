package com.work.plugin.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by admin on 2021/9/25.
 */
@XmlRootElement
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComboboxBean {

    @XmlElement private String comboboxValue;

}
