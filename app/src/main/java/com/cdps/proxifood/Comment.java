package com.cdps.proxifood;

import java.util.Date;

public class Comment {
    String authorFirstname;
    String authorName;
    int mark;
    String comment;
    Date date;

    public Comment(String authorFirstname, String authorName, int mark, String comment, Date date) {
        this.authorFirstname = authorFirstname;
        this.authorName = authorName;
        this.mark = mark;
        this.comment =  comment;
        this.date = date;
    }

    public String getAuthorFirstname() {
        return this.authorFirstname;
    }

    public String getAuthorName() {
        return this.authorName;
    }

    public int getMark() {
        return this.mark;
    }

    public String getComment() {
        return this.comment;
    }

    public Date getDate() {
        return this.date;
    }
}
