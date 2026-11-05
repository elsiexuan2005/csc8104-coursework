package uk.ac.newcastle.enterprisemiddleware.customers;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Entity 
public class Customer {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank 
    @Size(max=49)
    @Pattern(regexp = "^[a-zA-Z]+(?: [a-zA-Z]+)*$", message = "Please provide your first and last name")
    private String name;
    
    @Email(message = "Please provide a valid email address")
    @NotBlank 
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank 
    @Size(max=11, min=11)
    @Pattern(
        regexp = "^0[0-9]{10}$",
        message = "Phone number must contain 11 digits and start with 0"
    )
    private String phoneNumber;


    protected Customer() {

    };

    public Customer(String name, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
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