package com.bit.yourmine.domain.posts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r WHERE r.seller = :id AND r.postId = :postId AND r.sellerScore is null")
    List<Review> findSellReview(String id, Long postId);

    @Query("SELECT r FROM Review r WHERE r.buyer = :id AND r.postId = :postId AND r.buyerScore is Null")
    List<Review> findBuyReview(String id, Long postId);

    Optional<Review> findByNo(Long no);

    @Query("SELECT r FROM Review r WHERE r.buyer = :id AND r.buyerScore is Null")
    List<Review> findAllByBuyer(String id);
    @Query("SELECT r FROM Review r WHERE r.seller = :id AND r.sellerScore is null")
    List<Review> findAllBySeller(String id);

    // Sell Review on YourPage
    @Query("SELECT r FROM Review r WHERE r.seller = :seller AND " +
            "r.buyerScore is not null AND r.buyerReviewContent is not null")
    List<Review> findAllSellReview(String seller);

    // Sell Review on YourPage
    @Query("SELECT r FROM Review r WHERE r.buyer = :buyer AND " +
            "r.sellerScore is not null AND r.sellerReviewContent is not null")
    List<Review> findAllBuyReview(String buyer);

    @Transactional
    @Modifying
    @Query("UPDATE Review r SET r.buyerScore = :score , r.buyerReviewContent = :buyerReviewContent WHERE r.no = :no")
    void setBuyerScore(Long no, Long score, String buyerReviewContent);

    @Transactional
    @Modifying
    @Query("UPDATE Review r SET r.sellerScore = :score , r.sellerReviewContent = :sellerReviewContent WHERE r.no = :no")
    void setSellerScore(Long no, Long score, String sellerReviewContent);
}
