package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.CrowdService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.java.ao.DBParam;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

//import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LSDataAOServiceImpl implements LSDataAOService {
	private final ActiveObjects ao;
	private final CrowdService crowdService;



	public LSDataEntity[] getAll( ) {

			val entitylsDataEntity = ao.find(LSDataEntity.class);


			return entitylsDataEntity;

	}

	public LSDataEntity getByPP(String lsDataEntitygetP) {

		if (ao.count(LSDataEntity.class,
				String.format("%s = ? ", LSDataEntity.COLUMN.PP),
				lsDataEntitygetP) > 0) {
			val entitylsDataEntity = ao.find(LSDataEntity.class,
					String.format("%s = ? ", LSDataEntity.COLUMN.PP),
					lsDataEntitygetP);


			return entitylsDataEntity[0];

		} else
			return null;
	}
	public Pair<LSDataEntity, Map<String, String>>  update(String lsDataEntitygetP,String lsDataEntitygetCompany,String lsDataEntitygetV,String lsDataEntitygetKey) {
		LSDataEntity  rdentity=null;
		if (ao.count(LSDataEntity.class,
				String.format("%s = ? ", LSDataEntity.COLUMN.PP),
				lsDataEntitygetP) > 0)
		{
			val entitylsDataEntity= ao.find(LSDataEntity.class,
					String.format("%s = ? ", LSDataEntity.COLUMN.PP),
					lsDataEntitygetP);
			entitylsDataEntity[0].setCompany(lsDataEntitygetCompany);
			entitylsDataEntity[0].setVv(lsDataEntitygetV);
			entitylsDataEntity[0].setKeykey(lsDataEntitygetKey);
			entitylsDataEntity[0].save();
			rdentity=(LSDataEntity)entitylsDataEntity[0];
			//return ImmutablePair.of(entitylsDataEntity[0], null);
		}else {
			val entity = ao.create(LSDataEntity.class,
					new DBParam(LSDataEntity.COLUMN.PP.name(), lsDataEntitygetP), new DBParam(LSDataEntity.COLUMN.COMPANY.name(), lsDataEntitygetCompany),
					new DBParam(LSDataEntity.COLUMN.VV.name(), lsDataEntitygetV), new DBParam(LSDataEntity.COLUMN.KEYKEY.name(), lsDataEntitygetKey));
			rdentity=(LSDataEntity)entity;
			//return ImmutablePair.of(entity, null);
		}





		return ImmutablePair.of(rdentity, null);
	}
	@Override
	public void delete(int id) {
		ao.delete(ao.get(LSDataEntity.class, id));
	}

}
