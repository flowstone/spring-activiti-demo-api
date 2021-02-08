package me.xueyao.config;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.xueyao.entity.SysUser;
import me.xueyao.mapper.SysUserMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Simon.Xue
 * @date 2019-11-22 16:13
 **/
@Slf4j
public class MyShiroRealm extends AuthorizingRealm {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        return new SimpleAuthorizationInfo();
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        log.info("token = {}", JSONObject.toJSONString(token));

        String username = (String) token.getPrincipal();
        System.out.println(token.getCredentials());

        SysUser sysUser = sysUserMapper.selectUserByLoginName(username);
        log.info("sysUser = {}", JSONObject.toJSONString(sysUser));
        if (null == sysUser) {
            return null;
        }

        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                ////这里传入的是user对象，比对的是用户名，直接传入用户名也没错，但是在授权部分就需要自己重新从数据库里取权限
                sysUser,
                //密码
                sysUser.getPassword(),
                //不需要盐了
                //ByteSource.Util.bytes(userVo.getUser().getSalt()),
                getName()
        );
        return authenticationInfo;
    }

    /**
     * 清理缓存权限
     */
    public void clearCachedAuthorizationInfo()
    {
        this.clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
    }
}
