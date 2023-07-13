package highfive.nowness.repository;
import highfive.nowness.dto.RepliesDTO;
import highfive.nowness.dto.ReplyData;
import highfive.nowness.dto.RequestDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RequestBoardRepository {

    @Autowired
    private SqlSessionTemplate sql;


    //전체 게시글(전체내용)
    public List<RequestDTO> findAll() {
        return sql.selectList("request.findAll");
    }

    // 페이징 처리 -'맵'사용
    public List<RequestDTO> boardPagingList(Map<String, Integer> pagingParams) {
        return sql.selectList("request.boardPagingList", pagingParams);
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

    //해당 게시글 좋아요(갯수) 가져오기
    public int getLikes(int id) { return sql.selectOne("request.getLikes", id); }


    //조회수증가-리턴X
    public void updateContentViews(int id){ sql.selectOne("request.updateContentViews",id); }

    //글삭제 -(deleted=1해서 안보이도록 처리)
    public void deleteContents(int id){ sql.selectOne("request.deleteContents",id); }

    //댓글조회 - 콘텐츠이름.
    public List<RepliesDTO> getReply(int id) { return sql.selectList("request.getReply",id); }




    //테스트중 ---------카테고리-----
    public int getRequestsByBoardTypeCount(int boardType) {
        return sql.selectOne("request.getRequestsByBoardTypeCount", boardType);
    }

    //테스트중 : 댓글등록
//    public boolean addReply(Arraylist list) { return sql.selectOne("request.addReply",list); }
//    public void addReply(ArrayList<Object> list) { sql.insert("request.addReply", list);
//    }

    public void addReply(ReplyData replyData) {
        sql.insert("request.addReply", replyData);
    }



}

