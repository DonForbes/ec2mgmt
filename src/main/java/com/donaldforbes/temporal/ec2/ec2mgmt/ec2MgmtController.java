package com.donaldforbes.temporal.ec2.ec2mgmt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.donaldforbes.temporal.ec2.ec2mgmt.beans.Ec2Config;
import com.donaldforbes.temporal.ec2.ec2mgmt.ec2service.Ec2Service;

import software.amazon.awssdk.regions.Region;


@Controller
public class ec2MgmtController {
    @Autowired
    private Ec2Config ec2Configuration;

    @GetMapping("hello")
    public String hello(Model model){
        model.addAttribute("sample", "Example Hello");
        return "hello";
    }
    
    @PostMapping(value="/ec2-createvm")
    public ResponseEntity<String> ec2PostCreate(@RequestBody String body) {
        System.out.println("Creating the VM.");
        System.out.println(body);

        Ec2Service myEc2Service = new Ec2Service(ec2Configuration);
        //myEc2Service.createVM();
        return new ResponseEntity<>("\"" + "Don - CreateVM" + "\"", HttpStatus.OK);
    }
    @PostMapping(value="/ec2-deletevm")
    public ResponseEntity<String> ec2PostDelete(@RequestBody String body) {
        System.out.println("Deletiung the VM.");
        System.out.println(body);

        Ec2Service myEc2Service = new Ec2Service(ec2Configuration);
        //myEc2Service.createVM();
        return new ResponseEntity<>("\"" + "Don - delete VM" + "\"", HttpStatus.OK);
    }

    @GetMapping("ec2")
    public String ec2(Model model){
        model.addAttribute("sample", "VM  Instance creation");
       // Ec2Config myConfig = new Ec2Config();
        model.addAttribute("ec2Config", ec2Configuration);
        return "ec2";
    }
}
