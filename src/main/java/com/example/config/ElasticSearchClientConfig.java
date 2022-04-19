package com.example.config;


import com.example.bean.ElasticSearch;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

@Configuration
public class ElasticSearchClientConfig {


//    @Bean
//    public RestHighLevelClient restHighLevelClient() {
//        RestHighLevelClient client = new RestHighLevelClient(
//                RestClient.builder(new HttpHost("localhost", 9200, "http"), new HttpHost("localhost", 19200, "http")));
//        return  client;
//
//    }

    @Autowired
    ElasticSearch elasticSearch;

    /**
     * 增加账号和密码后，需要重新配置restHighLevelClient
     * @return
     */

    @Bean(name = "restHighLevelClient")
        public RestHighLevelClient transportClient() {


            final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                    .connectedTo("localhost"+":"+9200)
                    .withBasicAuth(elasticSearch.getAccount(), elasticSearch.getPassWord())
                    .build();
            return RestClients.create(clientConfiguration).rest();


    }

}
