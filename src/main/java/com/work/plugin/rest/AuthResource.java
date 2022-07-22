package com.work.plugin.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.work.plugin.ao.RoleEntity;
import com.work.plugin.ao.RoleService;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2022/3/20.
 */
@Path("/auth")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class AuthResource {
    private final RoleService roleService;

    @GET
    @Path("getTeamRole")
    public Response getTeamRole() {
        List<RoleEntity> roleEntities = roleService.getTeamRoles();

        List<Map<String, Object>> data = Lists.newArrayList();
        roleEntities.forEach(roleEntity -> {
            Map<String, Object> map = Maps.newHashMap();
            map.put("id", roleEntity.getID());
            map.put("name", roleEntity.getName());
            data.add(map);
        });

        return Response.ok(data).build();
    }

    @POST
    @Path("updateRole")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public Response updateRole(@FormParam("roleId") Integer roleId,
                               @FormParam("teamId") Integer teamId,
                               @FormParam("employeeId") Integer employeeId,
                               @FormParam("teamemployeeId") Integer teamemployeeId
    ) {
        teamemployeeId = roleService.updateRole(roleId, teamId, employeeId, teamemployeeId);
        Map<String, Integer>  data = Maps.newHashMap();
//        data.put("teamemployeeId", teamemployeeId);
        return Response.ok(data).build();
    }

    @GET
    @Path("defaultRole")
    public Response defaultRole() {
        RoleEntity defaultTeamRole = roleService.getDefaultTeamRole();
        if (defaultTeamRole != null) {
            Map<String, Object> data = Maps.newHashMap();
            data.put("id", defaultTeamRole.getID());
            data.put("name", defaultTeamRole.getName());
            return Response.ok(data).build();
        }
        return Response.ok().build();
    }


}
