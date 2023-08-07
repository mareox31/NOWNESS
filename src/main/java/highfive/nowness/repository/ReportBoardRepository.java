package highfive.nowness.repository;

import highfive.nowness.dto.PostData;
import highfive.nowness.dto.ReportDTO;
import highfive.nowness.dto.ReportsDTO;
import highfive.nowness.dto.TagsDTO;
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

    // 제목으로 게시글 검색 결과 수 가져오기
    public int getTotalPostsCountByTitle(String searchTitle) {
        return sql.selectOne("report.getTotalPostsCountByTitle", searchTitle);
    }

    // 제목으로 게시글 검색 결과 가져오기
    public List<ReportDTO> getPostsByTitleAndPage(String searchTitle, int offset, int pageSize) {
        Map<String, Object> params = Map.of("searchTitle", searchTitle, "offset", offset, "pageSize", pageSize);
        return sql.selectList("report.getPostsByTitleAndPage", params);
    }

    // 제보요청카테고리만 게시물 목록 출력
    public int getTotalPostsCountByBoardType(int boardType) {
        return sql.selectOne("report.getTotalPostsCountByBoardType", boardType);
    }

    public List<ReportDTO> getPostsByBoardTypeAndPage(int boardType, int offset, int pageSize) {
        Map<String, Integer> params = Map.of("boardType", boardType, "offset", offset, "pageSize", pageSize);
        return sql.selectList("report.getPostsByBoardTypeAndPage", params);
    }

    // 제보요청카테고리만 검색 목록 출력
    public int getTotalPostsCountByTitleAndBoardType(String searchTitle, int boardType) {
        Map<String, Object> params = Map.of("searchTitle", searchTitle, "boardType", boardType);
        return sql.selectOne("report.getTotalPostsCountByTitleAndBoardType", params);
    }

    public List<ReportDTO> getPostsByTitleAndBoardTypeAndPage(String searchTitle, int boardType, int offset, int pageSize) {
        Map<String, Object> params = Map.of("searchTitle", searchTitle, "boardType", boardType, "offset", offset, "pageSize", pageSize);
        return sql.selectList("report.getPostsByTitleAndBoardTypeAndPage", params);
    }

    // 게시글 삭제
    public void deletePostById(int postId) {
        sql.delete("report.deletePostById", postId);
    }

    // 글 상세 정보 조회 시 해당 글의 태그 정보도 함께 조회
    public List<TagsDTO> getTagsByContentsId(int contentsId) {
        return sql.selectList("report.getTagsByContentsId", contentsId);
    }

    // 게시글 신고 저장
    public void reportPost(ReportsDTO reportsDTO) {
        sql.insert("report.reportPost", reportsDTO);
    }

}