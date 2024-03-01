package com.donaldforbes.temporal.ec2.ec2mgmt.model;

public class Ec2Input {
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

    public Ec2Input() {}
    
    public Ec2Input(Ec2Config vmConfig)
    {
        keyName = vmConfig.getKeyName();
        fileName = vmConfig.getFileName();
        groupName = vmConfig.getGroupName();
        groupDesc = vmConfig.getGroupDesc();
        vpcId = vmConfig.getVpcId();
        myIpAddress = vmConfig.getMyIpAddress();
        region = vmConfig.getRegion();
        ami = vmConfig.getAmi();
        instanceType = vmConfig.getInstanceType();
        temporalTagKey = vmConfig.getTemporalTagKey();
        temporalTagValue = vmConfig.getTemporalTagValue();
    }
}