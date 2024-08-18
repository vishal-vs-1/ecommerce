package com.ecommerce.service.impl;

import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.CartItemException;
import com.ecommerce.exception.ItemStockException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.requests.AddToCartRequest;
import com.ecommerce.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private CartItemRepository cartItemRepo;
    private UserRepository userRepo;
    private ProductRepository productRepo;
    private OrderRepository orderRepo;

    @Override
    public void addProductToCart(AddToCartRequest req) {
        User user = getUser();
        if (user.getCartItems() == null) {
            user.setCartItems(new ArrayList<>());
        }
        List<CartItem> cartItems = user.getCartItems();
        CartItem cartItem = CartItemMapper.addToCart(req);
        if (cartItems.isEmpty()) {
            cartItems.add(cartItemRepo.save(cartItem));
        } else {
            boolean isPresent = false;
            for (CartItem c : cartItems) {
                if (c.getProductId() == req.getProductId()) {
                    c.setQuantity(c.getQuantity() + req.getQuantity());
                    isPresent = true;
                }
            }
            if (!isPresent) {
                cartItems.add(cartItemRepo.save(cartItem));
            }
        }
    }

    @Override
    public String removeProductFromCart(int id) {
        User user = getUser();
        List<CartItem> cartItems = user.getCartItems();
        if (cartItems == null)
            throw new CartItemException("EMPTY.CART");
        if (cartItems.isEmpty())
            throw new CartItemException("EMPTY.CART");
        for (CartItem c : cartItems) {
            if (c.getProductId() == id) {
                c.setQuantity(c.getQuantity() - 1);
            }
        }
        return "Product successfully removed";
    }

    @Override                 //incomplete
    public String orderItems() {
        User user = getUser();
        Double total = checkoutCart(user);
        List<CartItem> cartItems = user.getCartItems();
        for (CartItem item : cartItems) {
            Product product = productRepo.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("INVALID.PRODUCT.ID"));
            if(product.getQuantity() < item.getQuantity())
                throw new ItemStockException("NOT.ENOUGH.QUANTITY.AVAILABLE");
            else
                product.setQuantity(product.getQuantity()-item.getQuantity());
        }


        return "";
    }

    private Double checkoutCart(User user) {
        double finalPrice = 0.0d;
        for (CartItem cartItem : user.getCartItems()) {
            Product product = productRepo.findById(
                    cartItem.getProductId()
            ).orElseThrow(
                    () -> new ProductNotFoundException("INVALID.PRODUCT.ID")
            );

            double cost = product.getCost();
            int discount = product.getDiscount();
            finalPrice += (cost - (cost * discount / 100.0));
        }
        return finalPrice;
    }

    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return userRepo.findByEmail(currentPrincipalName).orElseThrow(() -> new RuntimeException("Something went wrong"));
    }
}