package com.clouderp.service;

import com.clouderp.config.SecurityUtils;
import com.clouderp.config.UserPrincipal;
import com.clouderp.dto.EmployeeDto;
import com.clouderp.exception.BadRequestException;
import com.clouderp.exception.ResourceNotFoundException;
import com.clouderp.model.User;
import com.clouderp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    private static final List<String> ALLOWED_ROLES = Arrays.asList("COMPANY_ADMIN", "MANAGER", "STAFF");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto.Response> getEmployees(String search, String role, Boolean active) {
        SecurityUtils.requireCompanyAdmin();
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        List<User> users = userRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);

        return users.stream()
                .filter(u -> {
                    if (StringUtils.hasText(search)) {
                        String s = search.trim().toLowerCase();
                        boolean matchName = u.getName() != null && u.getName().toLowerCase().contains(s);
                        boolean matchEmail = u.getEmail() != null && u.getEmail().toLowerCase().contains(s);
                        boolean matchPhone = u.getPhone() != null && u.getPhone().toLowerCase().contains(s);
                        if (!matchName && !matchEmail && !matchPhone) {
                            return false;
                        }
                    }
                    if (StringUtils.hasText(role)) {
                        if (!role.equalsIgnoreCase(u.getRole())) {
                            return false;
                        }
                    }
                    if (active != null) {
                        if (!active.equals(u.getIsActive())) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(EmployeeDto.Response::fromUser)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeDto.Response getEmployeeById(Long id) {
        SecurityUtils.requireCompanyAdmin();
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        User user = userRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        return EmployeeDto.Response.fromUser(user);
    }

    public EmployeeDto.Response createEmployee(EmployeeDto.CreateRequest request) {
        SecurityUtils.requireCompanyAdmin();
        Long companyId = SecurityUtils.requireCurrentCompanyId();

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists: " + email);
        }

        String role = request.getRole() != null ? request.getRole().trim().toUpperCase() : "STAFF";
        validateRole(role);

        String hashedPassword = passwordEncoder.encode(request.getPassword().trim());

        User user = new User(
                companyId,
                email,
                hashedPassword,
                request.getName().trim(),
                request.getPhone() != null ? request.getPhone().trim() : "",
                role
        );
        user.setIsActive(true);

        User saved = userRepository.save(user);
        return EmployeeDto.Response.fromUser(saved);
    }

    public EmployeeDto.Response updateEmployee(Long id, EmployeeDto.UpdateRequest request) {
        SecurityUtils.requireCompanyAdmin();
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        UserPrincipal currentPrincipal = SecurityUtils.getCurrentUserPrincipal();

        User user = userRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Prevent admin from modifying their own role or deactivating themselves
        if (currentPrincipal != null && currentPrincipal.getId().equals(user.getId())) {
            if (StringUtils.hasText(request.getRole()) && !request.getRole().equalsIgnoreCase(user.getRole())) {
                throw new BadRequestException("You cannot modify your own role.");
            }
            if (request.getIsActive() != null && !request.getIsActive()) {
                throw new BadRequestException("You cannot deactivate your own account.");
            }
        }

        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName().trim());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }

        if (StringUtils.hasText(request.getRole())) {
            String role = request.getRole().trim().toUpperCase();
            validateRole(role);
            user.setRole(role);
        }

        if (StringUtils.hasText(request.getPassword())) {
            if (request.getPassword().trim().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters.");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        User updated = userRepository.save(user);
        return EmployeeDto.Response.fromUser(updated);
    }

    public EmployeeDto.Response updateEmployeeStatus(Long id, Boolean active) {
        SecurityUtils.requireCompanyAdmin();
        Long companyId = SecurityUtils.requireCurrentCompanyId();
        UserPrincipal currentPrincipal = SecurityUtils.getCurrentUserPrincipal();

        if (currentPrincipal != null && currentPrincipal.getId().equals(id) && Boolean.FALSE.equals(active)) {
            throw new BadRequestException("You cannot deactivate your own account.");
        }

        User user = userRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        user.setIsActive(active != null ? active : true);
        User updated = userRepository.save(user);
        return EmployeeDto.Response.fromUser(updated);
    }

    private void validateRole(String role) {
        if (!ALLOWED_ROLES.contains(role)) {
            throw new BadRequestException("Invalid role '" + role + "'. Allowed roles are: " + String.join(", ", ALLOWED_ROLES));
        }
    }
}
