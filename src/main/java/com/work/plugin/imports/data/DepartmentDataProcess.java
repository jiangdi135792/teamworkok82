package com.work.plugin.imports.data;

import com.google.common.collect.Maps;
import com.mindprod.csv.CSVReader;
import com.work.plugin.ao.DepartmentAOService;
import com.work.plugin.api.Group;
import com.work.plugin.api.Organize;
import com.work.plugin.imports.field.DepartmentField;
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
import java.util.Optional;

/**
 * 导入部门数据处理
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class DepartmentDataProcess implements DataProcess
{
    private final ImportConfigService importConfigService;
    private final DepartmentAOService departmentAOService;

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
                        results.put(recordNo, callDepartmentAOService(fields));
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
     * 呼叫部门AO服务
     *
     * @param fields 字段群
     * @return 错误信息。如果成功，没有信息
     */
    private String callDepartmentAOService(String[] fields)
    {
        try
        {
            val group = Group.builder()
                    .id(Optional.ofNullable(getFieldValue(DepartmentField.ID, fields)).map(Integer::parseInt).orElse(0))
                    .name(getFieldValue(DepartmentField.NAME, fields))
                    .parent(getFieldValue(DepartmentField.DEPARTMENT_PARENT_ID, fields))
                    .build();
            Organize organize=null;
            //val updatedGroup = departmentAOService.updateGroupByImport(1,group);
            val updatedGroup = null;

            val organizeParentId = getFieldValue(DepartmentField.ORGANIZE_PARENT_ID, fields);
            if (Objects.nonNull(organizeParentId))
                //departmentAOService.updateOrganizeGroupByImport(updatedGroup.getId(), Integer.parseInt(organizeParentId));

            return "";
        }
        catch (ActiveObjectsException e)
        {
            e.getStackTrace();
            return e.getMessage();
        }
        return null;
    }

    /**
     * 取得字段值
     *
     * @param departmentField 字段名称
     * @param fields 字段群
     * @return 字段值
     */
    @Nullable
    private String getFieldValue(DepartmentField departmentField, String[] fields)
    {
        return importConfigService.getDepartmentFieldNumber(departmentField)
                .filter(n -> n < fields.length + 1)
                .map(n -> fields[n - 1])
                .filter(StringUtils::isNotEmpty)
                .orElse(null);
    }

}
