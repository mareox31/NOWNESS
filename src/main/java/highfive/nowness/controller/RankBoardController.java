package highfive.nowness.controller;

import highfive.nowness.dto.RankBoardDTO;
import highfive.nowness.dto.RankBoardPaginationDTO;
import highfive.nowness.service.RankBoardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// 로그 테스트용
@Slf4j
@Controller
public class RankBoardController {

    @Autowired
    private RankBoardService rankBoardService;
    private RankBoardPaginationDTO rankBoardPaginationDTO = new RankBoardPaginationDTO();
    private List<RankBoardDTO> RankList;

    @GetMapping(value="/rankboard")
    public String startView(Model model, @RequestParam(value = "page", required = false) Integer page) {
        Map<String, Object> map = new HashMap<String, Object>();

        log.info("RankBoard Access");

        if(page == null) {
            map.put("ST", "year");
            map.put("SM", "likeCount");
            // 초기 설정은 년단위, 좋아요 순으로 조회를 할 수 있도록 만든다.
            RankList = rankBoardService.getRank(map);
            // 순위 게시글을 받아온다.
            page = 1;
        }

        RankList = rankBoardService.calculation(RankList);
        // 순위 게시글에 들어갈 날짜를 수정하면서 게시글 출력 번호값을 설정한다.

        rankBoardPaginationDTO.setBlock(page); // 현재 블록 세팅

        rankBoardPaginationDTO = rankBoardService.pagination(RankList, rankBoardPaginationDTO);
        // 페이징을 위해 게시글 데이터를 받아와 페이징 DTO에 가공하여 넣어준다.

        List<RankBoardDTO> t_RankList = new LinkedList<RankBoardDTO>();
        // 임시 배열을 만든다.

        for(int i = 0; i < RankList.size(); i++) {
            if((rankBoardPaginationDTO.getBlock() - 1) * 10 == RankList.get(i).getViewsnum()){
                for(int j = 0; j < rankBoardPaginationDTO.getPageSize(); j++) {
                    t_RankList.add(RankList.get(i+j));
                }
                break;
            }
            //log.info(String.valueOf((rankBoardPaginationDTO.getBlock() - 1) * 10));
            //log.info(String.valueOf(RankList.get(i).getViewsnum()));
        }
        // 임시 리스트에 출력할 값들의 번호를 받아온다.

        model.addAttribute("RankList", t_RankList);
        model.addAttribute("RankNowPage", rankBoardPaginationDTO.getBlock());
        model.addAttribute("RankStartPage", rankBoardPaginationDTO.getStartBlock());
        model.addAttribute("RankEndPage", rankBoardPaginationDTO.getEndBlock());
        //model.addAttribute("RankList", RankList);

        return "/rankboard";
    }

    @PostMapping(value = "/dataSend")
    public String dataSend(Model model, @RequestParam Map<String, Object> map)
    {
        log.info("DataSend Access");
        List<RankBoardDTO> RankList = rankBoardService.getRank(map);

        RankList = rankBoardService.calculation(RankList);

        rankBoardPaginationDTO.setBlock(1);

        rankBoardPaginationDTO = rankBoardService.pagination(RankList, rankBoardPaginationDTO);

        List<RankBoardDTO> t_RankList = new LinkedList<RankBoardDTO>();

        for(int i = 0; i < RankList.size(); i++) {
            if((rankBoardPaginationDTO.getBlock() - 1) * 10 == RankList.get(i).getViewsnum()){
                for(int j = 0; j < rankBoardPaginationDTO.getPageSize(); j++) {
                    t_RankList.add(RankList.get(i+j));
                }
                break;
            }
        }

        //log.info(String.valueOf(rankBoardPaginationDTO.getStartBlock()));
        //log.info(String.valueOf(rankBoardPaginationDTO.getEndBlock()));

        model.addAttribute("RankList", t_RankList);
        model.addAttribute("RankNowPage", rankBoardPaginationDTO.getBlock());
        model.addAttribute("RankStartPage", rankBoardPaginationDTO.getStartBlock());
        model.addAttribute("RankEndPage", rankBoardPaginationDTO.getEndBlock());

        return "/rankboard::#contents";
    }
}
