package com.donaldforbes.temporal.ec2.ec2mgmt.beans;

import java.beans.JavaBean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties("ec2-config") 
public class Ec2Config {
    private String keyName;
    private String fileName;
    private String groupName ;
    private String groupDesc ;
    private String vpcId ;
    private String myIpAddress;
    private String region;
    private String ami;
    private String instanceType;
    private String temporalTagKey;
    private String temporalTagValue;
}