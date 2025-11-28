// cart.js

// 1. Hàm thêm vào giỏ hàng
function addToCart(id, name, price, image) {
    // Lấy giỏ hàng cũ từ bộ nhớ (nếu chưa có thì tạo mảng rỗng)
    let cart = JSON.parse(localStorage.getItem('cart')) || [];

    // Kiểm tra xem sản phẩm đã có trong giỏ chưa
    let existingItem = cart.find(item => item.id === id);

    if (existingItem) {
        // Nếu có rồi thì tăng số lượng
        existingItem.quantity += 1;
    } else {
        // Nếu chưa có thì thêm mới
        cart.push({
            id: id,
            name: name,
            price: price,
            image: image,
            quantity: 1
        });
    }

    // Lưu ngược lại vào bộ nhớ
    localStorage.setItem('cart', JSON.stringify(cart));

    // Thông báo và cập nhật số lượng trên icon
    alert("Đã thêm " + name + " vào giỏ hàng!");
    updateCartCount();
}

// 2. Hàm cập nhật số lượng trên Icon Giỏ hàng (Header)
function updateCartCount() {
    let cart = JSON.parse(localStorage.getItem('cart')) || [];
    let totalCount = cart.reduce((sum, item) => sum + item.quantity, 0);

    // Giả sử bạn có thẻ <span id="cart-count"> trên menu
    const countElement = document.getElementById('cart-count');
    if(countElement) countElement.innerText = totalCount;
}

// Chạy hàm này mỗi khi tải trang để số lượng luôn đúng
document.addEventListener("DOMContentLoaded", updateCartCount);