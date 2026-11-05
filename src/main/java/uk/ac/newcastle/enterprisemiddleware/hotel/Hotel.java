package uk.ac.newcastle.enterprisemiddleware.hotel;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Hotel {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank 
    @Size(max=49)
    @Pattern(regexp = "^[a-zA-Z]+(?: [a-zA-Z]+)*$")
    String name;

    @NotBlank 
    @Column(unique=true, nullable=false)
    @Size(max=11, min=11)
    @Pattern(regexp = "^0[0-9]{10}$")
    String phoneNumber;
    
    
    @NotBlank 
    @Size(max=6, min=6)
    @Pattern(regexp="^[A-Za-z0-9]*$")
    String postcode;

    public Hotel() {

    };

    public Hotel(String name, String phoneNumber, String postcode) {
        this.name = name;
        this. phoneNumber = phoneNumber;
        this.postcode = postcode;

    }

    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPostcocde() { return postcode; }
    public Long getId() {return id; }


    public void setName(String name) { this.name = name;}
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber;}
    public void setPostcode(String postcode) { this.postcode = postcode;}


}