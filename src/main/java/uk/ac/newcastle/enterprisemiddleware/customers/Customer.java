package uk.ac.newcastle.enterprisemiddleware.customers;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Entity 
public class Customer {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank 
    @Pattern(regexp = "^[a-zA-Z]+$", message="Please provide your name")
    private String name;
    
    @Email(message = "Please provide a valid email address")
    @NotBlank 
    private String email;

    @NotBlank 
    @Pattern(regexp = "^[0-9]*$", message="Please provide your phone number")
    private String phoneNumber;


    protected Customer() {

    };

    public Customer(String name, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber (String number) {
        this.phoneNumber = number;
    }


}