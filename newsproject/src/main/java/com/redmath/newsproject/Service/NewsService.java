package com.redmath.newsproject.Service;


import com.redmath.newsproject.Model.news;
import com.redmath.newsproject.Repository.NewsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.redmath.newsproject.Model.news;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class NewsService {

    NewsRepository repository;
    NewsService(NewsRepository NewsRepository)
    {
        this.repository=NewsRepository;
    }

    public List<news> getallnews()
    {
        log.info("This message is here to test logging");
        return repository.findAll();
    }

    public List<news> getnewsbytitle(String title)
    {
        return repository.findByTitle(title);
    }

    public news getNewsById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public news createNews(news newsItem) {
        return repository.save(newsItem);
    }


    public List<news> getNewsByTitle(String title) {
        return repository.findByTitle(title);
    }

    public List<news> getNewsByAuthor(String author) {
        return repository.findByAuthor(author);
    }

    public List<news> getNewsByCategory(String category) {
        return repository.findByCategory(category);
    }

    public List<news> searchNewsByContent(String keyword) {
        return repository.findByContentContaining(keyword);
    }

    public List<news> getNewsByPublishedat(LocalDate date) {
        return repository.findBypublishedat(date);
    }

    public List<news> getNewsPublishedBefore(LocalDate date) {
        return repository.findBypublishedatBefore(date);
    }

    public List<news> getNewsPublishedAfter(LocalDate date) {
        return repository.findBypublishedatAfter(date);
    }

    public List<news> getNewsByIspublished(boolean ispublished) {
        return repository.findByispublished(ispublished);
    }

    public news UpdateNews(Long id, news updated)
    {
        return repository.findById(id).map(existing->{
            existing.setTitle(updated.getTitle());
            existing.setContent(updated.getContent());
            existing.setAuthor(updated.getAuthor());
            existing.setCategory(updated.getCategory());
            existing.setPublishedat(updated.getPublishedat());
            existing.setIspublished(updated.getIspublished());
            return repository.save(existing);
        }).orElse(null);
    }

    public void DeleteNews(Long id)
    {
       repository.deleteById(id);

    }


}
