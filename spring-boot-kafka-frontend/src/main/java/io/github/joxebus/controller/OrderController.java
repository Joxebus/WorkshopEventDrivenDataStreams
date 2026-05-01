package io.github.joxebus.controller;

import io.github.joxebus.dto.ProductOrderDTO;
import io.github.joxebus.dto.PurchaseOrderDTO;
import io.github.joxebus.enums.PurchaseOrderStatus;
import io.github.joxebus.kafka.producer.PurchaseOrderProducer;
import io.github.joxebus.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PurchaseOrderProducer orderProducer;

    private static final String CUSTOMER_ID = "CUSTOMER-001";

    @GetMapping
    public String list(Model model) {
        List<PurchaseOrderDTO> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("title", "Orders");
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable String id, Model model) {
        try {
            PurchaseOrderDTO order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            model.addAttribute("title", "Order Details");
            return "orders/details";
        } catch (Exception e) {
            model.addAttribute("error", "Order not found: " + id);
            return "redirect:/orders";
        }
    }

    @GetMapping("/purchase")
    public String purchase(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<ProductController.CartItem> cartItems =
            (List<ProductController.CartItem>) session.getAttribute("cart");

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        Double total = cartItems.stream()
            .mapToDouble(ProductController.CartItem::getSubtotal)
            .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("title", "Purchase Order");
        return "orders/purchase";
    }

    @PostMapping("/purchase")
    public String createPurchase(HttpSession session, RedirectAttributes redirectAttributes) {
        @SuppressWarnings("unchecked")
        List<ProductController.CartItem> cartItems =
            (List<ProductController.CartItem>) session.getAttribute("cart");

        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cart is empty");
            return "redirect:/products";
        }

        try {
            // Convert cart items to ProductOrderDTOs
            List<ProductOrderDTO> products = new ArrayList<>();
            for (ProductController.CartItem item : cartItems) {
                products.add(new ProductOrderDTO(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    item.getProduct().getPrice(),
                    item.getSubtotal()
                ));
            }

            Double total = cartItems.stream()
                .mapToDouble(ProductController.CartItem::getSubtotal)
                .sum();

            // Create PurchaseOrderDTO
            PurchaseOrderDTO order = new PurchaseOrderDTO(
                UUID.randomUUID().toString(),
                CUSTOMER_ID,
                products,
                total,
                LocalDateTime.now(),
                PurchaseOrderStatus.CREATED
            );

            // Send to Kafka
            orderProducer.sendOrder(order);

            // Clear cart
            session.removeAttribute("cart");

            redirectAttributes.addFlashAttribute("message",
                "Order placed successfully! Order ID: " + order.getOrderId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                "Error placing order: " + e.getMessage());
        }

        return "redirect:/orders";
    }

    @PostMapping("/clear-cart")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("cart");
        redirectAttributes.addFlashAttribute("message", "Cart cleared");
        return "redirect:/products";
    }
}
