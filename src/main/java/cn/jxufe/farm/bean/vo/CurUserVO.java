package cn.jxufe.farm.bean.vo;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
public class CurUserVO implements Serializable {

    private Long id;
    private String username;
    private String nickname;
    private Long experience;
    private Long score;
    private Long coin;
    private String avatarPath;
    private String head;
    private Boolean loggedIn;
    private Map<String, String> defaultAssets;
}
