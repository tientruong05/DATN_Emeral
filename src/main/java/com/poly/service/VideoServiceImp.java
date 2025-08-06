package com.poly.service;

import com.poly.entity.Video;
import com.poly.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoServiceImp implements VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Override
    public List<Video> findAll() {
        return videoRepository.findAll();
    }

    @Override
    public Video findById(Long id) {
        return videoRepository.findById(id).orElse(null);
    }

    @Override
    public Video save(Video video) {
        return videoRepository.save(video);
    }

    @Override
    public void delete(Long id) {
        videoRepository.deleteById(id);
    }

    @Override
    public List<Video> findByCourseID_khoa_hoc(Long courseId) {
        return videoRepository.findVideosByCourseId(courseId);
    }

    @Override
    public void deleteOldVideos(Long courseId) {
        List<Video> videos = videoRepository.findVideosByCourseId(courseId);
        if (!videos.isEmpty()) {
            videoRepository.deleteAll(videos); // Use deleteAll for better performance
        }
    }

    @Override
    public void saveNewVideos(List<Video> videos) {
        videoRepository.saveAll(videos); // Bulk save for efficiency
    }

}