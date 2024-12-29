package com.zhisangui.zojbackendserviceclient.service;

import com.zhisangui.zojbackendcommon.common.ErrorCode;
import com.zhisangui.zojbackendcommon.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zojbackendmodel.model.entity.User;
import zojbackendmodel.model.enums.UserRoleEnum;
import zojbackendmodel.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static com.zhisangui.zojbackendcommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务
 *
 * @author <a href="https://github.com/zhisangui">zsg</a>
 */
@FeignClient(name = "zoj-backend-user-service", path = "/api/user/inner")
public interface UserFeignClient {

    /**
     * 根据id获取用户
     *
     * @param
     * @return
     */
    @GetMapping("/get/id")
    User getUserById(@RequestParam("userId") Long userId);

    /**
     * 根据id列表获取用户
     */
    @GetMapping("get/ids")
    List<User> listByIds(@RequestParam("idList") Collection<Long> idList);

    /**
     * 获取登录用户
     *
     * @param request
     * @return
     */
    default User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    default boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 获得脱敏的用户信息
     *
     * @param user
     * @return
     */
    default UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

}
