package com.donaldforbes.temporal.ec2.ec2mgmt.ec2service;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterResponse;

import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.DeleteKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.DeleteSecurityGroupRequest;

import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;

import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;

import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Instance;

import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.IpRange;
import software.amazon.awssdk.services.ec2.model.ResourceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;

import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TagSpecification;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;
import software.amazon.awssdk.services.ec2.model.Reservation;

import software.amazon.awssdk.services.ssm.model.SsmException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2Config;
import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2Input;
import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2Instance;


import io.temporal.activity.Activity;
import java.util.Collection;


public class Ec2Service  { 
    private Ec2Client ec2;
    private Ec2Config ec2Configuration;
    private static final Logger logger = LoggerFactory.getLogger(Ec2Service.class);


    public Ec2Client getEc2() {
        return this.ec2;
    }
    public Ec2Service(Ec2Config pEc2Configuration) {
        ec2Configuration = pEc2Configuration;
        Region region = Region.of(ec2Configuration.getRegion());
     
        ec2 = Ec2Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public Ec2Service(Ec2Input vmInput) {
        
       this.ec2Configuration = new Ec2Config(); 
       this.ec2Configuration.setAmi(vmInput.ami);
       this.ec2Configuration.setFileName(vmInput.fileName);
       this.ec2Configuration.setGroupDesc(vmInput.groupDesc);
       this.ec2Configuration.setGroupName(vmInput.groupName);
       this.ec2Configuration.setInstanceType(vmInput.instanceType);
       this.ec2Configuration.setKeyName(vmInput.keyName);
       this.ec2Configuration.setMyIpAddress(vmInput.myIpAddress);
       this.ec2Configuration.setRegion(vmInput.region);
       this.ec2Configuration.setTemporalTagKey(vmInput.temporalTagKey);
       this.ec2Configuration.setTemporalTagValue(vmInput.temporalTagValue);
       this.ec2Configuration.setVpcId(vmInput.vpcId);
       Region region = Region.of(ec2Configuration.getRegion());

       ec2 = Ec2Client.builder()
               .region(region)
               .credentialsProvider(DefaultCredentialsProvider.create())
               .build();
    }

    public List<Ec2Instance> getDemoInstanceIds(Ec2Client ec2)
    {
        List<Ec2Instance> instancesList = new ArrayList<>();

        Filter myFilter = Filter.builder()
                                 .name("tag:"+ec2Configuration.getTemporalTagKey())
                                 .values(this.ec2Configuration.getTemporalTagValue())
                                 .build();

        DescribeInstancesRequest instanceRequest = DescribeInstancesRequest.builder()
                                                        .filters(myFilter) 
                                                        .build();

        DescribeInstancesResponse response = ec2.describeInstances(instanceRequest);
        if (response.hasReservations())
        {
            logger.debug("Believe we have some reservations - {}", response.reservations().size());
            List<Reservation> reservations = response.reservations();
            for (Reservation reservation : reservations)
            {

                List<Instance> instances = reservation.instances();
        
                for (Instance instance : instances)
                {
                    logger.debug("Found instance ID: {}", instance.instanceId());
                    if (instance.state().code() == 48) // Terminated.
                        {
                            logger.debug("Instance [{}] has been terminated, not returning.", instance.instanceId());
                        }
                    else
                    {
                        instancesList.add(new Ec2Instance(instance.instanceId(),instance.publicDnsName()));
                    }
                }
            logger.debug("The instances found are [{}]", instancesList);
            }
       }
       else 
        logger.debug("No instances found matching filter.");

        return instancesList;
    }

    public void terminateEC2(Ec2Client ec2, String instanceId) {
        try {
            Ec2Waiter ec2Waiter = Ec2Waiter.builder()
                    .overrideConfiguration(b -> b.maxAttempts(100))
                    .client(ec2)
                    .build();

            TerminateInstancesRequest ti = TerminateInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();

            logger.debug("Using an Ec2Waiter to wait for the instance to terminate. This will take a few minutes.");
            ec2.terminateInstances(ti);
            DescribeInstancesRequest instanceRequest = DescribeInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();

            WaiterResponse<DescribeInstancesResponse> waiterResponse = ec2Waiter
                    .waitUntilInstanceTerminated(instanceRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);
            logger.info("{} is terminated!", instanceId);

        } catch (Ec2Exception e) {
            logger.error("Exception occurred:- {}", e.awsErrorDetails().errorMessage());
        }
    }

    public String runInstance(Ec2Client ec2, String instanceType, String keyName, String groupName,
            String amiId) {
        try {
            Collection<Tag> myTags = new ArrayList<>();
            myTags.add(Tag.builder()
                    .key(this.ec2Configuration.getTemporalTagKey())
                    .value(this.ec2Configuration.getTemporalTagValue())
                    .build());

            TagSpecification myTagSpec = TagSpecification.builder()
                    .tags(myTags)
                    .resourceType(ResourceType.INSTANCE)
                    .build();

            logger.debug("Tags: [{}]", myTagSpec.tags());

            RunInstancesRequest runRequest = RunInstancesRequest.builder()
                    .instanceType(instanceType)
                    .keyName(keyName)
                    .securityGroups(groupName)
                    .maxCount(1)
                    .minCount(1)
                    .imageId(amiId)
                    .tagSpecifications(myTagSpec)
                    .build();

            RunInstancesResponse response = ec2.runInstances(runRequest);
            String instanceId = response.instances().get(0).instanceId();
            logger.debug("Successfully started EC2 instance [{}], based on AMI [{}]", instanceId,  amiId);
            String publicIPAddresses = "Unknown";
            if (response.hasInstances())
                {
                    publicIPAddresses = response.instances().get(0).publicIpAddress();
                }

            return instanceId + "[" + publicIPAddresses + "]";

        } catch (SsmException e) {
            logger.error("SsmException occurred - errored trying to create the instance. [{}]", e.getMessage());
        }
        return "";
    }


    public List<String> getSecurityGroupsByGroupName(Ec2Client ec2, String groupName)
    {
        List<String> groupIds = new ArrayList<>();
        try {

            Filter groupNameFilter = Filter.builder()
                                    .name("group-name")
                                    .values(groupName)
                                    .build();

            DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder()
                                                    .filters(groupNameFilter)
                                                    .build();

            DescribeSecurityGroupsResponse response = ec2.describeSecurityGroups(request);
            for (SecurityGroup group : response.securityGroups()) {
                groupIds.add(group.groupId());
                logger.debug("Found Security Group with Id [{}] and group VPC [{}]", 
                                                    group.groupId(),  
                                                    group.vpcId());
            }
        }

        catch (Ec2Exception e) {
            logger.error("Unable to find security groups - {}", e.awsErrorDetails().errorMessage());
        }

        return groupIds;

    } //End getSecurityGroupsByGroupName
    public void deleteSecurityGroup(Ec2Client ec2, String groupId)
    {
        try {
            DeleteSecurityGroupRequest request = DeleteSecurityGroupRequest.builder()
                    .groupId(groupId)
                    .build();

            ec2.deleteSecurityGroup(request);
            logger.debug("Successfully deleted security group with Id {}", groupId);

        } catch (Ec2Exception e) {
            logger.error("Failed to delete security group {}", e.awsErrorDetails().errorMessage());           
        }

    } //End deleteSecurityGroup

    public String createSecurityGroup(Ec2Client ec2, String groupName, String groupDesc, String vpcId,
            String myIpAddress) {
        try {
            CreateSecurityGroupRequest createRequest = CreateSecurityGroupRequest.builder()
                    .groupName(groupName)
                    .description(groupDesc)
                    .vpcId(vpcId)
                    .build();

            CreateSecurityGroupResponse resp = ec2.createSecurityGroup(createRequest);
            IpRange ipRange = IpRange.builder()
                    .cidrIp(myIpAddress + "/0")
                    .build();

            IpPermission ipPerm = IpPermission.builder()
                    .ipProtocol("tcp")
                    .toPort(80)
                    .fromPort(80)
                    .ipRanges(ipRange)
                    .build();

            IpPermission ipPerm2 = IpPermission.builder()
                    .ipProtocol("tcp")
                    .toPort(22)
                    .fromPort(22)
                    .ipRanges(ipRange)
                    .build();

            AuthorizeSecurityGroupIngressRequest authRequest = AuthorizeSecurityGroupIngressRequest.builder()
                    .groupName(groupName)
                    .ipPermissions(ipPerm, ipPerm2)
                    .build();

            ec2.authorizeSecurityGroupIngress(authRequest);
            logger.debug("Successfully added ingress policy to security group {}", groupName);
            return resp.groupId();

        } catch (Ec2Exception e) {
            logger.error("Exception occurred creating the security group - {}", e.getMessage());
            logger.error("EC2 specific error - {}", e.awsErrorDetails().errorMessage());
            
            if (e.awsErrorDetails().errorCode().equalsIgnoreCase("InvalidGroup.Duplicate"))
                {
                    logger.debug("The error code returned was : {}.  As this is a duplicate assuming success.", e.awsErrorDetails().errorCode());
                    return groupName;
                }
        }
        return "";
    }

    public String createKeyPair(Ec2Client ec2, String keyName, String fileName) {
        try {
            CreateKeyPairRequest request = CreateKeyPairRequest.builder()
                    .keyName(keyName)
                    .build();

            CreateKeyPairResponse response = ec2.createKeyPair(request);
            String content = response.keyMaterial();
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(content);
            writer.close();
            logger.debug("Successfully created key pair named {}", keyName);

            return keyName + " - " + fileName;

        } catch (Ec2Exception | IOException e) {
            logger.error("Failed to create keypair called - {}", keyName);
            if (e.getMessage().startsWith("The keypair already exists"))
                return keyName + " - Already Exists. Check you have the secret key.";
            else
                throw Activity.wrap(e);
        }
    } //End createKeyPair

    public void deleteKeys(Ec2Client ec2, String keyPair) {
        try {
            DeleteKeyPairRequest request = DeleteKeyPairRequest.builder()
                    .keyName(keyPair)
                    .build();

            ec2.deleteKeyPair(request);
            logger.debug("Successfully deleted key pair named [{}]", keyPair);

        } catch (Ec2Exception e) {
            logger.error("Unable to delete key pair - {}", e.awsErrorDetails().errorMessage());
            
    }
}
}