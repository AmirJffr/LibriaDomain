package com.libria.domain;

public class Member extends User {
    public Member(String id, String name, String email, String password) {
        super(id, name, email, password);
    }

    @Override public String getRole() { return "MEMBER"; }
}
