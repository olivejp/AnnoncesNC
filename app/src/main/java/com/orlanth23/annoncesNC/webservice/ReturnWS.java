package com.orlanth23.annoncesNC.webservice;

public class ReturnWS {

    private String tag;
    private boolean status;
    private String msg;
    private Integer id;

    public ReturnWS(String tag, boolean status, String msg, Integer id) {
        this.tag = tag;
        this.status = status;
        this.msg = msg;
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean statusValid() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}