package highfive.nowness.service;

import highfive.nowness.dto.PostData;
import highfive.nowness.dto.ReportDTO;
import highfive.nowness.dto.TagsDTO;
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

    // 제보요청카테고리만 게시물 목록 출력
    public int getTotalPostsCountByBoardType(int boardType) {
        return reportBoardRepository.getTotalPostsCountByBoardType(boardType);
    }

    public List<ReportDTO> getPostsByBoardTypeAndPage(int boardType, int offset, int pageSize) {
        return reportBoardRepository.getPostsByBoardTypeAndPage(boardType, offset, pageSize);
    }

    // 제보요청카테고리만 검색 목록 출력
    public int getTotalPostsCountByTitleAndBoardType(String searchTitle, int boardType) {
        return reportBoardRepository.getTotalPostsCountByTitleAndBoardType(searchTitle, boardType);
    }

    public List<ReportDTO> getPostsByTitleAndBoardTypeAndPage(String searchTitle, int boardType, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return reportBoardRepository.getPostsByTitleAndBoardTypeAndPage(searchTitle, boardType, offset, pageSize);
    }

    // 게시글 삭제
    public void deletePostById(int postId) {
        reportBoardRepository.deletePostById(postId);
    }

    // 게시글 상세 정보 조회
    public List<TagsDTO> getTagsByContentsId(int contentsId) {
        return reportBoardRepository.getTagsByContentsId(contentsId);
    }
}