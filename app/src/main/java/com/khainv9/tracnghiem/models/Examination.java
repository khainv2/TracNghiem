package com.khainv9.tracnghiem.models;

import org.msgpack.annotation.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Message
public class Examination {

    public int id;
    public String name;

    public long createdTime;

    public int chapterACount;
    public int chapterBCount;
    public int chapterCCount;

    public List<QuestionPaper> questionPapers;

    public Examination(){}

    public Examination(String name, int chapterACount, int chapterBCount, int chapterCCount) {
        this.name = name;
        this.createdTime = System.currentTimeMillis();
        this.id = (int) (createdTime / 1000);

        questionPapers = new ArrayList<>();

        this.chapterACount = chapterACount;
        this.chapterBCount = chapterBCount;
        this.chapterCCount = chapterCCount;
    }

    public long getLNgayTao() {
        return createdTime;
    }
}
