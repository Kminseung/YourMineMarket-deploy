package com.bit.yourmain.domain.posts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r WHERE r.seller = :id AND  r.sellerScore is null")
    List<Review> findSellReview(String id);

    @Query("SELECT r FROM Review r WHERE r.buyer = :id AND r.buyerScore is Null")
    List<Review> findBuyReview(String id);

    Optional<Review> findByNo(Long no);

    @Transactional
    @Modifying
    @Query("UPDATE Review r SET r.buyerScore = :score WHERE r.no = :no")
    void setBuyerScore(Long no, Long score);

    @Transactional
    @Modifying
    @Query("UPDATE Review r SET r.sellerScore = :score WHERE r.no = :no")
    void setSellerScore(Long no, Long score);
}