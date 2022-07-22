package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by work on 2022/7/16.
 */
@RequiredArgsConstructor
public class StrPowerRoleServiceImpl implements StrPowerRoleService {
    private final ActiveObjects ao;
    @Override
    public void createRelationOfPowerAndRole(RoleEntity roleEntity, PowerEntity powerEntity) {
        StrPowerRole[] strPowerRoles = ao.find(StrPowerRole.class, String.format("POWER_ENTITY_ID = '%d' AND ROLE_ENTITY_ID = '%d' ", powerEntity.getID(), roleEntity.getID()));
        if (strPowerRoles.length>0){
        }else {
        StrPowerRole strPowerRole = ao.create(StrPowerRole.class);
        strPowerRole.setRoleEntity(roleEntity);
        strPowerRole.setPowerEntity(powerEntity);
        strPowerRole.save();
        }
    }
    @Override
    public void delRelationOfPowerAndRole(RoleEntity roleEntity, PowerEntity powerEntity) {
        StrPowerRole[] strPowerRoles = ao.find(StrPowerRole.class, String.format("POWER_ENTITY_ID = '%d' AND ROLE_ENTITY_ID = '%d' ", powerEntity.getID(), roleEntity.getID()));
        if (strPowerRoles.length>0){
            Arrays.stream(strPowerRoles).forEach(strPowerRole -> ao.delete(strPowerRole));
        }
    }

    @Override
    public List<PowerEntity> getAllPowerOfSelf(Integer integer) {
        List<PowerEntity> list=new LinkedList<>();
        StrPowerRole[] strPowerRoles = ao.find(StrPowerRole.class, String.format("ROLE_ENTITY_ID = '%d' ", integer));
        Arrays.stream(strPowerRoles).forEach(strPowerRole -> {
            list.add(strPowerRole.getPowerEntity());
        });
        return list;
    }

    @Override
    public List<PowerEntity> getAllPowewr() {
        PowerEntity[] powerEntities = ao.find(PowerEntity.class, String.format("POWER_OF_TYPE = '%d' ", 2));
        List<PowerEntity> list=new LinkedList<>();
        Arrays.stream(powerEntities).forEach(strPowerRole -> {
            list.add(strPowerRole);
        });
        return list;
    }
}
