package com.rventura.abscinema.showcase;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;

@Controller
public class ShowcaseController {

    @GetMapping("/")
    public String movies(Model model) {
        return "movies";
    }

    // @GetMapping("/admin/user")
    // @ResponseBody
    // public Principal user(Principal user) {
    // return user;
    // }
}
