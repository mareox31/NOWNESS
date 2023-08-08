package highfive.nowness.service;

import highfive.nowness.dto.RankBoardDTO;
import highfive.nowness.dto.RankBoardPaginationDTO;
import highfive.nowness.repository.RankBoardRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class RankBoardService {

    @Autowired
    private RankBoardRepository rankBoardRepository;
    private RankBoardPaginationDTO t_pageDTO = new RankBoardPaginationDTO();
    // 날짜 년월일로 계산시키기
    public List<RankBoardDTO> calculation(List<RankBoardDTO> RankList) {

        for(int i = 0; i < RankList.size(); i++) {
            RankBoardDTO temp = RankList.get(i);
            if(temp == null) continue;

            String Datediff = "";
            LocalDateTime nowtime = LocalDateTime.now();

            //mysql 날짜 값 받아오기
            String dateString = String.valueOf(temp.getDate());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            LocalDateTime date = LocalDateTime.parse(dateString, formatter);

            Duration duration = Duration.between(date, nowtime);

            long years = duration.toDays() / 365;
            long months = duration.toDays() / 30;
            long days = duration.toDays();
            long hours = duration.toHours();
            long minutes = duration.toMinutes();
            long seconds = duration.getSeconds();

            if (years > 0) {
                Datediff = years + "년 전";
            } else if (months > 0) {
                Datediff = months + "개월 전";
            } else if (days > 0) {
                Datediff = days + "일 전";
            } else if (hours > 0) {
                Datediff = hours + "시간 전";
            } else if (minutes > 0) {
                Datediff = minutes + "분 전";
            } else {
                Datediff = seconds + "초 전";
            }

            
            // 이미지 추출
            String text = RankList.get(i).getContents();
            String t_img = text;
            String pattern = "src=\"(.*?)\"";

            Pattern srcPattern = Pattern.compile(pattern);
            Matcher matcher = srcPattern.matcher(t_img);

            if (matcher.find()) {
                t_img = matcher.group(1);
                RankList.get(i).setImgsrc(t_img);
            }

            // html 태그 제거
            String convertingText = Jsoup.clean(text, Whitelist.none());
            RankList.get(i).setContents(convertingText);

            if(temp.getTagString() != null) {
                String[] tagarr = temp.getTagString().split(" ");
                ArrayList<String> arrTemp = new ArrayList<>();

                for (int j = 0; j < tagarr.length; j++) {
                    if(!Objects.equals(tagarr[j], "0")) arrTemp.add(tagarr[j]);
                    if(j >= 4) break;
                }
                RankList.get(i).setTagarray(arrTemp);
            }

            RankList.get(i).setDaycount(Datediff);
            RankList.get(i).setViewsnum(i);

        }
        return RankList;
    }

    public RankBoardPaginationDTO pagination(List<RankBoardDTO> RankList, RankBoardPaginationDTO pageDTO) {

        pageDTO.setPageSize(t_pageDTO.getPageSize()); // 페이지 당 보여지는 게시글의 최대 개수
        pageDTO.setBlockSize(t_pageDTO.getBlockSize()); // 페이징된 버튼의 블럭당 최대 개수

        int temp = (int)Math.ceil(RankList.size()*1.0/ pageDTO.getPageSize());
        if(RankList.size() % pageDTO.getPageSize() != 0 && RankList.size() > temp * pageDTO.getPageSize()) temp++;

        pageDTO.setTotalBlockCnt(temp); // 총 블록 수
        pageDTO.setTotalPageCnt(RankList.size()); // 총 게시글 수

        pageDTO.setStartBlock(((pageDTO.getBlock()-1) / pageDTO.getBlockSize()) * pageDTO.getBlockSize() + 1); // 시작 블록

        if(pageDTO.getBlock() < 1) {
            pageDTO.setBlock(1);;
        }

        pageDTO.setEndBlock(pageDTO.getStartBlock() + pageDTO.getBlockSize() - 1); // 종료 블록

        if(pageDTO.getEndBlock() >= pageDTO.getTotalBlockCnt()) {
            pageDTO.setEndBlock(pageDTO.getTotalBlockCnt()); // 종료 블록이 총 블록 개수를 넘어가면 안됨.
        }

        // 종료 블록
        if(pageDTO.getBlock() > pageDTO.getEndBlock()) {
            pageDTO.setBlock(pageDTO.getEndBlock());;
        }

        //log.info("pagesize");
        //log.info(String.valueOf(pageDTO.getPageSize()));

        //log.info("block");
        //log.info(String.valueOf(pageDTO.getBlock()));

        //log.info("total");
        //log.info(String.valueOf(pageDTO.getTotalPageCnt()));

        //log.info("blockcnt");
        //log.info(String.valueOf(pageDTO.getTotalBlockCnt()));

        if(pageDTO.getBlock() == pageDTO.getTotalBlockCnt()) {
            if(pageDTO.getTotalPageCnt() <= pageDTO.getPageSize()) {
                pageDTO.setPageSize(pageDTO.getTotalPageCnt());
            }
            else {
                if(pageDTO.getTotalPageCnt()%pageDTO.getPageSize() != 0) pageDTO.setPageSize(pageDTO.getTotalPageCnt()%pageDTO.getPageSize()); // 마지막 블록일 경우 카드형 출력 개수 값을 조정해줍니다.
            }
        }

        if(pageDTO.getEndBlock() == 0) pageDTO.setEndBlock(1); // 0일 경우를 대비하여 1로 설정

        return pageDTO;
    }

    // 좋아요 올리기 혹은 내리기 (유저 아이디 값, 좋아요 id) 조회 후 값이 있다면 올리고, 없다면 내리기
    public void Likeupdate(long userid, long contentsid) {
        boolean temp = rankBoardRepository.findLike(userid, contentsid);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("contentsId", contentsid);
        paramMap.put("userId", userid);
        if(temp) {
            paramMap.put("action", "delete");
        }
        else {
            paramMap.put("action", "insert");
        }
        rankBoardRepository.deleteOrInsertLike(paramMap);
    }

    public List<RankBoardDTO> getRank(Map<String, Object> params) { return rankBoardRepository.getRank(params); }
}
