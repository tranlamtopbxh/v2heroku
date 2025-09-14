# HƯỚNG DẪN CÀI ĐẶT ỨNG DỤNG GPSDEV

## Bước 1: Chuẩn bị môi trường phát triển

### 1.1 Cài đặt Android Studio
1. Tải Android Studio từ: https://developer.android.com/studio
2. Cài đặt với các components mặc định
3. Mở Android Studio và cài đặt Android SDK (API level 24 trở lên)

### 1.2 Cài đặt Java Development Kit (JDK)
- Android Studio thường đã bao gồm JDK, nhưng nếu cần cài riêng:
- Tải JDK 8 hoặc 11 từ: https://adoptium.net/

## Bước 2: Tạo tài khoản Mapbox

### 2.1 Đăng ký tài khoản
1. Truy cập: https://account.mapbox.com/
2. Nhấn "Sign up" để đăng ký
3. Xác thực email

### 2.2 Tạo Access Token
1. Đăng nhập vào tài khoản Mapbox
2. Vào mục "Access tokens": https://account.mapbox.com/access-tokens/
3. Nhấn "Create a token"
4. Đặt tên token: `GPSDEV-Android`
5. Trong phần "Secret scopes", chọn:
   - ✅ Downloads:Read
   - ✅ Styles:Read
   - ✅ Fonts:Read
   - ✅ Datasets:Read
   - ✅ Vision:Read
6. Nhấn "Create token"
7. **QUAN TRỌNG**: Sao chép token này ngay lập tức (chỉ hiển thị 1 lần)

### 2.3 Tạo Public Token
1. Tạo thêm 1 token khác với tên: `GPSDEV-Public`
2. Chỉ chọn "Public scopes":
   - ✅ Styles:Read
   - ✅ Fonts:Read
   - ✅ Datasets:Read
   - ✅ Vision:Read
3. Nhấn "Create token"
4. Sao chép token này

## Bước 3: Cấu hình Project

### 3.1 Cấu hình Secret Token
1. Mở file `~/.gradle/gradle.properties` (trong thư mục home của bạn)
2. Nếu file không tồn tại, tạo mới
3. Thêm dòng:
   ```
   MAPBOX_DOWNLOADS_TOKEN=YOUR_SECRET_TOKEN_HERE
   ```
   Thay `YOUR_SECRET_TOKEN_HERE` bằng secret token bạn vừa tạo

### 3.2 Cấu hình Public Token
1. Mở file `app/src/main/res/values/strings.xml`
2. Tìm dòng:
   ```xml
   <string name="mapbox_access_token">YOUR_MAPBOX_ACCESS_TOKEN</string>
   ```
3. Thay `YOUR_MAPBOX_ACCESS_TOKEN` bằng public token

## Bước 4: Mở Project trong Android Studio

### 4.1 Import Project
1. Mở Android Studio
2. Chọn "Open an existing project"
3. Duyệt đến thư mục `/workspace/GPSDEV`
4. Chọn thư mục `GPSDEV` và nhấn "OK"

### 4.2 Đợi Gradle Sync
- Android Studio sẽ tự động sync project
- Đợi quá trình này hoàn tất (có thể mất vài phút lần đầu)
- Nếu có lỗi, kiểm tra lại token configuration

## Bước 5: Cấu hình thiết bị

### 5.1 Sử dụng Emulator
1. Trong Android Studio, nhấn "Device Manager"
2. Nhấn "Create Device"
3. Chọn "Phone" → "Pixel 4" (hoặc thiết bị khác)
4. Chọn API level 30 hoặc cao hơn
5. Nhấn "Next" → "Finish"

### 5.2 Sử dụng thiết bị thật
1. Bật "Developer Options" trên điện thoại:
   - Vào Settings → About Phone
   - Nhấn 7 lần vào "Build Number"
2. Bật "USB Debugging" trong Developer Options
3. Kết nối điện thoại với máy tính qua USB
4. Cho phép USB Debugging khi được hỏi

## Bước 6: Chạy ứng dụng

### 6.1 Build và Run
1. Chọn thiết bị/emulator từ dropdown
2. Nhấn nút "Run" (▶️) hoặc Shift + F10
3. Đợi ứng dụng build và cài đặt

### 6.2 Cấp quyền
Khi ứng dụng chạy lần đầu:
1. Cho phép truy cập vị trí khi được hỏi
2. Cho phép ghi âm (cho voice guidance)
3. Cho phép thông báo

## Bước 7: Kiểm tra hoạt động

### 7.1 Kiểm tra cơ bản
- ✅ Bản đồ hiển thị
- ✅ Vị trí hiện tại được định vị
- ✅ Các nút điều khiển hoạt động

### 7.2 Kiểm tra điều hướng
1. Nhấn nút "Tìm kiếm"
2. Nhập địa điểm (ví dụ: "Hồ Gươm, Hà Nội")
3. Nhấn "Bắt đầu" để bắt đầu điều hướng
4. Kiểm tra:
   - ✅ Tuyến đường hiển thị trên bản đồ
   - ✅ Thông tin chuyến đi cập nhật
   - ✅ Voice guidance hoạt động

## Xử lý lỗi thường gặp

### Lỗi "Mapbox token not found"
**Nguyên nhân**: Token không được cấu hình đúng
**Giải pháp**:
1. Kiểm tra lại token trong `strings.xml`
2. Đảm bảo token có đúng quyền
3. Restart Android Studio

### Lỗi "Location permission denied"
**Nguyên nhân**: Ứng dụng không có quyền truy cập vị trí
**Giải pháp**:
1. Vào Settings → Apps → GPSDEV → Permissions
2. Bật "Location" permission
3. Restart ứng dụng

### Lỗi "Route calculation failed"
**Nguyên nhân**: Không có kết nối internet hoặc vị trí không hợp lệ
**Giải pháp**:
1. Kiểm tra kết nối internet
2. Đảm bảo GPS được bật
3. Thử lại sau vài giây

### Lỗi "Voice guidance not working"
**Nguyên nhân**: Volume tắt hoặc không có quyền ghi âm
**Giải pháp**:
1. Tăng volume thiết bị
2. Kiểm tra quyền "Microphone" trong Settings
3. Đảm bảo không ở chế độ im lặng

### Lỗi Gradle sync failed
**Nguyên nhân**: Token secret không đúng hoặc kết nối mạng
**Giải pháp**:
1. Kiểm tra `gradle.properties` file
2. Đảm bảo secret token đúng
3. Kiểm tra kết nối internet
4. Clean và Rebuild project

## Tùy chỉnh ứng dụng

### Thay đổi màu sắc
1. Mở `app/src/main/res/values/colors.xml`
2. Thay đổi các giá trị màu theo ý muốn
3. Rebuild ứng dụng

### Thay đổi ngôn ngữ
1. Mở `app/src/main/res/values/strings.xml`
2. Thay đổi các chuỗi văn bản
3. Rebuild ứng dụng

### Thêm tính năng mới
1. Tham khảo [Mapbox Documentation](https://docs.mapbox.com/android/navigation/)
2. Xem các ví dụ trong [Mapbox Examples](https://github.com/mapbox/mapbox-navigation-android)
3. Implement theo hướng dẫn

## Hỗ trợ

Nếu gặp vấn đề:
1. Kiểm tra logs trong Android Studio Logcat
2. Tham khảo tài liệu Mapbox
3. Tìm kiếm trên Stack Overflow
4. Tạo issue trên GitHub repository

---

**Chúc bạn thành công với ứng dụng GPSDEV! 🚀**