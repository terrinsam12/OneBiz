package com.clouderp.config;

import com.clouderp.model.Company;
import com.clouderp.model.Customer;
import com.clouderp.model.Expense;
import com.clouderp.model.Product;
import com.clouderp.model.Purchase;
import com.clouderp.model.Sale;
import com.clouderp.model.Supplier;
import com.clouderp.model.User;
import com.clouderp.repository.CompanyRepository;
import com.clouderp.repository.CustomerRepository;
import com.clouderp.repository.ExpenseRepository;
import com.clouderp.repository.ProductRepository;
import com.clouderp.repository.PurchaseRepository;
import com.clouderp.repository.SaleRepository;
import com.clouderp.repository.SupplierRepository;
import com.clouderp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final ExpenseRepository expenseRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.clouderp.service.NotificationService notificationService;

    public DataInitializer(CompanyRepository companyRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository,
                           SupplierRepository supplierRepository,
                           ExpenseRepository expenseRepository,
                           CustomerRepository customerRepository,
                           SaleRepository saleRepository,
                           PurchaseRepository purchaseRepository,
                           PasswordEncoder passwordEncoder,
                           com.clouderp.service.NotificationService notificationService) {
        this.companyRepository = companyRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
        this.expenseRepository = expenseRepository;
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Override
    public void run(String... args) {
        if (companyRepository.count() == 0) {
            log.info("[DATA INIT] No companies found. Seeding initial MSME company and product data...");

            Company company1 = new Company("ABC Manufacturing", "manufacturing");
            company1.setOwnerName("Admin");
            company1.setEmail("admin@abcmfg.com");
            companyRepository.save(company1);

            Company company2 = new Company("abs", "manufacton");
            company2.setOwnerName("ABS Admin");
            companyRepository.save(company2);

            Product prod1 = new Product(company1.getId(), "indrustrial motors", 5000.0, 20, 3500.0, 5);
            Product prod2 = new Product(company1.getId(), "industrial motors", 5000.0, 20, 3500.0, 5);
            productRepository.save(prod1);
            productRepository.save(prod2);

            log.info("[DATA INIT] Successfully seeded {} companies and {} products.",
                    companyRepository.count(), productRepository.count());
        }

        // Seed initial admin user if not present
        if (!userRepository.existsByEmail("admin@example.com") && companyRepository.count() > 0) {
            Company firstCompany = companyRepository.findAll().get(0);
            User admin = new User(
                    firstCompany.getId(),
                    "admin@example.com",
                    passwordEncoder.encode("Admin@123"),
                    "Admin Owner",
                    "9876543210",
                    "COMPANY_ADMIN"
            );
            userRepository.save(admin);
            log.info("[DATA INIT] Seeded default admin user 'admin@example.com' with password 'Admin@123'");
        }

        userRepository.findAll().forEach(u -> {
            if (u.getIsActive() == null) {
                u.setIsActive(true);
                userRepository.save(u);
            }
        });

        // Seed initial supplier if not present
        if (supplierRepository.count() == 0 && companyRepository.count() > 0) {
            Company firstCompany = companyRepository.findAll().get(0);
            Supplier sampleSupplier = new Supplier(
                    firstCompany.getId(),
                    "Apex Industrial Supplies",
                    "Rajesh Kumar",
                    "9876543220",
                    "sales@apexsupplies.com",
                    "Plot 14, Phase II, Industrial Area, Bangalore"
            );
            supplierRepository.save(sampleSupplier);
            log.info("[DATA INIT] Seeded sample supplier 'Apex Industrial Supplies'");
        }

        // Seed initial expenses if not present
        if (expenseRepository.count() == 0 && companyRepository.count() > 0) {
            Company firstCompany = companyRepository.findAll().get(0);
            LocalDate today = LocalDate.now();

            expenseRepository.save(new Expense(firstCompany.getId(), "Factory Workshop Rent", "Rent", 25000.0, today.minusDays(10), "Bank Transfer", "Monthly factory premises lease", null, null));
            expenseRepository.save(new Expense(firstCompany.getId(), "Industrial Power & Electricity", "Electricity", 8450.0, today.minusDays(8), "UPI", "Grid power bill for motors testing", null, null));
            expenseRepository.save(new Expense(firstCompany.getId(), "Staff Monthly Payroll", "Salary", 45000.0, today.minusDays(5), "Bank Transfer", "Assembly line technicians payroll", null, null));
            expenseRepository.save(new Expense(firstCompany.getId(), "High-Speed Fiber Lease Line", "Internet", 2800.0, today.minusDays(3), "Credit Card", "Office & cloud ERP connectivity", null, null));
            expenseRepository.save(new Expense(firstCompany.getId(), "CNC Machine Preventative Maintenance", "Maintenance", 4200.0, today.minusDays(1), "Cash", "Lubrication and tool calibration", null, null));

            log.info("[DATA INIT] Seeded sample business expenses for Company ID {}", firstCompany.getId());
        }

        // Seed initial customer if not present
        if (customerRepository.count() == 0 && companyRepository.count() > 0) {
            Company firstCompany = companyRepository.findAll().get(0);
            Customer cust = new Customer(
                    firstCompany.getId(),
                    "Zenith Heavy Machinery Ltd",
                    "contact@zenithmachinery.com",
                    "9876543230",
                    "Tech Park Phase 1, Whitefield, Bangalore"
            );
            customerRepository.save(cust);
            log.info("[DATA INIT] Seeded sample customer 'Zenith Heavy Machinery Ltd'");
        }

        // Seed initial sales if not present
        if (saleRepository.count() == 0 && companyRepository.count() > 0 && productRepository.count() > 0) {
            Company firstCompany = companyRepository.findAll().get(0);
            Product firstProd = productRepository.findByCompanyId(firstCompany.getId()).get(0);
            Long custId = customerRepository.count() > 0 ? customerRepository.findAll().get(0).getId() : null;

            // 3 sales: 15 units, 20 units, 10 units @ 5000 = 75000 + 100000 + 50000 = 225,000 Revenue
            saleRepository.save(new Sale(firstCompany.getId(), firstProd.getId(), custId, 15, 5000.0, 75000.0));
            saleRepository.save(new Sale(firstCompany.getId(), firstProd.getId(), custId, 20, 5000.0, 100000.0));
            saleRepository.save(new Sale(firstCompany.getId(), firstProd.getId(), custId, 10, 5000.0, 50000.0));
            log.info("[DATA INIT] Seeded sample sales for Company ID {}", firstCompany.getId());
        }

        // Seed initial purchases if not present
        if (purchaseRepository.count() == 0 && companyRepository.count() > 0 && supplierRepository.count() > 0) {
            Company firstCompany = companyRepository.findAll().get(0);
            Supplier firstSupplier = supplierRepository.findByCompanyId(firstCompany.getId()).get(0);

            // 1 RECEIVED purchase (Rs 52,500) and 1 PENDING purchase (Rs 35,000)
            Purchase po1 = new Purchase(firstCompany.getId(), firstSupplier.getId(), "PO-INIT-001", "RECEIVED", 52500.0, "Initial stock procurement");
            Purchase po2 = new Purchase(firstCompany.getId(), firstSupplier.getId(), "PO-INIT-002", "PENDING", 35000.0, "Next month restock order");
            purchaseRepository.save(po1);
            purchaseRepository.save(po2);
            log.info("[DATA INIT] Seeded sample purchases for Company ID {}", firstCompany.getId());
        }

        // Run initial alert scan for default company
        if (companyRepository.count() > 0) {
            Company firstCompany = companyRepository.findAll().get(0);
            notificationService.scanAndGenerateSystemAlerts(firstCompany.getId());
            log.info("[DATA INIT] Initialized system alerts and notifications for Company ID {}", firstCompany.getId());
        }
    }
}
