package kz.academycrud.msapar.controller;

import kz.academycrud.msapar.entity.RequestResponse;
import kz.academycrud.msapar.entity.User;
import kz.academycrud.msapar.service.UserService;
import kz.academycrud.msapar.util.FormValidationUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.LinkOption;
import java.util.List;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {

    @Autowired
    UserService userService;

    @RequestMapping(value="/home", method = RequestMethod.GET)
    public ModelAndView home(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("title", "Home");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            modelAndView.addObject("user_email", auth.getName());
            List<User> users = userService.findUserByEmail(auth.getName());
            modelAndView.setViewName("admin/home");
            if (users.size() == 1){
                modelAndView.addObject("full_name", users.get(0).getFirstName() + " " + users.get(0).getLastName());
            }
            modelAndView.addObject("teachers_count", userService.getUsersCount("TEACHER"));
            modelAndView.addObject("students_count", userService.getUsersCount("STUDENT"));
        } else {
            LoggerFactory.getLogger(LoggerFactory.class).error("AUTH ERROR :: Auth is null");
        }
        return modelAndView;
    }

    @RequestMapping(value="/teachers", method = RequestMethod.GET)
    public ModelAndView teachers(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("title", "Teachers");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            List<User> teachers = userService.getUsersByRoleNotLoggedIn("TEACHER", auth.getName());
            LoggerFactory.getLogger(LinkOption.class).info("GET TEACHERS DATA :: " + teachers.toString());
            modelAndView.addObject("teachers", teachers);
        } else {
            LoggerFactory.getLogger(LoggerFactory.class).error("AUTH ERROR :: Auth is null");
        }
        modelAndView.setViewName("admin/teachers");
        return modelAndView;
    }

    @RequestMapping(value="/students", method = RequestMethod.GET)
    public ModelAndView students(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("title", "Students");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            List<User> students = userService.getUsersByRoleNotLoggedIn("STUDENT", auth.getName());
            LoggerFactory.getLogger(LinkOption.class).info("GET STUDENTS DATA :: " + students.toString());
            modelAndView.addObject("students", students);
        } else {
            LoggerFactory.getLogger(LoggerFactory.class).error("AUTH ERROR :: Auth is null");
        }
        modelAndView.setViewName("admin/students");
        return modelAndView;
    }

    @RequestMapping(value = "/del_user", method = RequestMethod.GET)
    public String delAccount(@RequestParam(value = "i") String id){
        User user = User.builder().id(Long.valueOf(id)).build();

        RequestResponse requestResponse = userService.delUserbyid(user);

        return "redirect:/admin/teachers";

    }

    @RequestMapping(value = "/del_stud", method = RequestMethod.GET)
    public String delAccountStud(@RequestParam(value = "i") String id){
        User user = User.builder().id(Long.valueOf(id)).build();

        RequestResponse requestResponse = userService.delUserbyid(user);

        return "redirect:/admin/students";

    }

    @RequestMapping(value="/students/edit", method = RequestMethod.GET)
    public ModelAndView editStudent(@RequestParam(value = "k") String key){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("title", "Edit student");
        Authentication authy = SecurityContextHolder.getContext().getAuthentication();
        if (authy != null){
            LoggerFactory.getLogger(LinkOption.class).info("GET STUDENTS DATA :: " + userService.getUsersByRoleNotLoggedIn("STUDENT", authy.getName()));
            modelAndView.addObject("students", userService.getUsersByRoleNotLoggedIn("STUDENT", authy.getName()));
        } else {
            LoggerFactory.getLogger(LoggerFactory.class).error("AUTH ERROR :: Auth is null");
        }
        User user = userService.getUserById(Long.valueOf(key));
        modelAndView.addObject("edit_data", user);
        modelAndView.setViewName("admin/edit_student");
        return modelAndView;
    }

    @RequestMapping(value="/teachers/edit", method = RequestMethod.GET)
    public ModelAndView editTeacher(@RequestParam(value = "k") String key){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("title", "Edit teacher");
        Authentication authy = SecurityContextHolder.getContext().getAuthentication();
        if (authy != null){
            LoggerFactory.getLogger(LinkOption.class).info("GET TEACHER DATA :: " + userService.getUsersByRoleNotLoggedIn("TEACHER", authy.getName()));
            modelAndView.addObject("teachers", userService.getUsersByRoleNotLoggedIn("TEACHER", authy.getName()));
        } else {
            LoggerFactory.getLogger(LoggerFactory.class).error("AUTH ERROR :: Auth is null");
        }
        User user = userService.getUserById(Long.valueOf(key));
        modelAndView.addObject("edit_data", user);
        modelAndView.setViewName("admin/edit_teacher");
        return modelAndView;
    }

    @RequestMapping(value = "/update_user", method = RequestMethod.POST)
    public String updateUser(@RequestParam(value = "fname") String userFirstName, @RequestParam(value = "lname") String userLastName, @RequestParam(value = "email") String userEmail, @RequestParam("t") String userType, @RequestParam("i") String id, RedirectAttributes redirectAttributes){
        FormValidationUtil formValidationUtil = new FormValidationUtil();
        boolean suc = false;
        if (!formValidationUtil.isEmptyInput(userFirstName)){
            if (!formValidationUtil.isEmptyInput(userLastName)){
                if (!formValidationUtil.isEmptyInput(userLastName)){
                    User user = User.builder().firstName(userFirstName).lastName(userLastName).email(userEmail).id(Long.valueOf(id)).build();
                    RequestResponse requestResponse = userService.updateUserDetails(user);
                    if (requestResponse.getResponseCode() == 0){
                        redirectAttributes.addFlashAttribute("success", true);
                        suc = true;
                        redirectAttributes.addFlashAttribute("message", "<i class=\"fa fa-check-circle\"></i>&nbsp;User details successfully updated");
                    } else {
                        redirectAttributes.addFlashAttribute("success", false);
                        redirectAttributes.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp;" + requestResponse.getMessage());
                        redirectAttributes.addFlashAttribute("f", userFirstName);
                        redirectAttributes.addFlashAttribute("l", userLastName);
                        redirectAttributes.addFlashAttribute("e", userEmail);
                    }
                } else {
                    redirectAttributes.addFlashAttribute("success", false);
                    redirectAttributes.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp; Email address is required");
                    redirectAttributes.addFlashAttribute("f", userFirstName);
                    redirectAttributes.addFlashAttribute("l", userLastName);
                    redirectAttributes.addFlashAttribute("e", userEmail);
                }
            } else {
                redirectAttributes.addFlashAttribute("success", false);
                redirectAttributes.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp; Last name is required");
                redirectAttributes.addFlashAttribute("f", userFirstName);
                redirectAttributes.addFlashAttribute("l", userLastName);
                redirectAttributes.addFlashAttribute("e", userEmail);
            }
        } else {
            redirectAttributes.addFlashAttribute("success", false);
            redirectAttributes.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp; First name is required");
            redirectAttributes.addFlashAttribute("f", userFirstName);
            redirectAttributes.addFlashAttribute("l", userLastName);
            redirectAttributes.addFlashAttribute("e", userEmail);
        }
        switch (userType) {
            case "STUDENT":
                if (suc){
                    return "redirect:/admin/students";
                } else {
                    return "redirect:/admin/students/edit?k=" + id + "#edit";
                }
            case "TEACHER":
                if (suc){
                    return "redirect:/admin/teachers";
                } else {
                    return "redirect:/admin/teachers/edit?k=" + id + "#edit";
                }
            default:
                return "redirect:/logout";
        }
    }



    @RequestMapping(value = "/new_student", method = RequestMethod.POST)
    public String newStudent(@RequestParam("fname") String studentFirstName, @RequestParam("lname") String studentLastName, @RequestParam("email") String studentEmail, @RequestParam("password") String studentPassword, RedirectAttributes redirectAttributes){
        FormValidationUtil formValidationUtil = new FormValidationUtil();
        if (!formValidationUtil.isEmptyInput(studentFirstName)){
            if (!formValidationUtil.isEmptyInput(studentLastName)){
                if (!formValidationUtil.isEmptyInput(studentEmail)){
                    if(!formValidationUtil.isEmptyInput(studentPassword)){
                        studentFirstName = studentFirstName.trim();
                        studentLastName = studentLastName.trim();
                        studentEmail = studentEmail.trim();
                        studentPassword = studentPassword.trim();
                        User student = User.builder().firstName(studentFirstName.toLowerCase()).lastName(studentLastName.toLowerCase()).email(studentEmail).password(studentPassword).role("STUDENT").build();
                        LoggerFactory.getLogger(LoggerFactory.class).info("USER INPUT :: NEW STUDENT DATA --- " + student.toString());
                        RequestResponse requestResponse = userService.saveUser(student);
                        LoggerFactory.getLogger(LoggerFactory.class).info("NEW STUDENT :: RESP --- " + requestResponse.toString());
                        if (requestResponse.getResponseCode() == 0){
                            redirectAttributes.addFlashAttribute("success", true);
                            redirectAttributes.addFlashAttribute("message", "<i class=\"fa fa-check-circle\"></i>&nbsp;Student successfully added. Student's first time login password is <b>" + studentEmail + "</b>");
                        } else {
                            redirectAttributes.addFlashAttribute("success", false);
                            redirectAttributes.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp;" + requestResponse.getMessage());
                            redirectAttributes.addFlashAttribute("fname", studentFirstName);
                            redirectAttributes.addFlashAttribute("lname", studentLastName);
                            redirectAttributes.addFlashAttribute("email", studentEmail);
                            redirectAttributes.addFlashAttribute("password", studentPassword);
                        }
                    } else {
                        redirectAttributes.addFlashAttribute("success", false);
                        redirectAttributes.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp; Students password is required");
                        redirectAttributes.addFlashAttribute("fname", studentFirstName);
                        redirectAttributes.addFlashAttribute("lname", studentLastName);
                        redirectAttributes.addFlashAttribute("email", studentEmail);
                        redirectAttributes.addFlashAttribute("password", studentPassword);
                    }
                } else {
                    redirectAttributes.addFlashAttribute("success", false);
                    redirectAttributes.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp; Students email address is required");
                    redirectAttributes.addFlashAttribute("fname", studentFirstName);
                    redirectAttributes.addFlashAttribute("lname", studentLastName);
                    redirectAttributes.addFlashAttribute("email", studentEmail);
                    redirectAttributes.addFlashAttribute("password", studentPassword);
                }
            } else {
                redirectAttributes.addFlashAttribute("success", false);
                redirectAttributes.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp; Students last name is required");
                redirectAttributes.addFlashAttribute("fname", studentFirstName);
                redirectAttributes.addFlashAttribute("lname", studentLastName);
                redirectAttributes.addFlashAttribute("email", studentEmail);
                redirectAttributes.addFlashAttribute("password", studentPassword);
            }
        } else {
            redirectAttributes.addFlashAttribute("success", false);
            redirectAttributes.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp; Students first name is required");
            redirectAttributes.addFlashAttribute("fname", studentFirstName);
            redirectAttributes.addFlashAttribute("lname", studentLastName);
            redirectAttributes.addFlashAttribute("email", studentEmail);
            redirectAttributes.addFlashAttribute("password", studentPassword);
        }
        return "redirect:/admin/students";
    }

    @RequestMapping(value = "/new_teacher", method = RequestMethod.POST)
    public String newTeacher(@RequestParam("tfname") String teacherFirstName, @RequestParam("tlname") String teacherLastName, @RequestParam("temail") String teacherEmail,@RequestParam("tpassword") String teacherPassword, RedirectAttributes redirectAtt){
        FormValidationUtil formValidationUtil = new FormValidationUtil();
        if (!formValidationUtil.isEmptyInput(teacherFirstName)) {
            if (!formValidationUtil.isEmptyInput(teacherLastName)) {
                if (!formValidationUtil.isEmptyInput(teacherEmail)) {
                    if (!formValidationUtil.isEmptyInput(teacherPassword)) {
                        teacherFirstName = teacherFirstName.trim();
                        teacherLastName = teacherLastName.trim();
                        teacherEmail = teacherEmail.trim();
                        teacherPassword = teacherPassword.trim();
                        User student = User.builder().firstName(teacherFirstName.toLowerCase()).lastName(teacherLastName.toLowerCase()).email(teacherEmail).password(teacherPassword).role("TEACHER").build();
                        LoggerFactory.getLogger(LoggerFactory.class).info("USER INPUT :: NEW TEACHER DATA --- " + student.toString());
                        RequestResponse requestResponse = userService.saveUser(student);
                        LoggerFactory.getLogger(LoggerFactory.class).info("NEW TEACHER :: RESP --- " + requestResponse.toString());
                        if (requestResponse.getResponseCode() == 0){
                            redirectAtt.addFlashAttribute("success", true);
                            redirectAtt.addFlashAttribute("message", "<i class=\"fa fa-check-circle\"></i>&nbsp;Teacher successfully added. Teacher's first time login password is <b>" + teacherEmail + "</b>");
                        } else {
                            redirectAtt.addFlashAttribute("success", false);
                            redirectAtt.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp;" + requestResponse.getMessage());
                            redirectAtt.addFlashAttribute("tfname", teacherFirstName);
                            redirectAtt.addFlashAttribute("tlname", teacherLastName);
                            redirectAtt.addFlashAttribute("temail", teacherEmail);
                            redirectAtt.addFlashAttribute("tpassword", teacherPassword);
                        }
                    }
                    else {
                        redirectAtt.addFlashAttribute("success", false);
                        redirectAtt.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp;Teacher's password is required");
                        redirectAtt.addFlashAttribute("tfname", teacherFirstName);
                        redirectAtt.addFlashAttribute("tlname", teacherLastName);
                        redirectAtt.addFlashAttribute("temail", teacherEmail);
                        redirectAtt.addFlashAttribute("tpassword", teacherPassword);

                    }
                } else {
                    redirectAtt.addFlashAttribute("success", false);
                    redirectAtt.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp;Teacher's email address is required");
                    redirectAtt.addFlashAttribute("tfname", teacherFirstName);
                    redirectAtt.addFlashAttribute("tlname", teacherLastName);
                    redirectAtt.addFlashAttribute("temail", teacherEmail);
                    redirectAtt.addFlashAttribute("tpassword", teacherPassword);
                }
            } else {
                redirectAtt.addFlashAttribute("success", false);
                redirectAtt.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp;Teacher's last name is required");
                redirectAtt.addFlashAttribute("tfname", teacherFirstName);
                redirectAtt.addFlashAttribute("tlname", teacherLastName);
                redirectAtt.addFlashAttribute("temail", teacherEmail);
                redirectAtt.addFlashAttribute("tpassword", teacherPassword);
            }
        } else {
            redirectAtt.addFlashAttribute("success", false);
            redirectAtt.addFlashAttribute("message", "<i class=\"fa fa-times-circle\"></i>&nbsp;Teacher's first name is required");
            redirectAtt.addFlashAttribute("tfname", teacherFirstName);
            redirectAtt.addFlashAttribute("tlname", teacherLastName);
            redirectAtt.addFlashAttribute("temail", teacherEmail);
            redirectAtt.addFlashAttribute("tpassword", teacherPassword);
        }
        return "redirect:/admin/teachers";
    }
}