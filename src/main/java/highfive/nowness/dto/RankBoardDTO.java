package highfive.nowness.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RankBoardDTO {

    private int id; // 컨텐츠 번호
    private int user_id; // 컨텐츠 작성한 아이디의 번호
    private String nickname; // 작성자의 닉네임
    private Date date; // 글 작성한 날짜
    private int views; // 조회수
    private String contents; // 글 내용
    private String title; // 글 제목
    private int likeCount; // 추천 개수
    private String daycount; // 날짜를 출력에 알맞게 변경하기
    private int viewsnum; // 게시판 페이징을 위한 실제 출력되는 값 가져오기

    public RankBoardDTO(int id, int user_id, String nickname, Date date, int views, String contents, String title, int likeCount)
    {
        this.id = id;
        this.user_id = user_id;
        this.nickname = nickname;
        this.date = date;
        this.views = views;
        this.contents = contents;
        this.title = title;
        this.likeCount = likeCount;

    }

}
