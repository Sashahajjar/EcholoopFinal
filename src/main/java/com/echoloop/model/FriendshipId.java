package com.echoloop.model;

import java.io.Serializable;
import java.util.Objects;

public class FriendshipId implements Serializable {
    private Long userId;
    private Long friendId;

    public FriendshipId() {}

    public FriendshipId(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendshipId that)) return false;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(friendId, that.friendId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, friendId);
    }
}
