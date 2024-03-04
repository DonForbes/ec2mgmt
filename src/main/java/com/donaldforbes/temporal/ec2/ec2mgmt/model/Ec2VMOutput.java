package com.donaldforbes.temporal.ec2.ec2mgmt.model;

import java.util.Collection;
import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ec2VMOutput {

    private Collection<String> vmIdentifiers = new ArrayList<>(); 
    private Collection<String> securityGroups = new ArrayList<>();
    private Collection<String> keyPairNames = new ArrayList<>();

    public String toString()
    {
        return "VM Identifiers <br>[" + vmIdentifiers.toString() + "],<br> securityGroups [" + securityGroups.toString() + "],<br> Key Pairs [" + keyPairNames.toString() + "]";
    }
}
