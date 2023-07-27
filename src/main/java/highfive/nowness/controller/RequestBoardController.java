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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
                                 Model model) {
        if (!UserUtil.isNotLogin(user, oAuth2User)) {
            if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
            UserUtil.addPublicUserInfoToModel(model, user);
        }

        model.addAttribute("user", user);

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

        return comments;
    }


    //댓글등록
    @RequestMapping(value = "/writeReply", method = RequestMethod.POST)
    public ResponseEntity<String> writeReply(@RequestParam("userid") int userId,
                                             @RequestParam("contentsid") int contentsId,
                                             @RequestParam("reply") String reply,
                                             @RequestParam(value = "parentid", required = false) Integer parentid
                                             ) {
        boolean success;

        ReplyData replyData = new ReplyData();
        replyData.setContentsId(contentsId);
        replyData.setUserId(userId);
        replyData.setReply(reply);
        //대댓추가
        if (parentid != null) {
            replyData.setParentid(parentid);
            success = requestBoardService.add_reReply(replyData);
        }else {
            success = requestBoardService.addReply(replyData);
        }

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


    //(해당댓글)닉네임가져오기
    @GetMapping("/getNickname/{id}")
    @ResponseBody
    public String getNickname(@PathVariable int id) {
        return requestBoardService.getNickname(id);
    }



    // 게시글 - 개별 글 세부내용
    @RequestMapping("/post/{id}")
    public String requestPost(@PathVariable("id") int id, Model model, HttpServletRequest request, HttpServletResponse response,
                              @AuthenticationPrincipal User user,
                              @AuthenticationPrincipal OAuth2User oAuth2User) {

        if (!UserUtil.isNotLogin(user, oAuth2User)) {
            if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
            UserUtil.addPublicUserInfoToModel(model, user);
        }

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


        //게시글에 좋아요 한 적있는지 로그인유저, 글번호로 체크 후 하트 이미지결정. heart-empty/full
        boolean booleanResult = false;
        if(user!=null) {
            Map<String, Integer> likecheckParams = new HashMap<>();
            likecheckParams.put("contentsid", id);
            likecheckParams.put("userId", Math.toIntExact(user.getId()));

            int result = requestBoardService.checkIfUserLikedPost(likecheckParams);
            booleanResult = result != 0;
        }




        //전달데이터
        model.addAttribute("postdetail", requestPost);
        model.addAttribute("nickname", nickname);
        model.addAttribute("likes", likes);
        model.addAttribute("comments", comments);

        model.addAttribute("user", user);
        model.addAttribute("liked", booleanResult);

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


    //게시글 삭제 - deleted 1로 변경
    @GetMapping("/post/{id}/delete")
    public String deleteContents(@PathVariable("id") int id) {
        requestBoardService.deleteContents(id);
        return "redirect:/request/list";
    }




    //글 수정하는 페이지
    @PostMapping("/modify/{id}")
    public String postModify(@PathVariable("id") Integer id, Model model){
        model.addAttribute("modifypost", requestBoardService.getBoard(id));

        return "requestmodifypost";
    }

    //글수정저장-수정 후 다시 원래페이지로 돌아감.
    @PostMapping("/updatepost")
    public String updatepost(@RequestParam String contents, @RequestParam String title, @RequestParam int id,
                                 @RequestParam int boardType, @RequestParam int locale, @RequestParam int subcategory) {

        PostData postData = new PostData();
        postData.setContents(contents);
        postData.setTitle(title);
        postData.setBoardType(boardType);
        postData.setLocale(locale);
        postData.setSubcategory(subcategory);
        postData.setId(id);

        requestBoardService.updatePost(postData);

        return "redirect:/request/post/"+id;
    }





    //게시판 첫페이지 --기본.
    @GetMapping("/list")
    public String requestboardlist(Model model, @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                     @RequestParam(value = "boardType", required = false, defaultValue = "1") String boardType,
                                     @RequestParam(value = "locale", required = false, defaultValue = "1") int locale,
                                     @RequestParam(value = "subcategory", required = false, defaultValue = "1") int subcategory,
                                     @AuthenticationPrincipal User user,
                                     @AuthenticationPrincipal OAuth2User oAuth2User) {

        //유저 로그인여부판단 후 user에 등록. //비로그인user = null
        if (!UserUtil.isNotLogin(user, oAuth2User)) {
            if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
            UserUtil.addPublicUserInfoToModel(model, user);
        }


        int totalRequestCount = requestBoardService.getRequestsByBoardTypeCount(Integer.parseInt(boardType));
        int pageSize = 10;
        int totalPages = totalRequestCount > 0 ? (int) Math.ceil((double) totalRequestCount / pageSize) : 1;
        int pageIndex = (page - 1) * pageSize;

        //강제로들어올때, 페이지 번호보정.
        if (page <= 0) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }

        Map<String, Integer> pagingParams = new HashMap<>();
        pagingParams.put("boardType", Integer.parseInt(boardType));
        pagingParams.put("pageIndex", pageIndex);
        pagingParams.put("pageSize", pageSize);
        List<RequestDTO> list = requestBoardService.requestboardPagingList(pagingParams);

        model.addAttribute("lists", list); //페이징처리된 게시글 DTO
        model.addAttribute("currentPage", page); //해당 페이지가 몇번째 페이지인지.
        model.addAttribute("totalPages", totalPages);//총 페이지가 몇번째 까지 있는가.
        model.addAttribute("totalRequestCount", totalRequestCount);//총 갯수.
        model.addAttribute("user", user);


        return "requestboard";
    }


    //ajax이후 화면
    @GetMapping("/listtest")
    public String ajaxafterlist(Model model, @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                     @RequestParam(value = "boardType", required = false, defaultValue = "1") String boardType,
                                     @RequestParam(value = "locale", required = false, defaultValue = "1") int locale,
                                     @RequestParam(value = "subcategory", required = false, defaultValue = "1") int subcategory,
                                     @AuthenticationPrincipal User user,
                                     @AuthenticationPrincipal OAuth2User oAuth2User) {

        //유저 로그인여부판단 후 user에 등록. //비로그인user = null
        if (!UserUtil.isNotLogin(user, oAuth2User)) {
            if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
            UserUtil.addPublicUserInfoToModel(model, user);
        }

        Map<String, Integer> categoryListParams = new HashMap<>();
        int pageSize = 10;//한페이지몇개보여줄래
        int pageIndex = (page - 1) * pageSize;//페이징 인덱스.
        categoryListParams.put("locale", locale);
        categoryListParams.put("subcategory", subcategory);
        categoryListParams.put("pageIndex", pageIndex);
        categoryListParams.put("pageSize", pageSize);

        //전체 게시글 갯수
        int totalRequestCount = requestBoardService.categoryListMapCount(categoryListParams);
        int totalPages = (int) Math.ceil((double) totalRequestCount / pageSize);

        //강제로들어올때, 페이지 번호보정.
        if (page <= 0) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }

        Map<String, Integer> pagingParams = new HashMap<>();
        pagingParams.put("boardType", Integer.parseInt(boardType));
        pagingParams.put("pageIndex", pageIndex);
        pagingParams.put("pageSize", pageSize);

        List<RequestDTO> list = requestBoardService.categoryPagingList(categoryListParams);


        model.addAttribute("lists", list); //페이징처리된 게시글 DTO
        model.addAttribute("currentPage", page); //해당 페이지가 몇번째 페이지인지.
        model.addAttribute("totalPages", totalPages);//총 페이지가 몇번째 까지 있는가.
        model.addAttribute("totalRequestCount", totalRequestCount);//총 갯수.
        model.addAttribute("user", user);
        model.addAttribute("locale", locale);
        model.addAttribute("subcategory", subcategory);


        return "requestboard";
    }




    //다시테스트중.
    @GetMapping("/ajaxlist")
    @ResponseBody
    public Map<String, Object> usingajax(
                                    @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                    @RequestParam(value = "boardType", required = false, defaultValue = "1") String boardType,
                                    @RequestParam(value = "locale", required = false, defaultValue = "1") int locale,
                                    @RequestParam(value = "subcategory", required = false, defaultValue = "1") int subcategory
                                     ) {

        Map<String, Object> response = new HashMap<>();
        int pageSize = 10;
        int pageIndex = (page - 1) * pageSize;


        //페이징 : mapper- boardPagingList 게시판타입, 시작인덱스, 갯수(1페이지당)
        //반환값: 페이징처리된 DTO 리스트로 받아옴.
        Map<String, Integer> categoryListParams = new HashMap<>();
        categoryListParams.put("locale", locale);
        categoryListParams.put("subcategory", subcategory);
        categoryListParams.put("pageIndex", pageIndex);
        categoryListParams.put("pageSize", pageSize);

        //토탈 갯수-게시글(카테고리별)
        int totalRequestCount = requestBoardService.categoryListMapCount(categoryListParams);
        int totalPages = (int) Math.ceil((double) totalRequestCount / pageSize);

        //강제로들어올때, 페이지 번호보정.
        if (page <= 0) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }

        Map<String, Integer> pagingParams = new HashMap<>();
        pagingParams.put("boardType", Integer.parseInt(boardType));
        pagingParams.put("pageIndex", pageIndex);
        pagingParams.put("pageSize", pageSize);


        List<RequestDTO> list = requestBoardService.categoryPagingList(categoryListParams);


        response.put("requestList", list);
        response.put("currentPage", page);
        response.put("totalPages", totalPages);
        response.put("totalRequestCount", totalRequestCount);

        return response;
    }

    
    // 조회 : 전체글에대한것만 가능 - 조회항목 : 제목 / 내용 / 이름
    @GetMapping("/list/search")
    public String reportBoardList(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(required = false) String searchType,
                                  @RequestParam(required = false) String searchKeyword,
                                  @AuthenticationPrincipal User user,
                                  @AuthenticationPrincipal OAuth2User oAuth2User,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {

        //유저 로그인여부판단 후 user에 등록. //비로그인user = null
        if (!UserUtil.isNotLogin(user, oAuth2User)) {
            if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
            UserUtil.addPublicUserInfoToModel(model, user);
        }

        //검색컬럼 : searchType = title, contents, user_id (제목, 내용, 작성자)
        //검색어 : searchKeyword

        Map<String, Object> searchListParams = new HashMap<>();
        searchListParams.put("searchType", searchType);
        searchListParams.put("searchKeyword", searchKeyword);


        int totalRequestCount = requestBoardService.searchListMapCount(searchListParams);
        int pageSize = 10;//한페이지10개
        int pageIndex = (page - 1) * pageSize;
        int totalPages = (int) Math.ceil((double) totalRequestCount / pageSize);

        //없는 페이지 요구시 보정.
        if (page <= 0) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
//                if(searchKeyword!=null && searchType!=null){
//                redirectAttributes.addAttribute("page", totalPages);
//                return "redirect:/request/list/search?page={totalPages}&searchType={searchType}&searchKeyword={searchKeyword}";
//                }else{
//                    page = totalPages;
//                }
        }



        //해당 게시물 내용을 가져오자.DTO
        Map<String, Object> pagingParams = new HashMap<>();
        pagingParams.put("pageIndex", pageIndex);
        pagingParams.put("pageSize", pageSize);
        pagingParams.put("searchType", searchType);
        pagingParams.put("searchKeyword", searchKeyword);

        List<RequestDTO> list = requestBoardService.searchPagingList(pagingParams);



        //모델에 정보 넣어서 뷰로 보냄.
        model.addAttribute("lists", list); //페이징처리된 게시글 DTO
        model.addAttribute("currentPage", page); //해당 페이지가 몇번째 페이지인지.
        model.addAttribute("totalPages", totalPages);//총 페이지가 몇번째 까지 있는가.
        model.addAttribute("totalRequestCount", totalRequestCount);//총 갯수.
        model.addAttribute("user", user);
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchKeyword", searchKeyword);

        return "/requestboard";

    }



    //좋아요 저장.
    @PostMapping("/insertlike")
    @ResponseBody
    public boolean insertLike(@RequestParam("contentsid") int contentsid,
                             @RequestParam("userid") int userid) {

        //좋아요 저장.
        Map<String, Integer> insertLikeParams = new HashMap<>();
        insertLikeParams.put("contentsid", contentsid);
        insertLikeParams.put("userid", userid);
        requestBoardService.insertLike(insertLikeParams);

        //ajax -빈하트/풀하트
        Map<String, Integer> likecheckParams = new HashMap<>();
        likecheckParams.put("contentsid", contentsid);
        likecheckParams.put("userId", userid);

        int checkLike = requestBoardService.checkIfUserLikedPost(likecheckParams);

        //좋아요 유무 재검사.
        boolean liked;
        if (checkLike>0){
            liked = true;
        }else{
            liked =false;
        }

        //좋아요 수
        int likes = requestBoardService.getLikes(contentsid);

        return liked;
    }



}

//구현해야할 목록 --------------

//리스트 - 왜......없는 페이지번호 쓰면, 에러로가고.. 페이지 보정이안됨??

//해시태그

//첨부파일 : 미구현--->해야함

//지도 : 미구현--->해야함

//API유해콘텐츠 : 미구현--->해야함

//---------부분구현

//좋아요 : 미구현--->해야함 (좋아요등록O, 좋아요취소는 미구현)-필요시 구현예정.

//검색 : 전체 게시물에서 제목, 내용, 작성자로 조회가능.(카테고리별로는 불가)

//게시글 등록 : 부분구현.(이미지멀티미디어 아직)

//게시글 수정 : 부분구현.(이미지멀티미디어 아직)

//게시글 세부내용 : 세부내용, 댓글 조회까지 o(신고, 좋아요,미구현)

//코멘트 : 조회O, 등록o, 삭제o,작성자만 삭제가능o --->대댓글 미구현.

//게시판리스트 :카테고리별구현O

//---------완료
//글 수정, post변경o
//게시글 삭제 : 구현O
//대댓글 등록: 구현(1단계대댓만 가능)
