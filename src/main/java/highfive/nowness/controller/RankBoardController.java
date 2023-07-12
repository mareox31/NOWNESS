package highfive.nowness.controller;

import highfive.nowness.domain.RankBoard;
import highfive.nowness.service.RankBoardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

// 로그 테스트용
@Slf4j
@Controller
public class RankBoardController {

    @Autowired
    private RankBoardService rankBoardService;

    @RequestMapping(value = "/rankboard", method = RequestMethod.GET)
    public ModelAndView goHome(HttpServletRequest request) {
        ModelAndView ran = new ModelAndView();
        List<RankBoard> RankList = rankBoardService.getRank();
        ran.addObject("RankList", RankList);

        log.info(RankList.get(0).getContents());
        log.info(RankList.get(0).getTitle());
        log.info("잘 작동하는지?");
        ran.setViewName("rankboard.html");
        return ran;
    }
}
