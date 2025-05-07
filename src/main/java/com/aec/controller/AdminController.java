package com.aec.controller;

import java.io.File;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.aec.model.Category;
import com.aec.model.Product;
import com.aec.model.ProductOrder;
import com.aec.model.UserDtls;
import com.aec.service.CartService;
import com.aec.service.CategoryService;
import com.aec.service.OrderService;
import com.aec.service.ProductService;
import com.aec.service.UserService;
import com.aec.util.CommonUtil;
import com.aec.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CommonUtil commonUtil;
	
	
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	
	
	
	
	
	

	@GetMapping("/")
	public String index() {

		return "admin/index";

	}
	
	
	
	
	
	
	

	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {

		List<Category> categories = categoryService.getAllCategory();

		m.addAttribute("categories", categories);

		return "admin/add_product";

	}

	@GetMapping("/category")
	public String category(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "3") Integer pageSize) {
		// m.addAttribute("categorys", categoryService.getAllCategory());
		Page<Category> page = categoryService.getAllCategorPagination(pageNo, pageSize);
		List<Category> categorys = page.getContent();
		m.addAttribute("categorys", categorys);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/category";
	}


	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";

		category.setImageName(imageName);

		Boolean existCategory = categoryService.existCategory(category.getName());

		if (existCategory) {

			session.setAttribute("errorMsg", "Category Name already exists");
		} else {
			Category saveCategory = categoryService.saveCategory(category);

			if (ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("errorMsg", "internal Server Error !!!!!!! Not Saved");
			} else {

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "Category_image" + File.separator
						+ file.getOriginalFilename());

				System.out.println(path);

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				
				 // Send Email Notification to Admins
				 commonUtil.sendEmailToAllAdminsOnNewCategory(saveCategory);
				
				session.setAttribute("succMsg", "Saved Successfully");
				
				

			}

		}

		return "redirect:/admin/category";
	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		Boolean deleteCategory = categoryService.deleteCategory(id);

		if (deleteCategory) {
			session.setAttribute("succMsg", "category delete success");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {

		m.addAttribute("category", categoryService.getCategoryById(id));

		// by using "categories" we can get the get all the from database and show it in
		// the html page
		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		Category Oldcategory = categoryService.getCategoryById(category.getId());
		String imageName = file.isEmpty() ? Oldcategory.getImageName() : file.getOriginalFilename();

		if (!ObjectUtils.isEmpty(Oldcategory)) {
			Oldcategory.setName(category.getName());

			Oldcategory.setIsActive(category.getIsActive());

			Oldcategory.setImageName(imageName);
		}

		Category updateCategory = categoryService.saveCategory(Oldcategory);
		if (!ObjectUtils.isEmpty(updateCategory)) {
			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "Category_image" + File.separator
						+ file.getOriginalFilename());

				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			commonUtil.sendEmailToAllAdminsOnCategoryUpdate(category);
			session.setAttribute("succMsg", "category updated successfully");
		}

		else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/loadEditCategory/" + category.getId();
	}

	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			HttpSession session) throws IOException {

		String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();

		product.setImage(imageName);
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());

		Product saveProduct = productService.saveProduct(product);

		if (!ObjectUtils.isEmpty(saveProduct)) {

			File saveFile = new ClassPathResource("static/img").getFile();

			Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
					+ image.getOriginalFilename());

			System.out.println(path);
			Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			session.setAttribute("succMsg", "Product Saved Success");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/loadAddProduct";
	}

	@GetMapping("/products")
	public String loadViewProduct(Model m, @RequestParam(defaultValue = "") String ch,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "4") Integer pageSize) {

//		List<Product> products = null;
//		if (ch != null && ch.length() > 0) {
//			products = productService.searchProduct(ch);
//		} else {
//			products = productService.getAllProducts();
//		}
//		m.addAttribute("products", products);

		Page<Product> page = null;
		if (ch != null && ch.length() > 0) {
			page = productService.searchProductPagination(pageNo, pageSize, ch);
		} else {
			page = productService.getAllProductsPagination(pageNo, pageSize);
		}
		m.addAttribute("products", page.getContent());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/products";
	}

	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {
		Boolean deleteProduct = productService.deleteProduct(id);
		if (deleteProduct) {
			session.setAttribute("succMsg", "Product delete successfully");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/products";
	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/edit_product";
	}

	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			HttpSession session, Model m) {

		if (product.getDiscount() < 0 || product.getDiscount() > 100) {
			session.setAttribute("errorMsg", "invalid Discount");
		}

		else {

			Product updateProduct = productService.updateProduct(product, image);
			if (!ObjectUtils.isEmpty(updateProduct)) {
				session.setAttribute("succMsg", "Product update successfully");
			} else {
				session.setAttribute("errorMsg", "Something wrong on server");
			}
		}

		return "redirect:/admin/editProduct/" + product.getId();
	}
	
	

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);

			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/users")
	public String getAllUsers(Model m, @RequestParam Integer type) {
		List<UserDtls> users = null;
		if (type == 1) {
			users = userService.getUsers("ROLE_USER");
		} else {
			users = userService.getUsers("ROLE_ADMIN");
		}
		m.addAttribute("userType",type);
		m.addAttribute("users", users);
		return "/admin/users";
	}

	@GetMapping("/updateSts")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id, 
	                                      @RequestParam Integer type, HttpSession session) {
	    Boolean updateSuccess = userService.updateAccountStatus(id, status);

	    UserDtls user = userService.getUserById(id);
	    if (user != null) {
	        String email = user.getEmail();
	        String statusMessage = status ? "activated" : "deactivated";
	        String subject = "Account Status Updated";
	        String content = "<p>Hello " + user.getName() + ",</p>"
	                + "<p>Your account has been <b>" + statusMessage + "</b> by the administrator.</p>"
	                + "<p>If you have any questions, please contact support.</p>";

	        try {
	            commonUtil.sendMailupdate(subject, email, content);
	        } catch (Exception e) {
	            session.setAttribute("errorMsg", "Error sending email notification.");
	        }
	    }

	    if (updateSuccess) {
	        session.setAttribute("succMsg", "Account Status Updated");
	    } else {
	        session.setAttribute("errorMsg", "Something went wrong on the server.");
	    }

	    return "redirect:/admin/users?type=" + type;
	}


	@GetMapping("/orders")
	public String getAllOrders(Model m,@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "2") Integer pageSize) {
//		List<ProductOrder> allOrders = orderService.getAllOrders();
//		m.addAttribute("orders", allOrders);
//		m.addAttribute("srch", false);
		
		Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
		m.addAttribute("orders", page.getContent());
		m.addAttribute("srch", false);
	
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		
		return "/admin/orders";
	}


	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);

		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status Updated");
		} else {
			session.setAttribute("errorMsg", "status not updated");
		}
		return "redirect:/admin/orders";
	}


	@GetMapping("/search-order")
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession session,@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "2") Integer pageSize) {

		if (orderId != null && orderId.length() > 0) {

			ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());

			if (ObjectUtils.isEmpty(order)) {
				session.setAttribute("errorMsg", "Incorrect orderId");
				m.addAttribute("orderDtls", null);
			} else {
				m.addAttribute("orderDtls", order);
			}

			m.addAttribute("srch", true);
		} else {
//			List<ProductOrder> allOrders = orderService.getAllOrders();
//			m.addAttribute("orders", allOrders);
//			m.addAttribute("srch", false);
			
			Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
			m.addAttribute("orders", page);
			m.addAttribute("srch", false);
			
			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());
			
		}
		return "/admin/orders";

	}
	
	
	
	
	
	//Multiple admin added 
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@GetMapping("/add-admin")
	public String loadAdminAdd() {
		return "/admin/add_admin";
	}
	@PostMapping("/save-admin")
	public String saveAdmin(@ModelAttribute UserDtls user, 
	                        @RequestParam("img") MultipartFile file, 
	                        HttpSession session) throws IOException {

	    String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
	    user.setProfileImage(imageName);
	    UserDtls saveUser = userService.saveAdmin(user);

	    if (!ObjectUtils.isEmpty(saveUser)) {
	        // Save profile image
	        if (!file.isEmpty()) {
	            File saveFile = new ClassPathResource("static/img").getFile();
	            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
	                    + file.getOriginalFilename());
	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	        }

	        // Send email notification
	        String email = saveUser.getEmail();  // Assuming UserDtls has an email field
	        String subject = "Welcome to Urban Cart!";
	        String content = "<p>Hello " + saveUser.getName() + ",</p>" +
	                         "<p>Your admin account has been created successfully.</p>" +
	                         "<p>You can now log in and manage the system.</p>";

	        try {
	            commonUtil.sendMailadminsave(subject, content, email);
	            session.setAttribute("succMsg", "Register successfully & Email Sent");
	        } catch (Exception e) {
	            session.setAttribute("errorMsg", "Admin saved but email failed to send.");
	            e.printStackTrace();
	        }
	    } else {
	        session.setAttribute("errorMsg", "Something went wrong on the server.");
	    }

	    return "redirect:/admin/add-admin";
	}



	@GetMapping("/profile")
	public String profile() {
		return "/admin/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDtls user, 
	                            @RequestParam MultipartFile img, 
	                            HttpSession session) {

	    UserDtls updateUserProfile = userService.updateUserProfile(user, img);

	    if (ObjectUtils.isEmpty(updateUserProfile)) {
	        session.setAttribute("errorMsg", "Profile not updated");
	    } else {
	        // Send email notification
	        String email = updateUserProfile.getEmail();  // Assuming UserDtls has an email field
	        String subject = "Profile Updated Successfully!";
	        String content = "<p>Hello " + updateUserProfile.getName() + ",</p>" +
	                         "<p>Your profile has been successfully updated.</p>" +
	                         "<p>If you did not make this change, please contact support immediately.</p>";

	        try {
	            commonUtil.sendMailadminsave(subject, content, email);
	            session.setAttribute("succMsg", "Profile Updated & Email Sent");
	        } catch (Exception e) {
	            session.setAttribute("errorMsg", "Profile updated but email notification failed.");
	            e.printStackTrace();
	        }
	    }
	    return "redirect:/admin/profile";
	}


	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, 
	                             @RequestParam String currentPassword, 
	                             Principal p, HttpSession session) {
	    
	    UserDtls loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);
	    
	    boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

	    if (matches) {
	        // Encrypt new password
	        String encodePassword = passwordEncoder.encode(newPassword);
	        loggedInUserDetails.setPassword(encodePassword);
	        UserDtls updateUser = userService.updateUser(loggedInUserDetails);

	        if (ObjectUtils.isEmpty(updateUser)) {
	            session.setAttribute("errorMsg", "Password not updated !! Error in server");
	        } else {
	            session.setAttribute("succMsg", "Password Updated Successfully");

	            // Send email notification
	            String email = updateUser.getEmail();  // Assuming UserDtls has an email field
	            String subject = "Password Changed Successfully!";
	            String content = "<p>Hello " + updateUser.getName() + ",</p>" +
	                             "<p>Your password has been successfully changed.</p>" +
	                             "<p>If you did not make this change, please contact support immediately.</p>";

	            try {
	                commonUtil.sendMailadminsave(subject, content, email);
	                session.setAttribute("succMsg", "Password Updated & Email Sent");
	            } catch (Exception e) {
	                session.setAttribute("errorMsg", "Password updated but email notification failed.");
	                e.printStackTrace();
	            }
	        }
	    } else {
	        session.setAttribute("errorMsg", "Current Password Incorrect");
	    }

	    return "redirect:/admin/profile";
	}

	
	
	
	

}
