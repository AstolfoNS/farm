package cn.jxufe.farm.service;

import cn.jxufe.farm.model.bean.EasyUIData;
import cn.jxufe.farm.model.bean.EasyUIDataPageRequest;
import cn.jxufe.farm.model.bean.Message;
import cn.jxufe.farm.model.entity.User;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

public interface UserService {

    EasyUIData list();

    EasyUIData gridDataFilterSortPage(String name, EasyUIDataPageRequest pageRequest);

    Message addOrUpdate(Map<String, String> params);

    Message delete(Long id);

    Message updateAvatar(Long id, String avatarPath);

    List<Map<String, Object>> loginUserOptions();

    Message setCurUser(HttpSession session, User user);

    Message getCurUser(HttpSession session);
}
