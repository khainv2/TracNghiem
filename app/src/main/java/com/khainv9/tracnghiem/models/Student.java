package com.khainv9.tracnghiem.models;

import org.msgpack.annotation.Message;



@Message
public class Student {
    public String id;
    public String name;
    public String class1;

    public Student() {
    }

    public Student(String id, String name, String class1) {
        this.id = id;
        this.name = name;
        this.class1 = class1;
    }
}
