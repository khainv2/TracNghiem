package com.khainv9.tracnghiem.models;

import org.msgpack.annotation.Message;



@Message
public class Student {
    public String id;
    public String name;
    public String iclass;

    public Student() {}
    public Student(String id, String name, String iclass) {
        this.id = id;
        this.name = name;
        this.iclass = iclass;
    }
}
