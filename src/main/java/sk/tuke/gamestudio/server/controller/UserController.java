package sk.tuke.gamestudio.server.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping("/user")
public class UserController {
    private String loggedUser;

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/login")
    public String login(String login, Model model) {
        loggedUser = login;
        return "redirect:/";
    }

    @RequestMapping("/logout")
    public String logout(Model model) {
        loggedUser = null;
        return "redirect:/";
    }

    public String getLoggedUser() {
        return loggedUser;
    }

    public boolean isLogged() {
        return loggedUser != null;
    }
}
