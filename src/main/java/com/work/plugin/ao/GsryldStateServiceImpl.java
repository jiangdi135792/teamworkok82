package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.google.common.collect.Lists;
import com.work.plugin.rest.GsryldStateBean;
import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by admin on 2021/7/5.
 */
@RequiredArgsConstructor
public class GsryldStateServiceImpl implements GsryldStateService {
    private final ActiveObjects ao;
    private final StrEmployeeService strEmployeeService;

    private List<GsryldStateBean> beans = new ArrayList<GsryldStateBean>();
    private List<GsryldStateBean> orgBeans = new ArrayList<GsryldStateBean>();

    String name = "\"name\":";
    String children = "\"children\":";
    String str_org = "";
    String str_dept = "";

    public String all(){
        String json = "{\"root\":{" + children + "[";
        List<StrOrganize> orgs = Arrays.asList(ao.find(StrOrganize.class));
        if(orgs.size() == 0){
            return "";
        } else {
            for(StrOrganize org : orgs){
                if(org.getParent() == null || org.getParent() == 0 || org.getID()==org.getParent()){
                    json += getOrgChildren(org.getID());
                }
            }
        }
        json = subStr(json);
        json += "]}}";
        String str = json.replaceAll("\\}\\{","},{");
        String str1 = str.replaceAll("\\},\\]","}]");
        String str2 = str1.replaceAll("\\},,\\]","}]");
        String str3 = str2.replaceAll("\\},,\\{","},{");
        return str3;
    }


    /**
     * 根据机构编号获取机构及其子机构下所有报表数据（json格式）
     * @param orgNo
     * @return
     */
    public String getOrgChildren(int orgNo){
        str_org = "";
        buildOrgChildren(orgNo);
        return str_org + ",";
    }

    /**
     * 循环递归机构
     * @param orgNo
     */
    public void buildOrgChildren(int orgNo){
        str_org = str_org + "{" + name + "\"" + getOrgName(orgNo) + "\",";
        DepartmentEntity[] depts = getGroupByOrgId(orgNo,"");
        //有子机构
        if(isHaveOrg(orgNo)){
            StrOrganize[] orgs = ao.find(StrOrganize.class, MessageFormat.format("PARENT = {0} and ID <> {0}", orgNo));
            if(isHaveDirDept(orgNo)){//有直属部门
                str_org = str_org + children + "[";
                for(StrOrganize org : orgs){
                    buildOrgChildren(org.getID());
                }
                for(DepartmentEntity dept : depts){
                    if(dept.getType() == 0){
                        str_org += getDeptChildren(dept.getID());
                    }
                }
                str_org = str_org + "],";
            } else {
                str_org = str_org + children + "[";
                for(StrOrganize org : orgs){
                    buildOrgChildren(org.getID());
                }
                str_org = str_org + "],";
            }
        } else {
            if(isHaveDirDept(orgNo)){
                str_org = str_org + children + "[";
                for(DepartmentEntity dept : depts){
                    if(dept.getType() == 0){
                        str_org += getDeptChildren(dept.getID());
                    }

                }
                str_org = subStr(str_org);
                str_org = str_org + "],";
            } else {
                str_org = str_org + children + "\"\",";
            }

        }
        str_org = str_org + beanChangeToString(getByOrgNo(orgNo)) + "},";
    }

    /**
     * 根据部门编号获取机构及其子部门下所有报表数据（json格式）
     * @param deptNo
     * @return
     */
    public String getDeptChildren(int deptNo){
        str_dept ="";
        buildDeptChildren(deptNo);
//        str_dept = subStr(str_dept);
        return str_dept + ",";
    }

    /**
     * 循环递归
     * @param deptNo
     */
    public void buildDeptChildren(int deptNo){
        str_dept = str_dept + "{" + name + "\"" + getDeptName(deptNo) + "\",";
        //有子部门
        if(isHaveDept(deptNo)){
            DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", deptNo, deptNo));
            str_dept = str_dept + children + "[";
            for(DepartmentEntity dept : depts){
                buildDeptChildren(dept.getID());
            }
            str_dept = str_dept + "],";
        } else {
            str_dept = str_dept + children + "\"\",";
        }
        str_dept = str_dept + beanChangeToString(getByDeptNo(deptNo)) + "}";
    }


