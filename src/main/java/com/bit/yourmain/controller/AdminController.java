package com.bit.yourmain.controller;

import com.bit.yourmain.dto.posts.PostsResponseDto;
import com.bit.yourmain.service.AdminService;
import com.bit.yourmain.service.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final PostsService postsService;
    static final PageRequest indexPage = PageRequest.of(0,8);

    @GetMapping("/adminPage")
    public String admin(HttpSession session, Model model, HttpServletResponse response) {

        final Long cursor = 0L;
        List<PostsResponseDto> postsList = postsService.findAllDesc(indexPage, cursor);
        model.addAttribute("posts", postsList);
        model.addAttribute("allUsers", adminService.findAll());

        return "admin/adminPage";
    }
}