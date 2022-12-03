package com.xxxx.crm.model;

public class TreeModel {
    private Integer id;
    private Integer pId;
    private String name;

    public Integer getpId() {
        return pId;
    }

    public void setpId(Integer pId) {
        this.pId = pId;
    }

    private boolean checked = false;// 复选框是否被勾选。如果是true，则勾选；如果是false，则不勾选

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
