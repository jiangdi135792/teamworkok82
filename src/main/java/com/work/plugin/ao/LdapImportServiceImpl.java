package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.work.plugin.rest.LdapImportBean;
import net.java.ao.DBParam;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Ldap导入类
 * <p>
 * Created by admin on 2021/8/27.
 * TODO 没重构
 */
public class LdapImportServiceImpl implements LdapImportService {

    public SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public LdapImportServiceImpl(ActiveObjects ao, UserManager userManager) {
        this.ao = Objects.requireNonNull(ao);
        this.userManager = Objects.requireNonNull(userManager);
    }

    private final ActiveObjects ao;
    private final UserManager userManager;

    private static final String EMAIL_CHECK = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$";
    private static final String PHONE_NUMBER_REG = "^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$";

    @Override
    public Map doImport(List<LdapImportBean> ldapImportBeanList) throws Exception {

        final Map<String, Object> infoMap = new HashMap<>();//导入后返回的信息集合

        switch (ldapImportBeanList.size()) {
            case 0:
                infoMap.put("Backend information:", "empty lists");
                return infoMap;
            default:
                List<Object> failList = new ArrayList<>();
                for (LdapImportBean ldapImportBean : ldapImportBeanList) {
                    try {
                        String employeeName = ldapImportBean.getEmployeeName();//姓名
                        String email = ldapImportBean.getEmail();              //邮箱
                        String orgName = ldapImportBean.getOrgName();          //公司名

                        if (null == email || "".equals(email) || !email.matches(EMAIL_CHECK) || null == employeeName || "".equals(employeeName) || !email.matches(EMAIL_CHECK) || null == orgName || "".equals(orgName)) {
                            failList.add(ldapImportBean);
                            continue;//必要字段不合格
                        }

                        Integer parentOrgId = ldapImportBean.getParentOrgId();//父公司id
                        String departmentName = ldapImportBean.getDepartmentName();//部门名
                        Integer parentDepartmentId = ldapImportBean.getParentDepartmentId();//父部门id

                        String entryTime = ldapImportBean.getEntryTime();
                        String employeeSex = ldapImportBean.getEmployeeSex();
                        String phone = ldapImportBean.getPhone();

                        StrOrganize strOrganize = null;
                        DepartmentEntity departmentEntity = null;
                        int alreadyHaveOrg = 0;//DB是否已经有这个组织
                        int alreadyHaveDepartment = 0;//DB是否已经有这个部门

                        if (parentOrgId != null) {
                            boolean flag = true;
                            for (LdapImportBean bean : ldapImportBeanList) {
                                if (parentOrgId.equals(bean.getOrgId())) {
                                    StrOrganize so = ao.find(StrOrganize.class, String.format("NAME = '%s' ", bean.getOrgName()))[0];
                                    if (so == null) {
                                        flag = false;
                                        StrOrganize pso = ao.create(StrOrganize.class,
                                                new DBParam(StrOrganize.COLUMN.NAME.name(), bean.getOrgName()),
                                                new DBParam(StrOrganize.COLUMN.PARENT.name(), 0));//TODO 这里的父id，导致只能支持两级的父子关系，暂不考虑第三级
                                        pso.save();
                                        int parentId = pso.getID();

                                        strOrganize = ao.create(StrOrganize.class,
                                                new DBParam(StrOrganize.COLUMN.NAME.name(), orgName),
                                                new DBParam(StrOrganize.COLUMN.PARENT.name(), parentId));
                                        strOrganize.save();
                                    } else {
                                        flag = false;
                                        StrOrganize[] strOrganizes = ao.find(StrOrganize.class, String.format("NAME = '%s' ", orgName));
                                        if (strOrganizes.length == 0) {
                                            strOrganize = ao.create(StrOrganize.class,
                                                    new DBParam(StrOrganize.COLUMN.NAME.name(), orgName),
                                                    new DBParam(StrOrganize.COLUMN.PARENT.name(), so.getID()));
                                            strOrganize.save();
                                        } else {
                                            //TODO 是否需要更新子公司所属父公司
                                            strOrganize = strOrganizes[0];
                                            alreadyHaveOrg = 1;
                                        }
                                    }
                                    break;
                                }
                            }
                            if (flag) {
                                failList.add(orgName + " cannot find parent organization - parentId:" + parentOrgId);
                            }
                        } else {
                            StrOrganize[] strOrganizes = ao.find(StrOrganize.class, String.format("NAME = '%s' ", orgName));
                            if (strOrganizes.length == 0) {
                                strOrganize = ao.create(StrOrganize.class,
                                        new DBParam(StrOrganize.COLUMN.NAME.name(), orgName),
                                        new DBParam(StrOrganize.COLUMN.PARENT.name(), 0));
                                strOrganize.save();
                            } else {
                                alreadyHaveOrg = 1;//已经存在
                                strOrganize = strOrganizes[0];
                            }
                        }

                        if (parentDepartmentId != null) {
                            boolean flg = true;
                            for (LdapImportBean bean : ldapImportBeanList) {
                                if (parentDepartmentId.equals(bean.getDepartmentId())) {
                                    DepartmentEntity de = ao.find(DepartmentEntity.class, String.format("GROUP_NAME = '%s' ", bean.getDepartmentName()))[0];
                                    if (de == null) {
                                        failList.add(departmentName + " cannot find parent department - parentId:" + parentDepartmentId);
                                    } else {
                                        flg = false;
                                        //数据库中有这个父部门，取到父id，创建子部门
                                        if (departmentName != null) {
                                            DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, String.format("GROUP_NAME = '%s' ", departmentName));
                                            if (departmentEntities.length == 0) {//没有这个子部门，可以创建
                                                departmentEntity = ao.create(DepartmentEntity.class,
                                                        new DBParam(DepartmentEntity.COLUMN.GROUP_NAME.name(), departmentName),
                                                        new DBParam(DepartmentEntity.COLUMN.PARENT.name(), String.valueOf(de.getID())),
                                                        new DBParam(DepartmentEntity.COLUMN.TYPE.name(), 0));//0为部门
                                                departmentEntity.save();
                                            } else {
                                                //TODO 是否需要更新子部门所属父部门
                                                departmentEntity = departmentEntities[0];
                                                alreadyHaveDepartment = 1;//已存在这个部门
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                            if (flg) {
                                failList.add(departmentName + " cannot find parent department - parentId:" + parentDepartmentId);
                            }
                        } else {
                            if (departmentName != null) {
                                boolean flg = true;
                                DepartmentEntity[] departmentEntities = ao.find(DepartmentEntity.class, String.format("GROUP_NAME = '%s' ", departmentName));
                                if (departmentEntities.length == 0) {
                                    //部门没有父id，从现有公司中遍历，找不到则为不合格的department
                                    StrOrganize[] strOrganizes = ao.find(StrOrganize.class);
                                    tom:
                                    for (StrOrganize so : strOrganizes) {
                                        StrOrganizeGroup[] sogs = so.getStrOrganizeGroup();
                                        for (StrOrganizeGroup sog : sogs) {
                                            if (departmentName.equals(sog.getGroup().getGroupName())) {
                                                int pId = sog.getGroup().getID();
                                                departmentEntity = ao.create(DepartmentEntity.class,
                                                        new DBParam(DepartmentEntity.COLUMN.GROUP_NAME.name(), departmentName),
                                                        new DBParam(DepartmentEntity.COLUMN.PARENT.name(), pId),
                                                        new DBParam(DepartmentEntity.COLUMN.TYPE.name(), 0));
                                                departmentEntity.save();
                                                flg = false;
                                                break tom;
                                            }
                                        }
                                    }
                                } else {
                                    alreadyHaveDepartment = 1;//已存在这个部门
                                    departmentEntity = departmentEntities[0];
                                    flg = false;
                                }

                                if (flg) {
                                    failList.add(departmentName + " cannot find parent organization or department - parentId:" + parentDepartmentId);
                                }
                            }
                        }

                        StrEmployee[] employees = ao.find(StrEmployee.class, String.format("EMAIL = '%s' ", email));
                        StrEmployee strEmployee = null;
                        int employeeFlag = 0;
                        if (employees.length != 0) {
                            employeeFlag = 1;//邮箱（员工 已存在
                            strEmployee = employees[0];
                            //更新一下基本信息
                            strEmployee.setEmployeeName(employeeName);
                            strEmployee.setEmploymentStatus(ldapImportBean.getEmployeeStatus());//在职与否
                            strEmployee.setEmployeeSex(employeeSex);
                            strEmployee.setEntryTime(entryTime);
                            strEmployee.setPhone(phone.matches(PHONE_NUMBER_REG) ? phone : employees[0].getPhone());
                            strEmployee.setStrOrganize(strOrganize);
                            strEmployee.save();
                            failList.add(employeeName + " already exsist, updated");
                        } else {
                            //不存在
                            strEmployee = ao.create(StrEmployee.class);
                            strEmployee.setEmployeeName(employeeName);
                            strEmployee.setStrOrganize(strOrganize);
                            strEmployee.setEmploymentStatus(ldapImportBean.getEmployeeStatus());//在职与否
                            strEmployee.setEmail(email);
                            strEmployee.setEmployeeSex(employeeSex);
                            strEmployee.setEntryTime(entryTime);
                            if (phone.matches(PHONE_NUMBER_REG)) {
                                strEmployee.setPhone(phone);
                            } else {
                                failList.add(employeeName + "'s phone number format is invalid");
                            }
                            ApplicationUser userByKey = userManager.getUserByKey(ldapImportBean.getJiraUserKey());
                            if (userByKey != null) {
                                String key = userByKey.getKey();
                                StrEmployee[] es = ao.find(StrEmployee.class);
                                int flag = 0;
                                for (StrEmployee e : es) {
                                    if (key.equals(e.getJiraUserKey())) {
                                        flag = 1;
                                        break;
                                    }
                                }
                                if (flag == 0) {
                                    strEmployee.setJiraUserKey(key);
                                } else {
                                    failList.add(employeeName + "'s jiraUserKey already be used by another user");
                                }
                            }
                            strEmployee.save();
                            StrEmployeeOfRole strEmployeeOfRole = ao.create(StrEmployeeOfRole.class);
                            strEmployeeOfRole.setEmployee(strEmployee);
                            strEmployeeOfRole.setRole(ao.get(RoleEntity.class, 1));
                            strEmployeeOfRole.save();
                        }

                        //员工与部门关系
                        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class);
                        if (departmentName != null && departmentEntity != null) {
                            if (employeeFlag == 0) {//员工不存在，直接更新和部门的关系
                                StruGroupOfEmployee struGroupOfEmployee = ao.create(StruGroupOfEmployee.class);
                                struGroupOfEmployee.setGroup(departmentEntity);
                                struGroupOfEmployee.setEmployee(strEmployee);
                                struGroupOfEmployee.setCreateDate(format.format(new Date()));
                                struGroupOfEmployee.save();
                            } else {
                                for (StruGroupOfEmployee soe : struGroupOfEmployees) {
                                    if (null != soe && null != soe.getEmployee() && employeeName.equals(soe.getEmployee().getEmployeeName())) {
                                        //员工已存在，判断该雇员是否需要更新新的部门
                                        if (!(departmentName.equals(soe.getGroup().getGroupName()))) {
                                            StruGroupOfEmployee struGroupOfEmployee = ao.create(StruGroupOfEmployee.class);
                                            //员工本来存在的部门名和新的部门名不一致，需更新
                                            ao.delete(ao.find(StruGroupOfEmployee.class, String.format("EMPLOYEE_ID = '%s' ", soe.getEmployee().getID()))[0]);
                                            struGroupOfEmployee.setGroup(departmentEntity);
                                            struGroupOfEmployee.setEmployee(strEmployee);
                                            struGroupOfEmployee.setCreateDate(format.format(new Date()));
                                            struGroupOfEmployee.save();
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        //更新IMPORT_ENTITY表
                        ImportDataTempEntity importDataTempEntity = ao.create(ImportDataTempEntity.class);

                        importDataTempEntity.setUserId(String.valueOf(strEmployee.getID()));
                        importDataTempEntity.setUserName(strEmployee.getEmployeeName());
                        importDataTempEntity.setEmail(strEmployee.getEmail());
                        //importDataTempEntity.setUserParentId();
                        importDataTempEntity.setJiraUserKey(strEmployee.getJiraUserKey());

                        importDataTempEntity.setStatus(0);

                        importDataTempEntity.setOrgName(strOrganize.getName());
                        importDataTempEntity.setDepartName(departmentEntity.getGroupName());

                        importDataTempEntity.setOrgParentId(String.valueOf(strOrganize.getParent()));
                        importDataTempEntity.setDepartParentId(departmentEntity.getParent());
                        importDataTempEntity.setOrgId(String.valueOf(strOrganize.getID()));
                        importDataTempEntity.setDepartId(String.valueOf(departmentEntity.getID()));
                        //importDataTempEntity.setNewOrgId();
                        //importDataTempEntity.setNewDepId();
                        importDataTempEntity.save();
                    } catch (Exception e) {//TODO 测试用
                        failList.add(e.getMessage());
                        e.printStackTrace();
                    }
                }
                if (failList.size() == 0) {
                    infoMap.put("Backend information:", "all success");
                } else {
                    infoMap.put("Backend information:", failList);
                }
                return infoMap;
        }
    }
}
