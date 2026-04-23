package com.smart.campus.api1;

import com.smart.campus.api1.filter.LoggingFilter;
import com.smart.campus.api1.mapper.GlobalExceptionMapper;
import com.smart.campus.api1.mapper.LinkedResourceNotFoundExceptionMapper;
import com.smart.campus.api1.mapper.RoomNotEmptyExceptionMapper;
import com.smart.campus.api1.mapper.SensorUnavailableExceptionMapper;
import com.smart.campus.api1.resources.DiscoveryResource;
import com.smart.campus.api1.resources.RoomResource;
import com.smart.campus.api1.resources.SensorResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api/v1")
public class SmartCampusApp extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // Resources
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);

        // Exception Mappers
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);

        // Filter
        classes.add(LoggingFilter.class);

        return classes;
    }
}