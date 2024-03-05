package com.donaldforbes.temporal.ec2.ec2mgmt.ec2service;

import java.time.Duration;

import org.slf4j.Logger;

import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2Input;
import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2VMOutput;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;

@WorkflowImpl(taskQueues = "Ec2DemoTaskQueue")
public class DeleteVMServiceImpl implements DeleteVMService {

    public static final Logger logger = Workflow.getLogger(DeleteVMServiceImpl.class);
    private Ec2Service ec2Service;
    private VMActivities activity =
      Workflow.newActivityStub(
          VMActivities.class,
          ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(30)).build());

    @Override
    public Ec2VMOutput deleteAll(Ec2Input vmConfig) {
        if (ec2Service == null)
            ec2Service = new Ec2Service(vmConfig);

        Ec2VMOutput deletedVMOutput = new Ec2VMOutput();

        logger.debug("Deleting VMs taged for Temporal Demo [{}]", vmConfig.temporalTagKey);
        deletedVMOutput.setVmIdentifiers(activity.deleteVMs(vmConfig));

        logger.debug("Deleting Temporal security group[{}]", vmConfig.groupName);
        deletedVMOutput.setSecurityGroups(activity.deleteSecurityGroups(vmConfig));

        logger.debug("Deleting Key Pair[{}]", vmConfig.keyName);
        deletedVMOutput.setKeyPairNames(activity.deleteKeyPairs(vmConfig));

        return deletedVMOutput;

    }

}
