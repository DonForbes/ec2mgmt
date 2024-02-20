package com.donaldforbes.temporal.ec2.ec2mgmt.ec2service;

import com.donaldforbes.temporal.ec2.ec2mgmt.beans.Ec2Config;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.CreateKeyPairResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.ec2.model.AllocateAddressRequest;
import software.amazon.awssdk.services.ec2.model.AllocateAddressResponse;
import software.amazon.awssdk.services.ec2.model.AssociateAddressRequest;
import software.amazon.awssdk.services.ec2.model.AssociateAddressResponse;
import software.amazon.awssdk.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.CreateSecurityGroupResponse;
import software.amazon.awssdk.services.ec2.model.DeleteKeyPairRequest;
import software.amazon.awssdk.services.ec2.model.DeleteSecurityGroupRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;
import software.amazon.awssdk.services.ec2.model.DisassociateAddressRequest;
import software.amazon.awssdk.services.ec2.model.DomainType;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.InstanceTypeInfo;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.IpRange;
import software.amazon.awssdk.services.ec2.model.ReleaseAddressRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.ec2.waiters.Ec2Waiter;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.SsmException;
import software.amazon.awssdk.services.ssm.paginators.GetParametersByPathIterable;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Ec2Service {
    private Ec2Client ec2;
    private SsmClient ssmClient;
    private Ec2Config ec2Configuration;

    public Ec2Service(Ec2Config pEc2Configuration) {
        ec2Configuration = pEc2Configuration;
        Region region = Region.of(ec2Configuration.getRegion());

        ec2 = Ec2Client.builder()
                .region(region)
                .build();

        ssmClient = SsmClient.builder()
                .region(region)
                .build();
    }

    public void createVM() {
        System.out.println("1. Create an RSA key pair and save the private key material as a .pem file.");
        createKeyPair(ec2, ec2Configuration.getKeyName(), ec2Configuration.getFileName());
        System.out.println("Creating a security group based on values provided.");
        String groupId = createSecurityGroup(ec2,
                ec2Configuration.getGroupName(),
                ec2Configuration.getGroupDesc(),
                ec2Configuration.getVpcId(),
                ec2Configuration.getMyIpAddress());

        System.out.println("Created security group [" + groupId + "]");
        System.out.println("Creating an instance.");
        String newInstanceId = runInstance(ec2,
                ec2Configuration.getInstanceType(),
                ec2Configuration.getKeyName(),
                ec2Configuration.getGroupName(),
                ec2Configuration.getAmi());
        System.out.println("Created Instance with id [" + newInstanceId + "]");
    }


    private String runInstance(Ec2Client ec2, String instanceType, String keyName, String groupName,
            String amiId) {
        try {
            RunInstancesRequest runRequest = RunInstancesRequest.builder()
                    .instanceType(instanceType)
                    .keyName(keyName)
                    .securityGroups(groupName)
                    .maxCount(1)
                    .minCount(1)
                    .imageId(amiId)
                    .build();

            RunInstancesResponse response = ec2.runInstances(runRequest);
            String instanceId = response.instances().get(0).instanceId();
            System.out.println("Successfully started EC2 instance " + instanceId + " based on AMI " + amiId);
            return instanceId;

        } catch (SsmException e) {
            System.out.println("SsmException occurred - errored trying to create the instance.");
            System.err.println(e.getMessage());
        }
        return "";
    }

    private String createSecurityGroup(Ec2Client ec2, String groupName, String groupDesc, String vpcId,
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
            System.out.println("Successfully added ingress policy to security group " + groupName);
            return resp.groupId();

        } catch (Ec2Exception e) {
            System.out.println("Exception occurred creating the security group.");
            System.err.println(e.awsErrorDetails().errorMessage());

        }
        return "";
    }

    private void createKeyPair(Ec2Client ec2, String keyName, String fileName) {
        try {
            CreateKeyPairRequest request = CreateKeyPairRequest.builder()
                    .keyName(keyName)
                    .build();

            CreateKeyPairResponse response = ec2.createKeyPair(request);
            String content = response.keyMaterial();
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(content);
            writer.close();
            System.out.println("Successfully created key pair named " + keyName);

        } catch (Ec2Exception | IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
