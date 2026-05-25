package cn.jxufe.farm.controller;

import cn.jxufe.farm.bean.dto.PageQueryDTO;
import cn.jxufe.farm.bean.dto.SetCurUserDTO;
import cn.jxufe.farm.bean.dto.UserAddOrUpdateDTO;
import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.UserGridQueryDTO;
import cn.jxufe.farm.bean.dto.UserAvatarUpdateDTO;
import cn.jxufe.farm.bean.dto.UserSettingsUpdateDTO;
import cn.jxufe.farm.bean.vo.CurUserVO;
import cn.jxufe.farm.bean.vo.UserAvatarVO;
import cn.jxufe.farm.bean.vo.UserInfoVO;
import cn.jxufe.farm.bean.vo.UserSettingsVO;
import cn.jxufe.farm.common.pages.PageResult;
import cn.jxufe.farm.common.apis.R;
import cn.jxufe.farm.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public R<PageResult<UserInfoVO>> list() {
        return R.ok(userService.list());
    }

    @PostMapping(value = "/gridDataFilterSortPage", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<PageResult<UserInfoVO>> gridDataFilterSortPage(
            @Valid @RequestBody(required = false) UserGridQueryDTO query) {
        UserGridQueryDTO request = query == null ? new UserGridQueryDTO() : query;
        PageQueryDTO pageQuery = new PageQueryDTO();
        pageQuery.setPage(request.getPage());
        pageQuery.setRows(request.getRows());
        pageQuery.setSort(request.getSort());
        pageQuery.setOrder(request.getOrder());
        return R.ok(userService.gridDataFilterSortPage(request.getName(), pageQuery));
    }

    @PostMapping(value = "/addOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<UserInfoVO> addOrUpdate(@Valid @RequestBody UserAddOrUpdateDTO params) {
        return R.ok(userService.addOrUpdate(params));
    }

    @PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Void> delete(@Valid @RequestBody IdDTO params) {
        userService.delete(params);
        return R.ok();
    }

    @PostMapping(value = "/updateAvatar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<UserAvatarVO> updateAvatar(@Valid @RequestBody UserAvatarUpdateDTO params) {
        return R.ok(userService.updateAvatar(params));
    }

    @GetMapping("/loginOptions")
    public R<List<UserInfoVO>> loginOptions() {
        return R.ok(userService.loginUserOptions());
    }

    @PostMapping(value = "/setCurUser", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<CurUserVO> setCurUser(HttpSession session, @Valid @RequestBody SetCurUserDTO user) {
        return R.ok(userService.setCurUser(session, user));
    }

    @GetMapping("/getCurUser")
    public R<CurUserVO> getCurUser(HttpSession session) {
        return R.ok(userService.getCurUser(session));
    }

    @GetMapping("/settings/get")
    public R<UserSettingsVO> getCurUserSettings(HttpSession session) {
        return R.ok(userService.getCurUserSettings(session));
    }

    @PostMapping(value = "/settings/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<UserSettingsVO> saveCurUserSettings(
            HttpSession session,
            @Valid @RequestBody(required = false) UserSettingsUpdateDTO params
    ) {
        UserSettingsUpdateDTO request = params == null ? new UserSettingsUpdateDTO() : params;
        return R.ok(userService.saveCurUserSettings(session, request));
    }
}