    /**
     * 将传入的实体转化为对应json字符串
     * @param bean
     * @return
     */
    public String beanChangeToString(GsryldStateBean bean){
        if(bean != null){
            String str = "\"parameter\":{\"name\":\"" + bean.getName() + "\",\"entNum\":\"" + bean.getEntNum() + "\"," +
                    "\"dimNum\":\"" + bean.getDimNum() + "\"," +
                    "\"dimRate\":\"" + bean.getDimRate() + "\"," +
                    "\"entTotalNum\":\"" + bean.getEntTotalNum() + "\"," +
                    "\"dimTotalNum\":\"" + bean.getDimTotalNum() + "\"," +
                    "\"dimTotalRate\":\"" + bean.getDimTotalRate() + "\"}";
            return str;
        }
        return "";
    }


    /**
     * 根据组织id获取对应部门
     * @param id 组织id
     * @return
     */
    public DepartmentEntity[] getGroupByOrgId(int id,String sign){
        StrOrganizeGroup[] organizeGroups = ao.find(StrOrganizeGroup.class, MessageFormat.format("ORG_ID = {0}", id));
        if("bmryfb".equals(sign) || "bmrygz".equals(sign)){
            return Lists.newArrayList(organizeGroups).stream()
                    .map(e -> e.getGroup())
                    .filter(s -> s.getType() == 0)
                    .toArray(DepartmentEntity[]::new);
        } else if("tdryfb".equals(sign) || "tdrygz".equals(sign)){
            return Lists.newArrayList(organizeGroups).stream()
                    .map(e -> e.getGroup())
                    .filter(s -> s.getType() == 1)
                    .toArray(DepartmentEntity[]::new);
        } else {
            return Lists.newArrayList(organizeGroups).stream()
                    .map(e -> e.getGroup())
                    .toArray(DepartmentEntity[]::new);
        }
    }

    /**
     * 根据机构编号判断此机构下是否还有机构
     * @param orgNo 机构编号
     * @return true为机构下有机构，false为机构下没有机构
     */
    public boolean isHaveOrg(int orgNo){
        StrOrganize[] orgs = ao.find(StrOrganize.class, String.format("PARENT = '%s'  and ID <> %d", orgNo, orgNo));
        if(orgs.length != 0){
            return true;
        }
        return false;
    }

    public String subStr(String str){
        if(!"".equals(str)){
            if(str.charAt(str.length()-1) == ','){
                return str.substring(0,str.length()-1);
            }

        }
        return "";
    }

    /**
     * 根据机构编号获取机构名称
     * @param orgNo
     * @return
     */
    public String getOrgName(int orgNo){
        StrOrganize org = ao.get(StrOrganize.class,orgNo);
        if(org != null){
            return org.getName();
        }
        return "";
    }

    /**
     * 根据部门编号获取部门名称
     * @param deptNo
     * @return
     */
    public String getDeptName(int deptNo){
        DepartmentEntity dept = ao.get(DepartmentEntity.class,deptNo);
        if(dept != null){
            return dept.getGroupName();
        }
        return "";
    }

    /**
     * 根据部门编号获取部门类型，0为部门，1为团队
     * @param deptNo
     * @return
     */
    public int getDeptType(int deptNo){
        DepartmentEntity dept = ao.get(DepartmentEntity.class,deptNo);
        if(dept != null){
            return dept.getType();
        }
        return 1000;
    }

    /**
     * 根据机构编号获取机构及其所有子机构雇员流动信息的和
     * @param orgNo
     * @return
     */
    public GsryldStateBean getByOrgNo(int orgNo){
        if(orgBeans.size() != 0){
            orgBeans.clear();
        }
        buildOrg(orgNo);
        return sumBean(orgBeans,"org",orgNo);
    }


