package web.filter;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.work.plugin.ao.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by work on 2022/3/13.
 */
@AllArgsConstructor
public class LongZhiFilter implements Filter {
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private StrEmployeeService strEmployeeService;
    private  final RoleService roleService;
    private  final PowerService powerService;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        //过滤器只是过滤功能权限  以及 数据权限   菜单权限 在分配过程中  已经做好了
        HttpServletRequest request=(HttpServletRequest)req;
        HttpServletResponse response=(HttpServletResponse) resp;
        ApplicationUser loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        String jiraBaseUrl = getJiraBaseUrl(request);
        if (null == loggedInUser) {
            String url = request.getRequestURI()+"/login.jsp?permissionViolation=true&amp;os_destination="+ request.getRequestURI();
            StringBuffer stringBuffer = new StringBuffer();
            String fullUrl =(stringBuffer.append(url).append("?").append(request.getQueryString())).toString();
            response.sendRedirect(jiraBaseUrl);
              return;
        } else {
            //获取当前用户的角色集合
            //获取当前访问的菜单
            String servletPath = request.getRequestURI();
            if (servletPath.contains("/latest/orgstr/")){
                String menuName = "zzjggl";
                String url ="/latest/orgstr/";
                Map<Integer,Boolean> statusPower = getStatus(loggedInUser, url, menuName);
                request.setAttribute("powerInfo",statusPower);
            }
            if(servletPath.contains("/latest/bmryfbState/controlPower")){
                String menuName = "bmryfb";
                String url = "/1/bmryfbState/bmryfb";
                Map<Integer,Boolean> statusPower = getStatus(loggedInUser, url, menuName);
                request.setAttribute("powerInfo",statusPower);
            }
            if(servletPath.contains("/latest/bmryfbState/getPower")){
                String menuName = "bmrygz";
                String url = "/1/bmryfbState/bmrygz";
                Map<Integer,Boolean> statusPower = getStatus(loggedInUser, url, menuName);
                request.setAttribute("powerInfo",statusPower);
            }
            if(servletPath.contains("/1/setreport/controlPower")){
                String menuName = "setReport";
                String url = "/1/setreport/";
                Map<Integer,Boolean> statusPower = getStatus(loggedInUser, url, menuName);
                request.setAttribute("powerInfo",statusPower);
            }
            if(servletPath.contains("/latest/showReport/controlPower")){
                String menuName = "showReport";
                String url = "/latest/showReport";
                Map<Integer,Boolean> statusPower = getStatus(loggedInUser, url, menuName);
                request.setAttribute("powerInfo",statusPower);
            }
            if(servletPath.contains("/latest/info/controlPower")){
                String menuName = "adsjtb";
                String url = "/latest/info/";
                Map<Integer,Boolean> statusPower = getStatus(loggedInUser, url, menuName);
                request.setAttribute("powerInfo",statusPower);
            }
            if(servletPath.contains("/1/gsryldState/controlPower")){
                String menuName = "gsryld";
                String url = "/1/gsryldState/";
                Map<Integer,Boolean> statusPower = getStatus(loggedInUser, url, menuName);
                request.setAttribute("powerInfo",statusPower);
            }
            if(servletPath.contains("/1/bmryfbState/getPower")){
                String menuName = "tdryfb";
                String url = "/1/bmryfbState/";
                Map<Integer,Boolean> statusPower = getStatus(loggedInUser, url, menuName);
                request.setAttribute("powerInfo",statusPower);
            }
            if(servletPath.contains("/1/bmryfbState/getPowerZ")){
                String menuName = "tdrygz";
                String url = "/1/bmryfbState/tdrygz";
                Map<Integer,Boolean> statusPower = getStatus(loggedInUser, url, menuName);
                request.setAttribute("powerInfo",statusPower);
            }
            if(servletPath.contains("/latest/power/controlPower")){
                String menuName = "qxkznew";
                String url = "/latest/power/";
                Map<Integer,Boolean> statusPower = getStatus(loggedInUser, url, menuName);
                request.setAttribute("powerInfo",statusPower);
            }
            if(servletPath.contains("/latest/lowerissues/controlPower")){
                String menuName = "lowerissues";
                String url = "/latest/lowerissues/getAll";
                Map<Integer,Boolean> statusPower = getStatus(loggedInUser, url, menuName);
                request.setAttribute("powerInfo",statusPower);
            }
            chain.doFilter(request,response);
        }

    }
    @Override
    public void destroy() {
    }

    private Map<Integer,Boolean> getStatus(ApplicationUser loggedInUser,String url,String menuName){
        Set<RoleEntity> roleSet = strEmployeeService.getRoleByJiraUserKey(loggedInUser.getKey());
        PowerEntity[] allPowerOfMenu = powerService.getAllPowerOfMenu(url);
        Map<Integer,Boolean> map = new HashMap<>();
        //组织机构管理页面  获取当前页面所有权限管理 并默认没有权限
        Stream.of(allPowerOfMenu).forEach(s -> {map.put(s.getID(),false);});
        for (RoleEntity roleEntity:roleSet){
                 /*获取当前所有角色在此菜单下的权限   判定哪些权限是限制的 (不包括下属 ，其他)*/
            if (isExit(roleEntity.getName(), menuName)) {
            MemberEntity menuDetailPower = roleService.getMenuDetailPower(roleEntity.getName(), menuName);
            String powerList = menuDetailPower.getPowerList();
            if (StringUtils.isNotBlank(powerList)) {
                String ownerPower = powerList.split("\\|")[2];
                String[] split = ownerPower.split(";");
                Stream.of(split).forEach(s -> {
                    String[] split1 = s.split(":");
                    if (Integer.valueOf(split1[1]) == 1) {
                        map.put(Integer.valueOf(split1[0]), true);
                    }
                });
            }
        }}
        return map;
    }//TODO 数据导入 优化 ScheduledExecutorService
    /**
     *
     * @param roleName 角色名字
     * @param menuName  菜单名字
     * @return
     */
    private boolean isExit(String roleName,String menuName){
        List<String> menuPowerByRoleId = roleService.getMenuPowerByRoleId(roleName);
        if (menuPowerByRoleId.contains(menuName)){
            return true;
        }else {
            return false;
        }
    }

    public static String getJiraBaseUrl(HttpServletRequest httpServletReq) {
        String requestUrlStr = null;
        try {
            if(httpServletReq != null){
                URL requestUrl = new URL(httpServletReq.getRequestURL().toString());
                requestUrlStr = requestUrl.getProtocol() + "://" + requestUrl.getAuthority();
            }
        } catch (MalformedURLException ex) {

        }
        String baseUrlFromAppProperties = ComponentAccessor.getApplicationProperties().getString("jira.baseurl");
        String baseUrl = baseUrlFromAppProperties;

        // now parse url and cut of host and port
        try {
            String baseUrlPath = new URL(baseUrlFromAppProperties).getPath();

            // if jira base url has a path e.g. titus.sec.intern/jira
            if(requestUrlStr != null){
                if(baseUrlPath.length() > 0){
                    requestUrlStr += baseUrlPath.charAt(0) == '/' ? baseUrlPath : "/" + baseUrlPath;
                }

                baseUrl = requestUrlStr;
            }
        } catch (Exception ex) {

        }

        return baseUrl;
    }
}
