# Kế hoạch Triển khai - SeeSound

## Mục tiêu
Xây dựng ứng dụng Android native để đo mức độ tiếng ồn (dB) và lưu lịch sử vào cơ sở dữ liệu cục bộ.
**Mục tiêu**: Android 10+
**Thiết kế**: Material Design 3 (XML Styles)

## Yêu cầu Người dùng Xem xét
-   **Quyền Ghi âm**: Ứng dụng yêu cầu quyền `RECORD_AUDIO` để đo decibel. Chúng ta cần đảm bảo xử lý quyền runtime chính xác.
-   **Tính toán dB**: Chúng ta sẽ sử dụng `maxAmplitude` của `MediaRecorder` để ước tính dB đơn giản hoặc `AudioRecord` để có độ chính xác cao hơn. Đối với MVP này, `MediaRecorder` đơn giản hơn, nhưng `AudioRecord` cho phép phân tích dữ liệu thô theo thời gian thực. Tôi sẽ giả định dùng `MediaRecorder` cho đơn giản trừ khi yêu cầu độ chính xác cao.

## Công nghệ Sử dụng (Tech Stack)
-   **Ngôn ngữ**: Kotlin
-   **Giao diện (UI)**: XML Layouts với ViewBinding (Tuyệt đối KHÔNG dùng findViewById, KHÔNG DataBinding, KHÔNG Compose)
-   **Kiến trúc**: MVVM (Model-View-ViewModel)
-   **Điều hướng**: Jetpack Navigation Component (Navigation Graph XML)
-   **Lưu trữ cục bộ**: Room Database
-   **Dependency Injection**: Hilt
-   **Xử lý bất đồng bộ**: Coroutines & Flow

## Cấu trúc Thư mục
```text
com.example.seesound
├── data
│   ├── local
│   │   ├── dao          # Room DAOs (NoiseDao)
│   │   ├── database     # Room Database Class (AppDatabase)
│   │   └── entity       # Room Entities (NoiseRecord)
│   ├── model            # Domain models (nếu khác với entities)
│   └── repository       # Triển khai Repository (NoiseRepository)
├── di                   # Hilt Modules (DatabaseModule, AppModule)
├── ui
│   ├── main             # MainActivity
│   ├── home             # HomeFragment, HomeViewModel
│   └── history          # HistoryFragment, HistoryViewModel
└── utils                # Các lớp tiện ích (Constants, PermissionHelpers)
```

## Các Màn hình Chính
1.  **MainActivity**: Activity chứa (Host activity) bao gồm `FragmentContainerView` và `NavHostFragment`.
2.  **HomeFragment**:
    -   **Giao diện**:
        -   `TextView` hoặc Custom View để hiển thị mức dB hiện tại.
        -   `MaterialButton` để Bắt đầu/Dừng theo dõi.
        -   `MaterialButton` để Lưu phiên hiện tại (hoặc tự động lưu).
    -   **Logic**: Xử lý quyền microphone, bắt đầu service/logic ghi âm, quan sát cập nhật dB từ ViewModel.
3.  **HistoryFragment**:
    -   **Giao diện**: `RecyclerView` hiển thị danh sách các bản ghi tiếng ồn đã lưu.
    -   **Logic**: Quan sát danh sách bản ghi từ ViewModel.

## Hướng dẫn Thực hiện Từng bước

### Bước 1: Thiết lập Dự án
-   **Cấu hình Gradle**:
    -   Thêm các thư viện (dependencies):
        -   Hilt (`hilt-android`, `hilt-compiler`)
        -   Navigation (`navigation-fragment-ktx`, `navigation-ui-ktx`)
        -   Room (`room-runtime`, `room-ktx`, `room-compiler`)
        -   Coroutines (`kotlinx-coroutines-android`)
        -   Lifecycle (`lifecycle-viewmodel-ktx`, `lifecycle-livedata-ktx`)
    -   Bật ViewBinding: `buildFeatures { viewBinding = true }`
    -   Áp dụng Plugins: `kotlin-kapt` (hoặc `ksp`), `dagger.hilt.android.plugin`, `androidx.navigation.safeargs.kotlin`.