    /**
     * 层级递归机构下的所有子机构
     * @param orgNo
     */
    public void buildOrg(int orgNo){
        DepartmentEntity[] depts = getGroupByOrgId(orgNo,"");
        if(depts.length != 0){
            for(DepartmentEntity dept : depts){
                if(dept.getType() == 0){
                    orgBeans.add(getByDeptNo(dept.getID()));
                }
            }
        }

        //添加机构直属雇员信息
        orgBeans.add(getDataByDeptNo(0,orgNo,"org"));

        if(isHaveOrg(orgNo)){
            StrOrganize[] orgs = ao.find(StrOrganize.class, String.format("PARENT = '%s'  and ID <> %d", orgNo, orgNo));
            for(StrOrganize org : orgs){
                buildOrg(org.getID());
            }
        }
    }


    /**
     * 根据部门编号获取部门及其所有子部门雇员流动信息的和
     * @param deptNo
     * @return
     */
    public GsryldStateBean getByDeptNo(int deptNo){
        if(beans.size() > 0){
            beans.clear();
        }
        //有子部门
        if(isHaveDept(deptNo)){
            DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'", deptNo));
            beans.add(getDataByDeptNo(deptNo,0,"dept"));
            for(DepartmentEntity dept : depts){
                bulid(dept.getID());
            }
        } else {
            beans.add(getDataByDeptNo(deptNo,0,"dept"));
        }
        return sumBean(beans,"dept",deptNo);
    }

    /**
     * 传入报表信息集合，所有数据相加后返回一个对象
     * @param beans
     * @return
     */
    public GsryldStateBean sumBean(List<GsryldStateBean> beans,String type,int id){
        GsryldStateBean bean = new GsryldStateBean();
        for(GsryldStateBean gsryldStateBean : beans){
            bean.setEntNum(bean.getEntNum() + gsryldStateBean.getEntNum());
            bean.setDimNum(bean.getDimNum() + gsryldStateBean.getDimNum());
            bean.setEntTotalNum(bean.getEntTotalNum() + gsryldStateBean.getEntTotalNum());
            bean.setDimTotalNum(bean.getDimTotalNum() + gsryldStateBean.getDimTotalNum());
        }
        int sum_ent_mon = bean.getEntNum();//上月入职人数
        int sum_lea_mon = bean.getDimNum();//上月离职人数
        int sum_ent_cou = bean.getEntTotalNum();//总入职人数
        int sum_lea_cou = bean.getDimTotalNum();//总离职人数
        double lea_mon_rate = (sum_ent_cou==0)?0:(sum_lea_mon*1.0/sum_ent_cou);//上月离职率
        double lea_cou_rate = (sum_ent_cou==0)?0:(sum_lea_cou*1.0/sum_ent_cou);//总离职率
        if("org".equals(type)){
            bean.setName(getOrgName(id));
        } else if ("dept".equals(type)){
            bean.setName(getDeptName(id));
        }
        bean.setDimRate(((int)(lea_mon_rate*10000))/100);
        bean.setDimTotalRate(((int)(lea_cou_rate*10000))/100);
        return bean;
    }


    /**
     * 层级递归部门下所有部门获取公司人员流动信息
     * @param deptNo
     */
    public void bulid(int deptNo){
        beans.add(getDataByDeptNo(deptNo,0,"dept"));
        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", deptNo, deptNo));
        for(DepartmentEntity dept : depts){
            if(isHaveDept(dept.getID())){
                bulid(dept.getID());
            } else {
                beans.add(getDataByDeptNo(dept.getID(),0,"dept"));
            }
        }
    }


