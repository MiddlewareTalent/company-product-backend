package com.accesshr.emsbackend.Service.JWT;

import com.accesshr.emsbackend.Entity.EmployeeManager;
import com.accesshr.emsbackend.Entity.EmployeeManagerPrinciple;
import com.accesshr.emsbackend.Repo.EmployeeManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmployeeManagerDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeManagerRepository employeeManagerRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        EmployeeManager employeeManager = employeeManagerRepository.findByCorporateEmail(email);
        if (employeeManager == null) {
            throw new UsernameNotFoundException("Employee not found");
        }
        return new EmployeeManagerPrinciple(employeeManager);
    }
}
