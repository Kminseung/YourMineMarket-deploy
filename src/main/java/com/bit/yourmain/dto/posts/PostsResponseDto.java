package com.bit.yourmain.dto.posts;

import com.bit.yourmain.domain.files.Files;
import com.bit.yourmain.domain.posts.Posts;
import com.bit.yourmain.domain.users.Users;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostsResponseDto {

    private final Long id;
    private final String title;
    private final String content;
    private final String author;
    private final String status;
    private final Long price;
    private final String area;
    private final String way;
    private final String ofSize;
    private final String category;
    private final Long hit;
    private final Users users;
    private final String thumbnail;
    private final List<Files> filesList;

    public PostsResponseDto(Posts entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.author = entity.getUsers().getId();
        this.status = entity.getStatus();
        this.price = entity.getPrice();
        this.area = entity.getArea();
        this.way = entity.getWay();
        this.ofSize = entity.getOfSize();
        this.category = entity.getCategory();
        this.hit = entity.getHit();
        this.users = entity.getUsers();
        this.thumbnail = entity.getFiles().get(0).getFileName();
        this.filesList = entity.getFiles();
    }
}