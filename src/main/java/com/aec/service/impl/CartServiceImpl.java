package com.aec.service.impl;

import org.springframework.stereotype.Service;

import com.aec.service.CartService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import com.aec.model.Cart;
import com.aec.model.Product;
import com.aec.model.UserDtls;
import com.aec.repository.CartRepository;
import com.aec.repository.ProductRepository;
import com.aec.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart saveCart(Integer productId, Integer userId) {
        UserDtls userDtls = userRepository.findById(userId).get();
        Product product = productRepository.findById(productId).get();

        if (product.getStock() <= 0) {
            throw new RuntimeException("Product out of stock!");
        }

        Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);
        Cart cart = null;

        if (ObjectUtils.isEmpty(cartStatus)) {
            cart = new Cart();
            cart.setProduct(product);
            cart.setUser(userDtls);
            cart.setQuantity(1);
            cart.setTotalPrice(1 * product.getDiscountPrice());

            // Reduce stock by 1
            product.setStock(product.getStock() - 1);
        } else {
            if (cartStatus.getQuantity() + 1 > product.getStock()) {
                throw new RuntimeException("Not enough stock available!");
            }

            cart = cartStatus;
            cart.setQuantity(cart.getQuantity() + 1);
            cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice());

            // Reduce stock by 1
            product.setStock(product.getStock() - 1);
        }

        productRepository.save(product);
        return cartRepository.save(cart);
    }

    @Override
    public void updateQuantity(String action, Integer cartId, HttpSession session) {
        try {
            Cart cart = cartRepository.findById(cartId).get();
            Product product = cart.getProduct();
            int updatedQuantity;

            if (action.equalsIgnoreCase("de")) {
                updatedQuantity = cart.getQuantity() - 1;

                if (updatedQuantity <= 0) {
                    // Restore stock before deletion
                    product.setStock(product.getStock() + cart.getQuantity());
                    productRepository.save(product);
                    cartRepository.delete(cart);
                    session.setAttribute("succMsg", "Product removed from cart.");
                    return;
                } else {
                    cart.setQuantity(updatedQuantity);
                    cart.setTotalPrice(cart.getQuantity() * product.getDiscountPrice());

                    // Increase stock by 1
                    product.setStock(product.getStock() + 1);
                }
            } else {
                if (cart.getQuantity() + 1 > product.getStock()) {
                    session.setAttribute("errorMsg", "Not enough stock available!");
                    return;
                }

                updatedQuantity = cart.getQuantity() + 1;
                cart.setQuantity(updatedQuantity);
                cart.setTotalPrice(cart.getQuantity() * product.getDiscountPrice());

                // Reduce stock by 1
                product.setStock(product.getStock() - 1);
            }

            productRepository.save(product);
            cartRepository.save(cart);
            session.setAttribute("succMsg", "Cart updated successfully.");
        } catch (Exception e) {
            session.setAttribute("errorMsg", "Something went wrong while updating the cart.");
        }
    }

    @Override
    public List<Cart> getCartsByUser(Integer userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);
        Double totalOrderPrice = 0.0;
        List<Cart> updatedCarts = new ArrayList<>();

        for (Cart c : carts) {
            Double totalPrice = c.getProduct().getDiscountPrice() * c.getQuantity();
            c.setTotalPrice(totalPrice);
            totalOrderPrice += totalPrice;
            c.setTotalOrderPrice(totalOrderPrice);
            updatedCarts.add(c);
        }
        return updatedCarts;
    }

    @Override
    public Integer getCountCart(Integer userId) {
        return cartRepository.countByUserId(userId);
    }
}
