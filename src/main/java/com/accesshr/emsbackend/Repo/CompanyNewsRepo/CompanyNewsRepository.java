package com.accesshr.emsbackend.Repo.CompanyNewsRepo;


import java.util.*;
import com.accesshr.emsbackend.Entity.*;

public interface CompanyNewsRepository {
    void addNews(CompanyNews companyNews);
    List<CompanyNews> getAllNews();
    void deleteNews(int id);
}

