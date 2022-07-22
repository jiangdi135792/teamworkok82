package com.work.plugin.rest;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.work.plugin.util.DicTypeEnum;
import com.work.plugin.util.GetDicValue;
import com.work.plugin.ao.*;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by work on 2022/7/2.
 */
@Path("issue")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class IssueInfoResource {
    private final StrEmployeeService strEmployeeService;
    private final StrOrganizeService strOrganizeService;
    private final DepartmentAOService departmentAOService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    @GET
    @Path("{targetIdInfo}")
    public Response getEmployee(@PathParam("targetIdInfo")  String targetIdInfo) {
        I18nHelper i18nHelper = jiraAuthenticationContext.getI18nHelper();
        //emp_a1|$!assigneeStaffId
        int i = targetIdInfo.lastIndexOf("m");
        String substring = targetIdInfo.substring(0,3);
        if (substring.equals("org")){
             String substring1 = targetIdInfo.substring(i+1);
            final StrOrganize strOrganize = strOrganizeService.get(Integer.valueOf(substring1));
            String character = GetDicValue.getDictionaryValue(DicTypeEnum.BRANCHCHARACTER, strOrganize.getCharacter().toString(), i18nHelper, "workorg.property.organization.character.");
            String status = GetDicValue.getDictionaryValue(DicTypeEnum.STATUS, strOrganize.getStatus().toString(), i18nHelper, "workorg.property.common.status.");
            String type = GetDicValue.getDictionaryValue(DicTypeEnum.ORGANIZATIONTYPE, strOrganize.getType().toString(), i18nHelper, "workorg.property.organization.type.");
            String name = strOrganize.getName();
            return Response.ok(new StrOrganizaitonBean(String.valueOf(strOrganize.getID()),type,status,name,character)).build();
        }else if (substring.equals("dep")){
             String substring1 = targetIdInfo.substring(i+1);
            DepartmentEntity departmentEntity = departmentAOService.get(Integer.valueOf(substring1));
            String status = GetDicValue.getDictionaryValue(DicTypeEnum.STATUS, Integer.toString(departmentEntity.getStatus()), i18nHelper, "workorg.property.common.status.");
            String type = GetDicValue.getDictionaryValue(DicTypeEnum.GROUPTYPE, Integer.toString(departmentEntity.getType()), i18nHelper, "workorg.property.Department.");
            String groupName = departmentEntity.getGroupName();
            String groupNo = departmentEntity.getGroupNo();
            int  id =departmentEntity.getID();
            return Response.ok(new DepartmentBean(id,groupNo,groupName,type,status)).build();
        }else if (substring.equals("emp")){
             String substring1 = targetIdInfo.substring(i+1);
             final StrEmployee strEmployee = strEmployeeService.getEmployee(Integer.valueOf(substring1));
            String employeeSex = GetDicValue.getDictionaryValue(DicTypeEnum.SEX, strEmployee.getEmployeeSex(), i18nHelper, "workorg.property.StrEmployee.");
            String employmentStatus = GetDicValue.getDictionaryValue(DicTypeEnum.EMPLOYMENTSTATUS, strEmployee.getEmploymentStatus(), i18nHelper, "workorg.property.StrEmployee.");
            //entityreporterStrOrganize.setType(GetDicValue.getDictionaryValue(DicTypeEnum.ORGANIZATIONTYPE, entityassigneeStrEmployee_s.getType().toString(), i18nHelper, "workorg.property.organization.type."));
            String employeeName = strEmployee.getEmployeeName();
            String email = strEmployee.getEmail();
            String employeeNo = strEmployee.getEmployeeNo();
            String entryTime = strEmployee.getEntryTime();
            String leaveTime = strEmployee.getLeaveTime();
            String phone = strEmployee.getPhone();
            String otherPhone = strEmployee.getOtherPhone();
            String orgName = strEmployee.getStrOrganize()==null?"":strEmployee.getStrOrganize().getName();
            String groupName ="";
            if (strEmployee.getStruGroupOfEmployee().length!=0){
                List<StruGroupOfEmployee> collect = Arrays.stream(strEmployee.getStruGroupOfEmployee()).filter(struGroupOfEmployee -> struGroupOfEmployee.getPostType() == 0).collect(Collectors.toList());
                groupName = collect.size() >0 ?collect.get(0).getGroup().getGroupName():"";
            }else {
                groupName ="";
            }
            int id = strEmployee.getID();
            Integer supervisor = strEmployee.getSupervisor();
            String supName="";
            if (supervisor != null&&supervisor!=0){
                supName = strEmployeeService.getEmployee(supervisor).getEmployeeName();
            }
            return Response.ok(new StrEmployeeBean( id, employeeName, employeeNo, email, employeeSex, phone, employmentStatus, entryTime,orgName,groupName,supName,supervisor)).build();
        }else {
            return Response.ok().build();
        }
    }
}
