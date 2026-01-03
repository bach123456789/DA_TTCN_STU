package com.web.DA_TTCN_STU.Controllers;

import com.web.DA_TTCN_STU.DTOs.CartItem;
import com.web.DA_TTCN_STU.Entities.Order;
import com.web.DA_TTCN_STU.Entities.OrderDetail;
import com.web.DA_TTCN_STU.Entities.Product;
import com.web.DA_TTCN_STU.Entities.User;
import com.web.DA_TTCN_STU.Repositories.OrderDetailRepository;
import com.web.DA_TTCN_STU.Repositories.OrderRepository;
import com.web.DA_TTCN_STU.Repositories.ProductRepository;
import com.web.DA_TTCN_STU.Services.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    // ==========================================
    // 1. XỬ LÝ ĐẶT HÀNG (CHECKOUT)
    // ==========================================
    @GetMapping("/checkout")
    public String checkout(HttpSession session, RedirectAttributes redirectAttributes) {
        // 1. Kiểm tra đăng nhập (Lấy User từ Session như bạn yêu cầu)
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // Chưa đăng nhập thì đá về login
        }

        // 2. Kiểm tra giỏ hàng có rỗng không
        List<CartItem> cartItems = cartService.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống, vui lòng chọn sản phẩm!");
            return "redirect:/cart";
        }

        // 3. TẠO ĐƠN HÀNG (ORDER)
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING"); // Trạng thái mặc định: Chờ xử lý
        order.setTotalAmount(cartService.getTotalAmount()); // Lấy tổng tiền từ Service (đã trừ mã giảm giá nếu có)

        // Lưu Order trước để có ID
        Order savedOrder = orderRepository.save(order);

        // 4. TẠO CHI TIẾT ĐƠN HÀNG (ORDER DETAIL)
        List<OrderDetail> details = new ArrayList<>();
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);

            // Lấy Product từ DB để đảm bảo toàn vẹn dữ liệu
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if(product != null) {
                detail.setProduct(product);
                detail.setQuantity(item.getQuantity());

                // Lưu ý: Entity OrderDetail của bạn để totalPrice là Integer
                // Trong khi CartItem tính bằng BigDecimal. Cần ép kiểu:
                detail.setTotalPrice(item.getTotalPrice().intValue());

                details.add(detail);
            }
        }

        // Lưu danh sách chi tiết vào DB
        orderDetailRepository.saveAll(details);

        // 5. XÓA GIỎ HÀNG & HOÀN TẤT
        cartService.clear(); // Nhớ viết hàm clear() trong CartService nhé (xóa list item, xóa coupon)

        redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công! Mã đơn: #" + savedOrder.getOrderID());
        return "redirect:/user/orders"; // Chuyển hướng đến trang lịch sử đơn hàng
    }

    // ==========================================
    // 2. XEM LỊCH SỬ ĐƠN HÀNG
    // ==========================================
    @GetMapping("/user/orders")
    public String listOrders(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // Lấy danh sách đơn hàng của User này
        List<Order> orders = orderRepository.findByUser_UserIDOrderByOrderDateDesc(user.getUserID());
        model.addAttribute("orders", orders);

        return "user/order-history"; // Bạn cần tạo file HTML này
    }

    // ==========================================
    // 3. XEM CHI TIẾT MỘT ĐƠN HÀNG
    // ==========================================
    @GetMapping("/user/orders/detail/{id}")
    public String orderDetail(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Order order = orderRepository.findById(id).orElse(null);

        // Kiểm tra đơn hàng có tồn tại và CÓ PHẢI CỦA USER NÀY KHÔNG (Bảo mật)
        if (order == null || !order.getUser().getUserID().equals(user.getUserID())) {
            ra.addFlashAttribute("error", "Đơn hàng không tồn tại hoặc bạn không có quyền xem.");
            return "redirect:/user/orders";
        }

        model.addAttribute("order", order);
        return "user/order-detail"; // Bạn cần tạo file HTML này
    }

    // ==========================================
    // 4. HỦY ĐƠN HÀNG
    // ==========================================
    @GetMapping("/user/orders/cancel/{id}")
    public String cancelOrder(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Order order = orderRepository.findById(id).orElse(null);

        if (order != null && order.getUser().getUserID().equals(user.getUserID())) {
            // Chỉ cho hủy khi đơn đang "PENDING"
            if ("PENDING".equals(order.getStatus())) {
                order.setStatus("CANCELLED"); // Chuyển trạng thái
                orderRepository.save(order);
                ra.addFlashAttribute("success", "Đã hủy đơn hàng thành công.");
            } else {
                ra.addFlashAttribute("error", "Không thể hủy đơn hàng đã được xử lý hoặc đang giao.");
            }
        } else {
            ra.addFlashAttribute("error", "Lỗi xử lý yêu cầu.");
        }

        return "redirect:/user/orders";
    }
}