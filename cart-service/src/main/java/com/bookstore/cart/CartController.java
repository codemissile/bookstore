package com.bookstore.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "http://localhost:3000")

public class CartController {

    private static final Logger logger = Logger.getLogger(CartController.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPassword;
    @Value("${catalog.service.url}")
    private String catalogServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public List<CartItem> getCartItems() {
        List<CartItem> cartItems = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            PreparedStatement stmt = conn.prepareStatement("SELECT id, book_id, title, price, quantity FROM cart_items");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cartItems.add(new CartItem(
                    rs.getLong("id"),
                    rs.getLong("book_id"),
                    rs.getString("title"),
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                ));
            }
            logger.info("Fetched cart items: " + cartItems.size());
        } catch (Exception e) {
            logger.severe("Failed to fetch cart items: " + e.getMessage());
            throw new RuntimeException("Failed to fetch cart items: " + e.getMessage());
        }
        return cartItems;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody CartRequest cartRequest) {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            ResponseEntity<String> response = restTemplate.getForEntity(
                catalogServiceUrl + "/inventory/" + cartRequest.getBookId(), String.class
            );
            if (response.getStatusCodeValue() != 200) {
                logger.warning("Book not found in inventory: " + cartRequest.getBookId());
                return ResponseEntity.status(404).body("Book not found in inventory");
            }
            JsonNode bookJson = objectMapper.readTree(response.getBody());
            if (bookJson.get("title") == null || bookJson.get("price") == null) {
                logger.warning("Invalid inventory response for book_id: " + cartRequest.getBookId());
                return ResponseEntity.status(500).body("Invalid inventory data");
            }
            String title = bookJson.get("title").asText();
            double price = bookJson.get("price").asDouble();

            PreparedStatement stmt = conn.prepareStatement("SELECT id, quantity FROM cart_items WHERE book_id = ?");
            stmt.setLong(1, cartRequest.getBookId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Long id = rs.getLong("id");
                int newQuantity = rs.getInt("quantity") + 1;
                stmt = conn.prepareStatement("UPDATE cart_items SET quantity = ? WHERE id = ?");
                stmt.setInt(1, newQuantity);
                stmt.setLong(2, id);
                stmt.executeUpdate();
                logger.info("Updated cart item: book_id=" + cartRequest.getBookId() + ", quantity=" + newQuantity);
            } else {
                stmt = conn.prepareStatement(
                    "INSERT INTO cart_items (book_id, title, price, quantity) VALUES (?, ?, ?, ?)"
                );
                stmt.setLong(1, cartRequest.getBookId());
                stmt.setString(2, title);
                stmt.setDouble(3, price);
                stmt.setInt(4, 1);
                stmt.executeUpdate();
                logger.info("Added new cart item: book_id=" + cartRequest.getBookId());
            }
            return ResponseEntity.ok("Item added to cart");
        } catch (Exception e) {
            logger.severe("Failed to add to cart: " + e.getMessage());
            return ResponseEntity.status(500).body("Failed to add to cart: " + e.getMessage());
        }
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> removeFromCart(@PathVariable Long id) {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM cart_items WHERE id = ?");
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Removed cart item: id=" + id);
                return ResponseEntity.ok("Item removed from cart");
            } else {
                logger.warning("Cart item not found: id=" + id);
                return ResponseEntity.status(404).body("Item not found");
            }
        } catch (Exception e) {
            logger.severe("Failed to remove item: " + e.getMessage());
            return ResponseEntity.status(500).body("Failed to remove item: " + e.getMessage());
        }
    }

    @PostMapping("/order")
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            conn.setAutoCommit(false);

            List<CartItem> cartItems = getCartItems();
            if (cartItems.isEmpty()) {
                logger.warning("Cart is empty for order attempt by: " + orderRequest.getCustomerName());
                return ResponseEntity.status(400).body("Cart is empty");
            }

            for (CartItem item : cartItems) {
                ResponseEntity<String> response = restTemplate.getForEntity(
                    catalogServiceUrl + "/inventory/" + item.getBookId(), String.class
                );
                if (response.getStatusCodeValue() != 200) {
                    throw new RuntimeException("Stock check failed for book ID: " + item.getBookId());
                }
                JsonNode stockJson = objectMapper.readTree(response.getBody());
                int stock = stockJson.get("stock").asInt();
                if (stock < item.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for book ID: " + item.getBookId());
                }
            }

            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO orders (customer_name, total_amount) VALUES (?, ?) RETURNING id"
            );
            double total = cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
            stmt.setString(1, orderRequest.getCustomerName());
            stmt.setDouble(2, total);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            Long orderId = rs.getLong("id");

            for (CartItem item : cartItems) {
                stmt = conn.prepareStatement(
                    "INSERT INTO order_items (order_id, book_id, quantity, price) VALUES (?, ?, ?, ?)"
                );
                stmt.setLong(1, orderId);
                stmt.setLong(2, item.getBookId());
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getPrice());
                stmt.executeUpdate();

                restTemplate.postForEntity(
                    catalogServiceUrl + "/inventory/update",
                    new StockUpdateRequest(item.getBookId(), item.getQuantity()),
                    String.class
                );
            }

            stmt = conn.prepareStatement("DELETE FROM cart_items");
            stmt.executeUpdate();

            conn.commit();
            logger.info("Order created: id=" + orderId + ", customer=" + orderRequest.getCustomerName());
            return ResponseEntity.ok(orderId.toString());
        } catch (Exception e) {
            logger.severe("Failed to create order: " + e.getMessage());
            return ResponseEntity.status(400).body("Failed to create order: " + e.getMessage());
        }
    }

    @PostMapping("/payment")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequest paymentRequest) {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            logger.info("Processing payment: orderId=" + paymentRequest.getOrderId() + ", ccnum=" + paymentRequest.getCcnum() + ", expdate=" + paymentRequest.getExpdate() + ", seccode=" + paymentRequest.getSeccode());
            
            PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM orders WHERE id = ?");
            checkStmt.setLong(1, paymentRequest.getOrderId());
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                logger.warning("Order not found: " + paymentRequest.getOrderId());
                return ResponseEntity.status(404).body("Order not found");
            }

            String hashedCcnum = md5Hash(paymentRequest.getCcnum());
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO card (order_id, ccnum, expdate, seccode) VALUES (?, ?, ?, ?)"
            );
            stmt.setLong(1, paymentRequest.getOrderId());
            stmt.setString(2, hashedCcnum);
            stmt.setDate(3, java.sql.Date.valueOf(paymentRequest.getExpdate()));
            stmt.setString(4, paymentRequest.getSeccode());
            stmt.executeUpdate();
            logger.info("Payment processed for order: " + paymentRequest.getOrderId());
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            logger.severe("Failed to process payment: " + e.getMessage());
            return ResponseEntity.status(400).body("tryagain: " + e.getMessage());
        }
    }

    private String md5Hash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

class CartItem {
    private Long id;
    private Long bookId;
    private String title;
    private double price;
    private int quantity;

    public CartItem(Long id, Long bookId, String title, double price, int quantity) {
        this.id = id;
        this.bookId = bookId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }
    public Long getId() { return id; }
    public Long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
}

class CartRequest {
    private Long bookId;
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
}

class OrderRequest {
    private String customerName;
    private List<CartItem> items;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}

class PaymentRequest {
    private Long orderId;
    private String ccnum;
    private String expdate;
    private String seccode;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getCcnum() { return ccnum; }
    public void setCcnum(String ccnum) { this.ccnum = ccnum; }
    public String getExpdate() { return expdate; }
    public void setExpdate(String expdate) { this.expdate = expdate; }
    public String getSeccode() { return seccode; }
    public void setSeccode(String seccode) { this.seccode = seccode; }
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