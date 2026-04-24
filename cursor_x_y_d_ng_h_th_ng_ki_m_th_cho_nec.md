# Xây dựng hệ thống kiểm thử cho necom
_Exported on 4/24/2026 at 08:27:26 GMT+7 from Cursor (3.1.15)_

---

**User**

hey, cách xây dựng hệ thống testing cho necom, SIT
Xây dựng nền tảng kiểm thử toàn diện cho hệ thống
Triển khai kiểm thử đa cấp độ từ logic đến luồng nghiệp vụ
Tự động hóa quy trình và quản lý kết quả trực quan
Phát hiện lỗi sớm, nâng cao chất lượng phần mềm
Hỗ trợ quy trình tích hợp và triển khai liên tục



Lên Plan gợi ý xây dựng

---

**Cursor**

Tôi đã có đủ thông tin về dự án. Đây là kế hoạch xây dựng hệ thống testing toàn diện cho **necom**.

---

## Phân tích hiện trạng

**Tech Stack đã xác định:**
- Backend: Spring Boot 2.6.7 / Java 11 / MySQL 8.0
- Frontend: React 17 + TypeScript + React Query
- Realtime: WebSocket (STOMP)
- Container: Docker Compose
- Existing tests: Cực kỳ ít — chỉ 4 file test cơ bản (`contextLoads`, injection checks)

**Domain modules cần cover:**
`auth` · `product` · `order` · `promotion` · `review` · `chat` · `cashbook` · `waybill` · `statistic` · `cart` · `inventory`

---

## Kế hoạch xây dựng Testing Platform

### Tầng 1 — Unit Tests (Backend Services)

**Framework:** JUnit 5 + Mockito + AssertJ (đã có qua `spring-boot-starter-test`)

Mục tiêu: Test isolated business logic ở Service layer, không cần DB.

```
src/test/java/com/necom/
├── service/
│   ├── order/
│   │   └── OrderServiceTest.java        # đặt hàng, huỷ đơn, tính phí
│   ├── promotion/
│   │   └── PromotionServiceTest.java    # áp mã giảm giá, điều kiện KM
│   ├── inventory/
│   │   └── InventoryServiceTest.java    # trừ tồn kho khi đặt hàng
│   └── auth/
│       └── VerificationServiceTest.java # OTP, token validation
└── utils/
    └── PriceCalculatorTest.java
```

Pattern: Mock tất cả dependencies (Repository, external services)

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orderRepository;
    @Mock InventoryRepository inventoryRepository;
    @InjectMocks OrderServiceImpl orderService;

    @Test
    void shouldDecreaseInventoryWhenOrderPlaced() { ... }
    @Test
    void shouldThrowWhenInsufficientStock() { ... }
    @Test
    void shouldCalculateDiscountCorrectly() { ... }
}
```

---

### Tầng 2 — Integration Tests (Repository + API)

**Framework:** `@DataJpaTest` + `@WebMvcTest` + `Testcontainers` (MySQL)

Thêm dependency vào `pom.xml`:

```xml
<!-- Testcontainers - MySQL thật, không mock -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

**Repository Tests** — kiểm tra queries JPA custom:

```
repository/
├── ProductRepositoryIT.java    # findDocketedProducts, search, filter
├── OrderRepositoryIT.java      # order by status, by customer
└── PromotionRepositoryIT.java  # validate coupon queries
```

**Controller/API Tests** — `@SpringBootTest` + `MockMvc` hoặc `WebTestClient`:

```
controller/
├── AuthControllerIT.java       # đăng ký, đăng nhập, refresh token
├── OrderControllerIT.java      # CRUD đơn hàng qua REST
└── ProductControllerIT.java    # search, filter, pagination
```

---

### Tầng 3 — SIT (System Integration Testing)

Đây là trọng tâm — test **business flows đầu cuối** chạy trên environment đầy đủ.

**Approach:** `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Testcontainers + RestAssured

Thêm dependency:

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>spring-mock-mvc</artifactId>
    <scope>test</scope>
</dependency>
```

**Business Flow SIT Tests:**

