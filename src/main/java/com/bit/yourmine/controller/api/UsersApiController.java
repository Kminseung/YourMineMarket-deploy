package com.bit.yourmine.controller.api;

import com.bit.yourmine.domain.users.SessionUser;
import com.bit.yourmine.domain.users.Users;
import com.bit.yourmine.dto.users.PasswordModifyDto;
import com.bit.yourmine.dto.users.UserModifyDto;
import com.bit.yourmine.dto.users.UserSaveRequestDto;
import com.bit.yourmine.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class UsersApiController {
    private final UsersService usersService;

    @PostMapping("/signup")
    public void save(@RequestBody UserSaveRequestDto users, HttpSession session) {
        session.removeAttribute("emailCodeInfo");
        String pass = (String) session.getAttribute("emailPass");
        usersService.save(users, pass);
        session.removeAttribute("emailPass");
    }

    @PostMapping("/idCheck")
    public Object idCheck(@RequestBody UserSaveRequestDto users) {
        Users checkUser = null;
        try {
            checkUser = usersService.getUsers(users.getId());
        } catch (NoSuchElementException e) {
            System.out.println("가입된 계정 없음");
        }

        boolean check;
        check = checkUser == null;

        Map<String, Object> map = new HashMap<>();
        map.put("result", "success");
        map.put("data", check);
        return map;
    }

    @PostMapping("/userModify")
    public void userModify(@RequestBody UserModifyDto modifyDto, HttpSession session) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("userInfo");
        sessionUser = usersService.userModify(modifyDto, sessionUser);
        session.removeAttribute("userInfo");
        session.setAttribute("userInfo" , sessionUser);
    }


    @PostMapping("/passwordModifyApi")
    public void passwordModify(@RequestBody PasswordModifyDto modifyDto) {
        usersService.passwordModify(modifyDto);
    }

    @GetMapping("/leave/{id}")
    public void leave(@PathVariable String id) {
        usersService.leave(id);
    }

    @PostMapping("/findIdByEmail")
    public String findIdByEmail(@RequestBody Map<String,String> map) {
        String id = "null";
        try {
            id = usersService.findByEmail(map.get("email")).getId();
        } catch (NoSuchElementException e) {
            System.out.println("id null");
        }
        return id;
    }

    @PostMapping("/findEmailById")
    public String findEmailById(@RequestBody Map<String,String> map) {
        String mail = "null";
        try {
            mail = usersService.getUsers(map.get("id")).getEmail();
        } catch (NoSuchElementException e) {
            System.out.println("id null");
        }
        return mail;
    }
}
