import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Route, Routes, NavLink, useNavigate } from "react-router-dom";
import axios from "axios";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import "bootstrap/dist/css/bootstrap.min.css";
import "./main.css";

function Header() {
    return (
        <header className="innerHeader mb-4">
            <div className="header-top">
                <NavLink to="/" className="logo-link">
                    <img src="/images/company-logo.png" className="logo" alt="Company Logo" />
                </NavLink>
                <h1 className="header-title">BookStore</h1>
            </div>
            <nav className="header-nav">
                <NavLink className={({ isActive }) => "nav-link" + (isActive ? " active" : "")} to="/">🏠 Home</NavLink>
                <NavLink className={({ isActive }) => "nav-link" + (isActive ? " active" : "")} to="/catalog">📚 Catalog</NavLink>
                <NavLink className={({ isActive }) => "nav-link" + (isActive ? " active" : "")} to="/cart">🛒 Cart</NavLink>
            </nav>
        </header>
    );
}

function Home() {
    return (
        <div className="container text-center">
            <h1>Welcome to Bookstore</h1>
            <NavLink to="/catalog" className="btn btn-primary mt-3">Browse Catalog</NavLink>
        </div>
    );
}

function Catalog({ sessionId }) {
    const [books, setBooks] = useState([]);
    const [addedToCart, setAddedToCart] = useState({});

    const fetchCatalog = () => {
        axios.get("http://localhost:5000/catalog")
            .then((response) => {
                console.log("Catalog data:", response.data);
                setBooks(response.data);
            })
            .catch((error) => {
                console.error("Error fetching catalog:", error);
                toast.error("Failed to load catalog");
            });
    };

    useEffect(() => {
        fetchCatalog();
    }, []);

    const addToCart = (bookId, stock) => {
        if (!sessionId) {
            toast.error("Session not initialized. Please refresh the page.");
            return;
        }
        if (stock === 0) {
            toast.error("❌ Out of stock!", { position: "top-right", autoClose: 3000 });
            return;
        }
        axios.post("http://localhost:8081/cart/add", { bookId }, { headers: { "Session-ID": sessionId } })
            .then(() => {
                setAddedToCart((prev) => ({ ...prev, [bookId]: true }));
                toast.success("✅ Added to cart!", { position: "top-right", autoClose: 2000 });
                window.dispatchEvent(new CustomEvent("cartUpdated"));
                fetchCatalog();
            })
            .catch((error) => {
                console.error("Error adding to cart:", error.response?.data || error.message);
                toast.error("❌ Add failed: " + (error.response?.data || "Unknown error"), { position: "top-right", autoClose: 3000 });
            });
    };

    useEffect(() => {
        const handleOrderComplete = () => fetchCatalog();
        window.addEventListener("orderCompleted", handleOrderComplete);
        return () => window.removeEventListener("orderCompleted", handleOrderComplete);
    }, []);

    return (
        <div className="container">
            <h1 className="mb-4">Book Catalog</h1>
            <div className="row">
                {books.length === 0 ? (
                    <p>Loading books...</p>
                ) : (
                    books.map((book) => (
                        <div key={book.id} className="col-md-4 mb-4">
                            <div className="card shadow-sm h-100">
                                <img src={book.image_url} className="card-img-top" alt={book.title} style={{ height: "250px", objectFit: "contain" }} />
                                <div className="card-body d-flex flex-column">
                                    <h5 className="card-title">{book.title}</h5>
                                    <p className="card-text text-muted">👨‍💻 {book.author}</p>
                                    <p className="card-text fw-bold text-primary">💰 ${Number(book.price).toFixed(2)}</p>
                                    <p className={`card-text ${book.stock_quantity > 0 ? "text-success" : "text-danger"}`}>
                                        {book.stock_quantity > 0 ? `📦 In Stock: ${book.stock_quantity}` : "❌ Out of Stock"}
                                    </p>
                                    <button
                                        className="btn btn-primary mt-auto"
                                        onClick={() => addToCart(book.id, book.stock_quantity)}
                                    >
                                        {addedToCart[book.id] ? "✔️ Added More" : "🛒 Add to Cart"}
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}

function Cart({ sessionId }) {
    const [cartItems, setCartItems] = useState([]);
    const [isProcessing, setIsProcessing] = useState(false);
    const navigate = useNavigate();

    const fetchCart = () => {
        if (!sessionId) return;
        axios.get("http://localhost:8081/cart", { headers: { "Session-ID": sessionId } })
            .then((response) => {
                console.log("Cart items:", response.data);
                setCartItems(response.data);
            })
            .catch((error) => {
                console.error("Error fetching cart:", error);
                toast.error("Failed to load cart");
            });
    };

    useEffect(() => {
        fetchCart();
        const updateCart = () => fetchCart();
        window.addEventListener("cartUpdated", updateCart);
        return () => window.removeEventListener("cartUpdated", updateCart);
    }, [sessionId]);

    const removeFromCart = (id) => {
        if (!sessionId) {
            toast.error("Session not initialized. Please refresh the page.");
            return;
        }
        axios.delete(`http://localhost:8081/cart/${id}`, { headers: { "Session-ID": sessionId } })
            .then(() => {
                console.log("Item removed:", id);
                fetchCart();
                toast.success("Item removed from cart!");
            })
            .catch((error) => {
                console.error("Error removing item:", error);
                toast.error("Failed to remove item");
            });
    };

    const handleCheckout = () => {
        if (isProcessing) return;
        if (cartItems.length === 0) {
            toast.error("Your cart is empty!");
            return;
        }
        setIsProcessing(true);
        axios.post("http://localhost:8081/cart/order", { customerName: "Anonymous" }, { headers: { "Session-ID": sessionId } })
            .then((response) => {
                console.log("Order response:", response.data);
                toast.success("✅ Order placed successfully!");
                setCartItems([]);
                fetchCart();
                window.dispatchEvent(new Event("orderCompleted"));
                setTimeout(() => navigate("/catalog"), 1000);
            })
            .catch((error) => {
                console.error("Order error:", error.response?.data || error.message);
                toast.error("❌ Checkout failed: " + (error.response?.data || "Unknown error"));
            })
            .finally(() => setIsProcessing(false));
    };

    const total = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);

    return (
        <div className="container">
            <h2 className="mb-4">🛒 Shopping Cart</h2>
            {cartItems.length === 0 ? (
                <p>🛍️ Cart is empty. <NavLink to="/catalog">Browse books</NavLink></p>
            ) : (
                <div>
                    <ul className="list-group mb-3">
                        {cartItems.map((item) => (
                            <li key={item.id} className="list-group-item d-flex align-items-center">
                                <div className="flex-grow-1 text-start">
                                    <span>{item.title}</span>
                                    <p className="mb-0">📦 Quantity: {item.quantity} - ${Number(item.price).toFixed(2)}</p>
                                </div>
                                <button className="btn btn-danger btn-sm ms-auto" onClick={() => removeFromCart(item.id)}>❌ Remove</button>
                            </li>
                        ))}
                    </ul>
                    <p className="total text-end">Total: ${Number(total).toFixed(2)}</p>
                    <div className="text-end">
                        <button className="btn btn-success btn-lg" onClick={handleCheckout} disabled={isProcessing}>
                            💳 Checkout
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}

function Footer() {
    return (
        <footer className="mt-5">
            <small>© 2025 Book Store</small>
        </footer>
    );
}

export default function App() {
    const [sessionId, setSessionId] = useState(localStorage.getItem("sessionId") || null);
    const [sessionError, setSessionError] = useState(null);

    useEffect(() => {
        if (!sessionId) {
            axios.post("http://localhost:8081/cart/session")
                .then((res) => {
                    const newSessionId = res.data;
                    localStorage.setItem("sessionId", newSessionId);
                    setSessionId(newSessionId);
                    console.log("Session ID set:", newSessionId);
                })
                .catch((err) => {
                    const errorMsg = err.response
                        ? `Status: ${err.response.status}, Data: ${JSON.stringify(err.response.data)}`
                        : err.message;
                    console.error("Session error:", errorMsg);
                    setSessionError(`Failed to initialize session: ${errorMsg}. Please ensure cart-service is running and refresh.`);
                });
        }
    }, [sessionId]);

    if (!sessionId && sessionError) {
        return <div className="container text-center mt-5">{sessionError}</div>;
    }

    if (!sessionId) {
        return <div className="container text-center mt-5">Loading session...</div>;
    }

    return (
        <Router>
            <div className="container">
                <ToastContainer position="top-right" autoClose={3000} />
                <Header />
                <main>
                    <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/catalog" element={<Catalog sessionId={sessionId} />} />
                        <Route path="/cart" element={<Cart sessionId={sessionId} />} />
                    </Routes>
                </main>
                <Footer />
            </div>
        </Router>
    );
}