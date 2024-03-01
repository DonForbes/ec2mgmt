package com.donaldforbes.temporal.ec2.ec2mgmt.ec2service;

import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2Input;
import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2VMOutput;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DeleteVMService {

    @WorkflowMethod
    public Ec2VMOutput deleteAll(Ec2Input vmConfig);
}
