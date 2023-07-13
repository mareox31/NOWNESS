package highfive.nowness.dto;

import java.util.Date;

public class RankBoardDTO {
    private int id; // 컨텐츠 번호
    private int user_id; // 컨텐츠 작성한 아이디의 번호
    private String nickname; // 작성자의 닉네임
    private Date date; // 글 작성한 날짜
    private int views; // 조회수
    private String contents; // 글 내용
    private String title; // 글 제목
    private int likeCount; // 추천 개수

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
