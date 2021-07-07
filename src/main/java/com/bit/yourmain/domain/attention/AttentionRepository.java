package com.bit.yourmain.domain.attention;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AttentionRepository extends JpaRepository<Attention, Long> {
    Attention findByUsersNoAndPostNo(Long usersNo, Long postNo);

    List<Attention> findAllByUsersNo(Long usersNo);
}