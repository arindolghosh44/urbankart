package com.aec.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.aec.model.Feedback;


public interface Feedbackrepository extends JpaRepository<Feedback,Integer> {
	
	
	

}
