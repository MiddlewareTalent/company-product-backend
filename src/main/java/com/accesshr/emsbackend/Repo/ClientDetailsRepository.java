package com.accesshr.emsbackend.Repo;

import com.accesshr.emsbackend.Entity.ClientDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ClientDetailsRepository extends JpaRepository<ClientDetails, Long> {
}
