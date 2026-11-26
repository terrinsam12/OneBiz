package com.clouderp.service;

import com.clouderp.config.JwtUtil;
import com.clouderp.config.SecurityUtils;
import com.clouderp.config.UserPrincipal;
import com.clouderp.dto.AuthDto;
import com.clouderp.exception.BadRequestException;
import com.clouderp.exception.ResourceNotFoundException;
import com.clouderp.model.Company;
import com.clouderp.model.User;
import com.clouderp.repository.CompanyRepository;
import com.clouderp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       CompanyRepository companyRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already registered: " + email);
        }

        // 1. Create Company record
        Company company = new Company(
                request.getCompanyName().trim(),
                request.getBusinessType() != null ? request.getBusinessType().trim() : ""
        );
        company.setOwnerName(request.getOwnerName().trim());
        company.setEmail(email);
        company.setPhone(request.getPhone() != null ? request.getPhone().trim() : "");
        company.setBusinessType(request.getBusinessType() != null ? request.getBusinessType().trim() : "");

        Company savedCompany = companyRepository.save(company);

        // 2. Create Owner User record with COMPANY_ADMIN role
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(
                savedCompany.getId(),
                email,
                hashedPassword,
                request.getOwnerName().trim(),
                request.getPhone() != null ? request.getPhone().trim() : "",
                "COMPANY_ADMIN"
        );
        User savedUser = userRepository.save(user);

        // 3. Generate JWT token
        UserPrincipal principal = UserPrincipal.create(savedUser, savedCompany.getName());
        String token = jwtUtil.generateToken(principal);

        return buildAuthResponse(token, savedUser, savedCompany);
    }

    @Transactional(readOnly = true)
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadRequestException("Account has been deactivated. Please contact your company administrator.");
        }

        Company company = companyRepository.findById(user.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for user"));

        UserPrincipal principal = UserPrincipal.create(user, company.getName());
        String token = jwtUtil.generateToken(principal);

        return buildAuthResponse(token, user, company);
    }

    @Transactional(readOnly = true)
    public AuthDto.AuthResponse getCurrentProfile() {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal();
        if (principal == null) {
            throw new BadRequestException("User not authenticated");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Company company = companyRepository.findById(principal.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        return buildAuthResponse(null, user, company);
    }

    private AuthDto.AuthResponse buildAuthResponse(String token, User user, Company company) {
        AuthDto.UserSummary userSummary = new AuthDto.UserSummary(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone() != null ? user.getPhone() : "",
                user.getRole(),
                user.getCompanyId(),
                user.getIsActive()
        );

        AuthDto.CompanySummary companySummary = new AuthDto.CompanySummary(
                company.getId(),
                company.getName(),
                company.getBusinessType(),
                company.getIndustry(),
                company.getOwnerName(),
                company.getEmail(),
                company.getPhone()
        );

        return new AuthDto.AuthResponse(token, userSummary, companySummary);
    }
}
