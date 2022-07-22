package com.work.plugin.report;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.google.common.collect.Lists;
import com.work.plugin.ao.DepartmentEntity;
import com.work.plugin.ao.StrOrganize;
import com.work.plugin.ao.StrOrganizeGroup;
import com.work.plugin.ao.TeamProjectEntity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin on 2021/7/5.
 */
@RequiredArgsConstructor
public class GetOrgTreeServiceImpl implements GetOrgTreeService{
    private static final Logger log = LoggerFactory.getLogger(GetOrgTreeServiceImpl.class);
    private final ActiveObjects ao;
    private final ProjectManager projectManager;

    /**
     * 获取组织机构树，用于生成树的下拉框
     * @return
     */
    @Override
    public List<ComboTreeModel> getAllTreeData(){
        List<ComboTreeModel> lists = new ArrayList<>();//所有返回的数据
        List<StrOrganize> orgs = Arrays.asList(ao.find(StrOrganize.class));//获取所有机构集合
        for(StrOrganize org : orgs){
            //判断是否为根组织,即没有上级,是根组织则继续向下遍历
            if(org.getParent() == null || org.getParent() == 0 || org.getID()==org.getParent()){
                ComboTreeModel model = new ComboTreeModel();
                model.setId(10000+org.getID());
                model.setText(org.getName());
                model.setType("org_type");//机构类型，用于区分是机构还是部门
                buildChildrenOrg(model);
                lists.add(model);
            }
        }
        return lists;
    }


    /**
     * 层级递归机构及其所有子机构
     * @param model 机构对象
     */
    public void buildChildrenOrg(ComboTreeModel model){
        StrOrganize[] strOrganize = ao.find(StrOrganize.class, String.format("PARENT = '%s'  and ID <> %d", model.getId()-10000, model.getId()-10000));//此机构下的所有子机构
        //判断机构下是否有子机构
        if(strOrganize.length != 0){//有子机构
            List<ComboTreeModel> templists = new ArrayList<>();
            for(StrOrganize org : strOrganize){
                ComboTreeModel tempModel = new ComboTreeModel();
                tempModel.setId(10000+org.getID());
                tempModel.setText(org.getName());
                tempModel.setType("org_type");//机构类型，用于区分是机构还是部门
                buildChildrenOrg(tempModel);//回调执行查询子机构函数
                templists.add(tempModel);
            }
            model.setChildren(templists);
        }

        StrOrganizeGroup[] organizeGroups = ao.find(StrOrganizeGroup.class, MessageFormat.format("ORG_ID = {0}", model.getId()-10000));//机构与部门的对应关系表对象
        //判断机构下是否有直属部门
        if(organizeGroups.length != 0){
            DepartmentEntity[] depts = Lists.newArrayList(organizeGroups).stream().map(e -> e.getGroup()).filter(s -> s.getType() == 0).toArray(DepartmentEntity[]::new);//type为0表示部门
            List<ComboTreeModel> templists = new ArrayList<>();
            for(DepartmentEntity dept : depts){
                ComboTreeModel tempModel = new ComboTreeModel();
                tempModel.setId(20000+dept.getID());
                tempModel.setText(dept.getGroupName());
                tempModel.setType("dept_type");//机构类型，用于区分是机构还是部门
                buildChildrenDept(tempModel);//回调执行查询子机构函数
                templists.add(tempModel);
            }
            if(model.getChildren() == null){
                model.setChildren(templists);
            } else {
                model.getChildren().addAll(templists);
            }
        }

        if(strOrganize.length == 0 && organizeGroups.length == 0){
            model.setChildren(new ArrayList<ComboTreeModel>());
        }

    }

    /**
     * 层级递归部门及其所有子部门
     * @param model 部门对象
     * @return
     */
    public void buildChildrenDept(ComboTreeModel model){
        DepartmentEntity[] depts = ao.find(DepartmentEntity.class, String.format("PARENT = '%s'  and ID <> %d", model.getId()-20000, model.getId()-20000));//获取此部门下的部门
        //判断部门下是否有部门
        if(depts.length != 0){//表示有部门
            List<ComboTreeModel> templists = new ArrayList<>();
            for(DepartmentEntity dept : depts){
                ComboTreeModel tempModel = new ComboTreeModel();
                tempModel.setId(20000+dept.getID());
                tempModel.setText(dept.getGroupName());
                tempModel.setType("dept_type");//机构类型，用于区分是机构还是部门
                buildChildrenDept(tempModel);//回调执行查询子机构函数
                templists.add(tempModel);
            }
            model.setChildren(templists);
        } else {
            model.setChildren(new ArrayList<ComboTreeModel>());
        }
    }

    /**
     * 获取团队树，用于生成树的下拉框
     * @param proname 选择的项目
     * @return
     */
    @Override
    public List<ComboTreeModel> getTeamTreeData(String proname){
        List<ComboTreeModel> lists = new ArrayList<>();//所有返回的数据
        List<Project> projects = new ArrayList<>();
        if("".equals(proname)){
            projects = projectManager.getProjects();
        } else {
            for(String name : proname.split(",")){
                projects.add(projectManager.getProjectObjByName(name));
            }
        }
        for(Project project : projects){
            TeamProjectEntity[] entity = ao.find(TeamProjectEntity.class,String.format(" PROJECT_ID = '%d' ",project.getId()));
            if(entity == null || entity.length == 0){
                ComboTreeModel model = new ComboTreeModel();
                model.setId(Integer.parseInt(""+project.getId()));//项目未映射团队,则显示项目id
                model.setText(project.getName()+" | 未映射团队");
                model.setType("dept_type");
                lists.add(model);
            } else {
                TeamProjectEntity teamProjectEntity = entity[0];
                DepartmentEntity departmentEntity = ao.get(DepartmentEntity.class,teamProjectEntity.getTeamId());
                ComboTreeModel model = new ComboTreeModel();
                model.setId(20000+teamProjectEntity.getTeamId());
                model.setText(project.getName() + " | " + departmentEntity.getGroupName());
                model.setType("dept_type");
                buildChildrenDept(model);
                lists.add(model);
            }
        }
        return lists;
    }

}
