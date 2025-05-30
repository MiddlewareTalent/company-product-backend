package com.accesshr.emsbackend.EmployeeController.CompanyNewsController;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.accesshr.emsbackend.Entity.*;
import com.accesshr.emsbackend.Service.CompanyNewsService.*;
@RestController

@CrossOrigin
@RequestMapping("apis/employees/companyNews")

public class CompanyNewsController {
    @Autowired
    public CompanyNewsService companyNewsService;

    @PostMapping("/addNews")
    public void addNews(@RequestBody CompanyNews companyNews){
        companyNewsService.addNews(companyNews);
    }

    @GetMapping("/getAllNews")
    public List<CompanyNews> getAllNews(){
        return companyNewsService.getAllNews();
    }

    @DeleteMapping("/deleteNewsById/{id}")
    public void deleteNews(@PathVariable int id){
        companyNewsService.deleteNews(id);
    }

}

