package highfive.nowness.controller;

import highfive.nowness.dto.RepliesDTO;
import highfive.nowness.dto.ReplyData;
import highfive.nowness.dto.RequestDTO;
import highfive.nowness.service.RequestBoardService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequestMapping("/request")
@Controller
@RequiredArgsConstructor
public class RequestBoardController {

    @Autowired
    private RequestBoardService requestBoardService;

    //댓글등록
    // 댓글 등록
//    @RequestMapping(value="/writeReply", method=RequestMethod.POST)
//    public String writeReply(int userid,
//                            int contentsid,
//                             String reply) {
//
//        System.out.println("유저id" + userid +"글번호" + contentsid +"댓글내용" + reply);
//
//        ArrayList<Object> list = new ArrayList<>();
//        list.add(contentsid);
//        list.add(userid);
//        list.add(reply);
//
//        requestBoardService.addReply(list);
//        return "redirect:/post?id=" + contentsid;
//    }

    @RequestMapping(value="/writeReply", method=RequestMethod.POST)
    public String writeReply(@RequestParam("userid") int userId,
                             @RequestParam("contentsid") int contentsId,
                             @RequestParam("reply") String reply) {

        ReplyData replyData = new ReplyData();
        replyData.setContentsId(contentsId);
        replyData.setUserId(userId);
        replyData.setReply(reply);

        requestBoardService.addReply(replyData);
        return "redirect:/request/post/" + contentsId;
//        return "redirect:/request/list" ;
    }




    // 게시글 - 개별 글 세부내용
    @RequestMapping("/post/{id}")
    public String requestPost(@PathVariable("id") int id, Model model, HttpServletRequest request, HttpServletResponse response) {
        // 조회수 증가 (하루에 한 번만 조회수 증가하도록 쿠키 사용)
        String viewCountCookie = getCookieValue(request, "viewCount_" + id);
        if (viewCountCookie == null) {
            requestBoardService.updateContentViews(id);
            setCookie(response, "viewCount_" + id, "true", 24 * 60 * 60); //쿠키 기한 24시간(1일)
        }

        // 한 개 게시글 전체 내용
        RequestDTO requestPost = requestBoardService.getBoard(id);
        // 해당 게시글 닉네임 가져오기
        String nickname = requestBoardService.getNicknameById(id);
        // 해당 게시글 좋아요 개수 가져오기
        int likes = requestBoardService.getLikes(id);
        //코멘트 가져오기
        List<RepliesDTO> comments = requestBoardService.getReply(id);


        //전달데이터
        model.addAttribute("postdetail", requestPost);
        model.addAttribute("nickname", nickname);
        model.addAttribute("likes", likes);
        model.addAttribute("comments", comments);

        return "requestpost";
    }


    //조회수 관련 -  이름별 쿠키값 검색
    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    //조회수 관련 - 쿠키 설정
    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }


    //게시글 삭제 - deleted 1로 변경 // **현재 사용자==글사용자 확인 구현X (클릭해서지워지기만함)
    @GetMapping("/post/{id}/delete")
    public String deleteContents(@PathVariable("id") int id) {
        requestBoardService.deleteContents(id);
        return "redirect:/request/list"; //삭제 후 리스트로 돌아감
    }








    //카테고리 다시손봐야함..-------------
    //다시수정....------------------클릭으로 들어와지도록.
    @GetMapping("/list")
    public String ajaxPagingTestPost(Model model,
                                     @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                     @RequestParam(value = "boardType", required = false, defaultValue = "1") String boardType) {
        int totalRequestCount = requestBoardService.getRequestsByBoardTypeCount(Integer.parseInt(boardType));
        int pageSize = 20;
        int totalPages = (int) Math.ceil((double) totalRequestCount / pageSize);

        if (page <= 0) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }

        int pageIndex = (page - 1) * pageSize;

        Map<String, Integer> pagingParams = new HashMap<>();
        pagingParams.put("boardType", Integer.parseInt(boardType));
        pagingParams.put("pageIndex", pageIndex);
        pagingParams.put("pageSize", pageSize);
        List<RequestDTO> list = requestBoardService.requestboardPagingList(pagingParams);

        //개수--테스트

//        System.out.println("request DTO 정보" + list);
        
        model.addAttribute("lists", list);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRequestCount", totalRequestCount);

        return "requestboard";
    }

    @GetMapping("/request/list")
    @ResponseBody
    public List<RequestDTO> getRequestList(Model model,
                                            @RequestParam(value = "boardType", required = false, defaultValue = "1") String boardType,
                                           @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        int totalRequestCount = requestBoardService.getRequestsByBoardTypeCount(Integer.parseInt(boardType));
        int pageSize = 20;
        int totalPages = (int) Math.ceil((double) totalRequestCount / pageSize);

        if (page <= 0) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }

        int pageIndex = (page - 1) * pageSize;


        Map<String, Integer> pagingParams = new HashMap<>();
        pagingParams.put("boardType", Integer.parseInt(boardType));
        pagingParams.put("pageIndex", pageIndex);
        pagingParams.put("pageSize", pageSize);
        List<RequestDTO> list = requestBoardService.requestboardPagingList(pagingParams);

        model.addAttribute("lists", list);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return list;
    }







}

//구현해야할 목록 --------------

//게시글 등록 : 미구현

//게시글 수정 : 등록 미구현으로 구현X

//게시글 삭제 : 버튼클릭시 삭제O // 사용자=글작성자 확인처리 미구현

//게시판리스트 : 수정필요.(카테고리별 미구현)

//게시글 세부내용 : 세부내용, 댓글 조회까지 o

//코멘트 : 조회O, 등록 삭제 미구현

//좋아요 : 미구현

//첨부파일 : 미구현

//지도 : 미구현

//API유해콘텐츠 : 미구현