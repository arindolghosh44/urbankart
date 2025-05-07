package com.aec.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.aec.service.CommonService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
@Service
public class CommonServiceImple implements CommonService{

	@Override
	public void removeSessionMsg() {
	HttpServletRequest re=((ServletRequestAttributes)(RequestContextHolder.getRequestAttributes())).getRequest();
		
	
	HttpSession session=re.getSession();
	
	
	session.removeAttribute("succMsg");

	
	session.removeAttribute("errorMsg");
	
	
	
	
	
	
	
	
	}

}
