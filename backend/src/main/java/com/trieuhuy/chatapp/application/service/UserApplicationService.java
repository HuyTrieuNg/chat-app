package com.trieuhuy.chatapp.application.service;

import com.trieuhuy.chatapp.domain.model.User;
import com.trieuhuy.chatapp.domain.model.UserSearchCriteria;
import com.trieuhuy.chatapp.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserApplicationService {

    private final UserRepository userRepository;

    public Page<@NonNull User> findAll(UserSearchCriteria criteria, Pageable pageable) {
        return userRepository.findAll(criteria, pageable);
    }
}
