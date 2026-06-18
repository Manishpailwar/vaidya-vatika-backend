package com.vaidyavatika.service;

import com.vaidyavatika.model.Order;
import com.vaidyavatika.model.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ── VERIFICATION EMAIL ────────────────────────────────
    @Async
    public void sendVerificationEmail(String toEmail, String name, String token) {
        try {
            log.info("Sending verification email to: {}", toEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your Vaidya Vatika account");

            String verifyLink = frontendUrl + "/verify-email?token=" + token;

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 560px; margin: 0 auto; background: #fff;">
                  <div style="background: #2D5016; padding: 32px 40px; text-align: center; border-radius: 12px 12px 0 0;">
                    <h1 style="color: #fff; font-size: 26px; margin: 0;">Vaidya Vatika</h1>
                    <p style="color: rgba(255,255,255,0.8); margin: 8px 0 0; font-size: 13px; letter-spacing: 2px; text-transform: uppercase;">Pure Ayurveda</p>
                  </div>
                  <div style="padding: 40px; background: #fff; border: 1px solid #e8e8e8; border-top: none; border-radius: 0 0 12px 12px;">
                    <h2 style="color: #2D5016; font-size: 22px; margin: 0 0 16px;">Welcome, %s!</h2>
                    <p style="color: #555; font-size: 15px; line-height: 1.7; margin: 0 0 28px;">
                      Please click the button below to verify your email address and activate your account.
                    </p>
                    <div style="text-align: center; margin: 32px 0;">
                      <a href="%s"
                         style="background: #2D5016; color: #fff; padding: 16px 40px; border-radius: 50px;
                                text-decoration: none; font-weight: 700; font-size: 16px;
                                display: inline-block;">
                        Verify My Email
                      </a>
                    </div>
                    <p style="color: #999; font-size: 13px; line-height: 1.6; margin: 28px 0 0;">
                      This link expires in <strong>24 hours</strong>.
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;" />
                    <p style="color: #bbb; font-size: 12px; text-align: center; margin: 0;">
                      If the button does not work, paste this link in your browser:<br/>
                      <a href="%s" style="color: #2D5016; word-break: break-all;">%s</a>
                    </p>
                  </div>
                </div>
                """.formatted(name, verifyLink, verifyLink, verifyLink);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ── ORDER CONFIRMATION EMAIL ──────────────────────────
    // Sent after order is placed successfully.
    // @Async so it doesn't slow down the order placement response.
    @Async
    public void sendOrderConfirmationEmail(Order order) {
        try {
            log.info("Sending order confirmation email to: {}", order.getCustomerEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("Order Confirmed! #" + order.getId() + " — Vaidya Vatika");

            String trackLink = frontendUrl + "/track/" + order.getId();

            // Build items rows
            StringBuilder itemsHtml = new StringBuilder();
            for (OrderItem item : order.getItems()) {
                itemsHtml.append("""
                    <tr>
                      <td style="padding: 12px 16px; border-bottom: 1px solid #f0f0f0;">
                        <div style="font-weight: 700; color: #333; font-size: 14px;">%s</div>
                        <div style="color: #888; font-size: 12px; margin-top: 2px;">Qty: %d × ₹%.0f</div>
                      </td>
                      <td style="padding: 12px 16px; border-bottom: 1px solid #f0f0f0; text-align: right; font-weight: 700; color: #2D5016; font-size: 14px;">
                        ₹%.0f
                      </td>
                    </tr>
                    """.formatted(
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ));
            }

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #fff;">

                  <!-- Header -->
                  <div style="background: linear-gradient(135deg, #2D5016, #3d6b1f); padding: 36px 40px; text-align: center; border-radius: 12px 12px 0 0;">
                    <h1 style="color: #fff; font-size: 26px; margin: 0 0 4px;">🌿 Vaidya Vatika</h1>
                    <p style="color: rgba(255,255,255,0.75); margin: 0; font-size: 12px; letter-spacing: 2px; text-transform: uppercase;">Pure Ayurveda</p>
                  </div>

                  <!-- Success banner -->
                  <div style="background: #f0faf0; border-bottom: 2px solid #c8e6c9; padding: 24px 40px; text-align: center;">
                    <div style="font-size: 48px; margin-bottom: 8px;">✅</div>
                    <h2 style="color: #2D5016; font-size: 22px; margin: 0 0 6px;">Order Confirmed!</h2>
                    <p style="color: #555; font-size: 14px; margin: 0;">Thank you, <strong>%s</strong>! Your order has been placed successfully.</p>
                  </div>

                  <div style="padding: 32px 40px;">

                    <!-- Order ID + Track button -->
                    <div style="background: #fafafa; border-radius: 12px; padding: 20px 24px; margin-bottom: 28px; display: flex; justify-content: space-between; align-items: center; border: 1px solid #eee;">
                      <div>
                        <div style="font-size: 12px; color: #888; text-transform: uppercase; letter-spacing: 1px;">Order ID</div>
                        <div style="font-size: 22px; font-weight: 700; color: #2D5016;">#%d</div>
                      </div>
                      <a href="%s" style="background: #2D5016; color: #fff; padding: 10px 22px; border-radius: 50px; text-decoration: none; font-weight: 700; font-size: 13px;">
                        Track Order →
                      </a>
                    </div>

                    <!-- Items table -->
                    <h3 style="color: #333; font-size: 16px; margin: 0 0 12px;">Order Items</h3>
                    <table style="width: 100%%; border-collapse: collapse; margin-bottom: 16px; border: 1px solid #eee; border-radius: 8px; overflow: hidden;">
                      <thead>
                        <tr style="background: #f5f5f5;">
                          <th style="padding: 10px 16px; text-align: left; font-size: 12px; color: #888; text-transform: uppercase; letter-spacing: 1px;">Product</th>
                          <th style="padding: 10px 16px; text-align: right; font-size: 12px; color: #888; text-transform: uppercase; letter-spacing: 1px;">Amount</th>
                        </tr>
                      </thead>
                      <tbody>
                        %s
                      </tbody>
                    </table>

                    <!-- Total -->
                    <div style="background: #2D5016; border-radius: 10px; padding: 16px 20px; display: flex; justify-content: space-between; margin-bottom: 28px;">
                      <span style="color: rgba(255,255,255,0.8); font-size: 14px; font-weight: 600;">Total Amount</span>
                      <span style="color: #fff; font-size: 20px; font-weight: 700;">₹%.0f</span>
                    </div>

                    <!-- Delivery details -->
                    <h3 style="color: #333; font-size: 16px; margin: 0 0 12px;">Delivery Details</h3>
                    <div style="background: #fafafa; border-radius: 12px; padding: 20px 24px; border: 1px solid #eee; margin-bottom: 28px;">
                      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
                        <div>
                          <div style="font-size: 11px; color: #888; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 3px;">Name</div>
                          <div style="font-weight: 600; color: #333; font-size: 14px;">%s</div>
                        </div>
                        <div>
                          <div style="font-size: 11px; color: #888; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 3px;">Phone</div>
                          <div style="font-weight: 600; color: #333; font-size: 14px;">%s</div>
                        </div>
                        <div style="grid-column: 1/-1;">
                          <div style="font-size: 11px; color: #888; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 3px;">Address</div>
                          <div style="font-weight: 600; color: #333; font-size: 14px;">%s, %s — %s</div>
                        </div>
                        <div>
                          <div style="font-size: 11px; color: #888; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 3px;">Payment</div>
                          <div style="font-weight: 600; color: #333; font-size: 14px;">%s</div>
                        </div>
                      </div>
                    </div>

                    <!-- Footer note -->
                    <p style="color: #888; font-size: 13px; line-height: 1.7; text-align: center; margin: 0;">
                      We'll send you an update when your order is shipped. 🚚<br/>
                      Questions? Reply to this email or contact us at <a href="mailto:%s" style="color: #2D5016;">%s</a>
                    </p>
                  </div>

                  <!-- Footer -->
                  <div style="background: #f5f5f5; padding: 20px 40px; text-align: center; border-radius: 0 0 12px 12px; border-top: 1px solid #eee;">
                    <p style="color: #aaa; font-size: 12px; margin: 0;">© 2025 Vaidya Vatika · Pure Ayurveda</p>
                  </div>
                </div>
                """.formatted(
                    order.getCustomerName(),
                    order.getId(),
                    trackLink,
                    itemsHtml.toString(),
                    order.getTotalAmount(),
                    order.getCustomerName(),
                    order.getCustomerPhone(),
                    order.getAddress(),
                    order.getCity(),
                    order.getPincode(),
                    order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD",
                    fromEmail, fromEmail
            );

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Order confirmation email sent to: {}", order.getCustomerEmail());

        } catch (Exception e) {
            // Don't throw — order is already placed, email failure shouldn't affect it
            log.error("Failed to send order confirmation email: {}", e.getMessage());
        }
    }

    // ── ORDER STATUS UPDATE EMAIL ─────────────────────────
    // Sent when admin changes status to SHIPPED or DELIVERED.
    @Async
    public void sendOrderStatusEmail(Order order) {
        try {
            String status = order.getStatus();
            if (!status.equals("SHIPPED") && !status.equals("DELIVERED")) return;

            log.info("Sending status update email ({}) to: {}", status, order.getCustomerEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getCustomerEmail());

            String emoji     = status.equals("SHIPPED") ? "🚚" : "🎉";
            String statusMsg = status.equals("SHIPPED") ? "Your order is on its way!" : "Your order has been delivered!";
            String subMsg    = status.equals("SHIPPED")
                    ? "Your Vaidya Vatika order is packed and shipped. It will reach you soon."
                    : "Your Vaidya Vatika order has been delivered. Enjoy your ayurvedic products!";

            helper.setSubject(emoji + " " + statusMsg + " Order #" + order.getId());

            String trackLink = frontendUrl + "/track/" + order.getId();

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 560px; margin: 0 auto; background: #fff;">
                  <div style="background: linear-gradient(135deg, #2D5016, #3d6b1f); padding: 32px 40px; text-align: center; border-radius: 12px 12px 0 0;">
                    <h1 style="color: #fff; font-size: 26px; margin: 0;">🌿 Vaidya Vatika</h1>
                  </div>
                  <div style="padding: 40px; background: #fff; border: 1px solid #e8e8e8; border-top: none; border-radius: 0 0 12px 12px; text-align: center;">
                    <div style="font-size: 64px; margin-bottom: 16px;">%s</div>
                    <h2 style="color: #2D5016; font-size: 24px; margin: 0 0 12px;">%s</h2>
                    <p style="color: #555; font-size: 15px; line-height: 1.7; margin: 0 0 8px;">%s</p>
                    <p style="color: #888; font-size: 14px; margin: 0 0 28px;">Order ID: <strong>#%d</strong></p>
                    <a href="%s" style="background: #2D5016; color: #fff; padding: 14px 36px; border-radius: 50px; text-decoration: none; font-weight: 700; font-size: 15px; display: inline-block;">
                      Track Your Order →
                    </a>
                    <p style="color: #bbb; font-size: 12px; margin-top: 28px;">Thank you for shopping with Vaidya Vatika 🌿</p>
                  </div>
                </div>
                """.formatted(emoji, statusMsg, subMsg, order.getId(), trackLink);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Status update email ({}) sent to: {}", status, order.getCustomerEmail());

        } catch (Exception e) {
            log.error("Failed to send status update email: {}", e.getMessage());
        }
    }

    // ── PASSWORD RESET EMAIL ──────────────────────────────
    // Sent when user requests password reset.
    // Token valid for 1 hour only.
    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String token) {
        try {
            log.info("Sending password reset email to: {}", toEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Reset your Vaidya Vatika password 🔒");

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 560px; margin: 0 auto; background: #fff;">
                  <div style="background: linear-gradient(135deg, #2D5016, #3d6b1f); padding: 32px 40px; text-align: center; border-radius: 12px 12px 0 0;">
                    <h1 style="color: #fff; font-size: 26px; margin: 0;">🌿 Vaidya Vatika</h1>
                    <p style="color: rgba(255,255,255,0.8); margin: 8px 0 0; font-size: 12px; letter-spacing: 2px; text-transform: uppercase;">Pure Ayurveda</p>
                  </div>
                  <div style="padding: 40px; background: #fff; border: 1px solid #e8e8e8; border-top: none; border-radius: 0 0 12px 12px;">
                    <div style="text-align: center; margin-bottom: 24px;">
                      <div style="font-size: 56px;">🔒</div>
                    </div>
                    <h2 style="color: #2D5016; font-size: 22px; margin: 0 0 12px; text-align: center;">Password Reset Request</h2>
                    <p style="color: #555; font-size: 15px; line-height: 1.7; margin: 0 0 8px;">Hi <strong>%s</strong>,</p>
                    <p style="color: #555; font-size: 15px; line-height: 1.7; margin: 0 0 28px;">
                      We received a request to reset your password. Click the button below to set a new password.
                      This link will expire in <strong>1 hour</strong>.
                    </p>
                    <div style="text-align: center; margin: 32px 0;">
                      <a href="%s"
                         style="background: #2D5016; color: #fff; padding: 16px 40px; border-radius: 50px;
                                text-decoration: none; font-weight: 700; font-size: 16px;
                                display: inline-block;">
                        🔑 Reset My Password
                      </a>
                    </div>
                    <p style="color: #999; font-size: 13px; line-height: 1.6; margin: 28px 0 0; text-align: center;">
                      If you didn't request this, you can safely ignore this email.<br/>
                      Your password will remain unchanged.
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;" />
                    <p style="color: #bbb; font-size: 12px; text-align: center; margin: 0;">
                      If the button doesn't work, paste this link in your browser:<br/>
                      <a href="%s" style="color: #2D5016; word-break: break-all;">%s</a>
                    </p>
                  </div>
                </div>
                """.formatted(name, resetLink, resetLink, resetLink);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }
}