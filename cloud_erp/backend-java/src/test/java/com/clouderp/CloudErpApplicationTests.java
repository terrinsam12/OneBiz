package com.clouderp;

import com.clouderp.config.SecurityUtils;
import com.clouderp.config.UserPrincipal;
import com.clouderp.dto.*;
import com.clouderp.exception.BadRequestException;
import com.clouderp.exception.ResourceNotFoundException;
import com.clouderp.model.*;
import com.clouderp.repository.*;
import com.clouderp.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CloudErpApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private ForecastService forecastService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportExportService reportExportService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private void authenticateAs(Long userId, Long companyId, String email, String role) {
        UserPrincipal principal = new UserPrincipal(userId, companyId, "Test Company", email, "", role);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void contextLoads() {
        assertNotNull(authService);
        assertNotNull(companyService);
        assertNotNull(productService);
        assertNotNull(inventoryService);
        assertNotNull(customerService);
        assertNotNull(saleService);
        assertNotNull(analyticsService);
        assertNotNull(forecastService);
    }

    @Test
    void testCompanyRegistrationAndLoginAndMultiTenantIsolation() {
        long ts = System.currentTimeMillis();
        String emailA = "alice_" + ts + "@alphacorp.com";
        String emailB = "bob_" + ts + "@betasolutions.com";

        // 1. Register Company A
        AuthDto.RegisterRequest regA = new AuthDto.RegisterRequest();
        regA.setCompanyName("Alpha Corp " + ts);
        regA.setOwnerName("Alice Alpha");
        regA.setEmail(emailA);
        regA.setPhone("1112223334");
        regA.setPassword("AlphaPass@123");
        regA.setBusinessType("Manufacturing");

        AuthDto.AuthResponse respA = authService.register(regA);
        assertNotNull(respA.getToken());
        assertNotNull(respA.getCompany().getId());
        assertEquals("COMPANY_ADMIN", respA.getUser().getRole());
        assertEquals("Alpha Corp " + ts, respA.getCompany().getName());

        // 2. Register Company B
        AuthDto.RegisterRequest regB = new AuthDto.RegisterRequest();
        regB.setCompanyName("Beta Solutions " + ts);
        regB.setOwnerName("Bob Beta");
        regB.setEmail(emailB);
        regB.setPhone("5556667778");
        regB.setPassword("BetaPass@123");
        regB.setBusinessType("Technology");

        AuthDto.AuthResponse respB = authService.register(regB);
        assertNotNull(respB.getToken());
        assertNotNull(respB.getCompany().getId());
        assertNotEquals(respA.getCompany().getId(), respB.getCompany().getId(), "Company IDs must be unique");

        // 3. Test Login
        AuthDto.LoginRequest loginA = new AuthDto.LoginRequest();
        loginA.setEmail(emailA);
        loginA.setPassword("AlphaPass@123");
        AuthDto.AuthResponse loginRespA = authService.login(loginA);
        assertNotNull(loginRespA.getToken());
        assertEquals(respA.getCompany().getId(), loginRespA.getCompany().getId());

        // Invalid password test
        AuthDto.LoginRequest invalidLogin = new AuthDto.LoginRequest();
        invalidLogin.setEmail(emailA);
        invalidLogin.setPassword("WrongPassword");
        assertThrows(BadRequestException.class, () -> authService.login(invalidLogin));

        // 4. Test Multi-Tenant Product Isolation
        // As Company A: create a product
        authenticateAs(respA.getUser().getId(), respA.getCompany().getId(), emailA, "COMPANY_ADMIN");
        ProductDto.Create prodA = new ProductDto.Create();
        prodA.setCompanyId(respA.getCompany().getId());
        prodA.setName("Alpha Product 1");
        prodA.setPrice(500.0);
        prodA.setQuantity(40);
        prodA.setCostPrice(300.0);
        prodA.setReorderLevel(5);
        ProductDto.Response createdA = productService.createProduct(prodA);
        assertEquals(respA.getCompany().getId(), createdA.getCompanyId());

        // As Company B: create a product
        authenticateAs(respB.getUser().getId(), respB.getCompany().getId(), emailB, "COMPANY_ADMIN");
        ProductDto.Create prodB = new ProductDto.Create();
        prodB.setCompanyId(respB.getCompany().getId());
        prodB.setName("Beta Gadget 1");
        prodB.setPrice(1500.0);
        prodB.setQuantity(20);
        prodB.setCostPrice(1000.0);
        prodB.setReorderLevel(3);
        ProductDto.Response createdB = productService.createProduct(prodB);
        assertEquals(respB.getCompany().getId(), createdB.getCompanyId());

        // 5. Verify Company A CANNOT see Company B's product
        authenticateAs(respA.getUser().getId(), respA.getCompany().getId(), emailA, "COMPANY_ADMIN");
        List<ProductDto.Response> productsA = productService.getProductsByCompany(respA.getCompany().getId());
        assertTrue(productsA.stream().anyMatch(p -> p.getName().equals("Alpha Product 1")));
        assertFalse(productsA.stream().anyMatch(p -> p.getName().equals("Beta Gadget 1")),
                "Company A must not see Company B's products");

        // 6. Verify Company B CANNOT see Company A's product
        authenticateAs(respB.getUser().getId(), respB.getCompany().getId(), emailB, "COMPANY_ADMIN");
        List<ProductDto.Response> productsB = productService.getProductsByCompany(respB.getCompany().getId());
        assertTrue(productsB.stream().anyMatch(p -> p.getName().equals("Beta Gadget 1")));
        assertFalse(productsB.stream().anyMatch(p -> p.getName().equals("Alpha Product 1")),
                "Company B must not see Company A's products");
    }

    @Test
    void testEmployeeManagementAndRolePermissions() {
        long ts = System.currentTimeMillis();
        // 1. Setup Company Alpha with Admin
        AuthDto.RegisterRequest reg = new AuthDto.RegisterRequest();
        reg.setCompanyName("Gamma Tech " + ts);
        reg.setOwnerName("Grace Gamma");
        reg.setEmail("grace_" + ts + "@gammatech.com");
        reg.setPhone("9988776655");
        reg.setPassword("GammaPass@123");
        reg.setBusinessType("Services");

        AuthDto.AuthResponse adminAuth = authService.register(reg);
        Long companyId = adminAuth.getCompany().getId();
        Long adminId = adminAuth.getUser().getId();

        // 2. Authenticate as Admin & Add Employees
        authenticateAs(adminId, companyId, "grace_" + ts + "@gammatech.com", "COMPANY_ADMIN");

        // Add MANAGER
        EmployeeDto.CreateRequest managerReq = new EmployeeDto.CreateRequest();
        managerReq.setName("Mark Manager");
        managerReq.setEmail("mark_" + ts + "@gammatech.com");
        managerReq.setPhone("9988771122");
        managerReq.setPassword("ManagerPass@123");
        managerReq.setRole("MANAGER");

        EmployeeDto.Response managerResp = employeeService.createEmployee(managerReq);
        assertNotNull(managerResp.getId());
        assertEquals("MANAGER", managerResp.getRole());
        assertEquals(companyId, managerResp.getCompanyId());
        assertTrue(managerResp.getIsActive());

        // Add STAFF
        EmployeeDto.CreateRequest staffReq = new EmployeeDto.CreateRequest();
        staffReq.setName("Sam Staff");
        staffReq.setEmail("sam_" + ts + "@gammatech.com");
        staffReq.setPhone("9988773344");
        staffReq.setPassword("StaffPass@123");
        staffReq.setRole("STAFF");

        EmployeeDto.Response staffResp = employeeService.createEmployee(staffReq);
        assertNotNull(staffResp.getId());
        assertEquals("STAFF", staffResp.getRole());
        assertEquals(companyId, staffResp.getCompanyId());

        // 3. Admin can list and filter employees
        List<EmployeeDto.Response> allEmployees = employeeService.getEmployees(null, null, null);
        assertTrue(allEmployees.size() >= 3); // Grace (admin), Mark (manager), Sam (staff)

        List<EmployeeDto.Response> managersOnly = employeeService.getEmployees(null, "MANAGER", null);
        assertTrue(managersOnly.stream().allMatch(e -> "MANAGER".equals(e.getRole())));

        List<EmployeeDto.Response> searchByName = employeeService.getEmployees("Mark", null, null);
        assertEquals(1, searchByName.size());
        assertEquals("Mark Manager", searchByName.get(0).getName());

        // 4. Test Role Restrictions: STAFF cannot access Employee Management APIs
        authenticateAs(staffResp.getId(), companyId, "sam_" + ts + "@gammatech.com", "STAFF");
        assertThrows(com.clouderp.exception.ForbiddenException.class, () ->
                employeeService.getEmployees(null, null, null)
        );
        EmployeeDto.CreateRequest unauthorizedCreate = new EmployeeDto.CreateRequest();
        unauthorizedCreate.setName("Hacker");
        unauthorizedCreate.setEmail("hacker_" + ts + "@gammatech.com");
        unauthorizedCreate.setPassword("Password@123");
        unauthorizedCreate.setRole("COMPANY_ADMIN");
        assertThrows(com.clouderp.exception.ForbiddenException.class, () ->
                employeeService.createEmployee(unauthorizedCreate)
        );

        // 5. Test Admin self-modification restrictions
        authenticateAs(adminId, companyId, "grace_" + ts + "@gammatech.com", "COMPANY_ADMIN");

        // Admin cannot modify own role
        EmployeeDto.UpdateRequest selfRoleUpdate = new EmployeeDto.UpdateRequest();
        selfRoleUpdate.setName("Grace Gamma");
        selfRoleUpdate.setRole("STAFF");
        assertThrows(BadRequestException.class, () ->
                employeeService.updateEmployee(adminId, selfRoleUpdate)
        );

        // Admin cannot deactivate own account
        assertThrows(BadRequestException.class, () ->
                employeeService.updateEmployeeStatus(adminId, false)
        );

        // 6. Admin can edit staff member
        EmployeeDto.UpdateRequest updateStaff = new EmployeeDto.UpdateRequest();
        updateStaff.setName("Samuel Staff");
        updateStaff.setPhone("9988779999");
        updateStaff.setRole("STAFF");
        EmployeeDto.Response updatedStaff = employeeService.updateEmployee(staffResp.getId(), updateStaff);
        assertEquals("Samuel Staff", updatedStaff.getName());
        assertEquals("9988779999", updatedStaff.getPhone());

        // 7. Deactivate and reactivate employee
        employeeService.updateEmployeeStatus(staffResp.getId(), false);
        EmployeeDto.Response deactivatedStaff = employeeService.getEmployeeById(staffResp.getId());
        assertFalse(deactivatedStaff.getIsActive());

        // Deactivated user cannot log in
        AuthDto.LoginRequest deactLogin = new AuthDto.LoginRequest();
        deactLogin.setEmail("sam_" + ts + "@gammatech.com");
        deactLogin.setPassword("StaffPass@123");
        BadRequestException loginEx = assertThrows(BadRequestException.class, () -> authService.login(deactLogin));
        assertTrue(loginEx.getMessage().contains("deactivated"));

        // Reactivate user
        employeeService.updateEmployeeStatus(staffResp.getId(), true);
        AuthDto.AuthResponse staffLogin = authService.login(deactLogin);
        assertNotNull(staffLogin.getToken());
        assertEquals("STAFF", staffLogin.getUser().getRole());
    }

    @Test
    void testSupplierAndPurchaseManagementWithStockAndIsolation() {
        long ts = System.currentTimeMillis();
        // 1. Setup Company Alpha
        AuthDto.RegisterRequest regA = new AuthDto.RegisterRequest();
        regA.setCompanyName("Titan Corp " + ts);
        regA.setOwnerName("Tara Titan");
        regA.setEmail("tara_" + ts + "@titancorp.com");
        regA.setPassword("TitanPass@123");
        regA.setBusinessType("Manufacturing");

        AuthDto.AuthResponse authA = authService.register(regA);
        Long companyAId = authA.getCompany().getId();
        Long adminAId = authA.getUser().getId();

        authenticateAs(adminAId, companyAId, "tara_" + ts + "@titancorp.com", "COMPANY_ADMIN");

        // 2. Create Product P1 in Company A
        ProductDto.Create prodReq = new ProductDto.Create();
        prodReq.setCompanyId(companyAId);
        prodReq.setName("Turbine Blade " + ts);
        prodReq.setPrice(1200.0);
        prodReq.setCostPrice(700.0);
        prodReq.setQuantity(10);
        prodReq.setReorderLevel(5);
        ProductDto.Response productP1 = productService.createProduct(prodReq);
        assertEquals(10, productP1.getQuantity());

        // 3. Create Supplier S1 in Company A
        SupplierDto.Create supReq = new SupplierDto.Create();
        supReq.setName("Precision Alloys Ltd");
        supReq.setContactPerson("Vikram Rao");
        supReq.setPhone("9988112233");
        supReq.setEmail("sales@precisionalloys.com");
        supReq.setAddress("Industrial Zone Phase 1");
        SupplierDto.Response supS1 = supplierService.createSupplier(supReq);
        assertNotNull(supS1.getId());
        assertEquals("Precision Alloys Ltd", supS1.getName());
        assertEquals(companyAId, supS1.getCompanyId());

        // 4. Create Purchase PO1 with status PENDING (15 units @ 650)
        PurchaseDto.Create poReq = new PurchaseDto.Create();
        poReq.setSupplierId(supS1.getId());
        poReq.setStatus("PENDING");
        poReq.setNotes("First batch trial order");

        PurchaseDto.ItemRequest item1 = new PurchaseDto.ItemRequest();
        item1.setProductId(productP1.getId());
        item1.setQuantity(15);
        item1.setUnitPrice(650.0);
        poReq.getItems().add(item1);

        PurchaseDto.Response po1 = purchaseService.createPurchase(poReq);
        assertNotNull(po1.getId());
        assertEquals("PENDING", po1.getStatus());
        assertEquals(9750.0, po1.getTotalAmount()); // 15 * 650 = 9750
        assertNotNull(po1.getPurchaseNumber());

        // Verify stock has NOT increased yet
        List<ProductDto.Response> prodsAfterPending = productService.getProductsByCompany(companyAId);
        ProductDto.Response p1Pending = prodsAfterPending.stream()
                .filter(p -> p.getId().equals(productP1.getId())).findFirst().orElseThrow();
        assertEquals(10, p1Pending.getQuantity(), "Stock must not increase while PENDING");

        // 5. Update PO1 status to RECEIVED
        PurchaseDto.Response po1Received = purchaseService.updatePurchaseStatus(po1.getId(), "RECEIVED");
        assertEquals("RECEIVED", po1Received.getStatus());
        assertNotNull(po1Received.getReceivedAt());

        // Verify stock HAS increased: 10 + 15 = 25
        List<ProductDto.Response> prodsAfterReceived = productService.getProductsByCompany(companyAId);
        ProductDto.Response p1Received = prodsAfterReceived.stream()
                .filter(p -> p.getId().equals(productP1.getId())).findFirst().orElseThrow();
        assertEquals(25, p1Received.getQuantity(), "Stock must increase by 15 when RECEIVED");
        assertEquals(650.0, p1Received.getCostPrice(), "Cost price should update to purchase unit price");

        // 6. Prevent duplicate stock update on redundant status update
        purchaseService.updatePurchaseStatus(po1.getId(), "RECEIVED");
        List<ProductDto.Response> prodsAfterRedundant = productService.getProductsByCompany(companyAId);
        ProductDto.Response p1Redundant = prodsAfterRedundant.stream()
                .filter(p -> p.getId().equals(productP1.getId())).findFirst().orElseThrow();
        assertEquals(25, p1Redundant.getQuantity(), "Redundant RECEIVED call must NOT add duplicate stock");

        // 7. Cancel PO1 -> stock should revert to 10
        purchaseService.updatePurchaseStatus(po1.getId(), "CANCELLED");
        List<ProductDto.Response> prodsAfterCancel = productService.getProductsByCompany(companyAId);
        ProductDto.Response p1Cancelled = prodsAfterCancel.stream()
                .filter(p -> p.getId().equals(productP1.getId())).findFirst().orElseThrow();
        assertEquals(10, p1Cancelled.getQuantity(), "Stock must revert when order is CANCELLED");

        // 8. Create Purchase PO2 directly as RECEIVED (20 units @ 600)
        PurchaseDto.Create po2Req = new PurchaseDto.Create();
        po2Req.setSupplierId(supS1.getId());
        po2Req.setStatus("RECEIVED");
        po2Req.setNotes("Direct delivery order");

        PurchaseDto.ItemRequest item2 = new PurchaseDto.ItemRequest();
        item2.setProductId(productP1.getId());
        item2.setQuantity(20);
        item2.setUnitPrice(600.0);
        po2Req.getItems().add(item2);

        PurchaseDto.Response po2 = purchaseService.createPurchase(po2Req);
        assertEquals("RECEIVED", po2.getStatus());
        assertEquals(12000.0, po2.getTotalAmount()); // 20 * 600 = 12000

        // Stock increases directly: 10 + 20 = 30
        List<ProductDto.Response> prodsAfterPo2 = productService.getProductsByCompany(companyAId);
        ProductDto.Response p1Po2 = prodsAfterPo2.stream()
                .filter(p -> p.getId().equals(productP1.getId())).findFirst().orElseThrow();
        assertEquals(30, p1Po2.getQuantity(), "Stock must increase to 30 when created directly as RECEIVED");

        // 9. Role Permissions Testing with STAFF
        authenticateAs(9999L, companyAId, "staff_" + ts + "@titancorp.com", "STAFF");

        // STAFF cannot create supplier
        SupplierDto.Create unauthorizedSup = new SupplierDto.Create();
        unauthorizedSup.setName("Illegal Supplier");
        assertThrows(com.clouderp.exception.ForbiddenException.class, () ->
                supplierService.createSupplier(unauthorizedSup)
        );

        // STAFF cannot create purchase
        assertThrows(com.clouderp.exception.ForbiddenException.class, () ->
                purchaseService.createPurchase(po2Req)
        );

        // STAFF cannot update purchase status
        assertThrows(com.clouderp.exception.ForbiddenException.class, () ->
                purchaseService.updatePurchaseStatus(po2.getId(), "CANCELLED")
        );

        // STAFF CAN view suppliers and purchases
        List<SupplierDto.Response> staffViewSuppliers = supplierService.getSuppliers(null);
        assertFalse(staffViewSuppliers.isEmpty());
        List<PurchaseDto.Response> staffViewPurchases = purchaseService.getPurchases(null, null, null);
        assertFalse(staffViewPurchases.isEmpty());

        // 10. Multi-Tenant Isolation: Company B
        AuthDto.RegisterRequest regB = new AuthDto.RegisterRequest();
        regB.setCompanyName("Omega Corp " + ts);
        regB.setOwnerName("Oscar Omega");
        regB.setEmail("oscar_" + ts + "@omegacorp.com");
        regB.setPassword("OmegaPass@123");
        regB.setBusinessType("Logistics");

        AuthDto.AuthResponse authB = authService.register(regB);
        Long companyBId = authB.getCompany().getId();
        Long adminBId = authB.getUser().getId();

        authenticateAs(adminBId, companyBId, "oscar_" + ts + "@omegacorp.com", "COMPANY_ADMIN");

        // Company B cannot see Company A's supplier
        List<SupplierDto.Response> suppliersB = supplierService.getSuppliers(null);
        assertTrue(suppliersB.stream().noneMatch(s -> s.getName().equals("Precision Alloys Ltd")));

        // Company B cannot see Company A's purchase
        List<PurchaseDto.Response> purchasesB = purchaseService.getPurchases(null, null, null);
        assertTrue(purchasesB.stream().noneMatch(p -> p.getId().equals(po1.getId()) || p.getId().equals(po2.getId())));
    }

    @Test
    void testExpenseManagementAndCategoryBreakdownAndRolePermissions() {
        long ts = System.currentTimeMillis();
        // 1. Setup Company A
        AuthDto.RegisterRequest regA = new AuthDto.RegisterRequest();
        regA.setCompanyName("Zenith Labs " + ts);
        regA.setOwnerName("Zack Zenith");
        regA.setEmail("zack_" + ts + "@zenithlabs.com");
        regA.setPassword("ZenithPass@123");
        regA.setBusinessType("Biotech");

        AuthDto.AuthResponse authA = authService.register(regA);
        Long companyAId = authA.getCompany().getId();
        Long adminAId = authA.getUser().getId();

        authenticateAs(adminAId, companyAId, "zack_" + ts + "@zenithlabs.com", "COMPANY_ADMIN");

        // 2. Create Expenses in Company A
        java.time.LocalDate today = java.time.LocalDate.now();

        ExpenseDto.Create exp1 = new ExpenseDto.Create();
        exp1.setTitle("Laboratory Lease");
        exp1.setCategory("Rent");
        exp1.setAmount(20000.0);
        exp1.setExpenseDate(today);
        exp1.setPaymentMethod("Bank Transfer");
        exp1.setDescription("Main lab premises monthly lease");
        ExpenseDto.Response res1 = expenseService.createExpense(exp1);
        assertNotNull(res1.getId());
        assertEquals("Rent", res1.getCategory());
        assertEquals(companyAId, res1.getCompanyId());

        ExpenseDto.Create exp2 = new ExpenseDto.Create();
        exp2.setTitle("Senior Researchers Payroll");
        exp2.setCategory("Salary");
        exp2.setAmount(35000.0);
        exp2.setExpenseDate(today);
        exp2.setPaymentMethod("Bank Transfer");
        ExpenseDto.Response res2 = expenseService.createExpense(exp2);

        ExpenseDto.Create exp3 = new ExpenseDto.Create();
        exp3.setTitle("Substation Power Supply");
        exp3.setCategory("Electricity");
        exp3.setAmount(5000.0);
        exp3.setExpenseDate(today);
        exp3.setPaymentMethod("UPI");
        ExpenseDto.Response res3 = expenseService.createExpense(exp3);

        // 3. Verify Summary & Category Breakdown
        ExpenseDto.SummaryResponse summary = expenseService.getSummary();
        assertEquals(60000.0, summary.getTotalExpenses()); // 20000 + 35000 + 5000 = 60000
        assertEquals(3L, summary.getExpenseCount());
        assertEquals("Salary", summary.getTopCategory());
        assertEquals(35000.0, summary.getTopCategoryAmount());
        assertFalse(summary.getCategoryBreakdown().isEmpty());
        assertFalse(summary.getMonthlyTrends().isEmpty());

        // 4. Test Filters
        List<ExpenseDto.Response> rentOnly = expenseService.getExpenses(null, "Rent", null, null);
        assertEquals(1, rentOnly.size());
        assertEquals("Laboratory Lease", rentOnly.get(0).getTitle());

        List<ExpenseDto.Response> searchMatch = expenseService.getExpenses("Substation", null, null, null);
        assertEquals(1, searchMatch.size());
        assertEquals("Electricity", searchMatch.get(0).getCategory());

        // 5. Update Expense
        ExpenseDto.Update updateExp = new ExpenseDto.Update();
        updateExp.setTitle("Laboratory Lease - Renewal");
        updateExp.setCategory("Rent");
        updateExp.setAmount(22000.0);
        updateExp.setExpenseDate(today);
        updateExp.setPaymentMethod("Bank Transfer");
        ExpenseDto.Response updatedRes1 = expenseService.updateExpense(res1.getId(), updateExp);
        assertEquals("Laboratory Lease - Renewal", updatedRes1.getTitle());
        assertEquals(22000.0, updatedRes1.getAmount());

        // 6. Delete Expense
        expenseService.deleteExpense(res3.getId());
        List<ExpenseDto.Response> expensesAfterDelete = expenseService.getExpenses(null, null, null, null);
        assertEquals(2, expensesAfterDelete.size());
        assertTrue(expensesAfterDelete.stream().noneMatch(e -> e.getId().equals(res3.getId())));

        // 7. Role Permissions Testing with STAFF
        authenticateAs(8888L, companyAId, "staff_" + ts + "@zenithlabs.com", "STAFF");

        // Staff CAN view expenses and summaries
        List<ExpenseDto.Response> staffViewExpenses = expenseService.getExpenses(null, null, null, null);
        assertEquals(2, staffViewExpenses.size());
        ExpenseDto.SummaryResponse staffViewSummary = expenseService.getSummary();
        assertNotNull(staffViewSummary);

        // Staff CANNOT create expense
        assertThrows(com.clouderp.exception.ForbiddenException.class, () ->
                expenseService.createExpense(exp1)
        );

        // Staff CANNOT update expense
        assertThrows(com.clouderp.exception.ForbiddenException.class, () ->
                expenseService.updateExpense(res2.getId(), updateExp)
        );

        // Staff CANNOT delete expense
        assertThrows(com.clouderp.exception.ForbiddenException.class, () ->
                expenseService.deleteExpense(res2.getId())
        );

        // 8. Multi-Tenant Isolation: Company B
        AuthDto.RegisterRequest regB = new AuthDto.RegisterRequest();
        regB.setCompanyName("Nexus Dynamics " + ts);
        regB.setOwnerName("Ned Nexus");
        regB.setEmail("ned_" + ts + "@nexusdynamics.com");
        regB.setPassword("NexusPass@123");
        regB.setBusinessType("Retail");

        AuthDto.AuthResponse authB = authService.register(regB);
        Long companyBId = authB.getCompany().getId();
        Long adminBId = authB.getUser().getId();

        authenticateAs(adminBId, companyBId, "ned_" + ts + "@nexusdynamics.com", "COMPANY_ADMIN");

        // Company B cannot see Company A's expenses
        List<ExpenseDto.Response> expensesB = expenseService.getExpenses(null, null, null, null);
        assertTrue(expensesB.stream().noneMatch(e -> e.getId().equals(res1.getId()) || e.getId().equals(res2.getId())));
    }

    @Test
    void testFinancialOverviewAndProfitabilityAndRolePermissions() {
        long ts = System.currentTimeMillis();

        // 1. Setup Company A
        AuthDto.RegisterRequest regA = new AuthDto.RegisterRequest();
        regA.setCompanyName("Vanguard Robotics " + ts);
        regA.setOwnerName("Victor Vanguard");
        regA.setEmail("victor_" + ts + "@vanguard.com");
        regA.setPassword("VanguardPass@123");
        regA.setBusinessType("Robotics");

        AuthDto.AuthResponse authA = authService.register(regA);
        Long companyAId = authA.getCompany().getId();
        Long adminAId = authA.getUser().getId();

        authenticateAs(adminAId, companyAId, "victor_" + ts + "@vanguard.com", "COMPANY_ADMIN");

        // 2. Create Product with Cost Price
        Product prod1 = new Product(companyAId, "Robotic Arm Kit", 2000.0, 100, 1200.0, 10);
        prod1 = productRepository.save(prod1);

        // 3. Create Sales (Revenue = 2000 * 10 + 2000 * 5 = 30,000; COGS = 1200 * 15 = 18,000; Gross Profit = 12,000)
        Sale sale1 = new Sale(companyAId, prod1.getId(), null, 10, 2000.0, 20000.0);
        Sale sale2 = new Sale(companyAId, prod1.getId(), null, 5, 2000.0, 10000.0);
        saleRepository.save(sale1);
        saleRepository.save(sale2);

        // 4. Create Operating Expenses (Rent 5,000 + Electricity 2,000 = 7,000; Net Profit = 12,000 - 7,000 = 5,000)
        java.time.LocalDate today = java.time.LocalDate.now();
        ExpenseDto.Create exp1 = new ExpenseDto.Create();
        exp1.setTitle("Assembly Facility Rent");
        exp1.setCategory("Rent");
        exp1.setAmount(5000.0);
        exp1.setExpenseDate(today);
        exp1.setPaymentMethod("Bank Transfer");
        expenseService.createExpense(exp1);

        ExpenseDto.Create exp2 = new ExpenseDto.Create();
        exp2.setTitle("Grid Power Bill");
        exp2.setCategory("Electricity");
        exp2.setAmount(2000.0);
        exp2.setExpenseDate(today);
        exp2.setPaymentMethod("UPI");
        expenseService.createExpense(exp2);

        // 5. Create Purchases (1 RECEIVED = 15,000; 1 PENDING = 8,000; 1 CANCELLED = 4,000)
        Purchase poReceived = new Purchase(companyAId, 1L, "PO-VG-001", "RECEIVED", 15000.0, "Servos shipment");
        Purchase poPending = new Purchase(companyAId, 1L, "PO-VG-002", "PENDING", 8000.0, "Sensors shipment");
        Purchase poCancelled = new Purchase(companyAId, 1L, "PO-VG-003", "CANCELLED", 4000.0, "Defective order");
        purchaseRepository.save(poReceived);
        purchaseRepository.save(poPending);
        purchaseRepository.save(poCancelled);

        // 6. Test Financial Summary Calculations
        com.clouderp.dto.FinanceDto.SummaryResponse fin = financeService.getFinancialSummary(null, null);

        assertEquals(companyAId, fin.getCompanyId());
        assertEquals("Vanguard Robotics " + ts, fin.getCompanyName());
        assertEquals(30000.0, fin.getTotalRevenue(), 0.01);
        assertEquals(2, fin.getTotalSalesCount());
        assertEquals(15, fin.getTotalUnitsSold());

        // COGS & Gross Profit
        assertEquals(18000.0, fin.getCogs(), 0.01);
        assertEquals(12000.0, fin.getGrossProfit(), 0.01);
        assertEquals(40.0, fin.getGrossProfitMargin(), 0.01); // (12000 / 30000) * 100 = 40.0%

        // Operating Expenses
        assertEquals(7000.0, fin.getTotalOperatingExpenses(), 0.01);
        assertEquals(2, fin.getExpenseCount());

        // Net Operating Profit
        assertEquals(5000.0, fin.getNetOperatingProfit(), 0.01); // 12000 - 7000 = 5000
        assertEquals(16.67, fin.getNetOperatingProfitMargin(), 0.01); // (5000 / 30000) * 100 = 16.67%

        // Procurement Spending (RECEIVED only, pending and cancelled excluded from spending)
        assertEquals(15000.0, fin.getProcurementSpending(), 0.01);
        assertEquals(1, fin.getProcurementCount());
        assertEquals(8000.0, fin.getPendingPurchasesAmount(), 0.01);
        assertEquals(1, fin.getPendingPurchasesCount());
        assertEquals(1, fin.getCancelledPurchasesCount());

        // Monthly trends & Best month
        assertFalse(fin.getMonthlyTrends().isEmpty());
        assertNotNull(fin.getBestMonthRevenue());
        assertNotNull(fin.getBestMonthProfit());

        // Category breakdown
        assertEquals(2, fin.getExpenseBreakdown().size());
        assertEquals("Rent", fin.getExpenseBreakdown().get(0).getCategory());
        assertEquals(5000.0, fin.getExpenseBreakdown().get(0).getAmount(), 0.01);

        // Product profitability
        assertEquals(1, fin.getProductProfitability().size());
        assertEquals("Robotic Arm Kit", fin.getProductProfitability().get(0).getProductName());
        assertEquals(12000.0, fin.getProductProfitability().get(0).getGrossProfit(), 0.01);

        // 7. Role Permissions: STAFF cannot access financial analytics
        authenticateAs(9999L, companyAId, "staff_" + ts + "@vanguard.com", "STAFF");
        assertThrows(com.clouderp.exception.ForbiddenException.class, () ->
                financeService.getFinancialSummary(null, null)
        );

        // 8. Multi-Tenant Isolation: Company B
        AuthDto.RegisterRequest regB = new AuthDto.RegisterRequest();
        regB.setCompanyName("Titan Aerospace " + ts);
        regB.setOwnerName("Tara Titan");
        regB.setEmail("tara_" + ts + "@titan.com");
        regB.setPassword("TitanPass@123");
        regB.setBusinessType("Aviation");

        AuthDto.AuthResponse authB = authService.register(regB);
        Long companyBId = authB.getCompany().getId();
        Long adminBId = authB.getUser().getId();

        authenticateAs(adminBId, companyBId, "tara_" + ts + "@titan.com", "COMPANY_ADMIN");

        // Company B has 0 sales and 0 expenses
        com.clouderp.dto.FinanceDto.SummaryResponse finB = financeService.getFinancialSummary(null, null);
        assertEquals(0.0, finB.getTotalRevenue(), 0.01);
        assertEquals(0.0, finB.getCogs(), 0.01);
        assertEquals(0.0, finB.getGrossProfit(), 0.01);
        assertEquals(0.0, finB.getTotalOperatingExpenses(), 0.01);
        assertEquals(0.0, finB.getNetOperatingProfit(), 0.01);
        assertEquals(0.0, finB.getProcurementSpending(), 0.01);
    }

    @Test
    void testCustomerPaymentsReceivablesAndRolePermissions() {
        long ts = System.currentTimeMillis();

        // 1. Setup Company A
        AuthDto.RegisterRequest regA = new AuthDto.RegisterRequest();
        regA.setCompanyName("Pinnacle Motors " + ts);
        regA.setOwnerName("Perry Pinnacle");
        regA.setEmail("perry_" + ts + "@pinnacle.com");
        regA.setPassword("Pinnacle@123");
        regA.setBusinessType("Automotive");

        AuthDto.AuthResponse authA = authService.register(regA);
        Long companyAId = authA.getCompany().getId();
        Long adminAId = authA.getUser().getId();

        authenticateAs(adminAId, companyAId, "perry_" + ts + "@pinnacle.com", "COMPANY_ADMIN");

        // 2. Setup Customer & Product
        Customer customer = customerRepository.save(new Customer(
                companyAId, "Acme Logistics", "9876543210", "accounts@acmelogistics.com", "Industrial Area, Plot 12"
        ));

        Product prod = new Product(companyAId, "Heavy Duty Axle", 10000.0, 50, 7000.0, 5);
        prod = productRepository.save(prod);

        // 3. Create Sale (Invoice total = 10000 * 2 = 20,000)
        Sale sale = new Sale(companyAId, prod.getId(), customer.getId(), 2, 10000.0, 20000.0);
        sale.setInvoiceNumber("INV-PNL-001");
        sale.setPaidAmount(0.0);
        sale.setBalanceAmount(20000.0);
        sale.setPaymentStatus("UNPAID");
        sale.setDueDate(java.time.LocalDate.now().minusDays(5)); // Set as overdue
        sale = saleRepository.save(sale);

        // 4. Verify Initial Receivables Status
        PaymentDto.ReceivablesSummary initialSummary = paymentService.getReceivablesSummary();
        assertEquals(20000.0, initialSummary.getTotalReceivables(), 0.01);
        assertEquals(20000.0, initialSummary.getTotalOverdue(), 0.01);
        assertEquals(1, initialSummary.getOverdueInvoicesCount());
        assertEquals(1, initialSummary.getUnpaidInvoicesCount());
        assertEquals(0.0, initialSummary.getTotalPaid(), 0.01);

        // 5. Record Partial Payment: Rs 8,000 via UPI
        PaymentDto.RecordRequest partialReq = new PaymentDto.RecordRequest();
        partialReq.setSaleId(sale.getId());
        partialReq.setAmount(8000.0);
        partialReq.setPaymentDate(java.time.LocalDate.now());
        partialReq.setPaymentMethod("UPI");
        partialReq.setReferenceNumber("UPI-TXN-12345");
        partialReq.setNotes("First installment");

        PaymentDto.Response pay1 = paymentService.recordPayment(partialReq);
        assertEquals(8000.0, pay1.getAmount(), 0.01);
        assertEquals("UPI", pay1.getPaymentMethod());
        assertEquals("PARTIALLY_PAID", pay1.getInvoicePaymentStatus());
        assertEquals(12000.0, pay1.getInvoiceBalance(), 0.01);

        // Verify sale record in DB
        Sale updatedSale1 = saleRepository.findById(sale.getId()).orElseThrow();
        assertEquals("PARTIALLY_PAID", updatedSale1.getPaymentStatus());
        assertEquals(8000.0, updatedSale1.getPaidAmount(), 0.01);
        assertEquals(12000.0, updatedSale1.getBalanceAmount(), 0.01);

        // 6. Test Overpayment Protection (Cannot pay more than remaining balance 12,000)
        PaymentDto.RecordRequest overpayReq = new PaymentDto.RecordRequest();
        overpayReq.setSaleId(sale.getId());
        overpayReq.setAmount(15000.0);
        overpayReq.setPaymentDate(java.time.LocalDate.now());
        overpayReq.setPaymentMethod("Cash");
        assertThrows(BadRequestException.class, () ->
                paymentService.recordPayment(overpayReq)
        );

        // 7. Record Full Remaining Payment: Rs 12,000 via Bank Transfer
        PaymentDto.RecordRequest finalReq = new PaymentDto.RecordRequest();
        finalReq.setSaleId(sale.getId());
        finalReq.setAmount(12000.0);
        finalReq.setPaymentDate(java.time.LocalDate.now());
        finalReq.setPaymentMethod("Bank Transfer");
        finalReq.setReferenceNumber("NEFT-987654");
        finalReq.setNotes("Settlement in full");

        PaymentDto.Response pay2 = paymentService.recordPayment(finalReq);
        assertEquals("PAID", pay2.getInvoicePaymentStatus());
        assertEquals(0.0, pay2.getInvoiceBalance(), 0.01);

        // Verify sale is fully paid
        Sale fullyPaidSale = saleRepository.findById(sale.getId()).orElseThrow();
        assertEquals("PAID", fullyPaidSale.getPaymentStatus());
        assertEquals(20000.0, fullyPaidSale.getPaidAmount(), 0.01);
        assertEquals(0.0, fullyPaidSale.getBalanceAmount(), 0.01);

        // 8. Verify Payment History for Sale & Customer
        List<PaymentDto.Response> salePayments = paymentService.getPaymentsForSale(sale.getId());
        assertEquals(2, salePayments.size());

        List<PaymentDto.Response> customerPayments = paymentService.getPaymentsForCustomer(customer.getId());
        assertEquals(2, customerPayments.size());

        // 9. Verify Updated Receivables Summary (Zero overdue, 100% collected)
        PaymentDto.ReceivablesSummary finalSummary = paymentService.getReceivablesSummary();
        assertEquals(0.0, finalSummary.getTotalReceivables(), 0.01);
        assertEquals(0.0, finalSummary.getTotalOverdue(), 0.01);
        assertEquals(20000.0, finalSummary.getTotalPaid(), 0.01);
        assertEquals(1, finalSummary.getPaidInvoicesCount());
        assertEquals(100.0, finalSummary.getCollectionRatePct(), 0.01);

        // 10. Role Permissions: STAFF cannot record payments (ForbiddenException)
        authenticateAs(7777L, companyAId, "staff_" + ts + "@pinnacle.com", "STAFF");

        PaymentDto.RecordRequest unauthorizedPay = new PaymentDto.RecordRequest();
        unauthorizedPay.setSaleId(sale.getId());
        unauthorizedPay.setAmount(100.0);
        unauthorizedPay.setPaymentDate(java.time.LocalDate.now());
        unauthorizedPay.setPaymentMethod("Cash");

        assertThrows(com.clouderp.exception.ForbiddenException.class, () ->
                paymentService.recordPayment(unauthorizedPay)
        );

        // Staff CAN view receivables summary and invoices
        PaymentDto.ReceivablesSummary staffSummary = paymentService.getReceivablesSummary();
        assertNotNull(staffSummary);
        List<PaymentDto.InvoiceReceivable> staffInvoices = paymentService.getInvoiceReceivables("ALL", null, null);
        assertFalse(staffInvoices.isEmpty());

        // 11. Multi-Tenant Isolation: Company B
        AuthDto.RegisterRequest regB = new AuthDto.RegisterRequest();
        regB.setCompanyName("Quantum Auto " + ts);
        regB.setOwnerName("Quinn Quantum");
        regB.setEmail("quinn_" + ts + "@quantum.com");
        regB.setPassword("Quantum@123");
        regB.setBusinessType("Retail");

        AuthDto.AuthResponse authB = authService.register(regB);
        Long companyBId = authB.getCompany().getId();
        Long adminBId = authB.getUser().getId();

        authenticateAs(adminBId, companyBId, "quinn_" + ts + "@quantum.com", "COMPANY_ADMIN");

        // Company B cannot see Company A's receivables or payments
        PaymentDto.ReceivablesSummary compBSummary = paymentService.getReceivablesSummary();
        assertEquals(0.0, compBSummary.getTotalReceivables(), 0.01);
        assertEquals(0.0, compBSummary.getTotalPaid(), 0.01);

        // Company B cannot pay Company A's invoice
        PaymentDto.RecordRequest compBPay = new PaymentDto.RecordRequest();
        compBPay.setSaleId(sale.getId());
        compBPay.setAmount(500.0);
        compBPay.setPaymentDate(java.time.LocalDate.now());
        compBPay.setPaymentMethod("Cash");

        assertThrows(ResourceNotFoundException.class, () ->
                paymentService.recordPayment(compBPay)
        );
    }

    @Test
    void testBusinessReportsCalculationsDateFilteringExportsAndIsolation() {
        long ts = System.currentTimeMillis();

        // 1. Register Company A
        AuthDto.RegisterRequest regA = new AuthDto.RegisterRequest();
        regA.setCompanyName("Nexus Analytics " + ts);
        regA.setOwnerName("Nathan Nexus");
        regA.setEmail("nathan_" + ts + "@nexus.com");
        regA.setPassword("Nexus@123");
        regA.setBusinessType("Manufacturing");

        AuthDto.AuthResponse authA = authService.register(regA);
        Long companyAId = authA.getCompany().getId();
        Long adminAId = authA.getUser().getId();

        authenticateAs(adminAId, companyAId, "nathan_" + ts + "@nexus.com", "COMPANY_ADMIN");

        // 2. Set up Products
        Product prod1 = productRepository.save(new Product(companyAId, "Hydraulic Pump " + ts, 7000.0, 50, 4000.0, 10));
        Product prod2 = productRepository.save(new Product(companyAId, "Filter Core " + ts, 2500.0, 4, 1000.0, 5)); // Low stock (4 <= 5)

        // 3. Set up Supplier & Purchase (RECEIVED)
        SupplierDto.Create supReq = new SupplierDto.Create();
        supReq.setName("Global Hydraulics " + ts);
        supReq.setContactPerson("George Glen");
        supReq.setPhone("9876543210");
        supReq.setEmail("george_" + ts + "@global.com");
        SupplierDto.Response sup = supplierService.createSupplier(supReq);

        PurchaseDto.Create purchaseReq = new PurchaseDto.Create();
        purchaseReq.setSupplierId(sup.getId());
        purchaseReq.setNotes("Bulk purchase");
        PurchaseDto.ItemRequest item = new PurchaseDto.ItemRequest();
        item.setProductId(prod2.getId());
        item.setQuantity(20);
        item.setUnitPrice(1000.0);
        purchaseReq.setItems(List.of(item));

        PurchaseDto.Response purchase = purchaseService.createPurchase(purchaseReq);
        purchaseService.updatePurchaseStatus(purchase.getId(), "RECEIVED");

        // 4. Set up Expenses
        ExpenseDto.Create exp1 = new ExpenseDto.Create();
        exp1.setTitle("Office Rent");
        exp1.setCategory("Rent");
        exp1.setAmount(15000.0);
        exp1.setExpenseDate(java.time.LocalDate.now());
        exp1.setPaymentMethod("Bank Transfer");
        expenseService.createExpense(exp1);

        ExpenseDto.Create exp2 = new ExpenseDto.Create();
        exp2.setTitle("Power & Internet");
        exp2.setCategory("Electricity");
        exp2.setAmount(5000.0);
        exp2.setExpenseDate(java.time.LocalDate.now());
        exp2.setPaymentMethod("UPI");
        expenseService.createExpense(exp2);

        // 5. Set up Customer & Sale
        Customer customer = customerRepository.save(new Customer(companyAId, "Atlas Infra " + ts, "9876500000", "finance@atlas.com", "Sector 5"));

        Sale sale = new Sale(companyAId, prod1.getId(), customer.getId(), 10, 7000.0, 70000.0);
        sale.setInvoiceNumber("INV-" + ts);
        sale.setDueDate(java.time.LocalDate.now().plusDays(30));
        sale = saleRepository.save(sale);

        // Record Partial Payment of 30,000 (Balance = 40,000)
        PaymentDto.RecordRequest payReq = new PaymentDto.RecordRequest();
        payReq.setSaleId(sale.getId());
        payReq.setAmount(30000.0);
        payReq.setPaymentDate(java.time.LocalDate.now());
        payReq.setPaymentMethod("Bank Transfer");
        paymentService.recordPayment(payReq);

        // =========================================================================
        // VERIFY 1. SALES REPORT
        // =========================================================================
        ReportDto.SalesReport salesReport = reportService.getSalesReport(null, null, "all_time");
        assertEquals("Nexus Analytics " + ts, salesReport.getCompanyName());
        assertEquals(70000.0, salesReport.getTotalRevenue(), 0.01);
        assertEquals(1, salesReport.getTotalTransactions());
        assertEquals(10, salesReport.getTotalUnitsSold());
        assertEquals(70000.0, salesReport.getAverageOrderValue(), 0.01);
        assertFalse(salesReport.getTopProducts().isEmpty());
        assertEquals("Hydraulic Pump " + ts, salesReport.getTopProducts().get(0).getProductName());
        assertEquals(10, salesReport.getTopProducts().get(0).getUnitsSold());

        // =========================================================================
        // VERIFY 2. PURCHASE REPORT
        // =========================================================================
        ReportDto.PurchaseReport purchaseReport = reportService.getPurchaseReport(null, null, "all_time");
        assertEquals(20000.0, purchaseReport.getTotalProcurementSpending(), 0.01);
        assertEquals(1, purchaseReport.getTotalPurchasesCount());
        assertEquals(1, purchaseReport.getReceivedPurchasesCount());
        assertEquals(0, purchaseReport.getPendingPurchasesCount());
        assertFalse(purchaseReport.getSupplierBreakdown().isEmpty());
        assertEquals("Global Hydraulics " + ts, purchaseReport.getSupplierBreakdown().get(0).getSupplierName());
        assertEquals(20000.0, purchaseReport.getSupplierBreakdown().get(0).getTotalSpending(), 0.01);

        // =========================================================================
        // VERIFY 3. INVENTORY REPORT
        // =========================================================================
        ReportDto.InventoryReport invReport = reportService.getInventoryReport();
        assertEquals(2, invReport.getTotalProducts());
        assertTrue(invReport.getTotalStockUnits() >= 54);
        assertTrue(invReport.getTotalValuationCost() > 0);
        assertTrue(invReport.getLowStockCount() >= 1);
        assertFalse(invReport.getLowStockAlerts().isEmpty());

        // =========================================================================
        // VERIFY 4. EXPENSE REPORT
        // =========================================================================
        ReportDto.ExpenseReport expReport = reportService.getExpenseReport(null, null, "all_time");
        assertEquals(20000.0, expReport.getTotalExpenses(), 0.01);
        assertEquals(2, expReport.getTotalTransactions());
        assertEquals(10000.0, expReport.getAverageExpense(), 0.01);
        assertEquals(2, expReport.getCategoryBreakdown().size());

        // =========================================================================
        // VERIFY 5. PROFIT & LOSS REPORT (Calculations match Financial Overview)
        // =========================================================================
        ReportDto.ProfitLossReport plReport = reportService.getProfitLossReport(null, null, "all_time");
        FinanceDto.SummaryResponse finSummary = financeService.getFinancialSummary(null, null);

        // Core P&L values
        assertEquals(70000.0, plReport.getGrossSalesRevenue(), 0.01);
        assertEquals(40000.0, plReport.getCostOfGoodsSold(), 0.01); // 10 units * 4000 cost
        assertEquals(30000.0, plReport.getGrossProfit(), 0.01);     // 70000 - 40000
        assertEquals(42.86, plReport.getGrossProfitMarginPct(), 0.1);
        assertEquals(20000.0, plReport.getTotalOperatingExpenses(), 0.01);
        assertEquals(10000.0, plReport.getNetOperatingProfit(), 0.01); // 30000 - 20000
        assertEquals(14.29, plReport.getNetOperatingProfitMarginPct(), 0.1);

        // Exact match with FinanceService calculations
        assertEquals(finSummary.getTotalRevenue(), plReport.getGrossSalesRevenue(), 0.01);
        assertEquals(finSummary.getCogs(), plReport.getCostOfGoodsSold(), 0.01);
        assertEquals(finSummary.getGrossProfit(), plReport.getGrossProfit(), 0.01);
        assertEquals(finSummary.getTotalOperatingExpenses(), plReport.getTotalOperatingExpenses(), 0.01);
        assertEquals(finSummary.getNetOperatingProfit(), plReport.getNetOperatingProfit(), 0.01);

        // =========================================================================
        // VERIFY 6. RECEIVABLES REPORT
        // =========================================================================
        ReportDto.ReceivablesReport recReport = reportService.getReceivablesReport(null, null, "all_time");
        assertEquals(70000.0, recReport.getTotalInvoiced(), 0.01);
        assertEquals(30000.0, recReport.getTotalPaid(), 0.01);
        assertEquals(40000.0, recReport.getTotalReceivables(), 0.01);
        assertEquals(42.86, recReport.getCollectionRatePct(), 0.1);
        assertEquals(1, recReport.getPartiallyPaidCount());
        assertFalse(recReport.getCustomerBreakdown().isEmpty());
        assertEquals("Atlas Infra " + ts, recReport.getCustomerBreakdown().get(0).getCustomerName());
        assertEquals(40000.0, recReport.getCustomerBreakdown().get(0).getOutstandingBalance(), 0.01);

        // =========================================================================
        // VERIFY 7. DATE FILTERING & PRESETS
        // =========================================================================
        ReportDto.SalesReport thisMonthSales = reportService.getSalesReport(null, null, "this_month");
        assertEquals(70000.0, thisMonthSales.getTotalRevenue(), 0.01);

        ReportDto.SalesReport lastMonthSales = reportService.getSalesReport(null, null, "last_month");
        assertEquals(0.0, lastMonthSales.getTotalRevenue(), 0.01); // No sales in last month

        // =========================================================================
        // VERIFY 8. EXPORT GENERATION (PDF & EXCEL)
        // =========================================================================
        // 1. PDF Export
        byte[] salesPdf = reportService.exportReport("sales", "pdf", null, null, "this_month");
        assertNotNull(salesPdf);
        assertTrue(salesPdf.length > 500, "PDF size should be substantial");
        String pdfHeader = new String(salesPdf, 0, 8, java.nio.charset.StandardCharsets.US_ASCII);
        assertTrue(pdfHeader.startsWith("%PDF-1.4"), "Valid standard PDF 1.4 header");

        byte[] plPdf = reportService.exportReport("profit-loss", "pdf", null, null, "all_time");
        assertNotNull(plPdf);
        assertTrue(new String(plPdf, 0, 8, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-1.4"));

        // 2. Excel (SpreadsheetML) Export
        byte[] salesExcel = reportService.exportReport("sales", "excel", null, null, "this_month");
        assertNotNull(salesExcel);
        String excelStr = new String(salesExcel, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(excelStr.contains("<?xml version=\"1.0\""));
        assertTrue(excelStr.contains("<Workbook"));
        assertTrue(excelStr.contains("EXECUTIVE KPI SUMMARY"));
        assertTrue(excelStr.contains("DETAILED SALES TRANSACTIONS"));

        byte[] plExcel = reportService.exportReport("profit-loss", "excel", null, null, "all_time");
        assertNotNull(plExcel);
        String plExcelStr = new String(plExcel, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(plExcelStr.contains("PROFIT &amp; LOSS STATEMENT"));
        assertTrue(plExcelStr.contains("GROSS PROFIT"));

        // =========================================================================
        // VERIFY 9. MULTI-TENANT ISOLATION
        // =========================================================================
        AuthDto.RegisterRequest regB = new AuthDto.RegisterRequest();
        regB.setCompanyName("Vortex Ventures " + ts);
        regB.setOwnerName("Victor Vortex");
        regB.setEmail("victor_" + ts + "@vortex.com");
        regB.setPassword("Vortex@123");
        regB.setBusinessType("Services");

        AuthDto.AuthResponse authB = authService.register(regB);
        Long companyBId = authB.getCompany().getId();
        Long adminBId = authB.getUser().getId();

        authenticateAs(adminBId, companyBId, "victor_" + ts + "@vortex.com", "COMPANY_ADMIN");

        // Tenant 2 has 0 sales, 0 purchases, 0 expenses, 0 receivables
        ReportDto.SalesReport tenant2Sales = reportService.getSalesReport(null, null, "all_time");
        assertEquals(0.0, tenant2Sales.getTotalRevenue(), 0.01);
        assertEquals(0, tenant2Sales.getTotalTransactions());

        ReportDto.ProfitLossReport tenant2PL = reportService.getProfitLossReport(null, null, "all_time");
        assertEquals(0.0, tenant2PL.getGrossSalesRevenue(), 0.01);
        assertEquals(0.0, tenant2PL.getNetOperatingProfit(), 0.01);

        ReportDto.ReceivablesReport tenant2Rec = reportService.getReceivablesReport(null, null, "all_time");
        assertEquals(0.0, tenant2Rec.getTotalReceivables(), 0.01);

        // =========================================================================
        // VERIFY 10. ROLE-BASED ACCESS CONTROL (STAFF)
        // =========================================================================
        authenticateAs(8888L, companyAId, "staff_" + ts + "@nexus.com", "STAFF");

        // Staff CAN view operational sales report
        ReportDto.SalesReport staffSales = reportService.getSalesReport(null, null, "all_time");
        assertNotNull(staffSales);
        assertEquals(70000.0, staffSales.getTotalRevenue(), 0.01);

        // Staff CANNOT view Profit & Loss Report (Throws ForbiddenException)
        assertThrows(com.clouderp.exception.ForbiddenException.class, () ->
                reportService.getProfitLossReport(null, null, "all_time")
        );
    }

    @Test
    void testNotificationsAndAlertsManagement() {
        long ts = System.currentTimeMillis();

        // 1. Register Company A
        AuthDto.RegisterRequest regA = new AuthDto.RegisterRequest();
        regA.setCompanyName("AlertCorp Alpha " + ts);
        regA.setOwnerName("Arthur Alert");
        regA.setEmail("arthur_" + ts + "@alertcorp.com");
        regA.setPassword("Arthur@123");
        regA.setBusinessType("Manufacturing");

        AuthDto.AuthResponse authA = authService.register(regA);
        Long compAId = authA.getCompany().getId();
        Long adminAId = authA.getUser().getId();

        authenticateAs(adminAId, compAId, "arthur_" + ts + "@alertcorp.com", "COMPANY_ADMIN");

        // 2. Setup Products for Stock Alert Testing
        // Product 1: In stock (50 units, reorder 10)
        ProductDto.Create p1 = new ProductDto.Create();
        p1.setName("Heavy Duty Steel Beam");
        p1.setPrice(2000.0);
        p1.setQuantity(50);
        p1.setCostPrice(1200.0);
        p1.setReorderLevel(10);
        ProductDto.Response prod1 = productService.createProduct(p1);

        // Product 2: Low stock (4 units, reorder 10)
        ProductDto.Create p2 = new ProductDto.Create();
        p2.setName("Titanium Fastener Bolt");
        p2.setPrice(250.0);
        p2.setQuantity(4);
        p2.setCostPrice(150.0);
        p2.setReorderLevel(10);
        ProductDto.Response prod2 = productService.createProduct(p2);

        // Product 3: Out of stock (0 units, reorder 5)
        ProductDto.Create p3 = new ProductDto.Create();
        p3.setName("Copper Cooling Pipe");
        p3.setPrice(800.0);
        p3.setQuantity(0);
        p3.setCostPrice(500.0);
        p3.setReorderLevel(5);
        ProductDto.Response prod3 = productService.createProduct(p3);

        // 3. Setup Customer and Sale (one overdue, one normal)
        CustomerDto.Create custReq = new CustomerDto.Create();
        custReq.setName("Metro Infra Projects");
        custReq.setEmail("contact@metro.com");
        custReq.setPhone("9876543201");
        custReq.setAddress("Sector 4, Noida");
        CustomerDto.Response customer = customerService.createCustomer(custReq);

        // Normal sale (will trigger NEW_SALE notification)
        SaleDto.Create saleReq1 = new SaleDto.Create();
        saleReq1.setCompanyId(compAId);
        saleReq1.setProductId(prod1.getId());
        saleReq1.setQuantity(2);
        saleReq1.setCustomerId(customer.getId());
        SaleDto.CreateResponse sale1 = saleService.createSale(saleReq1);
        assertNotNull(sale1);

        // Check that NEW_SALE notification was automatically generated
        List<NotificationDto.Response> notifs = notificationService.getNotifications(false, null, null);
        boolean hasSaleNotif = notifs.stream().anyMatch(n -> "NEW_SALE".equals(n.getType()) && n.getTitle().contains("INV-"));
        assertTrue(hasSaleNotif, "New sale notification should be auto-created");

        // Create an overdue sale manually in repo for testing overdue alert
        Sale overdueSale = new Sale(compAId, prod1.getId(), customer.getId(), 5, 2000.0, 10000.0);
        overdueSale.setInvoiceNumber("INV-OVERDUE-" + ts);
        overdueSale.setPaidAmount(2000.0);
        overdueSale.setBalanceAmount(8000.0);
        overdueSale.setPaymentStatus("PARTIALLY_PAID");
        overdueSale.setDueDate(java.time.LocalDate.now().minusDays(10)); // 10 days ago!
        saleRepository.save(overdueSale);

        // 4. Setup Supplier and Purchase Orders (one PENDING, one RECEIVED)
        SupplierDto.Create supReq = new SupplierDto.Create();
        supReq.setName("Apex Raw Materials");
        supReq.setContactPerson("Rajesh Apex");
        supReq.setPhone("9988776655");
        supReq.setEmail("rajesh@apex.com");
        supReq.setAddress("Industrial Estate");
        SupplierDto.Response supplier = supplierService.createSupplier(supReq);

        PurchaseDto.Create poReq = new PurchaseDto.Create();
        poReq.setSupplierId(supplier.getId());
        poReq.setStatus("PENDING");
        poReq.setNotes("Restock raw material batch");
        PurchaseDto.ItemRequest poItem = new PurchaseDto.ItemRequest();
        poItem.setProductId(prod3.getId());
        poItem.setQuantity(20);
        poItem.setUnitPrice(480.0);
        poReq.setItems(List.of(poItem));

        PurchaseDto.Response pendingPO = purchaseService.createPurchase(poReq);
        assertEquals("PENDING", pendingPO.getStatus());

        // Check that PURCHASE_PENDING notification was created
        notifs = notificationService.getNotifications(false, null, null);
        boolean hasPendingPONotif = notifs.stream().anyMatch(n -> "PURCHASE_PENDING".equals(n.getType()) && n.getTitle().contains("PO-"));
        assertTrue(hasPendingPONotif, "Pending purchase order alert should be auto-created");

        // 5. Test Customer Payment Notification
        PaymentDto.RecordRequest payReq = new PaymentDto.RecordRequest(
                sale1.getSale().getId(),
                2000.0,
                "UPI",
                java.time.LocalDate.now().toString(),
                "UPI-REF-" + ts,
                "Advance part payment"
        );
        PaymentDto.Response payResp = paymentService.recordPayment(payReq);
        assertNotNull(payResp);

        notifs = notificationService.getNotifications(false, null, null);
        boolean hasPayNotif = notifs.stream().anyMatch(n -> "PAYMENT_RECEIVED".equals(n.getType()) && n.getTitle().contains("PAY-"));
        assertTrue(hasPayNotif, "Payment received notification should be auto-created");

        // 6. Run System Alert Scan (low-stock, out-of-stock, overdue invoice, pending PO)
        NotificationDto.ScanResponse scan1 = notificationService.scanAndGenerateSystemAlerts(compAId);
        assertNotNull(scan1);
        assertTrue(scan1.getGeneratedAlerts() >= 3, "Scan should generate at least low-stock, out-of-stock, and overdue invoice alerts");

        // Verify out-of-stock and low-stock alerts exist
        List<NotificationDto.Response> allNotifs = notificationService.getNotifications(false, null, null);
        boolean hasOutOfStock = allNotifs.stream().anyMatch(n -> "STOCK_ALERT".equals(n.getType()) && n.getSeverity().equals("DANGER") && n.getTitle().contains("Copper Cooling Pipe"));
        boolean hasLowStock = allNotifs.stream().anyMatch(n -> "STOCK_ALERT".equals(n.getType()) && n.getSeverity().equals("WARNING") && n.getTitle().contains("Titanium Fastener Bolt"));
        boolean hasOverdue = allNotifs.stream().anyMatch(n -> "INVOICE_OVERDUE".equals(n.getType()) && n.getTitle().contains("INV-OVERDUE-"));

        assertTrue(hasOutOfStock, "Out of stock alert must exist");
        assertTrue(hasLowStock, "Low stock alert must exist");
        assertTrue(hasOverdue, "Overdue invoice alert must exist");

        // 7. Verify DUPLICATE PREVENTION:
        // Running scan a second time immediately MUST NOT create duplicates!
        int countBefore = notificationService.getNotifications(false, null, null).size();
        NotificationDto.ScanResponse scan2 = notificationService.scanAndGenerateSystemAlerts(compAId);
        int countAfter = notificationService.getNotifications(false, null, null).size();
        assertEquals(countBefore, countAfter, "Repeated alert scan must not create duplicate notifications");
        assertEquals(0, scan2.getGeneratedAlerts(), "Zero new alerts should be generated on immediate re-scan");

        // 8. Test Mark as Read and Unread Count
        NotificationDto.UnreadCountResponse unreadBefore = notificationService.getUnreadCount();
        assertTrue(unreadBefore.getUnreadCount() > 0);

        NotificationDto.Response firstNotif = allNotifs.get(0);
        NotificationDto.Response readResp = notificationService.markAsRead(firstNotif.getId());
        assertTrue(readResp.getIsRead());
        assertNotNull(readResp.getReadAt());

        NotificationDto.UnreadCountResponse unreadAfterSingle = notificationService.getUnreadCount();
        assertEquals(unreadBefore.getUnreadCount() - 1, unreadAfterSingle.getUnreadCount());

        // Mark all as read
        int updatedCount = notificationService.markAllAsRead();
        assertTrue(updatedCount > 0);
        assertEquals(0, notificationService.getUnreadCount().getUnreadCount());

        // 9. Test Tenant Isolation
        // Register Company B
        AuthDto.RegisterRequest regB = new AuthDto.RegisterRequest();
        regB.setCompanyName("AlertCorp Beta " + ts);
        regB.setOwnerName("Betty Beta");
        regB.setEmail("betty_" + ts + "@alertcorp.com");
        regB.setPassword("Betty@123");
        regB.setBusinessType("Retail");

        AuthDto.AuthResponse authB = authService.register(regB);
        Long compBId = authB.getCompany().getId();
        Long adminBId = authB.getUser().getId();

        authenticateAs(adminBId, compBId, "betty_" + ts + "@alertcorp.com", "COMPANY_ADMIN");

        // Company B should see 0 notifications from Company A
        List<NotificationDto.Response> compBNotifs = notificationService.getNotifications(false, null, null);
        assertEquals(0, compBNotifs.size(), "Company B must have 0 notifications (Tenant isolation)");
        assertEquals(0, notificationService.getUnreadCount().getUnreadCount());

        // Company B cannot mark or delete Company A's notification
        assertThrows(ResourceNotFoundException.class, () ->
                notificationService.markAsRead(firstNotif.getId())
        );
        assertThrows(ResourceNotFoundException.class, () ->
                notificationService.deleteNotification(firstNotif.getId())
        );

        // 10. Test Role-Based Access Control (STAFF)
        authenticateAs(9999L, compAId, "staff_" + ts + "@alertcorp.com", "STAFF");

        // Staff CAN view notifications and unread count
        List<NotificationDto.Response> staffNotifs = notificationService.getNotifications(false, null, null);
        assertFalse(staffNotifs.isEmpty(), "Staff can view notifications");

        // Staff CAN mark notifications as read
        NotificationDto.Response staffRead = notificationService.markAsRead(firstNotif.getId());
        assertNotNull(staffRead);
    }
}
