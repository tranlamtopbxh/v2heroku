# GPSDEV - Ứng dụng GPS Định vị và Chỉ đường

Ứng dụng Android GPS định vị vị trí hiện tại và chỉ đường sử dụng Mapbox Navigation SDK, được viết bằng Kotlin.

## Tính năng chính

### 🗺️ Bản đồ và Định vị
- Hiển thị bản đồ Mapbox với style đường phố
- Định vị vị trí hiện tại tự động
- Theo dõi vị trí real-time
- Nút định vị để quay về vị trí hiện tại

### 🧭 Điều hướng
- Tính toán tuyến đường tối ưu
- Điều hướng turn-by-turn
- Hiển thị tuyến đường trên bản đồ với màu sắc theo mức độ tắc đường
- Mũi tên chỉ hướng và chú thích tuyến đường

### 🎤 Hướng dẫn bằng giọng nói
- Hướng dẫn điều hướng bằng tiếng Việt
- Có thể bật/tắt hướng dẫn giọng nói
- Thông báo rẽ trái, rẽ phải, đi thẳng, quay đầu

### 🚦 Thông tin giao thông
- Hiển thị biển báo giao thông
- Thông tin nút giao thông
- Giới hạn tốc độ
- Mức độ tắc đường (màu xanh, vàng, đỏ)

### 📊 Tiến độ chuyến đi
- Khoảng cách còn lại
- Thời gian dự kiến
- Thời gian đến nơi (ETA)
- Tốc độ hiện tại
- Hướng dẫn thao tác tiếp theo

### 🎨 Giao diện người dùng
- Giao diện Material Design hiện đại
- Các nút điều khiển trực quan
- Thông tin chuyến đi được hiển thị rõ ràng
- Responsive design cho các kích thước màn hình khác nhau

## Cài đặt và Chạy ứng dụng

### Yêu cầu hệ thống
- Android Studio Arctic Fox trở lên
- Android SDK 24 (Android 7.0) trở lên
- Kotlin 1.9.10
- Gradle 8.1.2

