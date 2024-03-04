package com.donaldforbes.temporal.ec2.ec2mgmt.ec2service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2Input;

import io.temporal.spring.boot.ActivityImpl;

@Component
@ActivityImpl(taskQueues = "Ec2DemoTaskQueue")
public class VMActivitiesImpl implements VMActivities {
    private static final Logger logger = LoggerFactory.getLogger(VMActivitiesImpl.class);

    @Override
    public String createKeyPair(Ec2Input vmDetail) {

        Ec2Service ec2Service = this.getEc2Service(vmDetail);
        return ec2Service.createKeyPair(ec2Service.getEc2(), vmDetail.keyName, vmDetail.fileName);
    }

    @Override
    public String createSecurityGroup(Ec2Input vmDetail) {
       
        Ec2Service ec2Service = this.getEc2Service(vmDetail);

       return ec2Service.createSecurityGroup(ec2Service.getEc2(),
                    vmDetail.groupName,
                    vmDetail.groupDesc,
                    vmDetail.vpcId,
                    vmDetail.myIpAddress);
    }

    @Override
    public String runInstance(Ec2Input vmDetail) {
       Ec2Service ec2Service = this.getEc2Service(vmDetail);
       return ec2Service.runInstance(ec2Service.getEc2(),
                 vmDetail.instanceType,
                 vmDetail.keyName,
                 vmDetail.groupName,
                 vmDetail.ami);
    }

    @Override
    public Collection<String> deleteVMs(Ec2Input vmDetail) {
        Ec2Service ec2Service = this.getEc2Service(vmDetail);

        Collection<String> vmIdentifiers = new ArrayList<String>();
       logger.debug("Deleting all VMs with Temporal Demo tags and associated resources.");
        
        List<String> instanceIds = ec2Service.getDemoInstanceIds(ec2Service.getEc2());
        for (String instanceId : instanceIds) {
            logger.debug("Deleting instance ",instanceId);
            ec2Service.terminateEC2(ec2Service.getEc2(), instanceId);
            vmIdentifiers.add(instanceId);
        }

        return vmIdentifiers;
    }

    @Override
    public Collection<String> deleteSecurityGroups(Ec2Input vmDetail) {
        Ec2Service ec2Service = this.getEc2Service(vmDetail); 

        Collection<String> deletedGroups = new ArrayList<String>();
        Collection<String> groupIds = ec2Service.getSecurityGroupsByGroupName(ec2Service.getEc2(), vmDetail.groupName);
        for (String groupID : groupIds)
        {
            ec2Service.deleteSecurityGroup(ec2Service.getEc2(), groupID);
            deletedGroups.add(groupID);
        }

        return deletedGroups;
    }

    @Override
    public Collection<String> deleteKeyPairs(Ec2Input vmDetail) {
        Ec2Service ec2Service = this.getEc2Service(vmDetail);

        Collection<String> keyPairs = new ArrayList<String>();
        ec2Service.deleteKeys(ec2Service.getEc2(), vmDetail.keyName);
        keyPairs.add(vmDetail.keyName);
        
        return keyPairs;
    }

    private Ec2Service getEc2Service(Ec2Input vmDetail) {
        return new Ec2Service(vmDetail);
    }
}