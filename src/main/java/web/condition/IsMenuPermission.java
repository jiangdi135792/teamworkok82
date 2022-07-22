package web.condition;

import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.Lists;
import com.work.plugin.ao.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;


@RequiredArgsConstructor
public class IsMenuPermission  implements Condition {
    private final GroupManager groupManager;
    private final MemberAOService memberAOService;
    private final StrEmployeeService strEmployeeService;
    private final OrganizationAOService orgAOService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private String mMenuID="";

   /* public IsMenuPermission(ApplicationProperties applicationProperties) {

        String baseUrl = applicationProperties.getBaseUrl();



    }*/


    public void init(final Map<String, String> paramMap) throws PluginParseException {
        mMenuID = paramMap.get("menuID");


    }

    public boolean shouldDisplay(final Map<String, Object> context) {
        ApplicationUser user = (ApplicationUser)context.get("user");
        JiraHelper helper = (JiraHelper)context.get("helper");
        if(helper.getRequest().getCookies() == null){
            return false;
        }

        String[] str = Lists.newArrayList(helper.getRequest().getCookies()).stream().map(e -> e.getName()+"="+e.getValue()).toArray(String[]::new);

        String sessionId = StringUtils.join(str,";");

       if(user==null)return false;//no login

        if(null==mMenuID|| mMenuID.isEmpty())
        {
            return true;

        }


        else
        {
/*   remove to IssueCreatedEventListener Onstart
            if("permissionMgr".equals(mMenuID))
            {
                //initial menu resource and user
                //  memberAOService.create(""，"");
                if(!orgAOService.isExistpermissionMgr())
                {
                    {
                        val entity = orgAOService.create("Reports", "company staff incumbency", "gsryld");
                        memberAOService.create(entity.getID(), "admin", entity.getMenuNameid(), 1);
                        memberAOService.create(entity.getID(), "ADMINISTER_KEY", entity.getMenuNameid(), 2);
                    }
                    {
                        val entity = orgAOService.create("Reports", "department staff distribution", "bmryfb");
                        memberAOService.create(entity.getID(), "admin", entity.getMenuNameid(), 1);
                        memberAOService.create(entity.getID(), "ADMINISTER_KEY", entity.getMenuNameid(), 2);
                    }
                    {
                        val entity = orgAOService.create("management", "organization manage", "zzjggl");
                        memberAOService.create(entity.getID(), "admin", entity.getMenuNameid(), 1);
                        memberAOService.create(entity.getID(), "ADMINISTER_KEY", entity.getMenuNameid(), 2);
                    }
                    {
                        val entity = orgAOService.create("management", "permission  manage", "permissionMgr");
                        memberAOService.create(entity.getID(), "admin", entity.getMenuNameid(), 1);
                        memberAOService.create(entity.getID(), "ADMINISTER_KEY", entity.getMenuNameid(), 2);
                    }
                    {
                        val entity = orgAOService.create("management", "help", "help");
                        memberAOService.create(entity.getID(), "admin", entity.getMenuNameid(), 1);
                        memberAOService.create(entity.getID(), "ADMINISTER_KEY", entity.getMenuNameid(), 2);
                    }



                }



             //  MemberEntity[] memberEntities =  orgAOService.getMemberOrRole(2);
               // for(MemberEntity memberEntity : memberEntities) {
               //     if (userManager.isUserInGroup(jiraAuthenticationContext.getLoggedInUser().getKey(),memberEntity.getUserKey())) {
                    //    return true;
                  //  }
              //  }

                // return Response.ok(new OrganizationBean(entity.getID(), entity.getFunction1(),entity.getFunction2(),entity.getMenuNameid(), entity.getMembers().length)).build();


            }
        */
            if(null==user) return false;

           if("permissionMgr".equals(mMenuID)&&(user.getId()==10000||"admin".equals(user.getKey())))
               return true;




           if (memberAOService.isExistMenu(user.getKey(),mMenuID)){

                return true;

           }
           else {
               if (isRoleExitMenu(mMenuID,user.getKey())){
                   return true;
               }
               return false;
           }

        }

    }
    public boolean isRoleExitMenu(final String jsessionid,String mMenuID, String userKey){

      //  MemberEntity[] memberEntities = memberAOService.getRoleList(mMenuID);

      //  for (MemberEntity memberEntity : memberEntities) {
           // String s = Http.sendGet("http://localhost:2990/jira/rest/api/2/group/member", "groupname=" + memberEntity.getUserKey(),jsessionid);
          //  JsonArray jsonArray =  new JsonParser().parse(s).getAsJsonObject().get("values").getAsJsonArray();
           // Iterator iterator = jsonArray.iterator();
            //while(iterator.hasNext()) {
              //  JsonObject jo = (JsonObject) iterator.next();
               // if (jo.get("key").toString() == userKey){
                  //  return true;
                //}
           // }
        MemberEntity[] memberEntities = memberAOService.getRoleList(mMenuID);
        for (MemberEntity memberEntity : memberEntities) {
        Collection<ApplicationUser> d=groupManager.getUsersInGroup( memberEntity.getUserKey());
             Iterator iterator = d.iterator();
            while(iterator.hasNext()) {
                ApplicationUser jo = (ApplicationUser) iterator.next();
            if(jo.getKey().equals(userKey)){
                return true;
            }

            }
        }

        return false;
    }

    private boolean isRoleExitMenu(String mMenuID, String userKey){
        MemberEntity[] memberEntities = memberAOService.getRoleList(mMenuID);
        Set<RoleEntity> roleByJiraUserKey = strEmployeeService.getRoleByJiraUserKey(userKey);
        Set<String> roleName = new HashSet<>();
        if (roleByJiraUserKey.size() != 0) {
            roleByJiraUserKey.stream().forEach(s -> {
                roleName.add(s.getName());
            });
            for (MemberEntity memberEntity : memberEntities) {
                if (!roleName.add(memberEntity.getUserKey())) {
                    return true;
                }
            }
        } else {
            return false;
        }
        return false;
    }

}