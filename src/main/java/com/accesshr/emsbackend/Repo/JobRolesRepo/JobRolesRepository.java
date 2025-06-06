package com.accesshr.emsbackend.Repo.JobRolesRepo;
import java.util.*;
import com.accesshr.emsbackend.Entity.*;

public interface JobRolesRepository {
    List<JobRoles> getAllJobRoles();
    JobRoles getJobRoleByJobRoleId(int jobRoleId);
    JobRoles addJobRoles(JobRoles jobRoles);
    JobRoles updateJobRoles(int jobRoleId, JobRoles jobRoles);
    void deleteJobRoles(int jobRoleId);
}
