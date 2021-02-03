package me.xueyao.config;

import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 重写用户权限
 * @author Simon.Xue
 * @date 2/1/21 2:13 PM
 **/
@Component
public class UserGroupManagerConfig implements UserGroupManager {

    public static List<String> roles = new ArrayList<>(3);
    public static List<String> groups = new ArrayList<>(1);
    public static List<String> users = new ArrayList<>(3);
    public static Map<String, String> userRoleMap = new HashMap<>(3);


    static  {
        roles.add("workCreate");
        roles.add("workPermit");
        roles.add("workLeader");

        groups.add("workGroupA");

        users.add("admin");
        users.add("laowang");
        users.add("xiaofang");

        userRoleMap.put("admin", "workCreate");
        userRoleMap.put("laowang", "workPermit");
        userRoleMap.put("xiaofang", "workLeader");


    }
    @Override
    public List<String> getUserGroups(String s) {
        return groups;
    }

    @Override
    public List<String> getUserRoles(String s) {
        String role = userRoleMap.get(s);
        List<String> list = new ArrayList<>();
        list.add(role);
        return list;
    }

    @Override
    public List<String> getGroups() {
        return groups;
    }

    @Override
    public List<String> getUsers() {
        return users;
    }
}
