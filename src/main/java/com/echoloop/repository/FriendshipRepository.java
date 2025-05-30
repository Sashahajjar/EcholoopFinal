package com.echoloop.repository;

import com.echoloop.model.Friendship;
import com.echoloop.model.FriendshipId;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {

    @Query("SELECT f.friendId FROM Friendship f WHERE f.userId = :userId")
    List<Long> findDirectFriends(@Param("userId") Long userId);

    @Query("SELECT DISTINCT f2.friendId FROM Friendship f1 " +
           "JOIN Friendship f2 ON f1.friendId = f2.userId " +
           "WHERE f1.userId = :userId " +
           "AND f2.friendId != :userId " +
           "AND f2.friendId NOT IN (" +
           "    SELECT f3.friendId FROM Friendship f3 WHERE f3.userId = :userId" +
           ")")
    List<Long> findFoafs(@Param("userId") Long userId);
}
