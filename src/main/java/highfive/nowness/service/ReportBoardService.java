package highfive.nowness.service;

import highfive.nowness.dto.PostData;
import highfive.nowness.dto.ReportDTO;
import highfive.nowness.repository.ReportBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportBoardService {
    private final ReportBoardRepository reportBoardRepository;

    // 글저장
    public void addPost(PostData postData) {
        reportBoardRepository.addPost(postData);
    }

    // 게시글 출력
    public int getTotalPostsCount() {
        return reportBoardRepository.getTotalPostsCount();
    }

    // 게시물 목록 조회
    public List<ReportDTO> getPostsByPage(int offset, int pageSize) {
        return reportBoardRepository.getPostsByPage(offset, pageSize);
    }

    // 상세페이지 조회
    public ReportDTO getPostById(int postId) {
        return reportBoardRepository.getPostById(postId);
    }

    // 게시글 조회수 증가
    public void increasePostViewCount(int postId) {
        reportBoardRepository.increasePostViewCount(postId);
    }

    // 제목으로 게시글 검색 결과 수 가져오기
    public int getTotalPostsCountByTitle(String searchTitle) {
        return reportBoardRepository.getTotalPostsCountByTitle(searchTitle);
    }

    // 제목으로 게시글 검색 결과 가져오기
    public List<ReportDTO> getPostsByTitleAndPage(String searchTitle, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return reportBoardRepository.getPostsByTitleAndPage(searchTitle, offset, pageSize);
    }

}