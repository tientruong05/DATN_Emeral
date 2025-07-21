package com.poly.service;

import com.poly.entity.Video;
import com.poly.entity.Course;
import com.poly.repository.CourseRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class VideoExcelService {

    @Autowired
    private CourseRepository courseRepository;

    public List<Video> importVideosFromExcel(InputStream inputStream, Long courseId) throws IOException {
        List<Video> videos = new ArrayList<>();
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return videos;

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Bỏ qua header
            Video video = new Video();
            video.setTen_video(getCellString(row, 0));
            video.setDuong_dan_video(getCellString(row, 1));
            video.setCourse(course);
            videos.add(video);
        }
        workbook.close();
        return videos;
    }

    public byte[] exportVideosToExcel(List<Video> videos) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Videos");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Tên video");
        header.createCell(1).setCellValue("Đường dẫn video");

        int rowIdx = 1;
        for (Video v : videos) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(v.getTen_video());
            row.createCell(1).setCellValue(v.getDuong_dan_video());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        return cell != null ? cell.toString().trim() : "";
    }
}