package highfive.nowness.service;

import highfive.nowness.dto.RankBoardDTO;
import highfive.nowness.dto.RankBoardPaginationDTO;
import highfive.nowness.repository.RankBoardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class RankBoardService {

    @Autowired
    private RankBoardRepository rankBoardRepository;
    private RankBoardPaginationDTO t_pageDTO = new RankBoardPaginationDTO();
    // 날짜 년월일로 계산시키기
    public List<RankBoardDTO> calculation(List<RankBoardDTO> RankList) {

        log.info("calculation Access");

        for(int i = 0; i < RankList.size(); i++) {
            RankBoardDTO temp = RankList.get(i);
            if(temp == null) continue;

            String Datediff = "";
            LocalDateTime nowtime = LocalDateTime.now();
            //Date date = new Date();

            //mysql 날짜 값 받아오기
            String dateString = String.valueOf(temp.getDate());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            LocalDateTime date = LocalDateTime.parse(dateString, formatter);

            //log.info(String.valueOf(date));
            //log.info(String.valueOf(date.getYear()));
            //log.info(String.valueOf(date.getMonth()));
            //log.info(String.valueOf(nowtime.getYear()));

            if (nowtime.getYear() != date.getYear()) {
                Datediff = nowtime.getYear() - date.getYear() + "년 전";
            } else if (nowtime.getMonthValue() != date.getMonthValue()) {
                Datediff = nowtime.getMonthValue() - date.getMonthValue() + "개월 전";
            } else if (nowtime.getDayOfMonth() != date.getDayOfMonth()) {
                Datediff = nowtime.getDayOfMonth() - date.getDayOfMonth() + "일 전";
            } else if (nowtime.getHour() != date.getHour()) {
                Datediff = nowtime.getHour() - date.getHour() + "시간 전";
            } else if (nowtime.getMinute() != date.getMinute()) {
                Datediff = nowtime.getMinute() - date.getMinute() + "분 전";
            } else if (nowtime.getSecond() != date.getSecond()) {
                Datediff = nowtime.getSecond() - date.getSecond() + "초 전";
            }

            //log.info(Datediff);
            RankList.get(i).setDaycount(Datediff);
            RankList.get(i).setViewsnum(i);
        }
        return RankList;
    }

    public RankBoardPaginationDTO pagination(List<RankBoardDTO> RankList, RankBoardPaginationDTO pageDTO) {

        pageDTO.setPageSize(t_pageDTO.getPageSize()); // 페이지 당 보여지는 게시글의 최대 개수
        pageDTO.setBlockSize(t_pageDTO.getBlockSize()); // 페이징된 버튼의 블럭당 최대 개수

        pageDTO.setTotalBlockCnt((int)Math.ceil(RankList.size()*1.0/ pageDTO.getBlockSize())); // 총 블록 수
        pageDTO.setTotalPageCnt(RankList.size()); // 총 게시글 수

        pageDTO.setStartBlock((pageDTO.getBlock() / pageDTO.getBlockSize()) * pageDTO.getBlockSize()+1); // 시작 블록
        pageDTO.setEndBlock(pageDTO.getStartBlock() + pageDTO.getBlockSize()-1); // 종료 블록

        //log.info(String.valueOf(pageDTO.getEndBlock()));
        //log.info(String.valueOf(pageDTO.getTotalBlockCnt()));

        if(pageDTO.getEndBlock() > pageDTO.getTotalBlockCnt()) pageDTO.setEndBlock(pageDTO.getTotalBlockCnt()); // 종료 블록이 총 블록 개수를 넘어가면 안됨.
        if(pageDTO.getBlock() == pageDTO.getTotalBlockCnt()) pageDTO.setPageSize(pageDTO.getTotalPageCnt()/pageDTO.getPageSize()); // 마지막 블록일 경우 페이지 출력 개수 값을 조정해줍니다.

        if(pageDTO.getEndBlock() == 0) pageDTO.setEndBlock(1); // 0일 경우를 대비하여 1로 설정

        return pageDTO;
    }

    public List<RankBoardDTO> getRank(Map<String, Object> params) { return rankBoardRepository.getRank(params); }
}
