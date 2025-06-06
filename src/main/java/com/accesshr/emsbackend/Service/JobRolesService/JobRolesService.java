package com.accesshr.emsbackend.Service.JobRolesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import com.accesshr.emsbackend.Entity.*;
import com.accesshr.emsbackend.Repo.JobRolesRepo.*;

@Service
public class JobRolesService implements JobRolesRepository {

    @Autowired
    JobRolesJpaRepository jobRolesRepository;

    @Override
    public List<JobRoles> getAllJobRoles() {
        return jobRolesRepository.findAll();
    }

    @Override
    public JobRoles getJobRoleByJobRoleId(int jobRoleId) {
        return jobRolesRepository.findById(jobRoleId).orElse(null);
    }

    @Override
    public JobRoles addJobRoles(JobRoles jobRoles) {
        return jobRolesRepository.save(jobRoles);
    }

    @Override
    public JobRoles updateJobRoles(int jobRoleId, JobRoles jobRoles) {
        JobRoles newJobRoles = jobRolesRepository.findById(jobRoleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "JobRole not found"));

        newJobRoles.setJobRole(jobRoles.getJobRole());
        return jobRolesRepository.save(newJobRoles);
    }

    @Override
    public void deleteJobRoles(int jobRoleId) {
        if (!jobRolesRepository.existsById(jobRoleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "JobRole not found");
        }
        jobRolesRepository.deleteById(jobRoleId);
    }
}

