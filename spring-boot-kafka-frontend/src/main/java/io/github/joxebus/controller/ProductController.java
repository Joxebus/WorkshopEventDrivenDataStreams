package io.github.joxebus.controller;

import io.github.joxebus.dto.ProductDTO;
import io.github.joxebus.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String list(Model model) {
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("title", "Products");
        return "products/list";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new ProductDTO(null, "", "", 0.0, 0, ""));
        model.addAttribute("title", "Create Product");
        model.addAttribute("isEdit", false);
        return "products/form";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable String id, Model model) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("title", "Edit Product");
        model.addAttribute("isEdit", true);
        return "products/form";
    }

    @PostMapping
    public String create(@ModelAttribute ProductDTO product, RedirectAttributes redirectAttributes) {
        try {
            productService.createProduct(product);
            redirectAttributes.addFlashAttribute("message", "Product created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating product: " + e.getMessage());
        }
        return "redirect:/products";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable String id, @ModelAttribute ProductDTO product,
                         RedirectAttributes redirectAttributes) {
        try {
            productService.updateProduct(id, product);
            redirectAttributes.addFlashAttribute("message", "Product updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating product: " + e.getMessage());
        }
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("message", "Product deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting product: " + e.getMessage());
        }
        return "redirect:/products";
    }

    @PostMapping("/{id}/add-to-cart")
    public String addToCart(@PathVariable String id,
                           @RequestParam(defaultValue = "1") Integer quantity,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        try {
            ProductDTO product = productService.getProductById(id);

            @SuppressWarnings("unchecked")
            List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
            if (cart == null) {
                cart = new ArrayList<>();
            }

            // Check if product already in cart
            boolean found = false;
            for (CartItem item : cart) {
                if (item.getProduct().getId().equals(id)) {
                    item.setQuantity(item.getQuantity() + quantity);
                    found = true;
                    break;
                }
            }

            if (!found) {
                cart.add(new CartItem(product, quantity));
            }

            session.setAttribute("cart", cart);
            redirectAttributes.addFlashAttribute("message", "Product added to cart");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding product to cart: " + e.getMessage());
        }
        return "redirect:/products";
    }

    // Inner class for cart items
    public static class CartItem {
        private ProductDTO product;
        private Integer quantity;

        public CartItem(ProductDTO product, Integer quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public ProductDTO getProduct() {
            return product;
        }

        public void setProduct(ProductDTO product) {
            this.product = product;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Double getSubtotal() {
            return product.getPrice() * quantity;
        }
    }
}
