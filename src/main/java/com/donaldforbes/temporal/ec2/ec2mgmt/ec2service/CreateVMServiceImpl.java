package com.donaldforbes.temporal.ec2.ec2mgmt.ec2service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2Config;
import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2Input;
import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2VMOutput;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

@WorkflowImpl(taskQueues = "Ec2DemoTaskQueue")
public class CreateVMServiceImpl implements CreateVMService {

    private Ec2Service ec2Service;
    private VMActivities activity =
      Workflow.newActivityStub(
          VMActivities.class,
          ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(4)).build());

    @Override
    public Ec2VMOutput createVM(Ec2Input vmConfig) {
        if (ec2Service == null)
            ec2Service = new Ec2Service(vmConfig);

        Ec2VMOutput vmOutput = new Ec2VMOutput();
        System.out.println("1. Create an RSA key pair and save the private key material as a .pem file.");

        vmOutput.getKeyPairNames().add(activity.createKeyPair(vmConfig));

        System.out.println("Creating a security group based on values provided.");

        vmOutput.getSecurityGroups().add(activity.createSecurityGroup(vmConfig));

        System.out.println("Creating an instance.");

        vmOutput.getVmIdentifiers().add(activity.runInstance(vmConfig));

        return vmOutput;
    }
}
