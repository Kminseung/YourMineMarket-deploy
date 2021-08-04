package com.bit.yourmine.service;

import com.bit.yourmine.domain.chat.ChatRoom;
import com.bit.yourmine.domain.chat.ChatRoomRepository;
import com.bit.yourmine.domain.files.Files;
import com.bit.yourmine.domain.files.FilesRepository;
import com.bit.yourmine.domain.posts.Posts;
import com.bit.yourmine.domain.posts.PostsRepository;
import com.bit.yourmine.domain.users.Users;
import com.bit.yourmine.domain.users.UsersRepository;
import com.bit.yourmine.dto.posts.PostsResponseDto;
import com.bit.yourmine.dto.posts.PostsSaveRequestDto;
import com.bit.yourmine.dto.posts.PostsUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostsService {

    private final PostsRepository postsRepository;
    private final UsersRepository usersRepository;
    private final ChatRoomRepository roomRepository;
    private final FileService fileService;
    private final FilesRepository filesRepository;
    private final String wait = "거래대기";

    @Transactional
    public Long save(PostsSaveRequestDto requestDto) {
        isValid(requestDto.getTitle(), requestDto.getCategory(), requestDto.getPrice(), requestDto.getWay(), requestDto.getContent());
        Users users = usersRepository.findById(requestDto.getAuthor()).get();
        requestDto.setUsers(users);
        requestDto.setStatus(wait);
        requestDto.setHit(0L);
        return postsRepository.save(requestDto.toEntity()).getId();
    }

    public void imageSave(MultipartFile image, Long id) {
        if (image.isEmpty()) {
            delete(id);
            throw new IllegalArgumentException("post save : imageSave");
        }
        Posts posts = postsRepository.findById(id).get();
        String saveFileName = fileService.fileSave(image, "postImage");

        Files files = new Files(saveFileName, posts);
        filesRepository.save(files);
    }

    @Transactional
    public void update(PostsUpdateRequestDto requestDto) {
        isValid(requestDto.getTitle(), requestDto.getCategory(), requestDto.getPrice(), requestDto.getWay(), requestDto.getContent());
        Posts posts = postsRepository.findById(requestDto.getId()).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + requestDto));
        posts.update(requestDto.getTitle(), requestDto.getContent(), requestDto.getPrice(),
                requestDto.getArea(), requestDto.getWay(),
                requestDto.getOfSize(), requestDto.getCategory());
    }

    public void delete(Long id) {
        Posts posts = postsRepository.findById(id).get();
        for (ChatRoom chatRoom: roomRepository.findAllByPostId(id)) {
            roomRepository.buyerOut(chatRoom.getIdentify());
        }
        postsRepository.delete(posts);
    }

    @Transactional
    public List<PostsResponseDto> search(String keyword, Pageable pageable, Long cursor) {
        return postsRepository.findAllSearch(keyword, pageable, cursor, wait).stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    public PostsResponseDto findById(Long id) {
        Posts entity = postsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));

        return new PostsResponseDto(entity);
    }

    @Transactional(readOnly = true)
    public List<PostsResponseDto> findAllDesc(Pageable pageable, Long cursor) {
        return postsRepository.findAllDesc(pageable, wait).stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void hitUpdate(Long id) {
        postsRepository.hitUpdate(id);
    }

    public List<PostsResponseDto> findByCategory(String category, Pageable pageable) {
        return postsRepository.findByCategory(category, pageable, wait).stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<PostsResponseDto> findByAddress(String area, Pageable pageable, Long cursor) {
        int index = area.indexOf(" ",area.indexOf(" ")+1);
        String address = area.substring(0, index);
        return postsRepository.findByAddress(address, pageable, cursor, wait).stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<PostsResponseDto> findByHit(Pageable pageable, Long cursor) {
        return postsRepository.HitDesc(pageable, wait).stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    // Posts on YourPage
    public List<PostsResponseDto> findAllYour(Long usersNo) {
        return postsRepository.findAllYour(usersNo).stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<PostsResponseDto> findByAttention(List<Long> idList) {
        List<PostsResponseDto> responseDtos = new ArrayList<>();
        for (Long id: idList) {
            responseDtos.add(new PostsResponseDto(postsRepository.findById(id).get()));
        }
        return responseDtos;
    }

    public void SetStatus(Long id, String status) {
        Posts posts = postsRepository.findById(id).get();
        posts.updateStatus(status);
        postsRepository.save(posts);
    }

    public void isValid(String title, String category, Long price, String way, String content) {
        if (title.isEmpty()) {
            throw new IllegalArgumentException("post save : Title");
        } else if (category == null) {
            throw new IllegalArgumentException("post save : Category");
        } else if (price == null) {
            throw new IllegalArgumentException("post save : Price");
        } else if (way == null) {
            throw new IllegalArgumentException("post save : Way");
        } else if (content == null) {
            throw new IllegalArgumentException("post save : Content");
        }
    }

    public Long LastId() {
        return postsRepository.findByIdMax();
    }
}