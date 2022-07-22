package com.work.plugin.rest;

import com.atlassian.annotations.security.XsrfProtectionExcluded;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.collect.Lists;
import com.work.plugin.ao.OrganizationAOService;
import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Stream;

@Path("/")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class OrganizationResource {

    private final GlobalPermissionManager globalPermissionManager;
    private final OrganizationAOService orgAOService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WebResourceManager webResourceManager;
    @GET
    public Response all() {
      //  if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, jiraAuthenticationContext.getLoggedInUser()))
        if (null==jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();
        I18nHelper i18nHelper=jiraAuthenticationContext.getI18nHelper();
        //lombok.config
        //lombok.var.flagUsage = ALLOW;
      // var entity= orgAOService.all();
     //   entity[0].setFunction1(i18nHelper.getText("workorg.property.menu."+entity[0].getFunction1()));
        return Response.ok(
                Stream.of(orgAOService.all())
                        .map(e -> new OrganizationBean(e.getID(), e.getFunction1(),
                                (i18nHelper.getText("workorg.property.menu."+e.getMenuNameid()).equals("workorg.property.menu."+e.getMenuNameid()))?e.getFunction2():i18nHelper.getText("workorg.property.menu."+e.getMenuNameid()),e.getMenuNameid(),
                                Lists.newArrayList(e.getMembers()).stream().filter(s -> s.getType() == 2).toArray().length,
                                Lists.newArrayList(e.getMembers()).stream().filter(s -> s.getType() == 1).toArray().length))
                        .toArray(OrganizationBean[]::new))
                .build();
    }

    
    @POST
    @XsrfProtectionExcluded
    public Response create(final OrganizationBean bean) {
       // if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, jiraAuthenticationContext.getLoggedInUser()))
        if (null==jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();

        val entity = orgAOService.create("",bean.getName(),"gsryld");
        return Response.ok(new OrganizationBean(entity.getID(), entity.getFunction1(),entity.getFunction2(),entity.getMenuNameid(), Lists.newArrayList(entity.getMembers()).stream().filter(s -> s.getType() == 2).toArray().length,
                Lists.newArrayList(entity.getMembers()).stream().filter(s -> s.getType() == 1).toArray().length)).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") final int id, final OrganizationBean bean) {
       // if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, jiraAuthenticationContext.getLoggedInUser()))
        if (null==jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();

        val entity = orgAOService.update(id,"",bean.getName(),"gsryld");
        return Response.ok(new OrganizationBean(entity.getID(), entity.getFunction1(),entity.getFunction2(),entity.getMenuNameid(), Lists.newArrayList(entity.getMembers()).stream().filter(s -> s.getType() == 2).toArray().length,
                Lists.newArrayList(entity.getMembers()).stream().filter(s -> s.getType() == 1).toArray().length)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") final int id) {
        //if (!globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, jiraAuthenticationContext.getLoggedInUser()))
        if (null==jiraAuthenticationContext.getLoggedInUser())
            return Response.status(Response.Status.FORBIDDEN).build();

        orgAOService.delete(id);
        return Response.ok().build();
    }
}