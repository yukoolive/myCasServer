package org.jasig.cas.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/user")
public class UserController {

    @RequestMapping(value = "/user/checkUser")
    public String checkUser(String username,String password){
        String result="";
        return result;
    }
}
