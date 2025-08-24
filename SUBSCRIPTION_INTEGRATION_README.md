# Subscription Integration with Authentication API

## Tổng quan

Module authentication đã được cập nhật để trả về thông tin subscription trong API login, đồng bộ hóa với download-extension.

## Những thay đổi chính

### 1. Tạo `SubscriptionDTO` mới
- **File**: `authentication/src/main/java/com/malitool/authentication/dto/SubscriptionDTO.java`
- **Chức năng**: Chứa thông tin subscription bao gồm:
  - `planName`: Tên gói (Premium, Free Trial, Basic, Disabled)
  - `status`: Trạng thái subscription
  - `startDate`: Ngày bắt đầu
  - `endDate`: Ngày kết thúc
  - `isActive`: Subscription có đang hoạt động không
  - `remainingDays`: Số ngày còn lại

### 2. Cập nhật `LoginResponse`
- **File**: `authentication/src/main/java/com/malitool/authentication/dto/LoginResponse.java`
- **Thay đổi**: Thêm field `subscription` kiểu `SubscriptionDTO`
- **Constructor**: Thêm constructor mới nhận subscription parameter

### 3. Tạo `SubscriptionService`
- **File**: `authentication/src/main/java/com/malitool/authentication/service/SubscriptionService.java`
- **Chức năng**: 
  - Mapping từ `UserStatus` sang `PlanName`
  - Tính toán trạng thái active/inactive
  - Tính số ngày còn lại của subscription

### 4. Cập nhật `AuthController`
- **File**: `authentication/src/main/java/com/malitool/authentication/controller/AuthController.java`
- **Thay đổi**: 
  - Inject `SubscriptionService` và `UserRepository`
  - Trong API login: lấy user info và tạo subscription data
  - Trả về `LoginResponse` với subscription info

### 5. Cập nhật Extension (popup.js)
- **File**: `download-extention/scripts/popup.js`
- **Thay đổi**:
  - Thêm constant `AUTH_API_URL`
  - Cập nhật login success handler để lưu subscription info
  - Thay thế `displayUserProfile()` bằng `displayUserProfileFromAuth()`
  - Xử lý 4 loại plan với CSS classes tương ứng
  - Hiển thị số ngày còn lại

## Mapping UserStatus sang PlanName

| UserStatus | PlanName | Description | Badge Color |
|------------|----------|-------------|-------------|
| PAID | Premium | Tất cả tính năng premium | bg-success |
| NEW | Free Trial | Giai đoạn dùng thử | bg-warning |
| CONFIRMED | Basic | Tính năng cơ bản | bg-primary |
| DISABLE | Disabled | Tài khoản bị vô hiệu hóa | bg-danger |

## Luồng hoạt động mới

### 1. User Login
```
User nhập email/password → AuthController.login() → 
Lấy User từ database → SubscriptionService.getUserSubscription() → 
Tạo SubscriptionDTO → Trả về LoginResponse với subscription info
```

### 2. Extension xử lý
```
Extension nhận response → Lưu vào chrome.storage.local → 
Gọi displayUserProfileFromAuth() → Hiển thị thông tin subscription
```

### 3. Subscription Display
```
Extension đọc subscription từ storage → 
Mapping planName sang CSS classes → 
Hiển thị badge, description, remaining days
```

## API Response Format

### Trước (cũ)
```json
{
  "email": "user@example.com",
  "token": "jwt_token_here"
}
```

### Sau (mới)
```json
{
  "email": "user@example.com",
  "token": "jwt_token_here",
  "subscription": {
    "planName": "Premium",
    "status": "PAID",
    "startDate": "2024-01-01T00:00:00.000Z",
    "endDate": "2024-12-31T23:59:59.999Z",
    "isActive": true,
    "remainingDays": 365
  }
}
```

## Lợi ích

1. **Đồng bộ hóa**: Extension và backend sử dụng cùng một nguồn dữ liệu subscription
2. **Real-time**: Thông tin subscription được cập nhật ngay khi login
3. **Consistent UI**: Badge colors và descriptions nhất quán
4. **Better UX**: User thấy ngay trạng thái subscription sau khi login
5. **Maintainable**: Logic subscription tập trung trong SubscriptionService

## Cách sử dụng

### Backend
```java
@Autowired
private SubscriptionService subscriptionService;

// Trong login method
User user = userRepository.findByEmail(email);
SubscriptionDTO subscription = subscriptionService.getUserSubscription(user);
LoginResponse response = new LoginResponse(email, token, subscription);
```

### Frontend
```javascript
// Lưu subscription info
chrome.storage.local.set({
    userEmail: responseData.email,
    userToken: responseData.token,
    userSubscription: responseData.subscription
});

// Đọc và hiển thị
chrome.storage.local.get(['userSubscription'], (result) => {
    const subscription = result.userSubscription;
    displayUserProfileFromAuth({ subscription });
});
```

## Lưu ý

- Cần đảm bảo User entity có đầy đủ các field: `status`, `expiredDate`, `createdDate`
- Extension cần được reload sau khi cập nhật code
- Có thể cần clear chrome.storage.local để test lại từ đầu

