package com.github.jglm_184.travel_expense_manager.service;

import com.github.jglm_184.travel_expense_manager.dto.UserCreateDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserDetailsDTO;
import com.github.jglm_184.travel_expense_manager.dto.UserUpdateDTO;
import com.github.jglm_184.travel_expense_manager.exception.BusinessException;
import com.github.jglm_184.travel_expense_manager.exception.ResourceNotFoundException;
import com.github.jglm_184.travel_expense_manager.mapper.UserMapper;
import com.github.jglm_184.travel_expense_manager.model.Company;
import com.github.jglm_184.travel_expense_manager.model.User;
import com.github.jglm_184.travel_expense_manager.model.enums.Role;
import com.github.jglm_184.travel_expense_manager.repository.CompanyRepository;
import com.github.jglm_184.travel_expense_manager.repository.UserRepository;
import com.github.jglm_184.travel_expense_manager.util.FormatterUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final FormatterUtil formatterUtil;

    public Page<UserDetailsDTO> findAllActiveUsers(String name, String department, Pageable pageable) {
        Jwt jwt = getAuthenticatedUserJwt();
        String userRole = jwt.getClaimAsString("scope");

        if ("ROLE_ADMIN".equals(userRole)) {
            Page<User> userPage = userRepository
                    .findByNameContainingIgnoreCaseAndDepartmentContainingIgnoreCaseAndActiveTrue(name,
                            department,
                            pageable);

            return userPage.map(userMapper::toDto);
        }

        Long companyId = getCompanyIdFromToken(jwt);
        Page<User> userPage = userRepository
                .findByCompanyIdAndNameContainingIgnoreCaseAndDepartmentContainingIgnoreCaseAndActiveTrue(
                        companyId,
                        name,
                        department,
                        pageable);

        return userPage.map(userMapper::toDto);
    }

    public Page<UserDetailsDTO> findAllInactiveUsers(Pageable pageable) {
        Jwt jwt = getAuthenticatedUserJwt();
        String userRole = jwt.getClaimAsString("scope");

        if ("ROLE_ADMIN".equals(userRole)) {
            Page<User> userPage = userRepository.findByActiveFalse(pageable);
            return userPage.map(userMapper::toDto);
        }

        Long companyId = getCompanyIdFromToken(jwt);
        Page<User> userPage = userRepository.findByCompanyIdAndActiveFalse(companyId, pageable);
        return userPage.map(userMapper::toDto);
    }

    @Transactional
    public UserDetailsDTO createUser(UserCreateDTO userCreateDTO) {
        Jwt jwt = getAuthenticatedUserJwt();
        String creatorRole = jwt.getClaimAsString("scope");

        String cpf = formatterUtil.cleanNumbers(userCreateDTO.getCpf());

        if (userRepository.findByEmail(userCreateDTO.getEmail()).isPresent()) {
            throw new BusinessException("User with this email already exists");
        }

        if (userRepository.findByCpf(cpf).isPresent()) {
            throw new BusinessException("User with this CPF already exists");
        }

        User userToBeSaved = userMapper.toUser(userCreateDTO);

        userToBeSaved.setCpf(cpf);
        userToBeSaved.setPassword(bCryptPasswordEncoder.encode(userCreateDTO.getPassword()));
        userToBeSaved.setActive(true);

        if (!"ROLE_ADMIN".equals(creatorRole)) {
            Long managerId = Long.valueOf(jwt.getSubject());
            User managerLogado = userRepository.findByIdAndActiveTrue(managerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Authenticated manager not found"));

            userToBeSaved.setCompany(managerLogado.getCompany());
            userToBeSaved.setDepartment(managerLogado.getDepartment());

            if (userCreateDTO.getRole() == Role.ROLE_ADMIN) {
                throw new BusinessException("Managers cannot create Admin users");
            }
        } else {
            userToBeSaved.setDepartment(userCreateDTO.getDepartment().toUpperCase());

            if (userCreateDTO.getRole() != Role.ROLE_ADMIN) {
                if (userCreateDTO.getCompanyId() == null) {
                    throw new BusinessException("Company is required for this role");
                }
                Company company = companyRepository.findByIdAndActiveTrue(userCreateDTO.getCompanyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Company not found or is inactive"));
                userToBeSaved.setCompany(company);
            } else {
                userToBeSaved.setCompany(null);
            }
        }

        User savedUser = userRepository.save(userToBeSaved);
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDetailsDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        Jwt jwt = getAuthenticatedUserJwt();
        String updaterRole = jwt.getClaimAsString("scope");

        User userToBeUpdate = findUserActiveByIdAndTenant(id);

        String newName = userUpdateDTO.getName();
        String newEmployeeId = userUpdateDTO.getEmployeeId();
        String newDepartment = userUpdateDTO.getDepartment();
        Role newRole = userUpdateDTO.getRole();

        if (newName != null && !newName.isBlank()) {
            userToBeUpdate.setName(newName);
        }

        if (newEmployeeId != null && !newEmployeeId.isBlank()) {
            userToBeUpdate.setEmployeeId(newEmployeeId);
        }

        if (!"ROLE_ADMIN".equals(updaterRole)) {
            if (newDepartment != null && !newDepartment.isBlank()) {
                userToBeUpdate.setDepartment(newDepartment.toUpperCase());
            }
            if (newRole != null) {
                if (newRole == Role.ROLE_ADMIN) {
                    throw new BusinessException("Managers cannot promote users to Admin");
                }
                userToBeUpdate.setRole(newRole);
            }
        } else {
            if (newDepartment != null && !newDepartment.isBlank()) {
                userToBeUpdate.setDepartment(newDepartment.toUpperCase());
            }
            if (newRole != null) {
                userToBeUpdate.setRole(newRole);
            }
            if (userToBeUpdate.getRole() != Role.ROLE_ADMIN && userUpdateDTO.getCompanyId() != null) {
                Company company = companyRepository.findByIdAndActiveTrue(userUpdateDTO.getCompanyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Company not found or is inactive"));
                userToBeUpdate.setCompany(company);
            } else if (userToBeUpdate.getRole() == Role.ROLE_ADMIN) {
                userToBeUpdate.setCompany(null);
            }
        }

        User userUpdated = userRepository.save(userToBeUpdate);
        return userMapper.toDto(userUpdated);
    }

    public void deactivateUser(Long id) {
        User user = findUserActiveByIdAndTenant(id);
        user.setActive(false);
        userRepository.save(user);
    }

    public void activateUser(Long id) {
        User user = findUserInactiveByIdAndTenant(id);
        user.setActive(true);
        userRepository.save(user);
    }

    private Jwt getAuthenticatedUserJwt() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Long getCompanyIdFromToken(Jwt jwt) {
        Number companyIdClaim = jwt.getClaim("companyId");
        if (companyIdClaim == null) {
            throw new BusinessException("User does not belong to any company");
        }
        return companyIdClaim.longValue();
    }

    private User findUserActiveByIdAndTenant(Long id) {
        Jwt jwt = getAuthenticatedUserJwt();
        String userRole = jwt.getClaimAsString("scope");
        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found or is inactive"));

        if (!"ROLE_ADMIN".equals(userRole)) {
            Long companyId = getCompanyIdFromToken(jwt);
            if (user.getCompany() == null || !user.getCompany().getId().equals(companyId)) {
                throw new ResourceNotFoundException("User not found or is inactive");
            }
        }
        return user;
    }

    private User findUserInactiveByIdAndTenant(Long id) {
        Jwt jwt = getAuthenticatedUserJwt();
        String userRole = jwt.getClaimAsString("scope");
        User user = userRepository.findByIdAndActiveFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found or is active"));

        if (!"ROLE_ADMIN".equals(userRole)) {
            Long companyId = getCompanyIdFromToken(jwt);
            if (user.getCompany() == null || !user.getCompany().getId().equals(companyId)) {
                throw new ResourceNotFoundException("User not found or is active");
            }
        }
        return user;
    }
}