package com.echoloop.controller;

import com.echoloop.model.User;
import com.echoloop.service.FriendshipService;
import com.echoloop.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    @GetMapping("/{userId}/foaf")
    public List<UserDTO> getFoafs(@PathVariable Long userId) {
        List<User> foafs = friendshipService.getFoafs(userId);
        return foafs.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/{userId}/friends")
    public List<UserDTO> getDirectFriends(@PathVariable Long userId) {
        List<User> friends = friendshipService.getDirectFriends(userId);
        return friends.stream()
                      .map(UserDTO::fromEntity)
                      .collect(Collectors.toList());
    }

}
