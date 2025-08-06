package com.poly.service;

import com.poly.entity.Video;
import com.poly.entity.Course;
import com.poly.repository.CourseRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class VideoExcelService {

    @Autowired
    private CourseRepository courseRepository;

    public List<Video> importVideosFromExcel(InputStream inputStream, Long courseId) throws IOException {
        List<Video> videos = new ArrayList<>();
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            throw new IllegalArgumentException("Khóa học không tồn tại.");
        }

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Bỏ qua hàng tiêu đề

                String tenVideo = getCellString(row, 0, formatter);
                String duongDan = getCellString(row, 1, formatter);

                // Kiểm tra dữ liệu hợp lệ
                if (tenVideo.isEmpty() || duongDan.isEmpty()) {
                    continue; // Bỏ qua hàng không hợp lệ
                }

                Video video = new Video();
                video.setTen_video(tenVideo);
                video.setDuong_dan_video(duongDan);
                video.setCourse(course);
                videos.add(video);
            }
        }
        if (videos.isEmpty()) {
            throw new IllegalArgumentException("Không có video hợp lệ được nhập từ file Excel.");
        }
        return videos;
    }

    public byte[] exportVideosToExcel(List<Video> videos) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Videos");
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true); // Tự động xuống dòng cho nội dung dài

            // Tạo hàng tiêu đề
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Tên video");
            header.createCell(1).setCellValue("Đường dẫn video");

            // Điền dữ liệu
            int rowIdx = 1;
            for (Video video : videos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(video.getTen_video());
                row.createCell(1).setCellValue(video.getDuong_dan_video());
            }

            // Tự động điều chỉnh độ rộng cột
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                workbook.write(out);
                return out.toByteArray();
            }
        }
    }

    private String getCellString(Row row, int col, DataFormatter formatter) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return formatter.formatCellValue(cell).trim();
    }
}