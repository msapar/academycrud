package kz.academycrud.msapar.controller.rest;
import kz.academycrud.msapar.entity.User;
import kz.academycrud.msapar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/api/student")
public class Student {
    @Autowired
    UserService userService;

    public Student(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{id}")
    public User getUser(@PathVariable int id){
        return userService.getUserById(id);
    }
}
