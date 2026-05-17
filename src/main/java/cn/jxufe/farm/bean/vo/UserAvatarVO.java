package cn.jxufe.farm.bean.vo;

import java.io.Serializable;

public class UserAvatarVO implements Serializable {

    private Long id;
    private String avatarPath;
    private String head;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }
}
