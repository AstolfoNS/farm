package cn.jxufe.farm.bean.vo;

import java.io.Serializable;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getExperience() {
        return experience;
    }

    public void setExperience(Long experience) {
        this.experience = experience;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Long getCoin() {
        return coin;
    }

    public void setCoin(Long coin) {
        this.coin = coin;
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

    public Boolean getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}
