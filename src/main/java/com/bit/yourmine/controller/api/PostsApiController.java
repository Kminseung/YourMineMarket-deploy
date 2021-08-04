package com.bit.yourmine.controller.api;

import com.bit.yourmine.dto.posts.PostPageDto;
import com.bit.yourmine.dto.posts.PostPageResponseDto;
import com.bit.yourmine.dto.posts.PostsResponseDto;
import com.bit.yourmine.dto.reviews.ReviewSaveRequestDto;
import com.bit.yourmine.dto.reviews.ReviewScoreSetDto;
import com.bit.yourmine.service.FileService;
import com.bit.yourmine.service.PostsService;
import com.bit.yourmine.service.ReviewService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class PostsApiController {

    private final FileService fileService;
    private final PostsService postsService;
    private final ReviewService reviewService;

    @GetMapping("/post/files/{id}")
    public List<String> getFiles(@PathVariable Long id) {
        return fileService.getFiles(id);
    }

    @PutMapping("/post/status/{id}/{status}")
    public void setStatus(@PathVariable Long id, @PathVariable String status, @RequestBody ReviewSaveRequestDto requestDto) {
        postsService.SetStatus(id, status);
        if (status.equals("거래완료")) {
            reviewService.save(requestDto);
        }
    }

    @DeleteMapping("/post/files/del")
    public void delFiles(@RequestParam String fileName) {
        fileService.delFiles(fileName);
    }

    @PostMapping("/request/list")
    public String getMorePosts(@RequestBody PostPageDto pageDto) {
        PageRequest newPage = PageRequest.of(pageDto.getCursor(), 8);
        List<PostsResponseDto> responseDtoList = new ArrayList<>();
        Long id = pageDto.getId();
        String kind = pageDto.getKind();
        switch (kind) {
            case "category":
                responseDtoList = postsService.findByCategory(pageDto.getValue(), newPage);
                break;
            case "hit":
                responseDtoList = postsService.findByHit(newPage, id);
                break;
            case "all":
                responseDtoList = postsService.findAllDesc(newPage, id);
                break;
            case "area":
                responseDtoList = postsService.findByAddress(pageDto.getValue(), newPage, id);
                break;
            case "search":
                responseDtoList = postsService.search(pageDto.getValue(), newPage, id);
                break;
        }
        List<PostPageResponseDto> postPageResponseDto = new ArrayList<>();
        for (PostsResponseDto responseDto: responseDtoList) {
            postPageResponseDto.add(new PostPageResponseDto(responseDto));
        }
        return new Gson().toJson(postPageResponseDto);
    }

    @PutMapping("/review/set")
    public void setScore(@RequestBody ReviewScoreSetDto scoreSetDto) {
        reviewService.setScore(scoreSetDto);
    }
}