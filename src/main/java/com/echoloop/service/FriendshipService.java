package com.echoloop.service;

import com.echoloop.model.User;
import com.echoloop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getDirectFriends(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Set<User> followers = user.getFollowers();
        Set<User> following = user.getFollowing();

        // Mutual connections
        return followers.stream()
                .filter(following::contains)
                .collect(Collectors.toList());
    }

    public List<User> getFoafs(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Set<User> following = user.getFollowing();

        Set<User> foaf = new HashSet<>();

        for (User followed : following) {
            foaf.addAll(followed.getFollowing());
        }

        // Remove people already followed and the user themself
        foaf.removeAll(following);
        foaf.remove(user);

        return new ArrayList<>(foaf);
    }
}