| Test Suite | Flow được kiểm thử |
|---|---|
| `CustomerPurchaseFlowSIT` | Đăng ký → Duyệt SP → Thêm giỏ → Đặt hàng → Thanh toán → Cập nhật tồn kho |
| `PromotionFlowSIT` | Tạo mã KM → Áp dụng khi checkout → Verify discount |
| `OrderLifecycleSIT` | Đặt → Xác nhận → Vận chuyển → Huỷ → Hoàn tiền |
| `AuthFlowSIT` | Đăng ký → Verify email OTP → Đăng nhập → JWT refresh |
| `InventoryFlowSIT` | Đặt hàng → Trừ tồn kho → Đơn huỷ → Hoàn tồn kho |
| `ReviewFlowSIT` | Mua hàng thành công → Viết review → Verify hiển thị |
| `ChatFlowSIT` | Khởi tạo room → Gửi tin nhắn → WebSocket event |

**Ví dụ:**

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class CustomerPurchaseFlowSIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Test
    @Order(1) void registerCustomer() { ... }
    @Test
    @Order(2) void addProductToCart() { ... }
    @Test
    @Order(3) void checkoutWithPromotion() { ... }
    @Test
    @Order(4) void verifyInventoryDecreased() { ... }
    @Test
    @Order(5) void cancelOrderAndVerifyRestock() { ... }
}
```

---

### Tầng 4 — Frontend Tests (React)

**Framework:** Jest (đã có) + React Testing Library + Cypress (E2E)

```bash
npm install --save-dev @testing-library/react @testing-library/user-event cypress
```

**Cấu trúc:**

```
src/
├── __tests__/
│   ├── components/
│   │   ├── ProductCard.test.tsx      # render, price display
│   │   ├── CartSummary.test.tsx      # tính tiền, discount
│   │   └── CheckoutForm.test.tsx     # form validation
│   └── hooks/
│       └── useCart.test.tsx          # cart state management
└── cypress/
    └── e2e/
        ├── purchase-flow.cy.ts       # E2E: tìm SP → đặt hàng
        └── auth-flow.cy.ts           # E2E: đăng ký → đăng nhập
```

---

### Tầng 5 — CI/CD Pipeline & Báo cáo

**GitHub Actions Workflow:**

```yaml
# .github/workflows/testing.yml
name: Necom Testing Pipeline

on: [push, pull_request]

jobs:
  backend-unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run Unit Tests
        run: mvn test -Dtest="*Test" -DfailIfNoTests=false
      - name: Upload Coverage
        uses: codecov/codecov-action@v3

  backend-integration-tests:
    runs-on: ubuntu-latest
    steps:
      - name: Run Integration + SIT
        run: mvn test -Dtest="*IT,*SIT"

  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - name: Jest Tests
        run: npm test -- --coverage --watchAll=false
      - name: Cypress E2E
        run: npx cypress run

  test-report:
    needs: [backend-unit-tests, backend-integration-tests]
    steps:
      - name: Publish Test Report
        uses: dorny/test-reporter@v1
```

**Test Coverage & Reporting:**

```xml
<!-- pom.xml - JaCoCo coverage report -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <limits>
                    <limit>
                        <minimum>0.70</minimum> <!-- 70% coverage minimum -->
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

---

## Roadmap triển khai

```
Phase 1 (Tuần 1-2): Foundation
├── Setup Testcontainers + JaCoCo
├── Viết Unit Tests cho OrderService, PromotionService, InventoryService
└── Fix existing test failures (ElectroApplicationTests)

Phase 2 (Tuần 3-4): Integration Tests
├── Repository IT với MySQL container thật
├── API IT cho auth + product + order endpoints
└── Target: 60% code coverage

Phase 3 (Tuần 5-6): SIT Business Flows
├── CustomerPurchaseFlowSIT (flow quan trọng nhất)
├── OrderLifecycleSIT
├── PromotionFlowSIT
└── Test data management strategy

Phase 4 (Tuần 7-8): Frontend + CI/CD
├── React component tests
├── Cypress E2E cho critical paths
├── GitHub Actions pipeline
└── Dashboard báo cáo (Allure Reports)
```

---

## Các rủi ro & giải pháp

| Rủi ro | Giải pháp |
|---|---|
| DB state bị pollute giữa các test | `@Transactional` rollback + `@DirtiesContext` |
| JWT token expire trong SIT | Mock security context hoặc dùng test token factory |
| WebSocket khó test | Dùng `StompClient` + latch timeout assertion |
| Tốc độ Testcontainers chậm | Sử dụng `@Container static` (singleton container) |
| Test data phụ thuộc nhau | Builder pattern + dedicated `TestDataFactory` class |

---

Bạn muốn tôi **bắt đầu implement** từ phase nào? Tôi recommend bắt đầu từ **Phase 1** — setup Testcontainers, JaCoCo và viết Unit Tests cho `OrderService` và `PromotionService` vì đây là business logic cốt lõi nhất của hệ thống e-commerce này.

