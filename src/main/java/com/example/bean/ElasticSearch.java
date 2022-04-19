package com.example.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "elasticsearch")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElasticSearch {
    String account;
    String passWord;
}
