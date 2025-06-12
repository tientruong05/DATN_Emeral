package com.poly.service;

import com.poly.entity.Category;
import com.poly.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByTenDanhMuc(category.getTenDanhMuc())) {
            throw new RuntimeException("Tên danh mục đã tồn tại, vui lòng nhập tên khác");
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Integer id, Category categoryDetails) {
        Category category = getCategoryById(id);
        if (!category.getTenDanhMuc().equals(categoryDetails.getTenDanhMuc()) &&
                categoryRepository.existsByTenDanhMuc(categoryDetails.getTenDanhMuc())) {
            throw new RuntimeException("Tên danh mục đã tồn tại, vui lòng nhập tên khác");
        }
        category.setTenDanhMuc(categoryDetails.getTenDanhMuc());
        category.setDescription(categoryDetails.getDescription());
        category.setStatus(categoryDetails.getStatus());
        return categoryRepository.save(category);
    }

    public void deleteCategory(Integer id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}