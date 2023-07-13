package highfive.nowness.service;

import highfive.nowness.domain.RankBoard;
import highfive.nowness.repository.RankBoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class RankBoardService {

    @Autowired
    private RankBoardRepository rankBoardRepository;

    public List<RankBoard> getRank() {

        //DTO 수정 시 필요

        String Datediff = "";
        LocalDateTime nowtime = LocalDateTime.now();
        Date date = new Date();

        if(nowtime.getYear() != date.getYear()) {
            Datediff = date.getYear() - nowtime.getYear() + "년 전";
        }
        else if(nowtime.getMonthValue() != date.getMonth())
        {
            Datediff = nowtime.getMonthValue() - date.getMonth() + "개월 전";
        }
        else if(nowtime.getDayOfMonth() != date.getDay())
        {
            Datediff = nowtime.getDayOfMonth() - date.getDay() + "일 전";
        }
        else if(nowtime.getHour() != date.getHours())
        {
            Datediff = nowtime.getHour() - date.getHours() + "시간 전";
        }
        else if(nowtime.getMinute() != date.getMinutes())
        {
            Datediff = nowtime.getMinute() - date.getMinutes() + "분 전";
        }
        else if(nowtime.getSecond() != date.getSeconds())
        {
            Datediff = nowtime.getSecond() - date.getSeconds() + "초 전";
        }

        return rankBoardRepository.getRank();
    }
}
