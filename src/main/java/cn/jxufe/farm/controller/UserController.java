package cn.jxufe.farm.controller;

import cn.jxufe.farm.model.bean.EasyUIData;
import cn.jxufe.farm.model.bean.EasyUIDataPageRequest;
import cn.jxufe.farm.model.bean.Message;
import cn.jxufe.farm.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