---

**User**

cho hỏi triển khai từng phase ở đâu (chỉ trả lời gợi ý ngắn)

---

**Cursor**

**Phase 1 (Unit + nền tảng)**  
- Code: `necom-server/src/test/java/...` (JUnit/Mockito), cấu hình JaCoCo trong `necom-server/pom.xml`.  
- Chạy: máy dev + CI job `mvn test` (chỉ unit, không cần Docker DB nếu mock đủ).

**Phase 2 (Integration / API)**  
- Code: cùng `src/test/java`, suffix `*IT` hoặc package `...integration`.  
- Chạy: máy dev + CI (Docker runner + Testcontainers MySQL), hoặc pipeline có service MySQL.

**Phase 3 (SIT / luồng nghiệp vụ)**  
- Code: `necom-server/src/test/java/.../sit/` hoặc `.../e2e/` (RestAssured + Testcontainers / hoặc profile `sit`).  
- Chạy: CI nightly hoặc trước merge vào nhánh `release`/`sit`; có thể dùng stack `docker-compose` (SIT) làm target.

**Phase 4 (Frontend + E2E)**  
- Code: `necom-client/src/__tests__/` (Jest/RTL), `necom-client/cypress/e2e/` (Cypress).  
- Chạy: CI job riêng (build client + chạy Cypress chống backend đã lên container hoặc mock API).

**Báo cáo / tích hợp**  
- Cấu hình: `.github/workflows/` (hoặc GitLab CI/Jenkins tương đương), upload artifact báo cáo (JaCoCo, JUnit, Cypress).

---

**User**

không xây ở /Volumes/NVME_NHAT/Documents/necom-testing/necom-testing à ?

---

**Cursor**

Có thể — và thư mục `necom-testing` ở root hiện đang trống, rất hợp để làm **kho kiểm thử “black-box” / đa dịch vụ** (E2E API, Postman/Newman, k6, Cypress chạy theo URL SIT, script docker-compose test).

Gợi ý ngắn:

- **Unit + integration (JUnit, Testcontainers, MockMvc)** → vẫn nên nằm trong `necom/necom-server/src/test/...` (và tương tự client trong `necom-client`) để build/coverage/refactor đi cùng code.
- **SIT/E2E “hệ thống”, không cần import Java nội bộ** → đặt ở `.../necom-testing/necom-testing` (hoặc đổi tên rõ hơn như `tests/e2e`) là hợp lý: một repo/folder chỉ gọi HTTP/UI vào môi trường đã deploy.

Tóm lại: **hai lớp** — test sát code trong `necom/`, test luồng nghiệp vụ / môi trường trong `necom-testing/`.

---

**User**

vậy white box nên là nội bộ như phase 1 và 2? còn black box là necom-testing là phase 3 và 4?

---

**Cursor**

Gần đúng, chỉ cần tinh chỉnh một chút cho khớp thực tế:

**White-box (nhìn vào code / module)**  
- **Phase 1** (unit, mock dependency): rõ ràng white-box — trong `necom-server` / `necom-client`.  
- **Phase 2** (repo + API integration trong cùng repo): vẫn **chủ yếu white-box** (đôi khi gọi là *gray-box* vì test qua HTTP nhưng vẫn biết endpoint, schema nội bộ).

**Black-box (chỉ hành vi hệ thống / hợp đồng bên ngoài)**  
- **Phase 3** *nếu* là SIT chạy **chỉ** qua URL đã deploy, dữ liệu/fixture độc lập, không import class server → **black-box** — hợp đặt ở `necom-testing`.  
- **Phase 3** *nếu* vẫn là `@SpringBootTest` + Testcontainers **trong** `necom-server` → vẫn **white/gray-box** (test nội bộ cùng build Maven).  
- **Phase 4**: **Jest + RTL** (component/hook) = **white-box** trong `necom-client`; **Cypress E2E** qua trình duyệt = **black-box** — có thể để trong client hoặc gom vào `necom-testing` nếu muốn một chỗ chạy “full stack”.

**Tóm một dòng:** Phase 1–2 ≈ white-box nội bộ `necom/`; Phase 3–4 **chỉ là black-box khi** test theo môi trường/HTTP/UI từ bên ngoài — phần đó gọn nhất là `necom-testing/`; phần còn lại (unit, IT, RTL) vẫn nội bộ từng module.

