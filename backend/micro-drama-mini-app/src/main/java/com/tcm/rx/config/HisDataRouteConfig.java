package com.tcm.rx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "his.data")
public class HisDataRouteConfig {

    private Map<String,String> route = new HashMap<>();


}
