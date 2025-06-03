package com.poly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.entity.Video;

public interface VideoRepository extends JpaRepository<Video, Long> {
//    List<Video> findByCourseId(Long ID_khoa_hoc);
}