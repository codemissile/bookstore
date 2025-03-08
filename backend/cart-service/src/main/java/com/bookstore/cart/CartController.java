package com.bookstore.cart;

import com.bookstore.cart.model.*;
import com.bookstore.cart.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public ResponseEntity<List<CartItem>> getCartItems(@RequestHeader("Session-ID") String sessionId) {
        List<CartItem> items = cartItemRepository.findBySessionId(sessionId);
        System.out.println("Fetched cart items for session " + sessionId + ": " + items);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody CartRequest cartRequest, @RequestHeader("Session-ID") String sessionId) {
        try {
            Long bookId = cartRequest.getBookId();
            System.out.println("Adding book ID: " + bookId + " for session: " + sessionId);
            Book book = bookRepository.findById(bookId).orElse(null);
            if (book == null) {
                ResponseEntity<Book> response = restTemplate.getForEntity(
                    "http://localhost:5000/inventory/" + bookId, Book.class
                );
                if (response.getStatusCode().is2xxSuccessful()) {
                    book = response.getBody();
                    if (book == null || book.getId() == null) {
                        return ResponseEntity.status(500).body("Invalid book data from inventory");
                    }
                    bookRepository.save(book);
                } else {
                    return ResponseEntity.status(404).body("Book not found in inventory");
                }
            }
            CartItem item = new CartItem(sessionId, book.getId(), book.getTitle(), book.getPrice(), 1);
            cartItemRepository.save(item);
            System.out.println("Saved cart item: " + item);
            return ResponseEntity.ok("Item added to cart");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error adding to cart: " + e.getMessage());
        }
    }

    @PostMapping("/session")
    public ResponseEntity<String> createSession() {
        try {
            String sessionId = UUID.randomUUID().toString();
            Session session = new Session();
            session.setSessionId(sessionId);
            session.setUserData("{\"cart\": []}"); // Valid JSON string
            sessionRepository.save(session);
            return ResponseEntity.ok(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to create session: " + e.getMessage());
        }
    }

    @PostMapping("/order")
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest, @RequestHeader("Session-ID") String sessionId) {
        try {
            List<CartItem> cartItems = cartItemRepository.findBySessionId(sessionId);
            System.out.println("Cart items for session " + sessionId + ": " + cartItems);
            if (cartItems.isEmpty()) {
                return ResponseEntity.badRequest().body("Cart is empty");
            }
            Order order = new Order();
            order.setCustomerName(orderRequest.getCustomerName());
            order.setTotalAmount(cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum());
            orderRepository.save(order);
            for (CartItem item : cartItems) {
                OrderItem orderItem = new OrderItem(order.getId(), item.getBookId(), item.getQuantity(), item.getPrice());
                orderItemRepository.save(orderItem);
                ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:5000/inventory/update",
                    new StockUpdateRequest(item.getBookId(), item.getQuantity()),
                    String.class
                );
                System.out.println("Stock update response for book " + item.getBookId() + ": " + response.getBody());
            }
            cartItemRepository.deleteAll(cartItems);
            return ResponseEntity.ok("Order created with ID: " + order.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Order creation failed: " + e.getMessage());
        }
    }

    // New Delete Endpoint
    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeFromCart(@PathVariable Long id, @RequestHeader("Session-ID") String sessionId) {
        try {
            CartItem item = cartItemRepository.findById(id)
                .filter(cartItem -> cartItem.getSessionId().equals(sessionId))
                .orElse(null);
            if (item == null) {
                return ResponseEntity.status(404).body("Cart item not found or does not belong to this session");
            }
            cartItemRepository.deleteById(id);
            return ResponseEntity.ok("Item removed from cart");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error removing item: " + e.getMessage());
        }
    }
}

class CartRequest {
    private Long bookId;
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
}

class OrderRequest {
    private String customerName;
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}

class StockUpdateRequest {
    private Long bookId;
    private int quantity;
    public StockUpdateRequest(Long bookId, int quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }
    public Long getBookId() { return bookId; }
    public int getQuantity() { return quantity; }
}