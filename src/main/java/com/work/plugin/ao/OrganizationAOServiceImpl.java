package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.java.ao.DBParam;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public final class OrganizationAOServiceImpl implements OrganizationAOService {

    private final ActiveObjects ao;

    @Override
    public OrganizationEntity get(int id) {
        return ao.get(OrganizationEntity.class, id);
    }

    @Override
    public OrganizationEntity[] all() {
        return ao.find(OrganizationEntity.class);
    }

    @Override
    public OrganizationEntity create(String function1,String function2,String menuNameid) {
        return ao.create(OrganizationEntity.class,
                new DBParam(OrganizationEntity.COLUMN.FUNCTION1.name(), function1),
                new DBParam(OrganizationEntity.COLUMN.FUNCTION2.name(), function2),
                new DBParam(OrganizationEntity.COLUMN.MENU_NAMEID.name(),menuNameid)
                );
    }

    @Override
    public OrganizationEntity update(int id,String function1,String function2,String menuNameid) {
       val entity = ao.get(OrganizationEntity.class, id);
        entity.setFunction2(Objects.nonNull(function2) ? function2 : entity.getFunction2());
        entity.setFunction1(Objects.nonNull(function1) ? function1 : entity.getFunction1());
        entity.setMenuNameid(Objects.nonNull(menuNameid)? menuNameid : entity.getMenuNameid());
        entity.save();
        return entity;


    }

    @Override
    public void delete(int id) {
        val entity = ao.get(OrganizationEntity.class, id);

        // delete members belong to the organization.
        for (MemberEntity e: entity.getMembers())
            ao.delete(ao.get(MemberEntity.class, e.getID()));

        // delete the organization.
        ao.delete(entity);
    }

    @Override
    public OrganizationEntity get(String menuNameId) {
        OrganizationEntity[] organizationEntities=ao.find(OrganizationEntity.class, String.format("MENU_NAMEID = '%s' ", menuNameId));
        for (OrganizationEntity organizationEntity:organizationEntities){
            return organizationEntity;
        }
        return null;
    }

    public  boolean isExistpermissionMgr(String menuId)
    {

        if (ao.count(OrganizationEntity.class,
                String.format("%s = ? and 1=1", OrganizationEntity.COLUMN.MENU_NAMEID),
                /*"permissionMgr"*/menuId) > 0)
            return true;
        else
            return false;


    }
    @Override
    public void mainPowerAndMenu(PowerEntity powerEntity, OrganizationEntity organizationEntity) {
        OrganizationAndPowerEntity[] organizationAndPowerEntities = ao.find(OrganizationAndPowerEntity.class, String.format("ORGANIZATION_ID = '%d' AND POWER_ID = '%d' ", organizationEntity.getID(), powerEntity.getID()));
        if (organizationAndPowerEntities.length != 0){
            if (organizationAndPowerEntities.length >1){
                IntStream.range(0,organizationAndPowerEntities.length-1).forEach(s ->{
                    ao.delete(Arrays.asList(organizationAndPowerEntities).get(s));
                });
            }
        }else {
        OrganizationAndPowerEntity organizationAndPowerEntity = ao.create(OrganizationAndPowerEntity.class);
        organizationAndPowerEntity.setOrganization(organizationEntity);
        organizationAndPowerEntity.setPower(powerEntity);
        organizationAndPowerEntity.save();
        }
    }


}

