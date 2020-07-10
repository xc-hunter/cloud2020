package com.xc.springclod.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class ConfigClirntController {
    @Value("${server.port}")
    private String serverpost;
    @Value("${config.info}")
    private String configinfo;
    @GetMapping("/configinfo")
    public String getInfo(){
        return "serverport:::"+serverpost+"\t\n\nconfiginfo:::"+configinfo;
    }
}
