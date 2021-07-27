package com.bit.yourmine.service;

import com.bit.yourmine.domain.posts.Review;
import com.bit.yourmine.domain.posts.ReviewRepository;
import com.bit.yourmine.dto.posts.PostsResponseDto;
import com.bit.yourmine.dto.reviews.ReviewLinkDto;
import com.bit.yourmine.dto.reviews.ReviewResponseDto;
import com.bit.yourmine.dto.reviews.ReviewSaveRequestDto;
import com.bit.yourmine.dto.reviews.ReviewScoreSetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PostsService postsService;
    private final UsersService usersService;

    public void save(ReviewSaveRequestDto requestDto) {
        reviewRepository.save(requestDto.toEntity());
    }

    public ReviewResponseDto getReviewPosition(Long no, String id) {
        ReviewResponseDto reviewResponseDto = new ReviewResponseDto(reviewRepository.findByNo(no).get());
        if (reviewResponseDto.getSeller().equals(id)) {
            reviewResponseDto.setPosition("seller");
        } else {
            reviewResponseDto.setPosition("buyer");
        }
        return reviewResponseDto;
    }

    public List<ReviewResponseDto> getSellReview(String id, Long postId) {
        return getList(reviewRepository.findSellReview(id, postId), "seller");
    }

    public List<ReviewResponseDto> getBuyReview(String id, Long postId) {
        return getList(reviewRepository.findBuyReview(id, postId), "buyer");
    }

    // Sell Review on YourPage
    public List<ReviewResponseDto> findAllSellReview(String seller) {
        return getList(reviewRepository.findAllSellReview(seller), "seller");
    }

    // Buy Review on YourPage
    public List<ReviewResponseDto> findAllBuyReview(String buyer) {
        return getList(reviewRepository.findAllBuyReview(buyer), "buyer");
    }

    public List<ReviewResponseDto> getList(List<Review> reviewList, String position) {
        List<ReviewResponseDto> responseDto = new ArrayList<>();
        try {
            for (Review review : reviewList) {
                ReviewResponseDto reviewResponseDto = new ReviewResponseDto(review);
                reviewResponseDto.setTitle(postsService.findById(review.getPostId()).getTitle());
                reviewResponseDto.setPosition(position);
                responseDto.add(reviewResponseDto);
            }
        } catch (Exception e) {
            System.out.println("get List error");
        }
        return responseDto;
    }

    @Transactional
    public void setScore(ReviewScoreSetDto scoreSetDto) {
        if (scoreSetDto.getScore() == null) {
            throw new IllegalArgumentException("review save : score");
        } else if (scoreSetDto.getBuyerReviewContent() == null && scoreSetDto.getSellerReviewContent() == null) {
            throw new IllegalArgumentException("review save : content");
        }
        try {
            if (scoreSetDto.getPosition().equals("buyer")) {
                reviewRepository.setBuyerScore(scoreSetDto.getNo(), scoreSetDto.getScore(), scoreSetDto.getBuyerReviewContent());
            } else {
                reviewRepository.setSellerScore(scoreSetDto.getNo(), scoreSetDto.getScore(), scoreSetDto.getSellerReviewContent());
            }
            usersService.setScore(scoreSetDto);
        } catch (Exception e) {
            System.out.println("review set score error");
        }
    }

    public List<ReviewLinkDto> myPageSellReviewList(String id) {
        List<Review> reviewList = reviewRepository.findAllBySeller(id);
        return getReviewList(reviewList);
    }

    public List<ReviewLinkDto> myPageBuyReviewList(String id) {
        List<Review> reviewList = reviewRepository.findAllByBuyer(id);
        return getReviewList(reviewList);
    }

    public List<ReviewLinkDto> getReviewList(List<Review> reviewList) {
        List<ReviewLinkDto> reviewLink = new ArrayList<>();
        for (Review review: reviewList) {
            ReviewLinkDto linkDto = new ReviewLinkDto();
            PostsResponseDto posts = postsService.findById(review.getPostId());
            linkDto.setTitle(posts.getTitle());
            linkDto.setPostId(String.valueOf(posts.getId()));
            linkDto.setUserId(review.getBuyer());
            reviewLink.add(linkDto);
        }
        return reviewLink;
    }
}
