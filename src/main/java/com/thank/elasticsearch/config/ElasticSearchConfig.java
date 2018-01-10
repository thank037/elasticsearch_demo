package com.thank.elasticsearch.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * description: ElasticSearch 配置
 *
 * @author thank
 * 2018/1/10 10:09
 */
@Configuration
public class ElasticSearchConfig {

    /** 集群host */
    @Value("${spring.data.elasticsearch.cluster-nodes}")
    private String clusterNodes;

    /** 集群名称 */
    @Value("${spring.data.elasticsearch.cluster-name}")
    private String clusterName;



    @Bean
    public TransportClient client() throws UnknownHostException{

        InetSocketTransportAddress node = new InetSocketTransportAddress(
                InetAddress.getByName(clusterNodes), 9300
        );

        Settings settings = Settings.builder().put("cluster.name", clusterName).build();

        TransportClient client = new PreBuiltTransportClient(settings);
        client.addTransportAddress(node);
        return client;
    }
}
