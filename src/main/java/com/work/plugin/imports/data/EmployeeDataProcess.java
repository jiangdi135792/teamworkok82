package com.work.plugin.imports.data;

import com.google.common.collect.Maps;
import com.mindprod.csv.CSVReader;
import com.work.plugin.ao.StrEmployeeService;
import com.work.plugin.api.Employee;
import com.work.plugin.imports.field.EmployeeField;
import com.work.plugin.service.ImportConfigService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.java.ao.ActiveObjectsException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * 导入工员数据处理
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class EmployeeDataProcess implements DataProcess
{
    private final StrEmployeeService employeeService;
    private final ImportConfigService importConfigService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, String> execute(@NonNull String filePath, @NonNull Integer[] rows)
    {
        Map<Integer, String> results = Maps.newHashMap();
        try
        {
            val csv = new CSVReader(new BufferedReader(new FileReader(filePath)));
            int recordNo = 0;
            try
            {
                //noinspection InfiniteLoopStatement
                while (true)
                {
                    if (Arrays.asList(rows).contains(recordNo))
                    {
                        String[] fields = csv.getAllFieldsInLine();
                        results.put(recordNo, callEmployeeAOService(fields));
                    }
                    else
                    {
                        csv.skipToNextLine();
                    }
                        recordNo++;
                }
            }
            catch (EOFException e)
            {
                csv.close();
            }
            return results;
        }
        catch (IOException e)
        {
            results.put(0, e.getMessage());
            return results;
        }
    }

    /**
     * 呼叫工员AO服务
     *
     * @param fields 字段群
     * @return 错误信息。如果成功，没有信息
     */
    private String callEmployeeAOService(String[] fields)
    {
        try
        {
            val employee = Employee.builder()
                    .email(getFieldValue(EmployeeField.EMAIL, fields))
                    .name(getFieldValue(EmployeeField.NAME, fields))
                    .jiraUserKey(getFieldValue(EmployeeField.JIRA_USER_KEY, fields))
                    .phone(getFieldValue(EmployeeField.PHONE, fields))
                    .status("1")
                    .entryTime(getFieldValue(EmployeeField.ENTRY_TIME, fields))
                    .build();
            val updatedEmployee = employeeService.updateEmployeeByImport(employee);

            val departmentId = getFieldValue(EmployeeField.DEPARTMENT_ID, fields);
            if (Objects.nonNull(departmentId))
                employeeService.updateGroupOfEmployeeByImport(updatedEmployee.getId(), Integer.parseInt(departmentId));

            return "";
        }
        catch (ActiveObjectsException e)
        {
            e.getStackTrace();
            return e.getMessage();
        }
    }

    /**
     * 取得字段值
     *
     * @param employeeField 字段名称
     * @param fields 字段群
     * @return 字段值
     */
    @Nullable
    private String getFieldValue(EmployeeField employeeField, String[] fields)
    {
        return importConfigService.getEmployeeFieldNumber(employeeField)
                .filter(n -> n < fields.length + 1)
                .map(n -> fields[n - 1])
                .filter(StringUtils::isNotEmpty)
                .orElse(null);
    }
}
