package cn.jxufe.farm.controller;

import cn.jxufe.farm.model.bean.EasyUIData;
import cn.jxufe.farm.model.bean.EasyUIDataPageRequest;
import cn.jxufe.farm.model.bean.Message;
import cn.jxufe.farm.model.entity.User;
import cn.jxufe.farm.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EasyUIData list() {
        return userService.list();
    }

    @RequestMapping(value = "/gridDataFilterSortPage", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EasyUIData gridDataFilterSortPageGet(@RequestParam(required = false, defaultValue = "") String name,
                                                EasyUIDataPageRequest pageRequest) {
        return userService.gridDataFilterSortPage(name, pageRequest);
    }

    @PostMapping(value = "/gridDataFilterSortPage", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EasyUIData gridDataFilterSortPage(@RequestParam(required = false, defaultValue = "") String name,
                                             EasyUIDataPageRequest pageRequest) {
        return userService.gridDataFilterSortPage(name, pageRequest);
    }

    @PostMapping(value = "/addOrUpdate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message addOrUpdate(@RequestParam Map<String, String> params) {
        return userService.addOrUpdate(params);
    }

    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message delete(@RequestParam("id") Long id) {
        return userService.delete(id);
    }

    @PostMapping(value = "/updateAvatar", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message updateAvatar(@RequestParam("id") Long id,
                                @RequestParam("avatarPath") String avatarPath) {
        return userService.updateAvatar(id, avatarPath);
    }

    @GetMapping(value = "/loginOptions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> loginOptions() {
        return userService.loginUserOptions();
    }

    @PostMapping(value = "/setCurUser", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message setCurUser(HttpSession session, @RequestBody User user) {
        return userService.setCurUser(session, user);
    }

    @RequestMapping(value = "/getCurUser", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Message getCurUser(HttpSession session) {
        return userService.getCurUser(session);
    }
}
