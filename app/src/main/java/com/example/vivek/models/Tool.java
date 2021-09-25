package com.example.vivek.models;

public class Tool {
    private String bgColor;
    private int drawable;

    private int f4854id;
    private String title;

    public Tool(int i, String str, String str2, int i2) {
        this.f4854id = i;
        this.title = str;
        this.bgColor = str2;
        this.drawable = i2;
    }

    public int getId() {
        return this.f4854id;
    }

    public void setId(int i) {
        this.f4854id = i;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public String getBgColor() {
        return this.bgColor;
    }

    public void setBgColor(String str) {
        this.bgColor = str;
    }

    public int getDrawable() {
        return this.drawable;
    }

    public void setDrawable(int i) {
        this.drawable = i;
    }
}
