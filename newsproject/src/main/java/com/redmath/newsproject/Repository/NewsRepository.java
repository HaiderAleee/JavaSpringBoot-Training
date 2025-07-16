package com.redmath.newsproject.Repository;

import com.redmath.newsproject.Model.news;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<news,Long> {

    List<news> findByTitle(String title); // Spring will auto-implement this
    List<news> findByAuthor(String author); // Spring will auto-implement this
    List<news> findByCategory(String category); // Spring will auto-implement this
    List<news> findByContentContaining(String keyword);
    List<news> findBypublishedat(LocalDate publishedAt);
    List<news> findBypublishedatBefore(LocalDate date);
    List<news> findBypublishedatAfter(LocalDate date);
    List<news> findByispublished(boolean ispublished);


}