package com.poly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.poly.entity.Course;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
//    @Query("SELECT c FROM Course c WHERE c.status = true ORDER BY RAND() LIMIT 4")
//    List<Course> findTop4ByStatusTrue();

}