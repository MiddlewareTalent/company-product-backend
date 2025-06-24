package com.accesshr.emsbackend.EmployeeController.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        String tenantId = request.getHeader("X-Tenant-ID");
        String country=null;

        if (tenantId!=null) {
            int index = tenantId.indexOf("_");
            country = index != -1 ? tenantId.substring(0, index) : tenantId;
        }
        if (tenantId != null && !tenantId.isEmpty()) {
            TenantContext.setTenantId(tenantId);
        }
        if (country != null && !country.isEmpty()) {
            TenantContext.setCountry(country);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
