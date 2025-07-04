package com.cagri.hrms.service.impl;

import com.cagri.hrms.service.PublicApiService;
import org.springframework.stereotype.Service;

@Service
public class PublicApiServiceImpl implements PublicApiService {

    @Override
    public String getHomepageContent() {
        return "Homepage content";
    }

    @Override
    public String getPlatformFeatures() {
        return "Platform features";
    }

    @Override
    public String getHowItWorks() {
        return "How it works description";
    }

    @Override
    public String getHolidays() {
        return "Official holiday list";
    }
}