package com.necom.config.seed;

import com.necom.entity.address.Address;
import com.necom.entity.authentication.Role;
import com.necom.entity.authentication.User;
import com.necom.entity.customer.Customer;
import com.necom.entity.customer.CustomerGroup;
import com.necom.entity.customer.CustomerResource;
import com.necom.entity.customer.CustomerStatus;
import com.necom.entity.employee.Department;
import com.necom.entity.employee.Employee;
import com.necom.entity.employee.JobLevel;
import com.necom.entity.employee.JobTitle;
import com.necom.entity.employee.JobType;
import com.necom.entity.employee.Office;
import com.necom.repository.address.AddressRepository;
import com.necom.repository.authentication.RoleRepository;
import com.necom.repository.authentication.UserRepository;
import com.necom.repository.customer.CustomerGroupRepository;
import com.necom.repository.customer.CustomerRepository;
import com.necom.repository.customer.CustomerResourceRepository;
import com.necom.repository.customer.CustomerStatusRepository;
import com.necom.repository.employee.DepartmentRepository;
import com.necom.repository.employee.EmployeeRepository;
import com.necom.repository.employee.JobLevelRepository;
import com.necom.repository.employee.JobTitleRepository;
import com.necom.repository.employee.JobTypeRepository;
import com.necom.repository.employee.OfficeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthDomainDataInitializer implements CommandLineRunner {
    private static final String DEFAULT_PASSWORD_RAW = "123456";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final EmployeeRepository employeeRepository;
    private final OfficeRepository officeRepository;
    private final DepartmentRepository departmentRepository;
    private final JobTypeRepository jobTypeRepository;
    private final JobLevelRepository jobLevelRepository;
    private final JobTitleRepository jobTitleRepository;
    private final CustomerRepository customerRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final CustomerResourceRepository customerResourceRepository;
    private final CustomerStatusRepository customerStatusRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = findOrCreateRole("ADMIN", "Quản trị viên");
        Role employeeRole = findOrCreateRole("EMPLOYEE", "Nhân viên");
        Role customerRole = findOrCreateRole("CUSTOMER", "Khách hàng");

        saveUser("admin", "Quản trị hệ thống", "admin@necom.local", "0919000001", "M", 1L, adminRole);
        saveUser("admin2", "Quản trị vận hành", "admin2@necom.local", "0919000002", "F", 2L, adminRole);
        User employeeUser = saveUser("employee1", "Nhân viên bán hàng", "employee1@necom.local", "0919000003", "M", 3L, employeeRole);
        User customerUser1 = saveUser("customer1", "Khách hàng lẻ", "customer1@necom.local", "0919000004", "F", 4L, customerRole);
        User customerUser2 = saveUser("customer2", "Khách hàng thành viên", "customer2@necom.local", "0919000005", "F", 5L, customerRole);

        seedEmployee(employeeUser);
        seedCustomer(customerUser1, 4L, 5L, 3L);
        seedCustomer(customerUser2, 2L, 4L, 2L);
    }

    private Role findOrCreateRole(String code, String name) {
        return roleRepository.findAll()
                .stream()
                .filter(role -> code.equals(role.getCode()))
                .findFirst()
                .orElseGet(() -> roleRepository.save(
                        new Role()
                                .setCode(code)
                                .setName(name)
                                .setStatus(1)
                ));
    }

    private User saveUser(
            String username,
            String fullname,
            String email,
            String phone,
            String gender,
            Long addressId,
            Role role
    ) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalStateException("Missing address seed with id=" + addressId));

        User user = userRepository.findByUsername(username).orElse(new User());
        user
                .setUsername(username)
                .setPassword(encodedDefaultPassword())
                .setFullname(fullname)
                .setEmail(email)
                .setPhone(phone)
                .setGender(gender)
                .setAddress(address)
                .setAvatar(null)
                .setStatus(1)
                .setRoles(Set.of(role));

        return userRepository.save(user);
    }

    private void seedEmployee(User user) {
        boolean exists = employeeRepository.findAll()
                .stream()
                .anyMatch(employee -> employee.getUser().getId().equals(user.getId()));
        if (exists) {
            return;
        }

        Office office = officeRepository.findById(4L)
                .orElseThrow(() -> new IllegalStateException("Missing office seed with id=4"));
        Department department = departmentRepository.findById(5L)
                .orElseThrow(() -> new IllegalStateException("Missing department seed with id=5"));
        JobType jobType = jobTypeRepository.findById(2L)
                .orElseThrow(() -> new IllegalStateException("Missing job type seed with id=2"));
        JobLevel jobLevel = jobLevelRepository.findById(3L)
                .orElseThrow(() -> new IllegalStateException("Missing job level seed with id=3"));
        JobTitle jobTitle = jobTitleRepository.findById(4L)
                .orElseThrow(() -> new IllegalStateException("Missing job title seed with id=4"));

        employeeRepository.save(new Employee()
                .setUser(user)
                .setOffice(office)
                .setDepartment(department)
                .setJobType(jobType)
                .setJobLevel(jobLevel)
                .setJobTitle(jobTitle));
    }

    private void seedCustomer(User user, Long customerGroupId, Long customerResourceId, Long customerStatusId) {
        boolean exists = customerRepository.findAll()
                .stream()
                .anyMatch(customer -> customer.getUser().getId().equals(user.getId()));
        if (exists) {
            return;
        }

        CustomerGroup customerGroup = customerGroupRepository.findById(customerGroupId)
                .orElseThrow(() -> new IllegalStateException("Missing customer group seed with id=" + customerGroupId));
        CustomerResource customerResource = customerResourceRepository.findById(customerResourceId)
                .orElseThrow(() -> new IllegalStateException("Missing customer resource seed with id=" + customerResourceId));
        CustomerStatus customerStatus = customerStatusRepository.findById(customerStatusId)
                .orElseThrow(() -> new IllegalStateException("Missing customer status seed with id=" + customerStatusId));

        customerRepository.save(new Customer()
                .setUser(user)
                .setCustomerGroup(customerGroup)
                .setCustomerResource(customerResource)
                .setCustomerStatus(customerStatus));
    }

    private String encodedDefaultPassword() {
        return passwordEncoder.encode(DEFAULT_PASSWORD_RAW);
    }
}
