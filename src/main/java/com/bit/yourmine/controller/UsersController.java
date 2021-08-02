package com.bit.yourmine.controller;

import com.bit.yourmine.domain.posts.Posts;
import com.bit.yourmine.domain.users.SessionUser;
import com.bit.yourmine.domain.users.Users;
import com.bit.yourmine.dto.posts.PostsResponseDto;
import com.bit.yourmine.dto.reviews.ReviewResponseDto;
import com.bit.yourmine.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class UsersController {

    private final UsersService usersService;
    private final PostsService postsService;
    private final AttentionService attentionService;
    private final ReviewService reviewService;

    @GetMapping("/loginPage")
    public String loginPage(@RequestParam(value = "error", required = false) String error, HttpSession session, Model model) {

        SessionUser user = (SessionUser) session.getAttribute("userInfo");
        if (user != null) {
            return "index";
        }

        if (error != null) {
            String errCode = null;
            switch (error) {
                case "err1":
                    errCode = "존재하지 않는 아이디입니다";
                    break;
                case "err2":
                    errCode = "아이디 또는 비밀번호가 틀렸습니다";
                    break;
                case "err3":
                    errCode = "이미 접속중인 계정입니다";
                    break;
                default:
                    errCode = "로그인 실패";
                    break;
            }
            String content = "<b style=\"color:red;\">" + errCode + "</b>";
            model.addAttribute("error", content);
        }
        return "account/loginPage";
    }

    @GetMapping("/signup")
    public String signup() {
        return "account/signup";
    }

    @GetMapping("/myPage")
    public String myPage(HttpSession session, Model model) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("userInfo");
        Users users = usersService.getUsers(sessionUser.getId());
        try {
            List<PostsResponseDto> myPosts = new ArrayList<>();
            for (Posts posts : users.getPosts()) {
                myPosts.add(new PostsResponseDto(posts));
                model.addAttribute("posts", myPosts);
            }
        } catch (NullPointerException e) {
            System.out.println("posts null");
        }
        try {
            model.addAttribute("buyReview", reviewService.myPageBuyReviewList(users.getId()));
        } catch (NullPointerException e) {
            System.out.println("buyReview null");
        }
        try {
            model.addAttribute("sellReview", reviewService.myPageSellReviewList(users.getId()));

        } catch (NullPointerException e) {
            System.out.println("sellReview null");
        }
        try {
            List<Long> longList = attentionService.findAllByUsersNo(sessionUser.getNo());
            List<PostsResponseDto> postsList = postsService.findByAttention(longList);
            model.addAttribute("attention", postsList);
        } catch (NullPointerException e) {
            System.out.println("attention null");
        }
        return "account/myPage";
    }

    @GetMapping("/userModify")
    public String userModify() {
        return "account/modify/userModify";
    }

    @GetMapping("/passwordModify")
    public String passwordModify() {
        return "account/modify/passwordModify";
    }

    @PostMapping("/profileModify")
    public String profileModify(@RequestParam MultipartFile profile, @RequestParam String id, HttpSession session) {
        SessionUser users = (SessionUser) session.getAttribute("userInfo");
        users.setProfile(usersService.profileModify(profile, id));
        session.removeAttribute("userInfo");
        session.setAttribute("userInfo" , users);
        return "account/myPage";
    }

    @GetMapping("/delProfile")
    public String delProfile(HttpSession session) {
        SessionUser users = (SessionUser) session.getAttribute("userInfo");
        SessionUser sessionUser = new SessionUser(usersService.delProfile(users.getId()));
        session.removeAttribute("userInfo");
        session.setAttribute("userInfo" , sessionUser);
        return "account/myPage";
    }

    // YourPage Controller
    @GetMapping("/user/{id}")
    public String yourPage(@PathVariable String id, Model model) {
        Long userNo = usersService.getUsers(id).getNo();
        String name = usersService.getUsers(id).getName();

        model.addAttribute("name", name);

        List<PostsResponseDto> yourList = postsService.findAllYour(userNo);
        model.addAttribute("yourPost", yourList);

        List<ReviewResponseDto> sellerReviewList = reviewService.findAllSellReview(id);
        model.addAttribute("sellerReviewList", sellerReviewList);

        List<ReviewResponseDto> buyerReviewList = reviewService.findAllBuyReview(id);
        model.addAttribute("buyerReviewList", buyerReviewList);
        return "account/yourPage";
    }
}
