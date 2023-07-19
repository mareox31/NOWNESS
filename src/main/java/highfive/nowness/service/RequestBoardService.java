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

    // 페이징 처리 -'맵'사용
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


    //댓글 등록
    public boolean addReply(ReplyData replyData) {
        return requestBoardRepository.addReply(replyData);
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

}

