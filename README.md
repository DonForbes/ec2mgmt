# Demo app to show provisioning of AWS resource via Temporal Workflow

This is a simple spring boot application to demonstrate the usage of temporal to run a workflow which will provision a keypair, security group and VM from a springboot based application.

To run locally:-
1. Deploy the the temporal dev workflow management system.  (Install temporal on your platform first - https://learn.temporal.io/getting_started/typescript/dev_environment/)
``` 
$ temporal server start-dev
```
2. clone the repository locally. - 
```
$ git clone https://github.com/DonForbes/ec2mgmt.git
```
3. Run the spring boot app from the root directory of the repostitory.  (Ensuring you have authenticated to an AWS environment and have the permissions to create keypairs, security groups, VMs etc.)
```
$ ./gradlew bootRun
```