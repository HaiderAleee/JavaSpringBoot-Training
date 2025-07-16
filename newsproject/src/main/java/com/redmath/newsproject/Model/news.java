package com.redmath.newsproject.Model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity


public class news {

    @Id
    private long id;
    private String title;
    private String content;
    private String author;
    private String category;
    private LocalDate publishedat;
    private Boolean ispublished;


}


