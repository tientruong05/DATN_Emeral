
<body>
  <h1>💼 Quy Chuẩn Làm Việc - DATN_Emeral</h1>
  <p><em>Tài liệu quy chuẩn về cách đặt tên nhánh và viết commit message trong quá trình phát triển dự án DATN_Emeral.</em></p>

  <hr>

  <h2>🪴 Quy Ước Đặt Tên Nhánh</h2>
  <p>Sử dụng định dạng:</p>
  <pre><code>&lt;type&gt;/&lt;tên-nhiệm-vụ&gt;</code></pre>

  <h3>📌 Các loại nhánh:</h3>
  <table>
    <thead>
      <tr>
        <th>Loại</th>
        <th>Mô tả</th>
        <th>Ví dụ</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><code>feat</code></td>
        <td>Tính năng mới</td>
        <td><code>feat/user-management</code></td>
      </tr>
      <tr>
        <td><code>fix</code></td>
        <td>Sửa lỗi</td>
        <td><code>fix/login-error</code></td>
      </tr>
      <tr>
        <td><code>add</code></td>
        <td>Thêm thư viện, cấu hình, tài nguyên...</td>
        <td><code>add/loombok-dependency</code></td>
      </tr>
    </tbody>
  </table>

  <div class="note">
    🔸 <strong>Lưu ý:</strong> Dấu cách trong tên nhánh phải thay bằng dấu <code>-</code>.<br>
    ✅ Ví dụ đúng: <code>feat/create-user</code><br>
    ❌ Ví dụ sai: <code>feat/create user</code>
  </div>

  <h2>📝 Quy Ước Commit Message</h2>
  <p>Commit message sẽ giống với merge title, theo định dạng:</p>
  <pre><code>[TYPE] Nội dung chính</code></pre>

  <h3>📌 Các loại commit:</h3>
  <table>
    <thead>
      <tr>
        <th>Tag</th>
        <th>Ý nghĩa</th>
        <th>Ví dụ</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td><code>[FEAT]</code></td>
        <td>Thêm tính năng mới</td>
        <td><code>[FEAT] Import excel</code></td>
      </tr>
      <tr>
        <td><code>[FIX]</code></td>
        <td>Sửa lỗi</td>
        <td><code>[FIX] Make save button bigger</code></td>
      </tr>
      <tr>
        <td><code>[ADD]</code></td>
        <td>Thêm thư viện, config...</td>
        <td><code>[ADD] Add loombok dependency</code></td>
      </tr>
    </tbody>
  </table>

  <div class="note">
    🔹 <strong>Lưu ý:</strong> Nếu thêm thư viện trong khi làm tính năng hoặc sửa lỗi, commit message vẫn chỉ cần ghi rõ nội dung chính.
  </div>

  <h2>✅ Ví Dụ Đầy Đủ</h2>
  <pre><code># Tạo nhánh mới
git checkout -b feat/user-management

# Commit thay đổi
git commit -m "[FEAT] Implement user list table with pagination"</code></pre>
</body>
