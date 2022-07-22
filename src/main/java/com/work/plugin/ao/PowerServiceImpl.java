package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;

/**
 * Created by work on 2022/3/15.
 */

public class PowerServiceImpl implements PowerService {
    private  final ActiveObjects ao;

    public PowerServiceImpl(ActiveObjects ao) {
        this.ao=ao;
    }

    @Override
    public void setItMenu(OrganizationEntity organizationEntity, PowerEntity powerEntity) {
        if (organizationEntity!=null&&powerEntity!=null){
        OrganizationAndPowerEntity organizationAndPowerEntity = ao.create(OrganizationAndPowerEntity.class);
        organizationAndPowerEntity.setOrganization(organizationEntity);
        organizationAndPowerEntity.setPower(powerEntity);
        organizationAndPowerEntity.save();
        }
    }

    @Override
    public PowerEntity getById(Integer id) {
        return  ao.get(PowerEntity.class, id);
    }

    @Override
    public void bindPowerToRole(PowerEntity powerEntity, RoleEntity roleEntity) {
        StrPowerRole strPowerRole = ao.create(StrPowerRole.class);
        strPowerRole.setPowerEntity(powerEntity);
        strPowerRole.setRoleEntity(roleEntity);
        strPowerRole.save();
    }

    @Override
    public void unbindPowerFromRole(PowerEntity powerEntity, RoleEntity roleEntity) {
        StrPowerRole[] strPowerRoles = ao.find(StrPowerRole.class, String.format("POWER_ENTITY_ID = '%d'  AND  ROLE_ENTITY_ID = '%d'  ", powerEntity.getID(), roleEntity.getID()));
        ao.delete(strPowerRoles[0]);
    }

    @Override
    public PowerEntity[] getAllPowerOfMenu(String url) {
        PowerEntity[] powerEntities = ao.find(PowerEntity.class, String.format("URL = '%s' ", url));
        return  powerEntities;
    }

    @Override
    public PowerEntity getByDesc(String desc) {
        PowerEntity[] powerEntities = ao.find(PowerEntity.class, String.format("DESCRIPTION = '%s' ", desc));
        return powerEntities[0];
    }

    @Override
    public PowerEntity setPower(String url,String description,Boolean bool,Integer powerOfType) {
        PowerEntity[] powerEntities = ao.find(PowerEntity.class, String.format("URL = '%s' AND DESCRIPTION = '%s' ", url, description));
        PowerEntity powerEntity = null;
        if (powerEntities.length ==0){
         powerEntity=ao.create(PowerEntity.class);
            powerEntity.setDescription(description);
            powerEntity.setOperation(bool);
            powerEntity.setPowerOfType(powerOfType);
            powerEntity.setUrl(url);
            powerEntity.save();
        return powerEntity;
        }
        return powerEntities[0];
    }

}
