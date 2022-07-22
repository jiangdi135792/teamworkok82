package com.work.plugin.ao;

/**
 * Created by work on 2021/6/21.
 */
import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.OneToOne;

/**
 * Created by work on 2021/6/21.
 */
public interface DepartmentEntity extends Entity {

    String getGroupNo();

    void setGroupNo(String groupNo);

    String getGroupName();

    void setGroupName(String groupName);

    String getParent();

    void setParent(String parent);

    int getStatus();

    void setStatus(int status);

    /** type = 0 部门
     *  type = 1 团队*/
    int getType();

    void setType(int type);

    String getMappingCode();

    void setMappingCode(String mappingCode);

    String getMemo();
    void  setMemo(String memo);

    String getOwner();
    void  setOwner(String owner);

    String getCreateDate();
    void  setCreateDate(String createDate);

    String getModifier();
    void  setModifier(String modifier);

    String getModifierDate();
    void  setModifierDate(String modifierDate);

    @OneToOne
    StrOrganizeGroup getStrOrganizeGroup();



    @OneToMany
    StruGroupOfEmployee[] getStrGroupOfEmployee();

    /*
    2018-9-19 13:07:40 部门负责人、值班人
     */
    StrEmployee getHeaderPerson();
    StrEmployee setHeaderPerson(StrEmployee employee);

    StrEmployee getDutyPerson();
    void setDutyPerson(StrEmployee employee);


    enum COLUMN {CREATE_DATE, GROUP_NAME, GROUP_NO, ID, MAPPING_CODE, MEMO, MODIFIER, MODIFIER_DATE, OWNER, PARENT, STATUS, TYPE;

        COLUMN() {}
    }
}
