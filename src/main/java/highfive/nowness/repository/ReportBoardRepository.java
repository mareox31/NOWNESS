package highfive.nowness.repository;

import highfive.nowness.dto.PostData;
import highfive.nowness.dto.ReportDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReportBoardRepository {
    private final SqlSessionTemplate sql;

    // 글쓰기저장
    public  void addPost(PostData postData) {
        sql.insert("report.addPost", postData);
    }

    // 게시글 출력
    public int getTotalPostsCount() {
        return sql.selectOne("report.getTotalPostsCount");
    }

    // 게시물 목록 조회
    public List<ReportDTO> getPostsByPage(int offset, int pageSize) {
        Map<String, Integer> params = Map.of("offset", offset, "pageSize", pageSize);
        return sql.selectList("report.getPostsByPage", params);
    }

    // 상세페이지 조회
    public ReportDTO getPostById(int postId) {
        return sql.selectOne("report.getPostById", postId);
    }

    // 게시글 조회수 증가
    public void increasePostViewCount(int postId) {
        sql.update("report.increasePostViewCount", postId);
    }

}