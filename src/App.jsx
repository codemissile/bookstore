import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Route, Routes, NavLink, useNavigate, useLocation } from "react-router-dom";
import axios from "axios";
import "./main.css";

function Header() {
    return (
        <header className="innerHeader">
            <img className="logo" src="/images/company-logo.png" alt="Company Logo" title="Company Logo" />
            <h1>Book Store</h1>
            <nav>
                <ul>
                    <li><NavLink to="/" className={({ isActive }) => (isActive ? "active" : "")}>Home</NavLink></li>
                    <li><NavLink to="/catalog" className={({ isActive }) => (isActive ? "active" : "")}>Catalog</NavLink></li>
                    <li><NavLink to="/cart" className={({ isActive }) => (isActive ? "active" : "")}>Cart</NavLink></li>
                </ul>
            </nav>
        </header>
    );
}

function Home() {
    return (
        <div className="home-container">
            <h1>Welcome to Bookstore</h1>
            <NavLink to="/catalog" className="button">Browse Catalog</NavLink>
        </div>
    );
}

function Catalog() {
    const [books, setBooks] = useState([]);

    useEffect(() => {
        axios.get("http://localhost:5000/catalog")
            .then((response) => setBooks(response.data))
            .catch((error) => console.error("Error fetching catalog:", error));
    }, []);

    const addToCart = (bookId) => {
        axios.post("http://localhost:8081/cart/add", { bookId })
            .then(() => alert("Added to cart!"))
            .catch((error) => console.error("Error adding to cart:", error));
    };

    return (
        <div className="container">
            <h1>Book Catalog</h1>
            <div className="book-list">
                {books.length === 0 ? (
                    <p>Loading books...</p>
                ) : (
                    books.map((book) => (
                        <section key={book.id} className="book-section">
                            <h2>{book.title}</h2>
                            <p>Price: ${Number(book.price).toFixed(2)}</p>
                            <p>Stock: {book.stock_quantity}</p>
                            <button onClick={() => addToCart(book.id)} className="button">Add to Cart</button>
                        </section>
                    ))
                )}
            </div>
        </div>
    );
}

function Cart() {
    const [cartItems, setCartItems] = useState([]);
    const navigate = useNavigate();
    const [customerName, setCustomerName] = useState("");

    useEffect(() => {
        fetchCart();
    }, []);

    const fetchCart = () => {
        axios.get("http://localhost:8081/cart")
            .then((response) => setCartItems(response.data))
            .catch((error) => console.error("Error fetching cart:", error));
    };

    const removeFromCart = (id) => {
        axios.delete(`http://localhost:8081/cart/remove/${id}`)
            .then(() => fetchCart())
            .catch((error) => console.error("Error removing item:", error));
    };

    const total = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);

    const handleCheckout = () => {
        if (cartItems.length === 0) {
            alert("Your cart is empty!");
            return;
        }
        if (!customerName) {
            alert("Please enter your name!");
            return;
        }
        axios.post("http://localhost:8081/cart/order", { customerName, items: cartItems })
            .then((response) => {
                const orderId = response.data;
                console.log("Order ID from server:", orderId);
                navigate("/payment", { state: { orderId } }); // Simplified state object
            })
            .catch((error) => alert("Order failed: " + error.response?.data || error.message));
    };

    return (
        <div className="container">
            <h1>Shopping Cart</h1>
            {cartItems.length === 0 ? (
                <p>Your cart is empty. <NavLink to="/catalog">Browse books</NavLink></p>
            ) : (
                <>
                    <input
                        type="text"
                        placeholder="Your Name"
                        value={customerName}
                        onChange={(e) => setCustomerName(e.target.value)}
                        style={{ marginBottom: "20px", padding: "5px" }}
                    />
                    <ul>
                        {cartItems.map((item) => (
                            <li key={item.id}>
                                {item.title} - ${item.price.toFixed(2)} x {item.quantity}
                                <button onClick={() => removeFromCart(item.id)} className="button remove-btn">Remove</button>
                            </li>
                        ))}
                    </ul>
                    <p className="total">Total: ${total.toFixed(2)}</p>
                    <button onClick={handleCheckout} className="button checkout-btn">Proceed to Checkout</button>
                </>
            )}
        </div>
    );
}

