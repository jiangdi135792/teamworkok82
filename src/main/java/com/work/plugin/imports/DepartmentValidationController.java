package com.work.plugin.imports;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mindprod.csv.CSVReader;
import com.work.plugin.imports.field.DepartmentField;
import com.work.plugin.service.ImportConfigService;
import com.work.plugin.validator.*;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * 部门验证控制
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class DepartmentValidationController /*implements ValidationController*/
{
    private final ImportConfigService importConfigService;
    private final RequiredValidator requiredValidator;
    private final NumberValidator numberValidator;
    private final UniqueGroupValidator uniqueGroupValidator;
    private final LengthValidator lengthValidator;
    private final RelationDepartmentValidator relationDepartmentValidator;

    /**
     * {@inheritDoc}
     */
   // @Override
    public Map<Integer, List<String>> execute(String filePath)
    {
        Map<Integer, List<String>> results = Maps.newHashMap();
        try
        {
            val csv = new CSVReader(new BufferedReader(new FileReader(filePath)));
            int recordNo = 0;
            try
            {
                //noinspection InfiniteLoopStatement
                while (true)
                {
                    String[] fields = csv.getAllFieldsInLine();
                    List<String> messages = Lists.newArrayList();
                    IntStream.range(0, fields.length)
                            .forEach(idx -> {
                                val safeField = importConfigService.getDepartmentField(idx + 1);
                                if (safeField.isPresent())
                                    messages.addAll(validateField(safeField.get(), fields[idx], fields));
                                else
                                    messages.add(String.format("%d filed is not found", idx + 1));
                            });
                    results.put(recordNo, messages);
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
            results.put(0, ImmutableList.of(e.getMessage()));
            return results;
        }

    }

    /**
     *
     * 验证字段值
     *
     * @param field 字段名称
     * @param value 字段值
     * @return 验证结果信息
     * @exception ValidatorException 如果没有字段规则
     */
    private List<String> validateField(DepartmentField field, String value, String[] fields)
    {
        Pair<Boolean, ValidatorException> result;
        switch (field)
        {
            case ID:
                result = requiredValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = numberValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = lengthValidator.validate(value, 18);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = uniqueGroupValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                break;

            case NAME:
                result = requiredValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = lengthValidator.validate(value, 50);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                break;

            case DEPARTMENT_PARENT_ID:
                result = requiredValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    break;
                val safeOrgParent = importConfigService.getDepartmentFieldNumber(DepartmentField.ORGANIZE_PARENT_ID);
                if (safeOrgParent.isPresent() && safeOrgParent.get() <= fields.length)
                    if (Objects.isNull(requiredValidator.validate(fields[safeOrgParent.get() - 1]).getRight()))
                        return Lists.newArrayList("organize parent field already set");
                result = numberValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = lengthValidator.validate(value, 18);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = relationDepartmentValidator.validateDepartmentParent(Integer.parseInt(value));
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                break;

            case ORGANIZE_PARENT_ID:
                result = requiredValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    break;
                val safeDepParent = importConfigService.getDepartmentFieldNumber(DepartmentField.DEPARTMENT_PARENT_ID);
                if (safeDepParent.isPresent() && safeDepParent.get() <= fields.length)
                    if (Objects.isNull(requiredValidator.validate(fields[safeDepParent.get() - 1]).getRight()))
                        return Lists.newArrayList("department parent field already set");
                result = numberValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = lengthValidator.validate(value, 18);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = relationDepartmentValidator.validateOrganizeParent(Integer.parseInt(value));
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                break;

            default:
                throw new ValidatorException("Field rule not found.");
        }
        return Lists.newArrayList();
    }

}
