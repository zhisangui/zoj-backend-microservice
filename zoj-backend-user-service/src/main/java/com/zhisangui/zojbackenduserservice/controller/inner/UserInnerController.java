package com.zhisangui.zojbackenduserservice.controller.inner;

import com.zhisangui.zojbackendserviceclient.service.UserFeignClient;
import com.zhisangui.zojbackenduserservice.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zojbackendmodel.model.entity.User;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClient {
    @Resource
    private UserService userService;

    @Override
    @GetMapping("/get/id")
    public User getUserById(Long userId) {
        return userService.getById(userId);
    }

    @Override
    @GetMapping("get/ids")
    public List<User> listByIds(Collection<Long> idList) {
        return userService.listByIds(idList);
    }
}
