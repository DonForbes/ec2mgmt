package com.donaldforbes.temporal.ec2.ec2mgmt.ec2service;

import java.util.Collection;

import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2Input;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface VMActivities {

    @ActivityMethod
    public String createKeyPair(Ec2Input vmDetail);

    @ActivityMethod
    public String createSecurityGroup(Ec2Input vmDetail);
    
    @ActivityMethod
    public String runInstance(Ec2Input vmDetail);

    @ActivityMethod
    public Collection<String> deleteVMs(Ec2Input vmDetail);

    @ActivityMethod
    public Collection<String> deleteSecurityGroups(Ec2Input vmDetail);

    @ActivityMethod
    public Collection<String> deleteKeyPairs(Ec2Input vmDetail);

}
