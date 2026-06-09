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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "用户模块", description = "用户 CRUD、会话管理、头像与设置")
@RestController
@RequestMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户列表", description = "获取所有未删除的用户列表")
    @GetMapping("/list")
    public R<PageResult<UserInfoVO>> list() {
        return R.ok(userService.list());
    }

    @Operation(summary = "用户分页查询", description = "按用户名模糊搜索，支持分页排序。name 为空时返回全部")
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

    @Operation(summary = "新增或更新用户", description = "id 为空或 <=0 时新增用户并初始化默认地块；id > 0 时更新已有用户")
    @PostMapping(value = "/addOrUpdate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<UserInfoVO> addOrUpdate(@Valid @RequestBody UserAddOrUpdateDTO params) {
        return R.ok(userService.addOrUpdate(params));
    }

    @Operation(summary = "删除用户", description = "按 id 软删除用户")
    @PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Void> delete(@Valid @RequestBody IdDTO params) {
        userService.delete(params);
        return R.ok();
    }

    @Operation(summary = "更新头像", description = "按用户 id 更新头像路径")
    @PostMapping(value = "/updateAvatar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<UserAvatarVO> updateAvatar(@Valid @RequestBody UserAvatarUpdateDTO params) {
        return R.ok(userService.updateAvatar(params));
    }

    @Operation(summary = "登录选项列表", description = "返回所有可用用户列表，供前端下拉选择登录")
    @GetMapping("/loginOptions")
    public R<List<UserInfoVO>> loginOptions() {
        return R.ok(userService.loginUserOptions());
    }

    @Operation(summary = "设置当前用户", description = "将指定用户写入 HTTP Session，实现简化登录。后续请求从 Session 读取 curUser")
    @PostMapping(value = "/setCurUser", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<CurUserVO> setCurUser(HttpSession session, @Valid @RequestBody SetCurUserDTO user) {
        return R.ok(userService.setCurUser(session, user));
    }

    @Operation(summary = "获取当前用户", description = "从 HTTP Session 读取当前登录用户的信息与资产")
    @GetMapping("/getCurUser")
    public R<CurUserVO> getCurUser(HttpSession session) {
        return R.ok(userService.getCurUser(session));
    }

    @Operation(summary = "获取用户设置", description = "从 Session 中读取当前用户的偏好设置（音效/背景音乐等）")
    @GetMapping("/settings/get")
    public R<UserSettingsVO> getCurUserSettings(HttpSession session) {
        return R.ok(userService.getCurUserSettings(session));
    }

    @Operation(summary = "保存用户设置", description = "保存当前用户的偏好设置。body 为空时使用默认值")
    @PostMapping(value = "/settings/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<UserSettingsVO> saveCurUserSettings(
            HttpSession session,
            @Valid @RequestBody(required = false) UserSettingsUpdateDTO params
    ) {
        UserSettingsUpdateDTO request = params == null ? new UserSettingsUpdateDTO() : params;
        return R.ok(userService.saveCurUserSettings(session, request));
    }
}
