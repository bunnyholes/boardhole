package dev.xiyo.bunnyholes.boardhole.shared.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheConstants {

  public static final class Board {
    public static final String CACHE_NAME = "board";
    public static final String GET = "board:get";
    public static final String LIST = "board:list";
    public static final String SEARCH = "board:search";
    public static final String COUNT = "board:count";
    public static final String COUNT_TODAY = "board:count:today";
    public static final String COUNT_BY_AUTHOR = "board:count:author";
    public static final String RECENT = "board:recent";
  }

  public static final class User {
    public static final String CACHE_NAME = "user";
    public static final String GET = "user:get";
    public static final String LIST = "user:list";
    public static final String SEARCH = "user:search";
    public static final String PROFILE_IMAGE = "user:profile-image";
    public static final String COUNT_ACTIVE = "user:count:active";
  }

  public static final class CacheKey {
    public static String boardId(Object id) {
      return Board.GET + ":" + id;
    }

    public static String boardList(int page, int size) {
      return Board.LIST + ":" + page + ":" + size;
    }

    public static String boardSearch(String keyword, int page, int size) {
      return Board.SEARCH + ":" + keyword + ":" + page + ":" + size;
    }

    public static String boardCountByAuthor(Object authorId) {
      return Board.COUNT_BY_AUTHOR + ":" + authorId;
    }

    public static String boardRecent(int limit) {
      return Board.RECENT + ":" + limit;
    }

    public static String userId(String username) {
      return User.GET + ":" + username;
    }

    public static String userList(int page, int size) {
      return User.LIST + ":" + page + ":" + size;
    }

    public static String userSearch(String keyword, int page, int size) {
      return User.SEARCH + ":" + keyword + ":" + page + ":" + size;
    }

    public static String userProfileImage(String username) {
      return User.PROFILE_IMAGE + ":" + username;
    }
  }
}
