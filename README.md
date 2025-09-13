# Mapbox Navigation Android App

Ứng dụng điều hướng Mapbox đầy đủ tính năng cho Android với Empty View Activity.

## Tính năng chính

### 🗺️ Maps & Navigation
- **Mapbox Maps SDK**: Hiển thị bản đồ tương tác
- **Navigation Camera**: Camera tự động theo dõi vị trí
- **Route Line**: Vẽ tuyến đường trên bản đồ
- **Route Arrow**: Mũi tên chỉ hướng đi
- **Route Callouts**: Thông tin chi tiết về tuyến đường

### 📍 Location Tracking
- **Current Location**: Định vị vị trí hiện tại
- **Location Updates**: Cập nhật vị trí real-time
- **Location Permissions**: Quản lý quyền truy cập vị trí

### 🎤 Voice Instructions
- **Voice Guidance**: Hướng dẫn bằng giọng nói
- **Maneuver Instructions**: Hướng dẫn thao tác chi tiết
- **Voice Toggle**: Bật/tắt hướng dẫn giọng nói

### 🚦 Advanced Features
- **Speed Limit**: Hiển thị giới hạn tốc độ
- **Trip Progress**: Theo dõi tiến trình chuyến đi
- **Arrival Detection**: Phát hiện đến đích
- **Building Highlights**: Làm nổi bật tòa nhà
- **Device Notifications**: Thông báo thiết bị
- **Signboards**: Hiển thị biển báo
- **Junctions**: Giao lộ phức tạp

## Cài đặt

### 1. Yêu cầu hệ thống
- Android Studio Arctic Fox trở lên
- Android SDK 24+ (Android 7.0)
- Kotlin 1.8+

### 2. Mapbox Access Token
1. Đăng ký tài khoản tại [Mapbox](https://account.mapbox.com/)
2. Tạo access token tại [Access Tokens](https://account.mapbox.com/access-tokens/)
3. Thay thế `YOUR_MAPBOX_ACCESS_TOKEN` trong file `app/src/main/res/values/mapbox_access_token.xml`

### 3. Dependencies
Tất cả dependencies đã được cấu hình trong `app/build.gradle`:

```gradle
// Mapbox Navigation SDK
implementation 'com.mapbox.navigation:android:2.18.0'
implementation 'com.mapbox.navigation:ui:2.18.0'
implementation 'com.mapbox.navigation:ui-maps:2.18.0'
implementation 'com.mapbox.navigation:ui-voice:2.18.0'
// ... và nhiều module khác
```

### 4. Permissions
Ứng dụng yêu cầu các quyền sau:
- `ACCESS_FINE_LOCATION`: Truy cập vị trí chính xác
- `ACCESS_COARSE_LOCATION`: Truy cập vị trí gần đúng
- `RECORD_AUDIO`: Ghi âm cho voice instructions
- `POST_NOTIFICATIONS`: Gửi thông báo
- `INTERNET`: Kết nối internet

## Cấu trúc dự án

```
app/
├── src/main/
│   ├── java/com/example/mapboxnavigation/
│   │   ├── MainActivity.kt              # Activity chính
│   │   ├── NavigationHelper.kt          # Helper cho navigation
│   │   └── UIComponentsManager.kt       # Quản lý UI components
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml        # Layout chính
│   │   ├── values/
│   │   │   ├── strings.xml              # String resources
│   │   │   ├── colors.xml               # Color resources
│   │   │   ├── themes.xml               # Theme resources
│   │   │   └── mapbox_access_token.xml  # Mapbox token
│   │   └── xml/
│   │       ├── backup_rules.xml         # Backup rules
│   │       └── data_extraction_rules.xml # Data extraction rules
│   └── AndroidManifest.xml              # Manifest file
├── build.gradle                         # App build configuration
└── proguard-rules.pro                   # ProGuard rules
```

## Sử dụng

### 1. Khởi động ứng dụng
- Mở ứng dụng và cấp quyền truy cập vị trí
- Ứng dụng sẽ tự động định vị vị trí hiện tại

### 2. Bắt đầu điều hướng
- Nhấn nút "Bắt đầu điều hướng"
- Ứng dụng sẽ tính toán tuyến đường đến đích mặc định (TP.HCM)
- Camera sẽ tự động theo dõi vị trí

### 3. Điều khiển tính năng
- **Giọng nói**: Bật/tắt hướng dẫn bằng giọng nói
- **Giới hạn tốc độ**: Hiển thị giới hạn tốc độ hiện tại
- **Tiến trình**: Theo dõi khoảng cách và thời gian còn lại

### 4. Dừng điều hướng
- Nhấn nút "Dừng điều hướng" để kết thúc

## Tính năng chi tiết

### Navigation Camera
- Tự động theo dõi vị trí người dùng
- Zoom và pan thông minh
- Animation mượt mà

### Route Visualization
- **Route Line**: Đường kẻ màu xanh hiển thị tuyến đường
- **Route Arrow**: Mũi tên chỉ hướng đi tiếp theo
- **Route Callouts**: Popup thông tin chi tiết

### Voice Instructions
- Hướng dẫn bằng tiếng Việt
- Tự động phát khi có thay đổi hướng
- Có thể bật/tắt theo ý muốn

### Advanced UI Components
- **Speed Limit**: Hiển thị giới hạn tốc độ hiện tại
- **Trip Progress**: Khoảng cách và thời gian còn lại
- **Maneuver Instructions**: Hướng dẫn thao tác chi tiết
- **Building Highlights**: Làm nổi bật tòa nhà quan trọng

## Tùy chỉnh

### Thay đổi đích đến
Trong `MainActivity.kt`, thay đổi tọa độ đích:

```kotlin
// Thay đổi tọa độ đích (kinh độ, vĩ độ)
val destination = Point.fromLngLat(106.6297, 10.8231)
```

### Thay đổi style bản đồ
Trong `setupMap()`, thay đổi style:

```kotlin
mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
    // Các style khác: MAPBOX_SATELLITE, MAPBOX_OUTDOORS, etc.
}
```

### Tùy chỉnh màu sắc
Trong `colors.xml`, thay đổi màu sắc:

```xml
<color name="mapbox_blue">#3B82F6</color>
<color name="mapbox_red">#EF4444</color>
```

## Troubleshooting

### Lỗi thường gặp

1. **"Chưa có vị trí hiện tại"**
   - Kiểm tra quyền truy cập vị trí
   - Đảm bảo GPS được bật
   - Kiểm tra kết nối internet

2. **"Lỗi tính toán tuyến đường"**
   - Kiểm tra Mapbox access token
   - Kiểm tra kết nối internet
   - Kiểm tra tọa độ đích có hợp lệ

3. **Voice instructions không hoạt động**
   - Kiểm tra quyền RECORD_AUDIO
   - Kiểm tra volume thiết bị
   - Kiểm tra cài đặt voice trong ứng dụng

### Debug
Bật log để debug:

```kotlin
Log.d(TAG, "Debug message")
```

## Tài liệu tham khảo

- [Mapbox Navigation SDK](https://docs.mapbox.com/android/navigation/)
- [Mapbox Maps SDK](https://docs.mapbox.com/android/maps/)
- [Android Location Services](https://developers.google.com/location-context/fused-location-provider)

## License

Dự án này sử dụng Mapbox SDK với license riêng. Vui lòng tham khảo [Mapbox Terms of Service](https://www.mapbox.com/legal/tos) để biết thêm chi tiết.