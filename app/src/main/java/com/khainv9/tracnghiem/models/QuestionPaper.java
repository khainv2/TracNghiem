package com.khainv9.tracnghiem.models;

import org.msgpack.annotation.Message;

import java.util.Arrays;
import java.util.List;


@Message
public class QuestionPaper {
    public static final String NOT = "_";

    public String paperCode;
    public String[] answers;
    public int chapterACount;
    public int chapterBCount;
    public int chapterCCount;

    public QuestionPaper() {
    }

    public QuestionPaper(int chapterACount, int chapterBCount, int chapterCCount) {
        this.paperCode = "";
        this.chapterACount = chapterACount;
        this.chapterBCount = chapterBCount;
        this.chapterCCount = chapterCCount;
        answers = new String[chapterACount + (chapterBCount * 4) + (chapterCCount * 4)];
        Arrays.fill(answers, NOT);
    }


    public String[] chapterAAnswers(){
        String[] output = new String[chapterACount];
        System.arraycopy(this.answers, 0, output, 0, chapterACount);
        return output;
    }

    public String[] chapterBAnswers(){
        String[] output = new String[chapterBCount * 4];
        if (chapterBCount * 4 >= 0)
            System.arraycopy(this.answers, chapterACount, output, 0, chapterBCount * 4);
        return output;
    }

    public String[] chapterCAnswers(){
        String[] output = new String[chapterCCount * 4];
        for (int i = 0; i < chapterCCount * 4; i++) output[i] = this.answers[chapterACount + chapterBCount * 4 + i];
        return output;
    }

}
