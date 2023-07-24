package highfive.nowness.repository;
import highfive.nowness.dto.PostData;
import highfive.nowness.dto.RepliesDTO;
import highfive.nowness.dto.ReplyData;
import highfive.nowness.dto.RequestDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RequestBoardRepository {


    @Autowired
    private SqlSessionTemplate sql;


    //전체 게시글(전체내용)
    public List<RequestDTO> findAll() {
        return sql.selectList("request.findAll");
    }


    //게시글리리스트 조회(닉네임포함)
    public List<RequestDTO> boardPagingList(Map<String, Integer> pagingParams) {
        List<RequestDTO> requestList = sql.selectList("request.boardPagingList", pagingParams);
        for (RequestDTO requestDTO : requestList) {
           String nickname = sql.selectOne("request.getNickname2", requestDTO);
            requestDTO.setNickname(nickname);
        }
        return requestList;
    }

    //게시글 전체 ( 총 개수 )
    public int getTotalRequestCount() {
        return sql.selectOne("request.getTotalRequestCount");
    }

    //해당 번호 게시글 상세내용(DTO)
    public RequestDTO getBoard(int id) {
        return sql.selectOne("request.getBoard", id);
    }


    //해당 게시글 닉네임가져오기
    public String getNicknameById(int id) {
        return sql.selectOne("request.getNicknameById", id);
    }

    //리플쓴 유저id로 해당하는 닉네임 가져오기
    public String getNickname(int id) {
        return sql.selectOne("request.getNickname", id);
    }


    //해당 게시글 좋아요(갯수) 가져오기
    public int getLikes(int id) { return sql.selectOne("request.getLikes", id); }


    //조회수증가-리턴X
    public void updateContentViews(int id){ sql.selectOne("request.updateContentViews",id); }

    //글삭제 -(deleted=1해서 안보이도록 처리)
    public void deleteContents(int id){ sql.selectOne("request.deleteContents",id); }


    //댓글조회 - 콘텐츠이름.
    public List<RepliesDTO> getReply(int id) { return sql.selectList("request.getReply",id); }


    //댓글 등록
    public boolean addReply(ReplyData replyData) {
        return sql.insert("request.addReply", replyData) == 1;
    }

    //댓글 삭제
    public int deleteReply(int replyId) {
        Integer result = sql.selectOne("request.deleteReply", replyId);
        return result != null ? result.intValue() : 1;
    }


    //글저장(insert)
    public void addPost(PostData postData) {
        sql.insert("request.addPost", postData);
    }

    //글"수정" 저장(update)
    public void updatePost(PostData postData) {
        sql.insert("request.updatePost", postData);
    }

    //카테고리 리스트용-----
    public int getRequestsByBoardTypeCount(int boardType) {
        return sql.selectOne("request.getRequestsByBoardTypeCount", boardType);
    }

    //ajax카테고리별게시물가져오기
    public List<RequestDTO> categoryListMap(Map<String, Integer> categoryListParams) {
        return sql.selectList("request.categoryListMap", categoryListParams);
    }


    //ajax카테고리별게시물갯수.
    public int categoryListMapCount(Map<String, Integer> categoryListParams) {
        return sql.selectOne("request.categoryListMapCount", categoryListParams);
    }


    //테스트중ajax카테고리별 페이징처리된 게시물가져오기
    public List<RequestDTO> categoryPagingList(Map<String, Integer> categoryListParams) {
        List<RequestDTO> requestList = sql.selectList("request.categoryPagingList", categoryListParams);
        for (RequestDTO requestDTO : requestList) {
            String nickname = sql.selectOne("request.getNicknameByUserId", requestDTO.getUserId());
            requestDTO.setNickname(nickname);
        }
        return requestList;
    }


}


