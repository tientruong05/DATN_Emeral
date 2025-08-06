package com.poly.service;

import com.poly.entity.Category;
import com.poly.entity.Video;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VideoService {
    List<Video> findAll();
    Video findById(Long id);
    Video save(Video video);
    void delete(Long id);
    List<Video> findByCourseID_khoa_hoc(Long courseId);
    

    
    void deleteOldVideos(Long courseId); // Add this method
    void saveNewVideos(List<Video> videos); // Add this method
}