package com.donaldforbes.temporal.ec2.ec2mgmt;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.donaldforbes.temporal.ec2.ec2mgmt.ec2service.CreateVMService;
import com.donaldforbes.temporal.ec2.ec2mgmt.ec2service.DeleteVMService;
import com.donaldforbes.temporal.ec2.ec2mgmt.ec2service.Ec2Service;
import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2Config;
import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2Input;
import com.donaldforbes.temporal.ec2.ec2mgmt.model.Ec2VMOutput;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import software.amazon.awssdk.regions.Region;


@Controller
public class ec2MgmtController {
    @Autowired 
    private Ec2Config ec2Configuration;
    @Autowired WorkflowClient client;
    
    @GetMapping("hello")
    public String hello(Model model){
        model.addAttribute("sample", "Example Hello");
        return "hello";
    }

    @PostMapping(value="/ec2-createvm")
    public ResponseEntity<String> ec2PostCreate(@RequestBody String body) {
        System.out.println("Creating the VM.");
        System.out.println(body);

        CreateVMService workflow =
        client.newWorkflowStub(
            CreateVMService.class,
            WorkflowOptions.newBuilder()
                .setTaskQueue("Ec2DemoTaskQueue")
                .setWorkflowId("CreateVMWorkflow")
                .build());
                
        System.out.println("**** config value -" + ec2Configuration.getGroupName());
        Ec2Input vmInput = new Ec2Input(ec2Configuration);
        Ec2VMOutput vmDetails = workflow.createVM(vmInput);

        return new ResponseEntity<>("\"Created VM - " + vmDetails.toString() + "\"", HttpStatus.OK);
    }
 
    @PostMapping(value="/ec2-queryvms")
    public ResponseEntity<String> ec2PostQueryVM(@RequestBody String body)
    {
        Ec2Input vmInput = new Ec2Input(ec2Configuration);
        Ec2Service theEC2Service = new Ec2Service(ec2Configuration);
        Collection<String> instanceIds = theEC2Service.getDemoInstanceIds(theEC2Service.getEc2());
        System.out.println("There are [" + instanceIds.size() + "] instances found.");
        return new ResponseEntity<>("\"Matching VMs - " + instanceIds.toString() + "\"", HttpStatus.OK);
    } //End ec2 Post query VM

    @PostMapping(value="/ec2-deletevm")
    public ResponseEntity<String> ec2PostDelete(@RequestBody String body) {
        System.out.println("Deletiung the VM.");
        System.out.println(body);

        DeleteVMService workflow =
            client.newWorkflowStub(
                DeleteVMService.class,
                WorkflowOptions.newBuilder()
                    .setTaskQueue("Ec2DemoTaskQueue")
                    .setWorkflowId("DeleteVMWorkflow")
                    .build());

        Ec2Input vmInput = new Ec2Input(ec2Configuration);
        Ec2VMOutput vmDetails = workflow.deleteAll(vmInput);

        return new ResponseEntity<>("\"Deleted Objects - " + vmDetails.toString() + "\"", HttpStatus.OK);
    }

    @GetMapping("ec2")
    public String ec2(Model model){
        model.addAttribute("sample", "VM  Instance Management");
        model.addAttribute("ec2Config", ec2Configuration);
        return "ec2";
    }
}
