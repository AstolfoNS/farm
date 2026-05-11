package cn.jxufe.farm.service;

import cn.jxufe.farm.model.bean.EasyUIData;
import cn.jxufe.farm.model.bean.EasyUIDataPageRequest;
import cn.jxufe.farm.model.bean.Message;

import java.util.Map;

public interface UserService {

    EasyUIData list();

    EasyUIData gridDataFilterSortPage(String name, EasyUIDataPageRequest pageRequest);

    Message addOrUpdate(Map<String, String> params);

    Message delete(Long id);

    Message updateAvatar(Long id, String avatarPath);
}
