package highfive.nowness.service;

import highfive.nowness.dto.PostData;
import highfive.nowness.dto.RepliesDTO;
import highfive.nowness.dto.ReplyData;
import highfive.nowness.dto.RequestDTO;
import highfive.nowness.repository.RequestBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RequestBoardService {

    private final RequestBoardRepository requestBoardRepository;


    // 페이징 처리 -'맵'사용(닉네임용리스트에사용)
    public List<RequestDTO> requestboardPagingList(Map<String, Integer> pagingParams) {
        return requestBoardRepository.boardPagingList(pagingParams);
    }

    //전체 글 (총 개수)
    public int getTotalRequestCount() {
        return requestBoardRepository.getTotalRequestCount();
    }

    //해당 번호 게시글 상세내용(DTO)
    public RequestDTO getBoard(int id) {
        return requestBoardRepository.getBoard(id);
    }

    //해당 게시글 닉네임
    public String getNicknameById(int id) {
        return requestBoardRepository.getNicknameById(id);
    }

    //리플쓴 유저id로 해당하는 닉네임 가져오기
    public String getNickname(int id) {
        return requestBoardRepository.getNickname(id);
    }


    //해당 게시글 좋아요 (총 개수)
    public int getLikes(int id) { return requestBoardRepository.getLikes(id); }

    //해당 게시글 조회수 +1 증가
    public void updateContentViews(int id) { requestBoardRepository.updateContentViews(id); }

    //해당 게시글 삭제  (안보이게 deleted =1 업데이트)
    public void deleteContents(int id) { requestBoardRepository.deleteContents(id); }

    //댓글 조회
    public List<RepliesDTO> getReply(int id) { return requestBoardRepository.getReply(id); }

    //테스트 해당 글 댓글갯수 - postRepliesCount
    public int postRepliesCount(int id) { return requestBoardRepository.postRepliesCount(id); }


    //댓글 등록
    public boolean addReply(ReplyData replyData) {
        return requestBoardRepository.addReply(replyData);
    }

    //대댓글 등록
    public boolean add_reReply(ReplyData replyData) {
        return requestBoardRepository.add_reReply(replyData);
    }



    //댓글삭제
    public boolean deleteReply(int replyId) {
        return requestBoardRepository.deleteReply(replyId) > 0;
    }


    //게시글 리스트 가져올때(게시글0개여도 가능)
    public int getRequestsByBoardTypeCount(int boardType) {
        int count = requestBoardRepository.getRequestsByBoardTypeCount(boardType);
        return Math.max(count, 0);
    }

    //글저장(insert)
    public void addPost(PostData postData) { requestBoardRepository.addPost(postData); }

    //글"수정" 저장(update)
    public void updatePost(PostData postData) { requestBoardRepository.updatePost(postData); }


    //게시글ajax테스트-카테고리분류된 DTO끌고오기.
    public List<RequestDTO> categoryListMap(Map<String, Integer> categoryListParams) {
        return requestBoardRepository.categoryListMap(categoryListParams);
    }

    //게시글ajax테스트-카테고리분류 + 페이징처리된. DTO끌고오기.
    public List<RequestDTO> categoryPagingList(Map<String, Integer> categoryListParams) {
        return requestBoardRepository.categoryPagingList(categoryListParams);
    }

    //카테고리별ajax게시물갯수.
    public int categoryListMapCount(Map<String, Integer> categoryListParams) {
        int count = requestBoardRepository.categoryListMapCount(categoryListParams);
        return Math.max(count, 0);
    }

    //검색 : 해당 키워드 조회 총 갯수
    public int searchListMapCount(Map<String, Object> searchListParams) {
        int count = requestBoardRepository.searchListMapCount(searchListParams);
        return Math.max(count, 0);
    }

    //검색 : 해당 키워드 조회 총 글 DTO
    public List<RequestDTO> searchPagingList(Map<String, Object> pagingParams) {
        return requestBoardRepository.searchPagingList(pagingParams);
    }


    //해당 게시글 좋아요 기록이 있는지 검사(개수)
    public int checkIfUserLikedPost(Map<String, Integer>likecheckParams) {
        return requestBoardRepository.checkIfUserLikedPost(likecheckParams);
    }

    //해당 게시글에 좋아요 기록 저장.
    public int insertLike(Map<String, Integer> insertLikeParams) {
       return requestBoardRepository.insertLike(insertLikeParams);
    }



}