-   **Thiết lập Ứng dụng**:
    -   Tạo lớp `SeeSoundApp` kế thừa `Application` và chú thích với `@HiltAndroidApp`.
    -   Cập nhật `AndroidManifest.xml` để sử dụng `SeeSoundApp` và thêm quyền `RECORD_AUDIO`.
-   **Theme**:
    -   Cập nhật `res/values/themes.xml` để sử dụng parent là `Theme.Material3`.

### Bước 2: Thiết lập Cơ sở dữ liệu (Database)
-   **Entity**: Tạo data class `NoiseRecord` chú thích với `@Entity`.
    -   Các trường: `id` (PrimaryKey, tự động tạo), `timestamp` (Long), `dbValue` (Double).
-   **DAO**: Tạo interface `NoiseDao` chú thích với `@Dao`.
    -   `@Insert suspend fun insert(record: NoiseRecord)`
    -   `@Query("SELECT * FROM noise_records ORDER BY timestamp DESC") fun getAll(): Flow<List<NoiseRecord>>`
-   **Database**: Tạo abstract class `AppDatabase` kế thừa `RoomDatabase`.
-   **DI**: Tạo `DatabaseModule` để cung cấp `AppDatabase` và `NoiseDao` qua Hilt (`@Provides`, `@Singleton`).

### Bước 3: Thiết lập Repository & ViewModel
-   **Repository**: Tạo class `NoiseRepository`.
    -   Inject `NoiseDao`.
    -   Các hàm: `insertRecord`, `getAllRecords`.
-   **HomeViewModel**:
    -   Inject `NoiseRepository`.
    -   Quản lý logic `MediaRecorder` (hoặc ủy quyền cho use case/manager).
    -   Công khai `_dbLevel` dưới dạng `StateFlow` hoặc `LiveData`.
    -   Hàm `saveRecord(db: Double)` gọi xuống repository.
-   **HistoryViewModel**:
    -   Inject `NoiseRepository`.
    -   Công khai `val history: Flow<List<NoiseRecord>> = repository.getAllRecords()`.

### Bước 4: Triển khai Giao diện (UI)
-   **Navigation**:
    -   Tạo `res/navigation/nav_graph.xml`.
    -   Thêm `HomeFragment` làm điểm bắt đầu (start destination).
    -   Thêm `HistoryFragment`.
    -   Thêm action từ Home sang History.
-   **MainActivity**:
    -   Thiết lập `ActivityMainBinding`.
    -   Thiết lập `NavController` với `NavHostFragment`.
-   **HomeFragment**:
    -   Sử dụng `FragmentHomeBinding`.
    -   Triển khai logic yêu cầu quyền.
    -   Gắn kết trạng thái ViewModel với UI (cập nhật text dB).
    -   Điều hướng sang History khi nhấn nút.
-   **HistoryFragment**:
    -   Sử dụng `FragmentHistoryBinding`.
    -   Tạo `HistoryAdapter` (ListAdapter) cho RecyclerView.
    -   Quan sát `history` flow và submit list cho adapter.

## Best Practices (Thực hành tốt nhất)
-   **UI**: Sử dụng `ConstraintLayout` cho tất cả các màn hình phức tạp để duy trì cấu trúc phẳng.
-   **Navigation**: Sử dụng **SafeArgs** để điều hướng giữa các fragment nhằm đảm bảo an toàn kiểu dữ liệu.
-   **ViewBinding**: Tuyệt đối sử dụng `binding.viewId`, không bao giờ dùng `findViewById`.
-   **Coroutines**: Sử dụng `viewModelScope` cho các thao tác trong ViewModel. Thu thập Flows trong Fragments sử dụng `viewLifecycleOwner.lifecycleScope` và `repeatOnLifecycle`.
-   **Dependency Injection**: Sử dụng Constructor Injection cho tất cả các lớp nếu có thể.
