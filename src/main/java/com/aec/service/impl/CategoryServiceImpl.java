package com.aec.service.impl;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aec.model.Category;
import com.aec.model.UserDtls;
import com.aec.repository.CategoryRepository;
import com.aec.repository.UserRepository;
import com.aec.service.CategoryService;
import com.aec.util.CommonUtil;

import jakarta.mail.internet.MimeMessage;

@Service
public class CategoryServiceImpl implements CategoryService {
	
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	
	@Autowired
    private CommonUtil commonUtil; // Utility for sending emails
	
	

 
	

	@Override
	public Category saveCategory(Category category) {
		
		return categoryRepository.save(category);
	}
	
	
	

	@Override
	public List<Category> getAllCategory() {
	
		return categoryRepository.findAll();
	}

	@Override
	public Boolean existCategory(String name) {
		
		return categoryRepository.existsByName(name);
	}

	
	
	
	
	@Override
	public Boolean deleteCategory(int id) {
		Category category = categoryRepository.findById(id).orElse(null);

		if (!ObjectUtils.isEmpty(category)) {
			 commonUtil.sendEmailToAllAdminsOnCategoryDeletion(category);
			categoryRepository.delete(category);
			return true;
		}
		return false;
	}

	@Override
	public Category getCategoryById(int id) {
		
		Category category=categoryRepository.findById(id).orElse(null);
		return category;
	}

	
	

	@Override
	public List<Category> getAllActiveCategory() {
		List<Category> categories = categoryRepository.findByIsActiveTrue();
		return categories;
	}

	
	
	
	@Override
	public Page<Category> getAllCategorPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return categoryRepository.findAll(pageable);
	}

	
	
	
	
	
	
	

}