function Payment() {
    const navigate = useNavigate();
    const location = useLocation();
    const [card, setCard] = useState("");
    const [month, setMonth] = useState("0");
    const [year, setYear] = useState("0");
    const [cvv, setCvv] = useState("");
    const orderId = location?.state?.orderId;

    console.log("Payment page orderId:", orderId);

    if (!orderId) {
        console.error("No orderId provided, redirecting to cart");
        navigate("/cart"); // Redirect if orderId is missing
        return null;
    }

    const validate = () => {
        const cardPattern = /^(?:5[1-5][0-9]{14})$/;
        const cvvPattern = /^[0-9]{3,4}$/;
        const currentYear = new Date().getFullYear();
        const currentMonth = new Date().getMonth() + 1;

        if (!card.match(cardPattern)) return false;
        if (!cvv.match(cvvPattern)) return false;
        if (parseInt(year) < currentYear || (parseInt(year) === currentYear && parseInt(month) < currentMonth)) return false;

        return true;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (validate()) {
            const expdate = `${year}-${month.toString().padStart(2, "0")}-01`;
            console.log("Sending payment: ", { orderId, ccnum: card, expdate, seccode: cvv });
            axios.post("http://localhost:8081/cart/payment", { orderId, ccnum: card, expdate, seccode: cvv })
                .then((response) => {
                    console.log("Payment response:", response.data);
                    navigate(response.data === "success" ? "/success" : "/tryagain");
                })
                .catch((error) => {
                    console.error("Payment error:", error.response?.data || error.message);
                    navigate("/tryagain");
                });
        } else {
            console.log("Validation failed");
            navigate("/tryagain");
        }
    };

    return (
        <div className="paymentContainer">
            <h3>Payment Options</h3>
            <article>
                <h4>Debit/Credit Card</h4>
                <img className="masterCardImg" src="/images/mastercard-logo.png" alt="Mastercard Logo" />
            </article>
            <form className="paymentForm" onSubmit={handleSubmit}>
                <label className="card" htmlFor="card">Card Number:</label>
                <input
                    type="number"
                    placeholder="1111222233334444"
                    id="card"
                    value={card}
                    onChange={(e) => setCard(e.target.value)}
                    size="16"
                    required
                />
                <div className="expDateBox">
                    Expiration Date:
                    <select value={month} onChange={(e) => setMonth(e.target.value)}>
                        <option value="0">Month</option>
                        {["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"].map((m, i) => (
                            <option key={i} value={i + 1}>{m}</option>
                        ))}
                    </select>
                    <select value={year} onChange={(e) => setYear(e.target.value)}>
                        <option value="0">Year</option>
                        {Array.from({ length: 5 }, (_, i) => 2025 + i).map((y) => (
                            <option key={y} value={y}>{y}</option>
                        ))}
                    </select>
                </div>
                <label className="cvv" htmlFor="cvv">Security Code:</label>
                <input
                    type="number"
                    placeholder="1234"
                    id="cvv"
                    value={cvv}
                    onChange={(e) => setCvv(e.target.value)}
                    required
                />
                <p className="info">3-4 digit code on back of your card</p>
                <input type="submit" value="Continue" id="btn" />
            </form>
        </div>
    );
}

function Success() {
    return (
        <div className="paymentContainer">
            <h3>You have successfully placed your order</h3>
            <article>
                <h4>Debit/Credit Card</h4>
                <img className="masterCardImg" src="/images/mastercard-logo.png" alt="Mastercard Logo" />
            </article>
        </div>
    );
}

function TryAgain() {
    return (
        <div className="paymentContainer">
            <h3>Your details are incorrect</h3>
            <article>
                <h4><NavLink to="/payment">Try Again</NavLink></h4>
            </article>
        </div>
    );
}

function Footer() {
    return (
        <footer>
            <small>© 2025 Book Store</small>
        </footer>
    );
}

export default function App() {
    return (
        <Router>
            <Header />
            <main>
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/catalog" element={<Catalog />} />
                    <Route path="/cart" element={<Cart />} />
                    <Route path="/payment" element={<Payment />} />
                    <Route path="/success" element={<Success />} />
                    <Route path="/tryagain" element={<TryAgain />} />
                </Routes>
            </main>
            <Footer />
        </Router>
    );
}