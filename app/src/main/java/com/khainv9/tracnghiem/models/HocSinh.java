package com.khainv9.tracnghiem.models;

import org.msgpack.annotation.Message;



@Message
public class HocSinh {
    public String sbd;
    public String name;
    public String class1;

    public HocSinh() {
    }

    public HocSinh(String sbd, String name, String class1) {
        this.sbd = sbd;
        this.name = name;
        this.class1 = class1;
    }
}
