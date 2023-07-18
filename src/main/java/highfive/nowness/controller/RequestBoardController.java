package highfive.nowness.controller;
import highfive.nowness.domain.User;
import highfive.nowness.dto.PostData;
import highfive.nowness.dto.RepliesDTO;
import highfive.nowness.dto.ReplyData;
import highfive.nowness.dto.RequestDTO;
import highfive.nowness.service.RequestBoardService;
import highfive.nowness.util.UserUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequestMapping("/request")
@Controller
@RequiredArgsConstructor
public class RequestBoardController {

    @Autowired
    private RequestBoardService requestBoardService;


    //글쓰기창
    @GetMapping("/writer")
    public String boardWriteForm(@AuthenticationPrincipal User user,
                                 @AuthenticationPrincipal OAuth2User oAuth2User,
                                 Model model
    ) {
        //수정해야함.
        if (UserUtil.isNotLogin(user, oAuth2User)) System.out.println("Not Login");
        model.addAttribute(user);

        return "requestWriter";
    }


    //글쓰기 저장.
    @PostMapping("/writer")
    public String boardWriteForm(@RequestParam int userId, @RequestParam String contents, @RequestParam String title,
                                 @RequestParam int boardType, @RequestParam int locale, @RequestParam int subcategory) {


        PostData postData = new PostData();
        postData.setUserId(userId);
        postData.setContents(contents);
        postData.setTitle(title);
        postData.setBoardType(boardType);
        postData.setLocale(locale);
        postData.setSubcategory(subcategory);


        requestBoardService.addPost(postData);

        return "redirect:/request/list";
    }




    //댓글 가져오기
    @GetMapping("/getComments")
    @ResponseBody
    public List<RepliesDTO> getComments(@RequestParam("contentsid") int contentsId) {
        List<RepliesDTO> comments = requestBoardService.getReply(contentsId);
        System.out.println("어디까지" + comments);


        return comments;
    }


    //댓글등록----------우선 유저ID로 저장가능.(수정해야함)
    @RequestMapping(value = "/writeReply", method = RequestMethod.POST)
    public ResponseEntity<String> writeReply(@RequestParam("userid") int userId,
                                             @RequestParam("contentsid") int contentsId,
                                             @RequestParam("reply") String reply) {
        ReplyData replyData = new ReplyData();
        replyData.setContentsId(contentsId);
        replyData.setUserId(userId);
        replyData.setReply(reply);

        boolean success = requestBoardService.addReply(replyData);

        if (success) {
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failure");
        }
    }


    //댓글삭제
    @PostMapping("/deleteReply")
    @ResponseBody
    public ResponseEntity<String> deleteReply(@RequestParam("replyId") int replyId) {
        boolean success = requestBoardService.deleteReply(replyId);

        if (success) {
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failure");
        }
    }




    // 게시글 - 개별 글 세부내용
    @RequestMapping("/post/{id}")
    public String requestPost(@PathVariable("id") int id, Model model, HttpServletRequest request, HttpServletResponse response,
                              @AuthenticationPrincipal User user,
                              @AuthenticationPrincipal OAuth2User oAuth2User
    ) {

        if (UserUtil.isNotLogin(user, oAuth2User)) System.out.println("Not Login");
        System.out.println(user);



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

        model.addAttribute("user", user);

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








    //게시판리스트--------아직제대로 되지않음. (카테고리)
    //카테고리 다시손봐야함..------------- 게시글0개일때 원본.
    //다시수정....------------------클릭으로 들어와지도록.
    //게시글 0개일때 리스트 보이도록 수정.
    @GetMapping("/list")
    public String ajaxPagingTestPost(Model model, @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                     @RequestParam(value = "boardType", required = false, defaultValue = "1") String boardType,
                                     @AuthenticationPrincipal User user,
                                     @AuthenticationPrincipal OAuth2User oAuth2User) {

        if (UserUtil.isNotLogin(user, oAuth2User)) {
            System.out.println("Not Login");
        }
        System.out.println(user);

        int totalRequestCount = requestBoardService.getRequestsByBoardTypeCount(Integer.parseInt(boardType));
        int pageSize = 10;
        int totalPages = totalRequestCount > 0 ? (int) Math.ceil((double) totalRequestCount / pageSize) : 1;

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

        // Add the necessary attributes to the model
        model.addAttribute("lists", list);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRequestCount", totalRequestCount);
        model.addAttribute("user", user);

        return "requestboard";
    }



    @GetMapping("/request/list")
    @ResponseBody
    public List<RequestDTO> getRequestList(Model model,
                                           @RequestParam(value = "boardType", required = false, defaultValue = "1") String boardType,
                                           @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        int totalRequestCount = requestBoardService.getRequestsByBoardTypeCount(Integer.parseInt(boardType));
        int pageSize = 10;
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

//게시글 등록 : 부분구현

//게시글 수정 : 미구현 --->해야함

//게시글 삭제 : 버튼클릭시 삭제O // 사용자=글작성자 확인처리 미구현--->해야함

//게시판리스트 : 수정필요.(카테고리별 미구현)--->해야함

//게시글 세부내용 : 세부내용, 댓글 조회까지 o

//코멘트 : 조회O, 등록o, 삭제o // 대댓글 +작성자만삭제가능. --->해야함

//좋아요 : 미구현--->해야함

//첨부파일 : 미구현--->해야함

//지도 : 미구현--->해야함

//API유해콘텐츠 : 미구현--->해야함


