package cn.jxufe.farm.service;

import cn.jxufe.farm.bean.dto.PageQueryDTO;
import cn.jxufe.farm.bean.dto.UserAddOrUpdateDTO;
import cn.jxufe.farm.bean.dto.IdDTO;
import cn.jxufe.farm.bean.dto.SetCurUserDTO;
import cn.jxufe.farm.bean.dto.UserAvatarUpdateDTO;
import cn.jxufe.farm.bean.vo.CurUserVO;
import cn.jxufe.farm.bean.vo.UserAvatarVO;
import cn.jxufe.farm.bean.vo.UserInfoVO;
import cn.jxufe.farm.common.pages.PageResult;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface UserService {

    PageResult<UserInfoVO> list();

    PageResult<UserInfoVO> gridDataFilterSortPage(String name, PageQueryDTO pageRequest);

    UserInfoVO addOrUpdate(UserAddOrUpdateDTO params);

    void delete(IdDTO params);

    UserAvatarVO updateAvatar(UserAvatarUpdateDTO params);

    List<UserInfoVO> loginUserOptions();

    CurUserVO setCurUser(HttpSession session, SetCurUserDTO user);

    CurUserVO getCurUser(HttpSession session);
}
