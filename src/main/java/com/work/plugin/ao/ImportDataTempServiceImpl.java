package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by work on 2022/3/31.
 */
public class ImportDataTempServiceImpl implements ImportDataTempService {
    private final ActiveObjects ao;

    public ImportDataTempServiceImpl(ActiveObjects ao) {
        this.ao=ao;
    }

    @Override
    public void creatTempData(String[] field,long hashcode) {
        ImportDataTempEntity importDataTempEntity=ao.create(ImportDataTempEntity.class);
        importDataTempEntity.setUserName(field[0]);
        importDataTempEntity.setOrgName(field[1]);
        int length=field.length;
        if (length==3){
            importDataTempEntity.setEmail(field[2]);
        }else if (length==4){
            importDataTempEntity.setEmail(field[2]);
            importDataTempEntity.setDepartName(StringUtils.isBlank(field[3])?null:field[3]);
        }
        else if (length==5){
            importDataTempEntity.setEmail(field[2]);
            importDataTempEntity.setDepartName(StringUtils.isBlank(field[3])?null:field[3]);
            importDataTempEntity.setJiraUserKey(StringUtils.isBlank(field[4])?null:field[4]);
        }
        else if (length==6){
            importDataTempEntity.setEmail(field[2]);
            importDataTempEntity.setDepartName(StringUtils.isBlank(field[3])?null:field[3]);
            importDataTempEntity.setJiraUserKey(StringUtils.isBlank(field[4])?null:field[4]);
            importDataTempEntity.setUserId(StringUtils.isBlank(field[5])?"0": field[5]);
        }
        else if (length==7){
            importDataTempEntity.setEmail(field[2]);
            importDataTempEntity.setDepartName(StringUtils.isBlank(field[3])?null:field[3]);
            importDataTempEntity.setJiraUserKey(StringUtils.isBlank(field[4])?null:field[4]);
            importDataTempEntity.setUserId(StringUtils.isBlank(field[5])?"0": field[5]);
            importDataTempEntity.setUserParentId(StringUtils.isBlank(field[6])?null:(field[6]));
        }
        else if (length==8){
            importDataTempEntity.setEmail(field[2]);
            importDataTempEntity.setDepartName(StringUtils.isBlank(field[3])?null:field[3]);
            importDataTempEntity.setJiraUserKey(StringUtils.isBlank(field[4])?null:field[4]);
            importDataTempEntity.setUserId(StringUtils.isBlank(field[5])?"0": field[5]);
            importDataTempEntity.setUserParentId(StringUtils.isBlank(field[6])?null:(field[6]));
            importDataTempEntity.setOrgId(StringUtils.isBlank(field[7])?null:(field[7]));
        }
        else if (length==9){
            importDataTempEntity.setEmail(field[2]);
            importDataTempEntity.setDepartName(StringUtils.isBlank(field[3])?null:field[3]);
            importDataTempEntity.setJiraUserKey(StringUtils.isBlank(field[4])?null:field[4]);
            importDataTempEntity.setUserId(StringUtils.isBlank(field[5])?"0": field[5]);
            importDataTempEntity.setUserParentId(StringUtils.isBlank(field[6])?null:(field[6]));
            importDataTempEntity.setOrgId(StringUtils.isBlank(field[7])?null:(field[7]));
            importDataTempEntity.setOrgParentId(StringUtils.isBlank(field[8])?null:(field[8]));
        }else if (length==10){
            importDataTempEntity.setEmail(field[2]);
            importDataTempEntity.setDepartName(StringUtils.isBlank(field[3])?null:field[3]);
            importDataTempEntity.setJiraUserKey(StringUtils.isBlank(field[4])?null:field[4]);
            importDataTempEntity.setUserId(StringUtils.isBlank(field[5])?"0": field[5]);
            importDataTempEntity.setUserParentId(StringUtils.isBlank(field[6])?null:(field[6]));
            importDataTempEntity.setOrgId(StringUtils.isBlank(field[7])?null:(field[7]));
            importDataTempEntity.setOrgParentId(StringUtils.isBlank(field[8])?null:(field[8]));
            importDataTempEntity.setDepartId(StringUtils.isBlank(field[9])?null: (field[9]));
        }
        else if (length==11){
            importDataTempEntity.setEmail(field[2]);
            importDataTempEntity.setDepartName(StringUtils.isBlank(field[3])?null:field[3]);
            importDataTempEntity.setJiraUserKey(StringUtils.isBlank(field[4])?null:field[4]);
            importDataTempEntity.setUserId(StringUtils.isBlank(field[5])?"0": field[5]);
            importDataTempEntity.setUserParentId(StringUtils.isBlank(field[6])?null:(field[6]));
            importDataTempEntity.setOrgId(StringUtils.isBlank(field[7])?null:(field[7]));
            importDataTempEntity.setOrgParentId(StringUtils.isBlank(field[8])?null:(field[8]));
            importDataTempEntity.setDepartId(StringUtils.isBlank(field[9])?null: (field[9]));
            importDataTempEntity.setDepartParentId(StringUtils.isBlank(field[10])?null: (field[10]));
        }
        importDataTempEntity.setHashCode(hashcode);
        importDataTempEntity.setStatus(1);
        importDataTempEntity.save();
    }

    @Override
    public boolean toUpdateInfo(String email, long hashcode) {
        ImportDataTempEntity[] importDataTempEntities=ao.find(ImportDataTempEntity.class, String.format("EMAIL = '%s' ", email));
        if (importDataTempEntities.length != 0) {
            if (importDataTempEntities[0].getHashCode().equals(hashcode)) {
                return false;
            } else {
                ao.delete(importDataTempEntities[0]);
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public ImportDataTempEntity[] getToUpdateInfo() {
        return ao.find(ImportDataTempEntity.class, String.format("STATUS = 1 "));
    }

    @Override
    public ImportDataTempEntity getByOrgId(String orgId) {
        ImportDataTempEntity[] importDataTempEntities=ao.find(ImportDataTempEntity.class, String.format("ORG_ID = '%s' ", orgId));
        if (importDataTempEntities.length>0){
            return importDataTempEntities[0];
        }
        return null;
    }

    @Override
    public ImportDataTempEntity getByDepId(String orgId) {
        ImportDataTempEntity[] importDataTempEntities=ao.find(ImportDataTempEntity.class, String.format("DEPART_ID = '%s' ", orgId));
        if (importDataTempEntities.length>0){
            return importDataTempEntities[0];
        }
        return null;
    }


}
