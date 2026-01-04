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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public String checkout(HttpSession session, RedirectAttributes redirectAttributes,
                           @RequestParam("method") String method,
                           Model model) {
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

        // ===== CASH ===== MỚI UPDATE 04/01/2026
        if ("CASH".equalsIgnoreCase(method)) {
            cartService.clear();
            redirectAttributes.addFlashAttribute(
                    "success", "Đặt hàng thành công! Mã đơn #" + savedOrder.getOrderID()
            );
            return "redirect:/user/orders";
        }

        // ===== CARD → SHOW QR =====
        String qrUrl = generateQrUrl(order);
        System.out.println("QR URL = " + qrUrl);

        model.addAttribute("order", savedOrder);
        model.addAttribute("qrUrl", qrUrl);

        return "user/payment-qr"; // trang hiển thị QR
    }

    private String generateQrUrl(Order order) {
        String bankCode = "VCB";
        String accountNo = "0911000007540";
        String amount = order.getTotalAmount().toString();

        String description = "Thanh toan don hang #" + order.getOrderID();
        String encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8);

        return "https://img.vietqr.io/image/"
                + bankCode + "-" + accountNo
                + "-compact.png"
                + "?amount=" + amount
                + "&addInfo=" + encodedDescription;
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

    //PHẦN CỦA ADMIN
    @GetMapping("/order/list")
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        int pageSize = 5;

        Page<Order> orderPage = orderRepository.findAll(
                PageRequest.of(page, pageSize, Sort.by("orderDate").descending())
        );

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());

        return "/order/list";
    }

    /* =========================
       HIỂN THỊ FORM EDIT
       ========================= */
    @GetMapping("/order/edit/{id}")
    public String editOrderForm(
            @PathVariable("id") Long id,
            Model model
    ) {
        Optional<Order> optionalOrder = orderRepository.findById(id);

        if (optionalOrder.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/order/list";
        }

        Order order = optionalOrder.get();

        // ⚠️ đảm bảo orderDetails đã được load
        order.getOrderDetails().size();

        model.addAttribute("order", order);
        return "/order/edit";
    }

    /* =========================
       XỬ LÝ SUBMIT EDIT
       ========================= */
    @PostMapping("/order/edit/{id}")
    public String updateOrder(
            @PathVariable("id") Long id,
            @ModelAttribute("order") Order formOrder,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Order> optionalOrder = orderRepository.findById(id);

        if (optionalOrder.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại");
            return "redirect:/order/list";
        }

        Order dbOrder = optionalOrder.get();

        // ===== UPDATE ORDER =====
        dbOrder.setOrderDate(formOrder.getOrderDate());
        dbOrder.setStatus(formOrder.getStatus());
        dbOrder.setMethod(formOrder.getMethod());

        // ===== UPDATE ORDER DETAILS + CALC TOTAL =====
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < dbOrder.getOrderDetails().size(); i++) {
            OrderDetail dbDetail = dbOrder.getOrderDetails().get(i);
            OrderDetail formDetail = formOrder.getOrderDetails().get(i);

            dbDetail.setQuantity(formDetail.getQuantity());
            dbDetail.setTotalPrice(formDetail.getTotalPrice());
            dbDetail.setRating(formDetail.getRating());
            dbDetail.setComment(formDetail.getComment());

            // CỘNG TỔNG
            if (formDetail.getTotalPrice() != null) {
                totalAmount = totalAmount.add(
                        BigDecimal.valueOf(formDetail.getTotalPrice())
                );
            }
        }

        // ===== SET TOTAL AMOUNT =====
        dbOrder.setTotalAmount(totalAmount);

        orderRepository.save(dbOrder);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Cập nhật đơn hàng thành công"
        );

        return "redirect:/order/list";
    }

    @GetMapping("/order/delete/{id}")
    public String deleteOrder(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Order> optionalOrder = orderRepository.findById(id);

        if (optionalOrder.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Đơn hàng không tồn tại"
            );
            return "redirect:/order/list";
        }

        orderRepository.deleteById(id);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Xoá đơn hàng thành công"
        );

        return "redirect:/order/list";
    }
}