package highfive.nowness.controller;

import highfive.nowness.dto.RankBoardDTO;
import highfive.nowness.dto.RankBoardPaginationDTO;
import highfive.nowness.service.RankBoardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
//import highfive.nowness.util.UserUtil;

import java.util.*;

// 로그 테스트용
@Slf4j
@Controller
@SpringBootApplication(exclude = SecurityAutoConfiguration.class) // 스프링 부트 로그인 화면 제거
public class RankBoardController {

    @Autowired
    private RankBoardService rankBoardService;
    private RankBoardPaginationDTO rankBoardPaginationDTO = new RankBoardPaginationDTO();
    private List<RankBoardDTO> RankList;

    // 시작시 초기화
    @GetMapping(value="/rankboard")
    public String startView(Model model, @RequestParam(value = "page", required = false) Integer page) {
        Map<String, Object> map = new HashMap<String, Object>();

        //log.info("RankBoard Access");

        if(RankList == null) {
            map.put("ST", "year");
            map.put("SM", "likeCount");
            map.put("FL", "unfindlike");
            // 초기 설정은 년단위, 좋아요, 좋아요 체크 해제 값으로.
            RankList = rankBoardService.getRank(map);
            // 순위 게시글을 받아온다.
            page = 1;
        }

        RankList = rankBoardService.calculation(RankList);
        // 순위 게시글에 들어갈 날짜를 수정하면서 게시글 출력 번호값을 설정한다.
        //RankList = rankBoardService.likeCalculation(RankList, user);
        RankList = rankBoardService.likeCalculation(RankList, 1);
        // 좋아요 값 설정

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
        }
        // 임시 리스트에 출력할 값들의 번호를 받아온다.

        model.addAttribute("RankList", t_RankList);
        model.addAttribute("RankNowPage", rankBoardPaginationDTO.getBlock());
        model.addAttribute("RankStartPage", rankBoardPaginationDTO.getStartBlock());
        model.addAttribute("RankEndPage", rankBoardPaginationDTO.getEndBlock());

        return "/rankboard";
    }


    // 정렬을 바꿨을 경우 (확인 요청 : 정렬 방식 및 좋아요 구분)
    @PostMapping(value = "/dataSend")
    public String dataSend(Model model, @RequestParam Map<String, Object> map)
    {

        // 아이디 테스트, 추후에 추가
        map.put("userid", 1);

        List<RankBoardDTO> RankList = rankBoardService.getRank(map);

        RankList = rankBoardService.calculation(RankList);

        //RankList = rankBoardService.likeCalculation(RankList, user);
        RankList = rankBoardService.likeCalculation(RankList, 1);

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


        model.addAttribute("RankList", t_RankList);
        model.addAttribute("RankNowPage", rankBoardPaginationDTO.getBlock());
        model.addAttribute("RankStartPage", rankBoardPaginationDTO.getStartBlock());
        model.addAttribute("RankEndPage", rankBoardPaginationDTO.getEndBlock());

        return "/rankboard::#contents";
    }

    // 글에 좋아요 누르기
    // 로그인 했는지 확인하기 (기존 리스트에서 좋아요 한 게시글만 찾아 가져오기)
    @PostMapping(value = "/dataSendlike")
    public ResponseEntity<Map<String, Object>> dataSendlike(Model model, @RequestParam Map<String, Object> map) {
        boolean checklike = false;

        // 로그인 테스트
        /*
        if (!UserUtil.isNotLogin(user, oAuth2User)) {
            if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
            UserUtil.addPublicUserInfoToModel(model, user);
        }

        if(user != null) {

            checklike = true;
            long userid= Math.toIntExact(user.getId());
         }
         */

        long userid= 1; // 임시 테스트용
        Long contentsid = Long.valueOf((String) map.get("id"));
        // userid와 contents id를 받는다.
        
        // 만약 로그인 됬다면 true를 반환하고, like 값을 갱신해줍니다.
        rankBoardService.Likeupdate(userid, contentsid);
        checklike = true;

        Map<String, Object> response = new HashMap<>();
        response.put("checklike", checklike);

        return ResponseEntity.ok(response);
    }

    // 좋아요 글만 보기 체크
    // 로그인 했는지 확인하고 안되어 있다면 경고창 출력, 되어 있다면 datasend 출력하기
    @PostMapping(value = "/dataSendonlylike")
    public ResponseEntity<Map<String, Object>> dataSendonlylike(Model model, @RequestParam Map<String, Object> map) {
        //@AuthenticationPrincipal User user, @AuthenticationPrincipal OAuth2User oAuth2User
        // 로그인 되었는지 테스트하기

        boolean checklogin = true; // 테스트 도중에는 무조건 로그인 되었다고 함
        Map<String, Object> response = new HashMap<>();
        response.put("checklogin", checklogin);

        return ResponseEntity.ok(response);
    }
}
