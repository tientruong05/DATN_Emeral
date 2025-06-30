package com.poly.service;

import com.poly.entity.Category;
import com.poly.entity.Course;
import com.poly.entity.User;
import com.poly.repository.CategoryRepository;
import com.poly.repository.CourseRepository;
import com.poly.repository.UserRepository;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class CourseService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CategoryService cateService;

    public Page<Course> getAllCategories(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }
    
    public List<Category> getAllCate() {
        return categoryRepository.findAll();
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Course findById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }

    public void delete(Long id) {
        courseRepository.deleteById(id);
    }

    public List<Course> getRandomCourses(int limit) {
        return courseRepository.findTopNByStatusTrue(limit);
    }

    public List<Course> getCoursesByType(String type) {
        if (type == null || type.equals("all")) {
            return courseRepository.findTopNByStatusTrue(8);
        }
        return courseRepository.findByCategoryTenDanhMucAndStatusTrue(type);
    }

    public long getTotalStudents() {
        return courseRepository.countDistinctStudents();
    }

    public long getTotalCourses() {
        return courseRepository.countByStatusTrue();
    }

    public long getTotalInstructors() {
        return userRepository.countByVaiTroAndStatusTrue(true); // Đếm chuyên gia (vai_tro = true)
    }
    
    public ByteArrayInputStream exportCoursesToExcel() throws IOException {
        List<Course> courses = findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Courses");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Tên khóa học", "Mô tả","Điểm đạt", "Danh mục", "Giá tiền", "Ảnh đại diện", "Trạng thái"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Create data rows
            int rowNum = 1;
            for (Course course: courses) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(course.getID_khoa_hoc());
                row.createCell(1).setCellValue(course.getTen_khoa_hoc());
                row.createCell(2).setCellValue(course.getMo_ta()!= null ? course.getMo_ta() : "");
                row.createCell(3).setCellValue(course.getDiem_dat());
                row.createCell(4).setCellValue(course.getCategory().getTenDanhMuc());
                row.createCell(5).setCellValue(course.getGia_tien());
                row.createCell(6).setCellValue(course.getAnh_dai_dien() != null ? course.getAnh_dai_dien() : "");
                row.createCell(7).setCellValue(course.isStatus() ? "Hoạt động" : "Không hoạt động");
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

 // Thêm phương thức nhập Excel
    public String importCoursesFromExcel(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File Excel không được để trống!");
        }

        List<Course> courses = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet Excel không tồn tại!");
            }
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next(); // Bỏ qua header
            }

            int rowNum = 1;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNum++;
                
                try {
                	Course course = new Course();
                	
                	String tenKhoaHoc = getCellValue(row.getCell(1));
                	String mo_ta = getCellValue(row.getCell(2));
                    String diem_dat = getCellValue(row.getCell(3));
                    String danh_muc = getCellValue(row.getCell(4));
                    String gia_tien = getCellValue(row.getCell(5));
                    String anh_dai_dien = getCellValue(row.getCell(6));
                    String trang_thai = getCellValue(row.getCell(7));

                    // Kiểm tra dữ liệu bắt buộc
                    if (tenKhoaHoc == null || tenKhoaHoc.trim().isEmpty()) {
                        throw new IllegalArgumentException("Tên khóa học không được để trống tại dòng " + rowNum);
                    }
                    if (diem_dat == null || diem_dat.trim().isEmpty()) {
                        throw new IllegalArgumentException("Điểm đạt không được để trống tại dòng " + rowNum);
                    }
                    if (danh_muc == null || danh_muc.trim().isEmpty()) {
                        throw new IllegalArgumentException("Tên danh mục không được để trống tại dòng " + rowNum);
                    }
                    if (gia_tien == null || gia_tien.trim().isEmpty()) {
                    	throw new IllegalArgumentException("Giá tiền không được để trống tại dòng " + rowNum);
                    }

                    course.setTen_khoa_hoc(tenKhoaHoc);
                    course.setMo_ta(mo_ta);
                    course.setDiem_dat(Double.parseDouble(diem_dat));
                   // course.setCategory(categoryRepository.findByTenDanhMuc(danh_muc));
                    course.setGia_tien(Double.parseDouble(gia_tien));
                    course.setAnh_dai_dien(anh_dai_dien);
                    
                 // Tìm danh mục theo tên
                    course.setCategory(cateService.getCateByTen(danh_muc));

                    //Category category = cateService.getCateByTen(danh_muc);
                    //if (category.isEmpty()) {
                      //  throw new IllegalArgumentException("Không tìm thấy danh mục '" + danh_muc + "' tại dòng " + rowNum);
                    //}
                    //course.setCategory(categoryOpt.get());

                    
                    if (trang_thai != null && ! trang_thai.trim().isEmpty()) {
                        if (trang_thai.equals("Hoạt động")) {
                            course.setStatus(true);
                        } else if (trang_thai.equals("Không hoạt động")) {
                            course.setStatus(false);
                        } else {
                            throw new IllegalArgumentException("Trạng thái phải là 'Hoạt động' hoặc 'Không hoạt động' tại dòng " + rowNum);
                        }
                    } else {
                        course.setStatus(true); // Mặc định là Hoạt động
                    }
                    

                    courses.add(course);
                } catch (IllegalArgumentException e) {
                    errors.add("Dòng " + rowNum + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new IOException("Lỗi khi đọc file Excel: " + e.getMessage(), e);
        }

        int successCount = 0;
        for (Course course : courses) {
            try {
                save(course); // Gọi createUser để lưu user
                successCount++;
            } catch (RuntimeException e) {
                errors.add("Lỗi khi lưu khóa học " + course.getTen_khoa_hoc() + ": " + e.getMessage());
            }
        }

        if (errors.isEmpty()) {
            return "Nhập thành công " + successCount + " khóa học.";
        } else {
            return "Nhập thành công " + successCount + " khóa học. Lỗi: " + String.join("; ", errors);
        }
    }
    
 // Phương thức hỗ trợ để lấy giá trị ô trong Excel
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
                }
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    return String.valueOf((long) numericValue); // Tránh .0 nếu là số nguyên
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

}