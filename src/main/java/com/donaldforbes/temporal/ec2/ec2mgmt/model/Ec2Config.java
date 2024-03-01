package com.donaldforbes.temporal.ec2.ec2mgmt.model;

import java.beans.JavaBean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Configuration
@ConfigurationProperties("ec2-config") 
public class Ec2Config {
    public String keyName;
    public String fileName;
    public String groupName ;
    public String groupDesc ;
    public String vpcId ;
    public String myIpAddress;
    public String region;
    public String ami;
    public String instanceType;
    public String temporalTagKey;
    public String temporalTagValue;
}