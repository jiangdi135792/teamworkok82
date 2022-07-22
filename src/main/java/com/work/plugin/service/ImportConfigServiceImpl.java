package com.work.plugin.service;

import com.work.plugin.imports.field.DepartmentField;
import com.work.plugin.imports.field.EmployeeField;
import com.work.plugin.imports.field.OrganizeField;

import java.util.Optional;

/**
 * 导入配置服务组件实装
 *
 * @author Masato Morita
 */
public class ImportConfigServiceImpl implements ImportConfigService
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<EmployeeField> getEmployeeField(int number)
    {
        switch (number)
        {
            case 1:
                return Optional.of(EmployeeField.NAME);
            case 2:
                return Optional.of(EmployeeField.ORGNAME);
            case 3:
                return Optional.of(EmployeeField.EMAIL);
            case 4:
                return Optional.of(EmployeeField.DEPARTNAME);
            case 5:
                return Optional.of(EmployeeField.JIRA_USER_KEY);
            case 6:
                return Optional.of(EmployeeField.USERID);
            case 7:
                return Optional.of(EmployeeField.USERPARENTID);
            case 8:
                return Optional.of(EmployeeField.ORGID);
            case 9:
                return Optional.of(EmployeeField.ORGPARENTID);
            case 10:
                return Optional.of(EmployeeField.DEPARTID);
            case 11:
                return Optional.of(EmployeeField.DEPAPARENTID);
            default:
                return Optional.empty();
        }
    }

    /**
     * 取得工员字段列数
     *
     * @param field 工员字段
     * @return 可选字段列数
     */
    @Override
    public Optional<Integer> getEmployeeFieldNumber(EmployeeField field)
    {
        switch (field)
        {
            case EMAIL:
                return Optional.of(1);
            case NAME:
                return Optional.of(2);
            case JIRA_USER_KEY:
                return Optional.of(3);
            case PHONE:
                return Optional.of(4);
            case DEPARTMENT_ID:
                return Optional.of(5);
            case ENTRY_TIME:
                return Optional.of(6);
            default:
                return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DepartmentField> getDepartmentField(int number)
    {
        switch (number)
        {
            case 1:
                return Optional.of(DepartmentField.ID);
            case 2:
                return Optional.of(DepartmentField.NAME);
            case 3:
                return Optional.of(DepartmentField.DEPARTMENT_PARENT_ID);
            case 4:
                return Optional.of(DepartmentField.ORGANIZE_PARENT_ID);
            default:
                return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Integer> getDepartmentFieldNumber(DepartmentField field)
    {
        switch (field)
        {
            case ID:
                return Optional.of(1);
            case NAME:
                return Optional.of(2);
            case DEPARTMENT_PARENT_ID:
                return Optional.of(3);
            case ORGANIZE_PARENT_ID:
                return Optional.of(4);
            default:
                return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<OrganizeField> getOrganizeField(int number)
    {
        switch (number)
        {
            case 1:
                return Optional.of(OrganizeField.ID);
            case 2:
                return Optional.of(OrganizeField.NAME);
            case 3:
                return Optional.of(OrganizeField.TYPE);
            case 4:
                return Optional.of(OrganizeField.PARENT_ID);
            default:
                return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Integer> getOrganizeFieldNumber(OrganizeField field)
    {
        switch (field)
        {
            case ID:
                return Optional.of(1);
            case NAME:
                return Optional.of(2);
            case TYPE:
                return Optional.of(3);
            case PARENT_ID:
                return Optional.of(4);
            default:
                return Optional.empty();
        }
    }
}
