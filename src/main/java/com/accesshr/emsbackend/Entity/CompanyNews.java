package com.accesshr.emsbackend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor

public class CompanyNews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
       @Column (name="newsId")
       int newsId;

       @Column (name="userName")
       String userName;

       @Column (name="userId")
       String userId;

       @Column (name="userEmail")
       String userEmail;

       @Column (name="newsHeading")
       String newsHeading;

       @Column (name="news")
       String news;

       @Column(name="createdAt")
       LocalDateTime createdAt;
}

