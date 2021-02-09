package me.xueyao.controller;

import me.xueyao.base.R;
import me.xueyao.entity.SysUser;
import me.xueyao.mapper.SysUserMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户操作
 * @author Simon.Xue
 * @date 2/7/21 4:40 PM
 **/
@RestController
public class SysUserController {

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/toLogin")
    public R toLogin(@RequestParam("username") String username,
                     @RequestParam("password") String password) {
        SysUser sysUser = sysUserMapper.selectUserByLoginName(username);
        if (null == sysUser) {
            return R.ofParam("用户或密码错误");
        }
        Subject currentUser = SecurityUtils.getSubject();

        password = new SimpleHash("md5", password, null, 2).toString();
        //将用户名和密码封装到UsernamePasswordToken
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        //认证
        currentUser.login(token);
        Session session = currentUser.getSession();
        session.setAttribute("username", username);
        return R.ofSuccess("验证成功");
    }

    /**
     * 未登录会跳转到该接口
     * @return
     */
    @GetMapping("/login")
    public R login() {
        return R.ofSuccess("您需要登录后操作");
    }
}
