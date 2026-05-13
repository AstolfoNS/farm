package cn.jxufe.farm.model.entity;

import cn.jxufe.farm.model.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "farm")
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, length = 500)
    private String username;

    @Column(name = "nickname", nullable = false, length = 500)
    private String nickname;

    @Column(name = "password_hash", nullable = false, length = 500)
    private String passwordHash;

    @Column(name = "email", nullable = false, length = 500)
    private String email;

    @Column(name = "avatar_url", nullable = false, length = 1024)
    private String avatarUrl;

    @Column(name = "experience", nullable = false)
    private Long experience;

    @Column(name = "score", nullable = false)
    private Long score;

    @Column(name = "coin", nullable = false)
    private Long coin;
}
