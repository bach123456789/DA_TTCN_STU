// Chờ DOM load xong
document.addEventListener("DOMContentLoaded", function(event) {
    
    // Lấy phần tử nút bấm toggle
    var el = document.getElementById("menu-toggle");
    
    // Lấy phần tử wrapper
    var wrapper = document.getElementById("wrapper");

    // Bắt sự kiện click
    el.addEventListener("click", function() {
        // Thêm hoặc xóa class 'toggled' trên wrapper
        wrapper.classList.toggle("toggled");
    });
});