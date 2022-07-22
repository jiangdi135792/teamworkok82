package com.work.plugin.imports;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mindprod.csv.CSVReader;
import com.work.plugin.api.Employee;
import com.work.plugin.api.Group;
import com.work.plugin.api.Organize;
import com.work.plugin.imports.field.EmployeeField;
import com.work.plugin.service.ImportConfigService;
import com.work.plugin.validator.*;
import com.work.plugin.ao.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 工员验证控制
 *
 * @author Masato Morita
 */
@RequiredArgsConstructor
public class EmployeeValidationController implements ValidationController
{
    private final ImportConfigService importConfigService;
    private final RequiredValidator requiredValidator;
    private final UniqueEmployeeValidator uniqueEmployeeValidator;
    private final LengthValidator lengthValidator;
    private final EmailValidator emailValidator;
    private final UserNameValidator userNameValidator;
    private final PhoneNumberValidator phoneNumberValidator;
    private final NumberValidator numberValidator;
    private final RelationEmployeeValidator relationEmployeeValidator;
    private final DateTimeValidator dateTimeValidator;
    private final StrEmployeeService strEmployeeService;
    private final StrOrganizeService strOrganizeService;
    private final DepartmentAOService departmentAOService;
    private final ImportDataTempService importDataTempService;
    private final RoleService roleService;
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, Map<Integer,String>> execute(@NonNull String filePath)
    {
        Map<Integer, List<String>> results = Maps.newHashMap();
        Map<Integer,String> result=Maps.newHashMap();
        Map<Integer,String> result2=Maps.newHashMap();
        Map<Integer, Map<Integer,String>> resul = Maps.newHashMap();
        try
        {
            val csv =new CSVReader(new BufferedReader(new FileReader(filePath)));
            int recordNo = 0;
            try
            {
                //noinspection InfiniteLoopStatement
                while (true)
                {
                    recordNo++;
                    String[] fields = csv.getAllFieldsInLine();
                    int length = fields.length;
                    if (length>11){
                        break;
                    }
                    Boolean isRightOfType=false;
                    List<String> messages = Lists.newArrayList();
                    IntStream.range(0, length)
                            .forEach(idx -> {
                                val safeField = importConfigService.getEmployeeField(idx + 1);
                                if (safeField.isPresent()){
                                    List<String> strings=validateField(safeField.get(), fields[idx]);
                                    messages.addAll(strings);
                                }
                                else{
                                    messages.add(String.format("%d filed is not found", idx + 1));
                                }
                            });
                    if (messages.size()!=0){
                        isRightOfType=messages.contains("value is invalid.");
                        results.put(recordNo, messages);
                    }
                    if (!isRightOfType){
                        String join=StringUtils.join(fields);
                       int hash=0;
                        for (int a=0; a < (join).length(); ++a) {
                            hash=33 * hash + (join).charAt(a);
                        }
                        if (this.toUpdateInfo(fields[2],hash)){//TODO
                            this.creatTmepData(fields,hash);
                        }
                    }else {
                        result.put(recordNo,"value is invalid");
                    }
                }
                return null;
            }
            catch (EOFException e)
            {
                csv.close();
                ImportDataTempEntity[] toUpdateInfo=importDataTempService.getToUpdateInfo();
                Stream.of(toUpdateInfo)
                        .forEach(s -> {
                            Organize organize=Organize.builder().character(0).name(s.getOrgName()).id(StringUtils.isBlank(s.getOrgId())?0:Integer.parseInt(s.getOrgId())).status("0").type("0").parent(StringUtils.isBlank(s.getOrgParentId())?0:Integer.valueOf(s.getOrgParentId())).build();
                            Organize newOrganize=strOrganizeService.updateOrganizeByImport(organize);
                            s.setNewOrgId(newOrganize.getId());
                            s.save();
                        });
                ImportDataTempEntity[] toUpdateInfo1=importDataTempService.getToUpdateInfo();
                Stream.of(toUpdateInfo1)
                        .forEach(s ->{
                            Group group=Group.builder().id(StringUtils.isBlank(s.getDepartId())?0:Integer.parseInt(s.getDepartId())).name(s.getDepartName()).parent(s.getDepartParentId()).build();
                            Group newGroup=departmentAOService.updateGroupByImport(s.getNewOrgId(),group);
                            if (newGroup!=null){
                            s.setNewDepId(newGroup.getId());
                            s.save();
                            }
                            if (s.getNewDepId() != 0) {
                                if (StringUtils.isNotBlank(s.getDepartParentId())) {
                                    ImportDataTempEntity byDepId=importDataTempService.getByDepId(s.getDepartParentId());
                                    if (byDepId!=null){
                                        departmentAOService.updateDepAndDepRela(s.getNewDepId(), String.valueOf(byDepId.getNewDepId()));
                                    }else {
                                        departmentAOService.updateOrganizeGroupByImport(s.getNewOrgId(), s.getNewDepId());
                                    }
                                }else {
                                    departmentAOService.updateOrganizeGroupByImport(s.getNewOrgId(), s.getNewDepId());
                                }
                            }
                        });
                ImportDataTempEntity[] toUpdateInfo2=importDataTempService.getToUpdateInfo();
                Stream.of(toUpdateInfo2)
                        .forEach(s -> {
                            Employee newEmployee=Employee.builder().email(s.getEmail()).name(s.getUserName()).sex("1").status("1").jiraId(0).jiraUserKey(null).build();
                            Employee employee=strEmployeeService.updateEmployeeByImport(newEmployee);
                            RoleEntity roleByName = roleService.getRoleByName("General Staff");
                            StrEmployee employee1 = strEmployeeService.getEmployee(employee.getId());
                            roleService.setRoleToEmployee(roleByName,employee1);
                            if (StringUtils.isNotBlank(s.getOrgParentId())) {
                                ImportDataTempEntity byOrgId=importDataTempService.getByOrgId(s.getOrgParentId());
                                if (byOrgId != null) {
                                    strOrganizeService.updateOrgOfParentOrgByImport(s.getNewOrgId(), byOrgId.getNewOrgId());
                                }
                            }
                            strEmployeeService.updateOrgOfEmployeeByImport(employee.getId(), s.getNewOrgId());
                            if (s.getNewDepId() != 0) {
                                strEmployeeService.updateGroupOfEmployeeByImport(employee.getId(), s.getNewDepId());
                                if (StringUtils.isNotBlank(s.getDepartParentId())) {
                                    ImportDataTempEntity byDepId=importDataTempService.getByDepId(s.getDepartParentId());
                                    if (byDepId!=null){
                                    departmentAOService.updateDepAndDepRela(s.getNewDepId(), String.valueOf(byDepId.getNewDepId()));
                                    }else {
                                     departmentAOService.updateOrganizeGroupByImport(s.getNewOrgId(), s.getNewDepId());
                                    }
                                }else {
                                departmentAOService.updateOrganizeGroupByImport(s.getNewOrgId(), s.getNewDepId());
                                }
                            }
                            s.setStatus(0);
                            s.save();
                        });
            }
            resul.put(-1,result);
            int size=result.size();
            result2.put(0, String.valueOf(recordNo-1));
            result2.put(1, String.valueOf(recordNo-size-1));
            result2.put(2, String.valueOf(size));
            resul.put(-2,result2);
            return resul;
        }
        catch (IOException e)
        {
            results.put(0, ImmutableList.of(e.getMessage()));
            return resul;
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
    private List<String> validateField(EmployeeField field, String value)
    {
        Pair<Boolean, ValidatorException> result;
        switch (field)
        {
            case USERID:
                result=lengthValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                result = numberValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                break;
            case DEPAPARENTID:
                result=lengthValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                result = numberValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                break;
            case USERPARENTID:
                result=lengthValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                result = numberValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                break;
            case ORGID:
                result=lengthValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                result = numberValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                break;
            case ORGPARENTID:
                result=lengthValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                result = numberValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                break;
            case DEPARTID:
                result=lengthValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                result = numberValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                break;
            case DEPARTNAME:
                result = lengthValidator.validate(value, 50);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                break;
            case NAME:
                result = requiredValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                result = uniqueEmployeeValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                result = lengthValidator.validate(value, 50);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                break;
            case ORGNAME:
                result = requiredValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                result = lengthValidator.validate(value, 50);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                break;
            case EMAIL:
                result = emailValidator.validate(value);
                if (Objects.nonNull(result.getRight())){
                return Lists.newArrayList(result.getRight().getMessage());
                }
                result = lengthValidator.validate(value, 50);
                if (Objects.nonNull(result.getRight())){
                    return Lists.newArrayList(result.getRight().getMessage());
                }
                break;
            case JIRA_USER_KEY:
                result = userNameValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = lengthValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                break;

            case PHONE:
                result = requiredValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    break;
                result = phoneNumberValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = lengthValidator.validate(value, 50);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                break;

            case DEPARTMENT_ID:
                result = requiredValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = numberValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = lengthValidator.validate(value, 18);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = relationEmployeeValidator.validate(Integer.parseInt(value));
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                break;

            case ENTRY_TIME:
                result = requiredValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    break;
                result = dateTimeValidator.validate(value);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                result = lengthValidator.validate(value, 20);
                if (Objects.nonNull(result.getRight()))
                    return Lists.newArrayList(result.getRight().getMessage());
                break;

            default:
                throw new ValidatorException("Field rule not found.");
        }
        return Lists.newArrayList();
    }

    private void creatTmepData(String[] fields,long hashcode){
        importDataTempService.creatTempData(fields,hashcode);
    }
    private boolean toUpdateInfo(String email,long hashcode){
        return importDataTempService.toUpdateInfo(email, hashcode);
    }
}
