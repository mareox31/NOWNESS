package highfive.nowness.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RankBoardPaginationDTO {
    private int pageSize = 9; // 페이지 당 보여지는 게시글의 최대 개수
    private int blockSize = 10; // 페이징된 버튼의 블럭당 최대 개수
    private int block = 1; // 현재 블록
    private int totalListCnt; // 총 게시글 수
    private int totalPageCnt; // 총 페이지 수
    private int totalBlockCnt;
    private int startBlock; // 블럭 시작 페이지
    private int endBlock; // 블럭 마지막 페이지
    private int startIndex; // 시작 페이지
    private int prevBlock; // 이전 블럭의 마지막 페이지(ID)
    private int nextBlock; // 다음 블럭의 시작 페이지(ID)
}
