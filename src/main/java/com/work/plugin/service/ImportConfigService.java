package com.work.plugin.service;

import com.work.plugin.imports.field.DepartmentField;
import com.work.plugin.imports.field.EmployeeField;
import com.work.plugin.imports.field.OrganizeField;

import java.util.Optional;

/**
 * 导入配置服务组件
 *
 * @author Masato Morita
 */
public interface ImportConfigService
{
    /**
     *
     * 取得工员字段
     *
     * @param number 字段列数
     * @return 可选工员字段
     */
    Optional<EmployeeField> getEmployeeField(int number);

    /**
     *
     * 取得工员字段列数
     *
     * @param field 工员字段
     * @return 可选字段列数
     */
    Optional<Integer> getEmployeeFieldNumber(EmployeeField field);

    /**
     *
     * 取得部门字段
     *
     * @param number 字段列数
     * @return 可选部门字段
     */
    Optional<DepartmentField> getDepartmentField(int number);

    /**
     *
     * 取得部门字段列数
     *
     * @param field 部门字段
     * @return 可选字段列数
     */
    Optional<Integer> getDepartmentFieldNumber(DepartmentField field);

    /**
     *
     * 取得机构字段
     *
     * @param number 字段列数
     * @return 可选机构字段
     */
    Optional<OrganizeField> getOrganizeField(int number);

    /**
     *
     * 取得机构字段列数
     *
     * @param field 机构字段
     * @return 可选字段列数
     */
    Optional<Integer> getOrganizeFieldNumber(OrganizeField field);
}
