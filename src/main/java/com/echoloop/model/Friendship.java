package com.echoloop.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(FriendshipId.class)
public class Friendship implements Serializable {

    @Id
    private Long userId;

    @Id
    private Long friendId;

    // Constructors
    public Friendship() {}

    public Friendship(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }
}
