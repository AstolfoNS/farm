package cn.jxufe.farm.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private int code;
    private String msg;
    private Object data;
}