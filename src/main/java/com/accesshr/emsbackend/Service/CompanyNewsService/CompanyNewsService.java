package com.accesshr.emsbackend.Service.CompanyNewsService;
import org.hibernate.annotations.DialectOverride.OverridesAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.http.HttpStatus;
  import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.accesshr.emsbackend.Entity.*;
import com.accesshr.emsbackend.Repo.CompanyNewsRepo.*;
import java.util.*;
import java.time.LocalDateTime;

  @Service
public class CompanyNewsService implements CompanyNewsRepository  {

    @Autowired
    public CompanyNewsJpaRepository companyNewsJpaRepository;

    // Run every hour
    public void deleteExpiredEntities() {
        LocalDateTime expiryDate = LocalDateTime.now().minusHours(24);
        companyNewsJpaRepository.deleteExpiredEntities(expiryDate);
    }

    @Override
    public void addNews(CompanyNews companyNews){
        companyNewsJpaRepository.save(companyNews);
    }

    @Override 
    public List<CompanyNews> getAllNews(){
        deleteExpiredEntities();
        LocalDateTime expiryDate = LocalDateTime.now();
        System.out.println(expiryDate);
        return (List) companyNewsJpaRepository.findAll();
    }

    @Override 
    public void deleteNews(int id){
        companyNewsJpaRepository.deleteById(id);
    }
    
}

