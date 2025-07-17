package com.redmath.newsproject.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmath.newsproject.Model.news;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllNews() throws Exception {
        mockMvc.perform(get("/api/news"))
                .andExpect(status().isFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", Matchers.hasSize(2)));
    }



    @Test
    public void testCreateNews() throws Exception {
        news newNews = new news();
        newNews.setTitle("New News");
        newNews.setContent("Content here");
        newNews.setAuthor("Tester");
        newNews.setCategory("Test");
        newNews.setPublishedat(LocalDate.now());
        newNews.setIspublished(false);

        mockMvc.perform(post("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newNews)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", Matchers.is("New News")))
                .andExpect(jsonPath("$.author", Matchers.is("Tester")));
    }

    @Test
    public void testUpdateNews() throws Exception {
        news updatedNews = new news();
        updatedNews.setTitle("Updated Title");
        updatedNews.setContent("Updated content");
        updatedNews.setAuthor("Updated Author");
        updatedNews.setCategory("Updated Category");
        updatedNews.setPublishedat(LocalDate.now());
        updatedNews.setIspublished(true);

        mockMvc.perform(put("/api/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedNews)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", Matchers.is("Updated Title")))
                .andExpect(jsonPath("$.author", Matchers.is("Updated Author")));
    }

    @Test
    public void testDeleteNews() throws Exception {
        mockMvc.perform(delete("/api/2"))
                .andExpect(status().isOk());
    }


    @Test
    public void testGetByAuthor() throws Exception {
        mockMvc.perform(get("/api/by-author").param("author", "Haider Ali"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author", Matchers.is("Haider Ali")));
    }

    @Test
    public void testGetByCategory() throws Exception {
        mockMvc.perform(get("/api/by-category").param("category", "AI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category", Matchers.is("AI")));
    }

    @Test
    public void testSearchByContent() throws Exception {
        mockMvc.perform(get("/api/by-content").param("keyword", "OpenAI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content", Matchers.containsString("OpenAI")));
    }

    @Test
    public void testGetByPublishedat() throws Exception {
        mockMvc.perform(get("/api/by-date").param("date", "2025-07-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publishedat", Matchers.is("2025-07-15")));
    }


    @Test
    public void testGetByPublished() throws Exception {
        mockMvc.perform(get("/api/by-published").param("ispublished", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(1))));
    }
}
