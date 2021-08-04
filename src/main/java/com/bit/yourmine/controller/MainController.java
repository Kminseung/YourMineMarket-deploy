package com.bit.yourmine.controller;

import com.bit.yourmine.domain.posts.Posts;
import com.bit.yourmine.domain.users.SessionUser;
import com.bit.yourmine.domain.users.Users;
import com.bit.yourmine.dto.posts.PostPageDto;
import com.bit.yourmine.dto.posts.PostsResponseDto;
import com.bit.yourmine.service.PostsService;
import com.bit.yourmine.service.ReviewService;
import com.bit.yourmine.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class MainController {

    private final ReviewService reviewService;
    private final PostsService postsService;
    private final UsersService usersService;
    static final PageRequest indexPage = PageRequest.of(0,8);

    @GetMapping("/")
    public String index(HttpSession session, Model model, HttpServletResponse response) {
        Long cursor = postsService.LastId() + 1;
        List<PostsResponseDto> postsList = postsService.findAllDesc(indexPage, cursor);
        model.addAttribute("posts", postsList);
        List<PostsResponseDto> hitList = postsService.findByHit(indexPage, cursor);
        model.addAttribute("hitPost", hitList);
        try {
            SessionUser sessionUser = (SessionUser) session.getAttribute("userInfo");
            List<PostsResponseDto> areaList = postsService.findByAddress(sessionUser.getAddress(), indexPage, cursor);
            model.addAttribute("areaPost", areaList);
        } catch (NullPointerException e) {
            System.out.println("guest in");
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("java.lang.StringIndexOutOfBoundsException");
        }
        Cookie cookie = new Cookie("hitCheck", null);
        cookie.setMaxAge(60*60*24);
        response.addCookie(cookie);

        return "index";
    }

    @GetMapping("/request/{kind}/{value}")
    public String requestPage(@PathVariable String kind, @PathVariable String value, Model model) {
        Long lastId = postsService.LastId() + 1;
        switch (kind) {
            case "category":
                model.addAttribute("input", "해당 카테고리의 상품 목록");
                model.addAttribute("Post", postsService.findByCategory(value, indexPage));
                break;
            case "hit":
                System.out.println("request mapping");
                model.addAttribute("input", "인기 상품 목록");
                model.addAttribute("Post", postsService.findByHit(indexPage, lastId));
                break;
            case "all":
                model.addAttribute("input", "최근 상품 목록");
                model.addAttribute("Post", postsService.findAllDesc(indexPage, lastId));
                break;
            case "area":
                model.addAttribute("input", "근처 상품 목록");
                model.addAttribute("Post", postsService.findByAddress(value, indexPage, lastId));
                break;
            case "search":
                model.addAttribute("input", "검색어 : " + value);
                model.addAttribute("Post", postsService.search(value, indexPage, lastId));
                break;
        }
        PostPageDto pageDto = new PostPageDto();
        pageDto.setKind(kind);
        pageDto.setValue(value);
        model.addAttribute("page", pageDto);
        model.addAttribute("LastId", lastId);
        return "request";
    }

    @GetMapping("/accessDenied")
    public String accessDenied() {
        return "deny/accessDenied";
    }

    @GetMapping("/authDenied")
    public String authDenied() {
        return "deny/authDenied";
    }

    @GetMapping("/chat/{no}/{id}")
    public String chat(@PathVariable Long no, @PathVariable String id, Model model, HttpSession session, Long reviewNo) {
        model.addAttribute("roomId", no + "-" + id);
        model.addAttribute("post", postsService.findById(no));

        SessionUser sessionUser = (SessionUser) session.getAttribute("userInfo");
        Users users = usersService.getUsers(sessionUser.getId());
        String postOwner = postsService.findById(no).getUsers().getId();
        if (users.getId().equals(id)) {
            model.addAttribute("listener", postOwner);
        } else {
            model.addAttribute("listener", id);
        }
        for (Posts posts: users.getPosts()) {
            if (posts.getId().equals(no)) {
                model.addAttribute("owner", "owner");
            }
        }

        if (sessionUser.getId().equals(id)) {
            try {
                model.addAttribute("review", reviewService.getBuyReview(sessionUser.getId(), no));
            } catch (NullPointerException e) {
                System.out.println("BuyReview null");
            }
        } else {
            try {
                model.addAttribute("review", reviewService.getSellReview(sessionUser.getId(), no));
            } catch (NullPointerException e) {
                System.out.println("SellReview null");
            }
        }


        return "chat/chat";
    }
}
