package com.work.plugin.rest;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.work.plugin.ao.RoleEntity;
import com.work.plugin.ao.RoleService;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by work on 2022/5/23.
 */
@Path("role")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@RequiredArgsConstructor
public class RoleResource {
    private final RoleService roleService;
    /*------------------------*/
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final  UserManager userManager;
    private final IssueManager issueManager;
    private final ProjectManager projectManager;
    /*------------------------*/
    @GET
    @Path("getAll")
    public Response getAllRole(){
        List<RoleEntity> all = roleService.getAll();
        List<RoleEntity> collect = all.stream().filter(s -> {
            if (s.getType() == 1) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
        RoleBean[] result = collect.stream()
                .map(e -> new RoleBean(e.getID(), e.getName()))
                .toArray(RoleBean[]::new);
        return Response.ok(result).build();
    }



    @GET
    @Path("createIssue")
    @AnonymousAllowed
    public Response getColumn(){
        if (null==jiraAuthenticationContext.getLoggedInUser()){
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        IssueFactory issueFactory = ComponentAccessor.getIssueFactory();
        Project project = projectManager.getProjectByCurrentKey("RH");
        IntStream.range(0,10).forEach(num ->{
            MutableIssue issue = issueFactory.getIssue();
            issue.setIssueTypeId("10000");
            issue.setProjectObject(project);
            issue.setReporter(userManager.getUser("adminmm"));
            issue.setSummary("issue_" + num);
            try {
                issueManager.createIssueObject(jiraAuthenticationContext.getLoggedInUser(), issue);
            } catch (CreateException e) {
                e.printStackTrace();
            }
        });
        return Response.ok().build();
    }
}
