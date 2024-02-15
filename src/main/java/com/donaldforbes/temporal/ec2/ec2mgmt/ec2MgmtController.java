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


@Controller
public class ec2MgmtController {
    @GetMapping("hello")
    public String hello(Model model){
        model.addAttribute("sample", "Example Hello");
        return "hello";
    }
    @PostMapping(value="/hello")
    public ResponseEntity<String> helloPost(@RequestBody String body) {
        return new ResponseEntity<>("\"" + "Don" + "\"", HttpStatus.OK);
    }

}