### Bước 1: Tạo tài khoản Mapbox
1. Truy cập [Mapbox Account](https://account.mapbox.com/)
2. Đăng ký hoặc đăng nhập tài khoản
3. Tạo Access Token:
   - Vào [Tokens](https://account.mapbox.com/access-tokens/)
   - Nhấn "Create a token"
   - Đặt tên token (ví dụ: `GPSDEV-Android`)
   - Trong "Secret Scopes", chọn "Downloads:Read"
   - Nhấn "Create token" và sao chép token

### Bước 2: Cấu hình token
1. Mở file `gradle.properties` trong thư mục người dùng (`~/.gradle/gradle.properties`)
2. Thêm dòng:
   ```
   MAPBOX_DOWNLOADS_TOKEN=YOUR_SECRET_MAPBOX_ACCESS_TOKEN
   ```
   Thay `YOUR_SECRET_MAPBOX_ACCESS_TOKEN` bằng token bạn vừa tạo

3. Mở file `app/src/main/res/values/strings.xml`
4. Thay `YOUR_MAPBOX_ACCESS_TOKEN` bằng public token của bạn:
   ```xml
   <string name="mapbox_access_token">YOUR_PUBLIC_MAPBOX_ACCESS_TOKEN</string>
   ```

### Bước 3: Mở project trong Android Studio
1. Mở Android Studio
2. Chọn "Open an existing project"
3. Chọn thư mục `/workspace/GPSDEV`
4. Đợi Gradle sync hoàn tất

### Bước 4: Chạy ứng dụng
1. Kết nối thiết bị Android hoặc khởi động emulator
2. Nhấn nút "Run" (▶️) hoặc Shift + F10
3. Ứng dụng sẽ được cài đặt và chạy trên thiết bị

## Cấu trúc Project

```
GPSDEV/
├── app/
│   ├── src/main/
│   │   ├── java/com/gpsdev/navigation/
│   │   │   └── MainActivity.kt          # Activity chính
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml    # Layout chính
│   │   │   ├── values/
│   │   │   │   ├── strings.xml          # Chuỗi văn bản
│   │   │   │   ├── colors.xml           # Màu sắc
│   │   │   │   └── themes.xml           # Theme
│   │   │   ├── drawable/                # Icons và drawables
│   │   │   └── xml/                     # Backup rules
│   │   └── AndroidManifest.xml          # Manifest file
│   ├── build.gradle                     # App dependencies
│   └── proguard-rules.pro              # ProGuard rules
├── build.gradle                         # Project dependencies
├── settings.gradle                      # Project settings
├── gradle.properties                    # Gradle properties
└── README.md                           # Hướng dẫn này
```

## Các tính năng chi tiết

### Location Tracking
- Sử dụng `NavigationLocationProvider` để theo dõi vị trí
- Tự động cập nhật vị trí hiện tại
- Hiển thị tốc độ di chuyển real-time

### Route Planning
- Sử dụng Mapbox Directions API
- Hỗ trợ nhiều loại phương tiện (ô tô, xe máy, đi bộ)
- Tối ưu hóa tuyến đường theo thời gian và khoảng cách

### Voice Guidance
- Sử dụng `MapboxSpeechApi` và `MapboxVoiceInstructionsPlayer`
- Hướng dẫn bằng tiếng Việt
- Có thể bật/tắt theo ý muốn

### Traffic Information
- Hiển thị mức độ tắc đường bằng màu sắc
- Thông tin biển báo và giới hạn tốc độ
- Cập nhật real-time

### UI Components
- `NavigationView`: Component chính cho điều hướng
- `MapView`: Hiển thị bản đồ Mapbox
- Custom UI cho thông tin chuyến đi
- Material Design buttons và cards

## Permissions

Ứng dụng yêu cầu các quyền sau:
- `ACCESS_FINE_LOCATION`: Định vị chính xác
- `ACCESS_COARSE_LOCATION`: Định vị gần đúng
- `INTERNET`: Kết nối mạng
- `ACCESS_NETWORK_STATE`: Kiểm tra trạng thái mạng
- `WAKE_LOCK`: Giữ màn hình sáng khi điều hướng
- `FOREGROUND_SERVICE`: Chạy service nền
- `POST_NOTIFICATIONS`: Hiển thị thông báo
- `VIBRATE`: Rung khi có thông báo
- `RECORD_AUDIO`: Ghi âm cho voice guidance

## Troubleshooting

### Lỗi thường gặp

1. **"Mapbox token not found"**
   - Kiểm tra lại token trong `strings.xml`
   - Đảm bảo token có quyền truy cập Maps API

2. **"Location permission denied"**
   - Cấp quyền truy cập vị trí trong Settings
   - Restart ứng dụng sau khi cấp quyền

3. **"Route calculation failed"**
   - Kiểm tra kết nối internet
   - Đảm bảo có vị trí hiện tại và điểm đến hợp lệ

4. **"Voice guidance not working"**
   - Kiểm tra volume của thiết bị
   - Đảm bảo không ở chế độ im lặng

### Debug Mode
Để debug ứng dụng:
1. Mở Android Studio
2. Chọn "Run" → "Debug 'app'"
3. Sử dụng Logcat để xem logs
4. Đặt breakpoints trong code

## Phát triển thêm

### Thêm tính năng mới
1. **Tìm kiếm địa điểm**: Implement Mapbox Geocoding API
2. **Lưu lịch sử**: Thêm database để lưu các chuyến đi
3. **Offline maps**: Tải bản đồ offline
4. **Multiple waypoints**: Thêm nhiều điểm dừng
5. **Traffic incidents**: Hiển thị sự cố giao thông

### Customization
- Thay đổi màu sắc trong `colors.xml`
- Thay đổi style bản đồ trong `MainActivity.kt`
- Thêm animations trong layouts
- Custom voice instructions

## Liên hệ và Hỗ trợ

Nếu gặp vấn đề hoặc cần hỗ trợ:
1. Kiểm tra [Mapbox Documentation](https://docs.mapbox.com/android/navigation/)
2. Xem [Mapbox Examples](https://github.com/mapbox/mapbox-navigation-android)
3. Tham khảo [Android Developer Guide](https://developer.android.com/)

## License

Project này sử dụng Mapbox Navigation SDK với license riêng. Vui lòng xem [Mapbox Terms of Service](https://www.mapbox.com/legal/tos/) để biết thêm chi tiết.

---

**Chúc bạn phát triển ứng dụng thành công! 🚀**