package com.libria.domain;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MEMBER")
public class Member extends User {

    //pour que payara puisse linstancier
    public Member() {
        super();
    }
    public Member(String userId, String name, String email, String password) {
        super(userId, name, email, password);
    }

    @Override public String getRole() { return "MEMBER"; }
}
