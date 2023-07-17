package highfive.nowness.repository;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface BoardRepository {
    List<Map<String, String>> loadRecentContentsAndReplies(long userId);
}
