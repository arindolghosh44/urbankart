package com.aec.service;

import java.util.List;
import jakarta.servlet.http.HttpSession;
import com.aec.model.Cart;

public interface CartService {

	
	public Cart saveCart(Integer productId, Integer userId);

	public List<Cart> getCartsByUser(Integer userId);
	
	
	
	public Integer getCountCart(Integer userId);

	public void updateQuantity(String sy, Integer cid, HttpSession session);

}
