package com.accesshr.emsbackend.Repo.CompanyNewsRepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.accesshr.emsbackend.Entity.CompanyNews;

import java.time.LocalDateTime;

@Repository
public interface CompanyNewsJpaRepository extends JpaRepository<CompanyNews, Integer> {

    @Modifying
    @Transactional
    @Query("DELETE FROM CompanyNews c WHERE c.createdAt < :expiryDate")
    void deleteExpiredEntities(@Param("expiryDate") LocalDateTime expiryDate);
}
