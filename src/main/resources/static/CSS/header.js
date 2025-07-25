  document.addEventListener("DOMContentLoaded", function () {
	  const searchInput = document.getElementById("searchInput");

	  // Kiểm tra xem ô nhập có tồn tại không
	  if (searchInput) {
	    // Submit tìm kiếm khi nhấn Enter
	    searchInput.addEventListener("keypress", function (e) {
	      if (e.key === "Enter") {
	        console.log("Search submitted:", searchInput.value); // Kiểm tra giá trị
	        window.location.href = "/search?q=" + encodeURIComponent(searchInput.value);
	      }
	    });
	  } else {
	    console.error("Search input not found!"); // Thông báo lỗi nếu ô nhập không tồn tại
	  }
	});
/**
 * 
 */