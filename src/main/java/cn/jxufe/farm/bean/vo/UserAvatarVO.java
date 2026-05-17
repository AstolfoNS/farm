package cn.jxufe.farm.bean.vo;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
public class UserAvatarVO implements Serializable {

    private Long id;
    private String avatarPath;
    private String head;
}
