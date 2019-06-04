package com.cccmbiz.web.primefaces;

import com.cccmbiz.web.Person;
import com.cccmbiz.web.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;

@Named
public class HelloWorld {

    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private PersonRepository personRepository;

    private String firstName = "John";
    private String lastName = "Doe";

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String showGreeting() {

        Iterable<Person> result = personRepository.findAll(); //result returned from some method.
        for(Person p: result){
            System.out.println(p.getFirstName());
        }

        return "Hello " + firstName + " " + lastName + "!";


    }
}