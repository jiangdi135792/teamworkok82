package com.work.plugin.imports.data;

import com.google.common.collect.Maps;
import com.mindprod.csv.CSVReader;
import com.work.plugin.ao.StrOrganizeService;
import com.work.plugin.api.Organize;
import com.work.plugin.imports.field.OrganizeField;
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
import java.util.Optional;

/**
 * 导入机构数据处理
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class OrganizeDataProcess implements DataProcess
{
    private final StrOrganizeService organizeService;
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
                        results.put(recordNo, callOrganizeService(fields));
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
     * 呼叫机构AO服务
     *
     * @param fields 字段群
     * @return 错误信息。如果成功，没有信息
     */
    private String callOrganizeService(String[] fields)
    {
        try
        {
            val organize = Organize.builder()
                    .id(Optional.ofNullable(getFieldValue(OrganizeField.ID, fields)).map(Integer::parseInt).orElse(0))
                    .name(getFieldValue(OrganizeField.NAME, fields))
                    .type(getFieldValue(OrganizeField.TYPE, fields))
                    .parent(Optional.ofNullable(getFieldValue(OrganizeField.PARENT_ID, fields)).map(Integer::parseInt).orElse(null))
                    .build();
            organizeService.updateOrganizeByImport(organize);
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
     * @param organizeField 字段名称
     * @param fields 字段群
     * @return 字段值
     */
    @Nullable
    private String getFieldValue(OrganizeField organizeField, String[] fields)
    {
        return importConfigService.getOrganizeFieldNumber(organizeField)
                .filter(n -> n < fields.length + 1)
                .map(n -> fields[n - 1])
                .filter(StringUtils::isNotEmpty)
                .orElse(null);
    }
}
