package com.redmath.newsproject.Controller;


import com.redmath.newsproject.Model.news;
import com.redmath.newsproject.Service.NewsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/")
public class NewsController {

    NewsService newsService;
    NewsController(NewsService service){
        this.newsService=service;
    }

    //  Get All News
    @GetMapping("/news")
    public List<news> getAllNews() {
        return newsService.getallnews();
    }

    //   Get News by ID
    @GetMapping("/{id}")
    public news getNewsById(@PathVariable Long id) {
        return newsService.getNewsById(id);
    }

    //   Create News
    @PostMapping
    public news createNews(@RequestBody news newsItem) {
        return newsService.createNews(newsItem);
    }

    //   Update News
    @PutMapping("/{id}")
    public news updateNews(@PathVariable Long id, @RequestBody news updatedNews) {
        return newsService.UpdateNews(id, updatedNews);
    }

    //   Delete News
    @DeleteMapping("/{id}")
    public void deleteNews(@PathVariable Long id) {
        newsService.DeleteNews(id);
    }

    //  Search/Filter APIs
    @GetMapping("/by-title")
    public List<news> getByTitle(@RequestParam String title) {
        return newsService.getNewsByTitle(title);
    }

    @GetMapping("/by-author")
    public List<news> getByAuthor(@RequestParam String author) {
        return newsService.getNewsByAuthor(author);
    }

    @GetMapping("/by-category")
    public List<news> getByCategory(@RequestParam String category) {
        return newsService.getNewsByCategory(category);
    }

    @GetMapping("/by-content")
    public List<news> searchByContent(@RequestParam String keyword) {
        return newsService.searchNewsByContent(keyword);
    }

    @GetMapping("/by-date")
    public List<news> getByPublishedat(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date); // Format: yyyy-MM-dd
        return newsService.getNewsByPublishedat(parsedDate);
    }

    @GetMapping("/before-date")
    public List<news> getNewsBefore(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return newsService.getNewsPublishedBefore(parsedDate);
    }

    @GetMapping("/after-date")
    public List<news> getNewsAfter(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return newsService.getNewsPublishedAfter(parsedDate);
    }

    @GetMapping("/by-published")
    public List<news> getByPublished(@RequestParam boolean ispublished) {
        return newsService.getNewsByIspublished(ispublished);
    }
}
