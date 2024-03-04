package com.donaldforbes.temporal.ec2.ec2mgmt.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ec2Instance {
    private String instanceId;
    private String publicIPAddress;
    private String publicDNSName;
    
    
    public Ec2Instance(String instanceID, String instanceDNSName)
    {
        this.setInstanceId(instanceID);
        this.setPublicDNSName(instanceDNSName);
    }
    public String toString()
    {
        return "<br>" + this.getInstanceId() + " - " + this.getPublicDNSName();
    }
}
