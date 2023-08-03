package highfive.nowness.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface BoardRepository {
    List<Map<String, String>> loadRecentContentsAndReplies(long userId);
    List<Map<String, String>> loadUserRecentPostsByPage(long userId, long page);
    long loadUserPostsCount(long userId);
    List<Map<String, String>> loadUserRecentRepliesByPage(long userId, long page);
    long loadUserRepliesCount(long userId);
}
