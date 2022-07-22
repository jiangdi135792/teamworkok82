package com.work.plugin.rest;

import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.work.plugin.ao.MemberAOService;
import com.work.plugin.ao.OrganizationAOService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Path("org")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class MemberResource {

    private final GlobalPermissionManager globalPermissionManager;
    private final OrganizationAOService orgAOService;
    private final MemberAOService memberAOService;
    private final JiraAuthenticationContext jiraAuthenticationContext;


    @GET
    @Path("{orgId}/member/{type}/")
    public Response all(@PathParam("orgId") final int orgId,@PathParam("type") final int type) {
        //  if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, jiraAuthenticationContext.getLoggedInUser()))
        if (null==jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();

        return Response.ok(
                Stream.of(orgAOService.get(orgId).getMembers())
                        .filter(e -> e.getType() == type)
                        .map(e -> new MemberBean(e.getID(), e.getUserKey(),e.getMenuId(),e.getType()))
                        .toArray(MemberBean[]::new))
                .build();
    }


    @POST
    @Path("{orgId}/member/{type}")
    public Response create(@PathParam("orgId") final int orgId,@PathParam("type") final int type, final MemberBean bean) {
        //  if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, jiraAuthenticationContext.getLoggedInUser()))
        if (null==jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();

        val res = memberAOService.create(orgId, bean.getUserKey(),orgAOService.get(orgId).getMenuNameid(),type);
        if (Objects.nonNull(res.getRight()))
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorBean(res.getRight())).build();

        val entity = res.getLeft();
        return Response.ok(new MemberBean(entity.getID(), entity.getUserKey(),entity.getMenuId(),entity.getType())).build();
    }

    @DELETE
    @Path("{orgId}/member/{type}/{id}")
    public Response delete(@PathParam("orgId") final int orgId, @PathParam("id") final int id) {
      //  if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, jiraAuthenticationContext.getLoggedInUser()))
        if (null==jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();

        memberAOService.delete(id);
        return Response.ok().build();
    }

    @XmlRootElement
    @NoArgsConstructor
    @AllArgsConstructor
    public class ErrorBean {
        @XmlElement
        private Map<String, String> errors;
    }
}