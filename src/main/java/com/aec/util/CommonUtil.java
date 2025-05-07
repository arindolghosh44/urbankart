package com.aec.util;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.aec.model.Category;
import com.aec.model.Product;
import com.aec.model.ProductOrder;
import com.aec.model.UserDtls;
import com.aec.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

	
	@Autowired
	private JavaMailSender mailSender;
		
	
	@Autowired
	private UserService userService;
	
	public Boolean sendMail(String url, String reciepentEmail) throws UnsupportedEncodingException, MessagingException {

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("t01666122@gmail.com", "Urban Cart");
		helper.setTo(reciepentEmail);

		String content = "<p>Hello,</p>" + "<p>You have requested to reset your password.</p>"
				+ "<p>Click the link below to change your password:</p>" + "<p><a href=\"" + url
				+ "\">Change my password</a></p>";
		helper.setSubject("Password Reset");
		helper.setText(content, true);
		mailSender.send(message);
		return true;
	}
	
	
	 public Boolean sendMailupdate(String subject, String recipientEmail, String content) throws MessagingException, UnsupportedEncodingException {
	        MimeMessage message = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message);

	        helper.setFrom("t01666122@gmail.com", "Urban Cart");
	        helper.setTo(recipientEmail);
	        helper.setSubject(subject);
	        helper.setText(content, true);

	        mailSender.send(message);
	        return true;
	    }
	
	public static String generateUrl(HttpServletRequest request) {

		// http://localhost:8080/forgot-password
		String siteUrl = request.getRequestURL().toString();

		return siteUrl.replace(request.getServletPath(), "");

	}
	
	String msg=null;
	
	
	public Boolean sendMailForProductOrder(ProductOrder order,String status) throws Exception
	{
		
		msg="<p>Hello [[name]],</p>"
				+ "<p>Thank you order <b>[[orderStatus]]</b>.</p>"
				+ "<p><b>Product Details:</b></p>"
				+ "<p>Name : [[productName]]</p>"
				+ "<p>Category : [[category]]</p>"
				+ "<p>Quantity : [[quantity]]</p>"
				+ "<p>Price : [[price]]</p>"
				+"<p>TotalPrice : [[totalprice]]</p>"
				+ "<p>Payment Type : [[paymentType]]</p>";
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("t01666122@gmail.com", "Urban Cart");
		helper.setTo(order.getOrderAddress().getEmail());

		msg=msg.replace("[[name]]",order.getOrderAddress().getFirstName());
		msg=msg.replace("[[orderStatus]]",status);
		msg=msg.replace("[[productName]]", order.getProduct().getTitle());
		msg=msg.replace("[[category]]", order.getProduct().getCategory());
		msg=msg.replace("[[quantity]]", order.getQuantity().toString());
		msg=msg.replace("[[price]]", order.getPrice().toString());
		msg=msg.replace("[[totalprice]]", order.getTotalPrice().toString());
		msg=msg.replace("[[paymentType]]", order.getPaymentType());
		
		helper.setSubject("Product Order Status");
		helper.setText(msg, true);
		mailSender.send(message);
		return true;
	}
	
	public UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		UserDtls userDtls = userService.getUserByEmail(email);
		return userDtls;
	}
	
	public void sendMailWithCustomContent(String recipientEmail, String subject, String content) 
            throws UnsupportedEncodingException, MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("t01666122@gmail.com", "UrbanKart");
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }
	
	
	public void sendEmailToAllAdminsOnNewCategory(Category newCategory) {
	    // Fetch all users with ROLE_ADMIN
	    List<UserDtls> admins = userService.getUsers("ROLE_ADMIN");
	    
	    // List<UserDtls> admins = userService.getUsersByRole("ROLE_SUPERADMIN");

	    // Create email subject and content
	    String subject = "New Category Added: " + newCategory.getName();
	    String content = "<p>A new category has been added:</p>"
	            + "<p><strong>Name:</strong> " + newCategory.getName() + "</p>"
	            + "<p><strong>Status:</strong> " + (newCategory.getIsActive() ? "Active" : "Inactive") + "</p>"
	            + "<p><strong>Image:</strong> " + (newCategory.getImageName() != null ? newCategory.getImageName() : "No image") + "</p>";

	    // Send email to each admin
	    for (UserDtls admin : admins) {
	        try {
	            sendMailWithCustomContent(admin.getEmail(), subject, content);
	        } catch (UnsupportedEncodingException | MessagingException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	public void sendEmailToAllAdminsOnCategoryDeletion(Category category) {
	    // Fetch all users with ROLE_ADMIN
	    List<UserDtls> admins = userService.getUsers("ROLE_ADMIN");

	    // Create email subject and content
	    String subject = "Product Deleted: " + category.getName();
	    String content = "<p>The following product has been deleted:</p>"
	            + "<p><strong>Model:</strong> " + category.getIsActive() + "</p>";
	          

	    // Send email to each admin
	    for (UserDtls admin : admins) {
	        try {
	            sendMailWithCustomContent(admin.getEmail(), subject, content);
	        } catch (UnsupportedEncodingException | MessagingException e) {
	            e.printStackTrace();
	        }
	    }
	}
	public void sendEmailToAllAdminsOnCategoryUpdate(Category updatedCategory) {
	    // Fetch all users with ROLE_ADMIN
	    List<UserDtls> admins = userService.getUsers("ROLE_ADMIN");

	    // Create email subject and content
	    String subject = "Category Updated: " + updatedCategory.getName();
	    String content = "<p>The following category has been updated:</p>"
	            + "<p><strong>Category Name:</strong> " + updatedCategory.getName() + "</p>"
	            + "<p><strong>Active Status:</strong> " + (updatedCategory.getIsActive() ? "Active" : "Inactive") + "</p>";
	     

	    // Send email to each admin
	    for (UserDtls admin : admins) {
	        try {
	            sendMailWithCustomContent(admin.getEmail(), subject, content);
	        } catch (UnsupportedEncodingException | MessagingException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	
	public void sendEmailToAllAdminsOnNewProduct(Product newProduct) {
	    // Fetch all users with ROLE_ADMIN
	    List<UserDtls> admins = userService.getUsers("ROLE_ADMIN");

	    // Create email subject and content
	    String subject = "New Product Added: " + newProduct.getTitle();
	    String content = "<p>A new product has been added:</p>"
	            + "<p><strong>Model:</strong> " + newProduct.getDescription() + "</p>"
	            + "<p><strong>Category:</strong> " + newProduct.getCategory() + "</p>"
	            + "<p><strong>Price:</strong> " + newProduct.getPrice() + "</p>"
	            + "<p><strong>Discount Price:</strong> " + newProduct.getDiscountPrice() + "</p>";

	    // Send email to each admin
	    for (UserDtls admin : admins) {
	        try {
	            sendMailWithCustomContent(admin.getEmail(), subject, content);
	        } catch (UnsupportedEncodingException | MessagingException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	
	public void sendEmailToAllAdminsOnProductDeletion(Product deletedProduct) {
	    // Fetch all users with ROLE_ADMIN
	    List<UserDtls> admins = userService.getUsers("ROLE_ADMIN");

	    // Create email subject and content
	    String subject = "Product Deleted: " + deletedProduct.getTitle();
	    String content = "<p>The following product has been deleted:</p>"
	            + "<p><strong>Model:</strong> " + deletedProduct.getDescription() + "</p>"
	            + "<p><strong>Category:</strong> " + deletedProduct.getCategory() + "</p>"
	            + "<p><strong>Price:</strong> " + deletedProduct.getPrice() + "</p>";

	    // Send email to each admin
	    for (UserDtls admin : admins) {
	        try {
	            sendMailWithCustomContent(admin.getEmail(), subject, content);
	        } catch (UnsupportedEncodingException | MessagingException e) {
	            e.printStackTrace();
	        }
	    }
	}
	public void sendEmailToAllAdminsUpdateProduct(Product updatedProduct) {
	    // Fetch all users with ROLE_ADMIN
	    List<UserDtls> admins = userService.getUsers("ROLE_ADMIN");

	    // Create email subject and content
	    String subject = "Product Updated: " + updatedProduct.getTitle();
	    String content = "<p>The following product has been updated:</p>"
	            + "<p><strong>Model:</strong> " + updatedProduct.getDescription() + "</p>"
	            + "<p><strong>Category:</strong> " + updatedProduct.getCategory() + "</p>"
	            + "<p><strong>Price:</strong> " + updatedProduct.getPrice() + "</p>"
	            + "<p><strong>Discount Price:</strong> " + updatedProduct.getDiscountPrice() + "</p>";

	    // Send email to each admin
	    for (UserDtls admin : admins) {
	        try {
	            sendMailWithCustomContent(admin.getEmail(), subject, content);
	        } catch (UnsupportedEncodingException | MessagingException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	public Boolean sendMailadminsave(String subject, String content, String recipientEmail) 
	        throws UnsupportedEncodingException, MessagingException {

	    MimeMessage message = mailSender.createMimeMessage();
	    MimeMessageHelper helper = new MimeMessageHelper(message);

	    helper.setFrom("t01666122@gmail.com", "Urban Cart");
	    helper.setTo(recipientEmail);
	    helper.setSubject(subject);
	    helper.setText(content, true);

	    mailSender.send(message);
	    return true;
	}

	
}
