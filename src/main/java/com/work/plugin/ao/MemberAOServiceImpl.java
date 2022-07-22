package com.work.plugin.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

//import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberAOServiceImpl implements MemberAOService {
	private final ActiveObjects ao;
	private final CrowdService crowdService;

	@Override
	public Set<StrOrganize> getBy(String user) {
		Set<StrOrganize> names = Sets.newHashSet();

		ao.stream(StrOrganize.class,
				Query.select(StringUtils.join(StrOrganize.COLUMN.values(), ","))
						.where(String.format("MEMBER.%s = ?", MemberEntity.COLUMN.USER_KEY), user)
						.alias(OrganizationEntity.class, "ORG")
						.alias(MemberEntity.class, "MEMBER")
						.join(MemberEntity.class,
								String.format("ORG.%s = MEMBER.%s", OrganizationEntity.COLUMN.ID, MemberEntity.COLUMN.ORGANIZATION_ID)),
				entity -> names.add(entity));

		return names;
	}

	@Override
	public Pair<MemberEntity, Map<String, String>> create(int organization, String user, String menuId, int type) {
		// confirm the user
		if (Objects.isNull(crowdService.getUser(user)) && type == 1)
			return ImmutablePair.of(null, ImmutableMap.of("userKey", "The user specified is invalid."));

		// check existence of the member.
		if (ao.count(MemberEntity.class,
				String.format("%s = ? AND %s = ? AND %s = ?", MemberEntity.COLUMN.ORGANIZATION_ID, MemberEntity.COLUMN.USER_KEY, MemberEntity.COLUMN.TYPE),
				organization, user, type) > 0)
			return ImmutablePair.of(null, ImmutableMap.of("userKey", "No cannot be duplicate."));


		val entity = ao.create(MemberEntity.class,
				new DBParam(MemberEntity.COLUMN.ORGANIZATION_ID.name(), organization), new DBParam(MemberEntity.COLUMN.MENU_ID.name(), menuId),
				new DBParam(MemberEntity.COLUMN.USER_KEY.name(), user), new DBParam(MemberEntity.COLUMN.TYPE.name(), type));

		// new DBParam(MemberEntity.COLUMN.MENU_ID.name(),"")
		return ImmutablePair.of(entity, null);
	}

	@Override
	public void delete(int id) {
		ao.delete(ao.get(MemberEntity.class, id));
	}


	public boolean isExistMenu(String userkey, String menuId) {
		if (ao.count(MemberEntity.class,
				String.format("%s = ? AND %s = ?", MemberEntity.COLUMN.MENU_ID, MemberEntity.COLUMN.USER_KEY),
				menuId, userkey) > 0)
			return true;
		else
			return false;
	}

	public MemberEntity[] getRoleList(String mMenuID) {
		return ao.find(MemberEntity.class, "TYPE = 2 AND MENU_ID = '" + mMenuID + "'");
	}

	@Override
	public MemberEntity getByRoleAndMenu(String roleName, String menuName) {
		MemberEntity[] memberEntities = ao.find(MemberEntity.class, String.format("USER_KEY = '%s' AND MENU_ID = '%s' ", roleName, menuName));
		if (memberEntities.length != 0){
		return memberEntities[0];
		}else {
			return null;
		}
	}

	@Override
	public MemberEntity[] getMemberByOrganization(OrganizationEntity organizationEntity) {
		MemberEntity[] memberEntities = ao.find(MemberEntity.class, String.format("ORGANIZATION_ID = '%d '", organizationEntity.getID()));
		return memberEntities;
	}
}