    /**
     * 根据部门编号获取该部门的所有直属雇员的流动信息
     * @param deptNo
     * @return
     */
    public GsryldStateBean getDataByDeptNo(int deptNo,int orgNo,String type){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            startDate = format.parse(""+year+"-"+month+"-1");
            endDate = format.parse(""+year+"-"+(month+1)+"-1");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        StrEmployee[] employees;
        if("dept".equals(type)){
            employees = getEmployeeByDeptId(deptNo);
        } else {
            employees = getEmployeeByOrgId(orgNo);
        }

        int sum_ent_mon = 0;//上月入职人数
        int sum_lea_mon = 0;//上月离职人数
        int sum_ent_cou = 0;//总入职人数
        int sum_lea_cou = 0;//总离职人数
        for(StrEmployee employee : employees){
            try {
                if(Objects.nonNull(employee.getEntryTime())&&!"".equals(employee.getEntryTime() )){
                    if(format.parse(employee.getEntryTime()).getTime() >= startDate.getTime() && format.parse(employee.getEntryTime()).getTime() < endDate.getTime()) {
                        sum_ent_mon++;
                    }
                }
                //2表示离职
                if("2".equals(employee.getEmploymentStatus())){
                    if(Objects.nonNull(employee.getLeaveTime())&&!"".equals(employee.getLeaveTime())){
                        if(format.parse(employee.getLeaveTime()).getTime() >= startDate.getTime() && format.parse(employee.getLeaveTime()).getTime() < endDate.getTime()){
                            sum_lea_mon++;
                        }
                    }
                    sum_lea_cou++;
                }
                sum_ent_cou++;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        GsryldStateBean bean;
        if("dept".equals(type)){
            bean = new GsryldStateBean(getDeptName(deptNo),sum_ent_mon,sum_lea_mon,0.0,sum_ent_cou,sum_lea_cou,0.0);
        } else {
            bean = new GsryldStateBean(getOrgName(orgNo),sum_ent_mon,sum_lea_mon,0.0,sum_ent_cou,sum_lea_cou,0.0);
        }

        return bean;
    }

    /**
     * 通过部门ID找出对应雇员
     * @param deptNo
     * @return
     */
    public StrEmployee[] getEmployeeByDeptId(int deptNo){
        StruGroupOfEmployee[] struGroupOfEmployees = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0}", deptNo));
        return Lists.newArrayList(struGroupOfEmployees).stream().map(e -> e.getEmployee()).toArray(StrEmployee[]::new);
    }

    /**
     * 通过机构ID找出直属雇员
     * @param orgNo
     * @return
     */
    public StrEmployee[] getEmployeeByOrgId(int orgNo){
        StrEmployee[] employees = ao.find(StrEmployee.class,MessageFormat.format("STR_ORGANIZE_ID = {0}", orgNo));
        List<StrEmployee> lists = new ArrayList<>();
        if(employees.length != 0){//不为0表示机构下有雇员
            for(int i=0;i<employees.length;i++){
                StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("EMPLOYEE_ID = {0}", employees[i].getID()));
                if(groupemps.length == 0){//为0表示此雇员为机构直属雇员，因为此雇员不属于部门
                    lists.add(employees[i]);
                }
            }
        }
        StrEmployee[] result = new StrEmployee[lists.size()];
        int x = 0;
        for(StrEmployee emp : lists){
            result[x] = emp;
            x++;
        }
        return result;
    }


    /**
     * 根据部门编号判断此部门下是否还有部门
     * @param deptNo 部门编号
     * @return true为部门下有部门，false为部门下没有部门
     */
    public boolean isHaveDept(int deptNo){
        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", deptNo, deptNo));
        if(depts.length != 0){
            return true;
        }
        return false;
    }

    /**
     * 根据部门编号判断此部门下是否有直属雇员
     * @param deptNo
     * @return true为有直属雇员，false为没有直属雇员
     */
    public boolean isHaveEmployee(int deptNo){
        StruGroupOfEmployee[] groupemps = ao.find(StruGroupOfEmployee.class, MessageFormat.format("GROUP_ID = {0}", deptNo));
        if(groupemps.length != 0){
            return true;
        }
        return false;
    }

    /**
     * 根据机构编号判断此机构下是否有直属部门
     * @param orgNo
     * @return true为有直属部门，false为没有直属部门
     */
    public boolean isHaveDirDept(int orgNo){
        StrOrganizeGroup[] depts = ao.find(StrOrganizeGroup.class, MessageFormat.format("ORG_ID = {0}", orgNo));
        if(depts.length != 0){
            return true;
        }
        return false;
    }

}
