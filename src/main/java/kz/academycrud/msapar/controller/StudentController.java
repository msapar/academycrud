package kz.academycrud.msapar.controller;
import kz.academycrud.msapar.entity.Courses;
import kz.academycrud.msapar.entity.RequestResponse;
import kz.academycrud.msapar.entity.User;
import kz.academycrud.msapar.service.UserService;
import kz.academycrud.msapar.util.DateTimeUtil;
import kz.academycrud.msapar.util.FormValidationUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping(value = "/student")
public class StudentController {

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
            modelAndView.setViewName("student/home");
            if (users.size() == 1){
                modelAndView.addObject("full_name", users.get(0).getFirstName() + " " + users.get(0).getLastName());
                List<Courses> courses = userService.findCoursesByStudentId(String.valueOf(users.get(0).getId()));
                LoggerFactory.getLogger(LoggerFactory.class).info("STUDENT COURSES DATA :: " + courses.toString());
                modelAndView.addObject("courses", courses);
            }
        } else {
            LoggerFactory.getLogger(LoggerFactory.class).error("AUTH ERROR :: Auth is null");
        }
        return modelAndView;
    }


    @RequestMapping(value = "/change_status", method = RequestMethod.GET)
    public String registerCourse(@RequestParam("i") String courseId, @RequestParam("ac") String action, RedirectAttributes redirectAtt){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<User> users = userService.findUserByEmail(auth.getName());
        if (users.size() == 1){
            Courses courses = Courses.builder().id(Long.valueOf(courseId)).build();
            if (action.equals("1")){
                courses.setActive(1);
                RequestResponse requestResponse = userService.updateCourseStatus(courses);
                if (requestResponse.getResponseCode() == 0){
                    redirectAtt.addFlashAttribute("successc", true);
                    redirectAtt.addFlashAttribute("message", "<i class=\"fa fa-check-circle\"></i>&nbsp;Course successfully reactivated");
                } else {
                    redirectAtt.addFlashAttribute("successc", false);
                    redirectAtt.addFlashAttribute("message", "<i class=\"fa fa-check-circle\"></i>&nbsp;" + requestResponse.getMessage());
                }
            } else {
                courses.setActive(0);
                RequestResponse requestResponse = userService.updateCourseStatus(courses);
                if (requestResponse.getResponseCode() == 0){
                    redirectAtt.addFlashAttribute("successc", true);
                    redirectAtt.addFlashAttribute("message", "<i class=\"fa fa-check-circle\"></i>&nbsp;Course successfully suspended");
                } else {
                    redirectAtt.addFlashAttribute("successc", false);
                    redirectAtt.addFlashAttribute("message", "<i class=\"fa fa-check-circle\"></i>&nbsp;" + requestResponse.getMessage());
                }
            }
            return "redirect:/student/home";
        } else {
            return "redirect:/logout";
        }
    }
}