package highfive.nowness.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import highfive.nowness.domain.User;
import highfive.nowness.dto.*;
import highfive.nowness.service.RequestBoardService;
import highfive.nowness.util.UserUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;


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
                                 @RequestParam int boardType, @RequestParam int locale, @RequestParam int subcategory,
                                 @RequestParam List<String> hashtags,
                                 @RequestParam("files") List<MultipartFile> files) throws JsonProcessingException {

        PostData postData = new PostData();
        postData.setUserId(userId);
        postData.setContents(contents);
        postData.setTitle(title);
        postData.setBoardType(boardType);
        postData.setLocale(locale);
        postData.setSubcategory(subcategory);


        int postId = requestBoardService.addPost2(postData); //글 저장 + 글 번호 반환.


        //글에 대한 해시태그 저장
        requestBoardService.addTags(hashtags, postId);


        //파일저장 :DB와 서버에 저장
//        String savePath = "c:/kdt/upload/nowness/"; //파일저장경로.
        String savePath ="/usr/mydir/upload/";//서버 파일 저장경로

        for (MultipartFile file : files) {
            if (file.isEmpty() || file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
                continue;
            }

            FileData fileData = new FileData();

            fileData.setContentsid(postId);//글번호저장.
            //원본이름+ _ +uuid + . + 확장자로 저장됨.
            String originalFilenameWithoutExtension = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.'));
            String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);
            String savedFileName = originalFilenameWithoutExtension + "_" + UUID.randomUUID().toString() + "." + fileExtension;

            fileData.setSaveName(savedFileName);
            fileData.setPath(savePath);
            fileData.setSize(file.getSize());
            fileData.setExt(fileExtension);

            //파일 뒤에 _로 끝나는거 에러 방지.
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                if (originalFilename.endsWith("_")) {
                    String[] parts = originalFilename.split("_");
                    if (parts.length >= 2) {
                        fileData.setOrginName(parts[0]);
                    }
                } else {
                    fileData.setOrginName(originalFilename);
                }
            }

            // DB저장
            requestBoardService.saveFileData(fileData);

            // 서버에 저장
            Path filePath = Paths.get(savePath, savedFileName);
            try {
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
            }
        }



        return "redirect:/request/list";
    }



    //댓글 가져오기
    @GetMapping("/getComments")
    @ResponseBody
    public List<RepliesDTO> getComments(@RequestParam("contentsid") int contentsId) {
        List<RepliesDTO> comments = requestBoardService.getReply(contentsId);

        return comments;
    }

    //자식 댓글 갯수 가져오기(삭제x)
    @GetMapping("/childCommentsCount")
    @ResponseBody
    public int childCommentsCount(@RequestParam("id") int id) {
        return requestBoardService.childCommentsCount(id);
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



    //파일 다운로드.
    @PostMapping("/download/{fileId}")
//    public ResponseEntity<byte[]> downloadFile(@PathVariable("fileId") int fileId) -get용
    public ResponseEntity<byte[]> downloadFile(@RequestParam("fileId") int fileId) {
        FileData fileData = requestBoardService.getFileById(fileId);

        if (fileData != null) {
           //파일 가져오는 경로
//            String filePath = "c:/kdt/upload/nowness/" + fileData.getSaveName();//로컬용
            String filePath = "/usr/mydir/upload/" + fileData.getSaveName(); //서버용

            try {
                byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
                String contentType = "application/octet-stream";
                //한글인코딩
                String encodedFileName = UriUtils.encode(fileData.getOrginName(), "UTF-8");
                String contentDisposition = "attachment; filename=" + encodedFileName;

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                        .body(fileContent);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
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
        //글에 해당하는 태그 가져오기.
        List<TagsDTO> tagsDTOList = requestBoardService.getTags(id);

        //파일정보 가져오기.
        List<FileData>  fileDatalist = requestBoardService.getFileByContentsId(id);

        //댓글 개수(deleted=0)
        int repliesCount = requestBoardService.getRepliesCount(id);


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
        model.addAttribute("tagslist", tagsDTOList);
        model.addAttribute("fileDatalist", fileDatalist);
        model.addAttribute("repliesCount", repliesCount);



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
        model.addAttribute("tagslist", requestBoardService.getTags(id));
        //테스트중/파일정보 가져오기.
        model.addAttribute("existingFiles", requestBoardService.getFileByContentsId(id)); // Add the existing files to the model

        return "requestmodifypost";
    }

    //글수정저장-수정 후 다시 원래페이지로 돌아감.
    @PostMapping("/updatepost")
    public String updatepost(@RequestParam String contents, @RequestParam String title, @RequestParam int id,
                                 @RequestParam int boardType, @RequestParam int locale, @RequestParam int subcategory,
                             @RequestParam List<String> hashtags,
                             @RequestParam("files") List<MultipartFile> files,
                             @RequestParam(value = "existingSavenames", required = false) List<String> existingSavenames) {

        PostData postData = new PostData();
        postData.setContents(contents);
        postData.setTitle(title);
        postData.setBoardType(boardType);
        postData.setLocale(locale);
        postData.setSubcategory(subcategory);
        postData.setId(id);

        int postId = requestBoardService.updatePost2(postData); //글 저장 + 글 번호 반환.

        //태그
        List<TagsDTO> existingTagsDTO = requestBoardService.getTags(id);
        List<String> existingTags = existingTagsDTO.stream().map(TagsDTO::getTag).collect(Collectors.toList());


        //기존에 없던거 추가.
        List<String> newTags = new ArrayList<>();
        for (String tag : hashtags) {
            if (!existingTags.contains(tag)) {
                newTags.add(tag);
            }
        }
        if (!newTags.isEmpty()) {
            requestBoardService.addTags(newTags, postId);
        }

        // 지울거
        List<String> deletedTags = new ArrayList<>();
        for (String tag : existingTags) {
            if (!hashtags.contains(tag)) {
                deletedTags.add(tag);
            }
        }
        if (!deletedTags.isEmpty()) {
            requestBoardService.removeTags(deletedTags, postId);
        }


        //파일 관련--
        //DB 해당게시글에 저장된 첨부파일(전체파일DTO)
        List<FileData> existingFilesList = requestBoardService.getFileByContentsId(postId);

        //지울 목록 생성
        List<Long> deleteFileList = new ArrayList<>();

        if (existingSavenames != null) {
            for (FileData fileData : existingFilesList) {
                if (!existingSavenames.contains(fileData.getSaveName())) {
                    deleteFileList.add(fileData.getId());
                }
            }
        } else {
            for (FileData fileData : existingFilesList) {
                deleteFileList.add(fileData.getId());
            }
        }

        //DB서 파일정보 삭제
        if (!deleteFileList.isEmpty()) {
            requestBoardService.deleteFilesByIds(deleteFileList);
        }


        //파일저장 :DB와 서버에 저장
//        String savePath = "c:/kdt/upload/nowness/"; //파일저장경로.로컬용
        String savePath = "/usr/mydir/upload/"; //파일저장경로.서버용

        for (MultipartFile file : files) {
            if (file.isEmpty() || file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
                continue;
            }

            FileData fileData = new FileData();

            fileData.setContentsid(postId);//글번호저장.
            //원본이름+ _ +uuid + . + 확장자로 저장됨.
            String originalFilenameWithoutExtension = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.'));
            String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);
            String savedFileName = originalFilenameWithoutExtension + "_" + UUID.randomUUID().toString() + "." + fileExtension;

            fileData.setSaveName(savedFileName);
            fileData.setPath(savePath);
            fileData.setSize(file.getSize());
            fileData.setExt(fileExtension);

            //파일 뒤에 _로 끝나는거 에러 방지.
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                if (originalFilename.endsWith("_")) {
                    String[] parts = originalFilename.split("_");
                    if (parts.length >= 2) {
                        fileData.setOrginName(parts[0]);
                    }
                } else {
                    fileData.setOrginName(originalFilename);
                }
            }

            // DB저장
            requestBoardService.saveFileData(fileData);

            // 서버에 저장
            Path filePath = Paths.get(savePath, savedFileName);
            try {
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
            }
        }


        return "redirect:/request/post/"+id;
    }





    //게시판 첫페이지 --기본.
    @GetMapping("/list")
    public String requestboardlist(Model model, @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                     @RequestParam(value = "boardType", required = false, defaultValue = "1") String boardType,
                                   @RequestParam(value = "locale", required = false) Integer locale,
                                   @RequestParam(value = "subcategory", required = false) Integer subcategory,
                                     @AuthenticationPrincipal User user,
                                     @AuthenticationPrincipal OAuth2User oAuth2User) {

        //유저 로그인여부판단 후 user에 등록. //비로그인user = null
        if (!UserUtil.isNotLogin(user, oAuth2User)) {
            if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
            UserUtil.addPublicUserInfoToModel(model, user);
        }

        if(page<=0){
            page=1;
        }


        //카테고리 있는 경우.
        if(locale!=null && subcategory!=0){

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

            if (totalPages <= 0) {
                totalPages = 1;
            }

            if(totalPages<page){
                return "redirect:/request/list?page=" + totalPages + "&locale=" + locale + "&subcategory=" + subcategory;

            }

            Map<String, Integer> pagingParams = new HashMap<>();
            pagingParams.put("boardType", Integer.parseInt(boardType));
            pagingParams.put("pageIndex", pageIndex);
            pagingParams.put("pageSize", pageSize);

            List<RequestDTO> list = requestBoardService.categoryPagingList(categoryListParams);

            //페이지네이션
            int maxPagesToShow = 10;
            int halfMaxPagesToShow = maxPagesToShow / 2;

            int startPage = Math.max(page - halfMaxPagesToShow, 1);
            int endPage = Math.min(page + halfMaxPagesToShow-1, totalPages);

            // maxPagesToShow 페이지보다 적은 경우 startPage 및 endPage 조정
            if (endPage - startPage + 1 < maxPagesToShow) {
                if (startPage == 1) {
                    endPage = Math.min(totalPages, maxPagesToShow);
                } else {
                    startPage = Math.max(1, endPage - maxPagesToShow + 1);
                }
            }

            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
            model.addAttribute("maxPagesToShow", maxPagesToShow);


            model.addAttribute("lists", list); //페이징처리된 게시글 DTO
            model.addAttribute("currentPage", page); //해당 페이지가 몇번째 페이지인지.
            model.addAttribute("totalPages", totalPages);//총 페이지가 몇번째 까지 있는가.
            model.addAttribute("totalRequestCount", totalRequestCount);//총 갯수.
            model.addAttribute("user", user);
            model.addAttribute("locale", locale);
            model.addAttribute("subcategory", subcategory);


            return "requestboard";

        }



        int totalRequestCount = requestBoardService.getRequestsByBoardTypeCount(Integer.parseInt(boardType));
        int pageSize = 10;
        int totalPages = totalRequestCount > 0 ? (int) Math.ceil((double) totalRequestCount / pageSize) : 1;
        int pageIndex = (page - 1) * pageSize;

        if (totalPages <= 0) {
            totalPages = 1;
        }

        if(totalPages<page){
            return "redirect:/request/list";
        }

        Map<String, Integer> pagingParams = new HashMap<>();
        pagingParams.put("boardType", Integer.parseInt(boardType));
        pagingParams.put("pageIndex", pageIndex);
        pagingParams.put("pageSize", pageSize);
        List<RequestDTO> list = requestBoardService.requestboardPagingList(pagingParams);



        //페이지네이션
        int maxPagesToShow = 10;
        int halfMaxPagesToShow = maxPagesToShow / 2;

        int startPage = Math.max(page - halfMaxPagesToShow, 1);
        int endPage = Math.min(page + halfMaxPagesToShow-1, totalPages);

        // maxPagesToShow 페이지보다 적은 경우 startPage 및 endPage 조정
        if (endPage - startPage + 1 < maxPagesToShow) {
            if (startPage == 1) {
                endPage = Math.min(totalPages, maxPagesToShow);
            } else {
                startPage = Math.max(1, endPage - maxPagesToShow + 1);
            }
        }

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("maxPagesToShow", maxPagesToShow);

        model.addAttribute("lists", list); //페이징처리된 게시글 DTO
        model.addAttribute("currentPage", page); //해당 페이지가 몇번째 페이지인지.
        model.addAttribute("totalPages", totalPages);//총 페이지가 몇번째 까지 있는가.
        model.addAttribute("totalRequestCount", totalRequestCount);//총 갯수.
        model.addAttribute("user", user);


        return "requestboard";
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

        if(page<=0){
           page=1;
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

        if (totalPages <= 0) {
            totalPages = 1;
        }

        if (page > totalPages) {
            try {
                searchKeyword = URLEncoder.encode(searchKeyword, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }

            return "redirect:/request/list/search?page=" + totalPages +
                    "&searchType=" + searchType +
                    "&searchKeyword=" + searchKeyword;
        }



        //해당 게시물 내용을 가져오자.DTO
        Map<String, Object> pagingParams = new HashMap<>();
        pagingParams.put("pageIndex", pageIndex);
        pagingParams.put("pageSize", pageSize);
        pagingParams.put("searchType", searchType);
        pagingParams.put("searchKeyword", searchKeyword);

        List<RequestDTO> list = requestBoardService.searchPagingList(pagingParams);


        //페이지네이션
        int maxPagesToShow = 10;
        int halfMaxPagesToShow = maxPagesToShow / 2;

        int startPage = Math.max(page - halfMaxPagesToShow, 1);
        int endPage = Math.min(page + halfMaxPagesToShow-1, totalPages);

        // maxPagesToShow 페이지보다 적은 경우 startPage 및 endPage 조정
        if (endPage - startPage + 1 < maxPagesToShow) {
            if (startPage == 1) {
                endPage = Math.min(totalPages, maxPagesToShow);
            } else {
                startPage = Math.max(1, endPage - maxPagesToShow + 1);
            }
        }

        //모델에 정보 넣어서 뷰로 보냄.
        model.addAttribute("lists", list); //페이징처리된 게시글 DTO
        model.addAttribute("currentPage", page); //해당 페이지가 몇번째 페이지인지.
        model.addAttribute("totalPages", totalPages);//총 페이지가 몇번째 까지 있는가.
        model.addAttribute("totalRequestCount", totalRequestCount);//총 갯수.
        model.addAttribute("user", user);
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("maxPagesToShow", maxPagesToShow);

        return "requestboard";

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




    // 해시태그로 검색
    @GetMapping("/list/searchtag")
    public String searchtaglist(@RequestParam(defaultValue = "1") int page,
                                  @AuthenticationPrincipal User user,
                                  @AuthenticationPrincipal OAuth2User oAuth2User,
                                  String tag ,
                                  Model model) {

        //유저 로그인여부판단 후 user에 등록. //비로그인user = null
        if (!UserUtil.isNotLogin(user, oAuth2User)) {
            if (user == null) user = UserUtil.convertOAuth2UserToUser(oAuth2User);
            UserUtil.addPublicUserInfoToModel(model, user);
        }

        if (page <= 0) {
            page = 1;
        }

        int totalRequestCount = requestBoardService.searchTagListCount(tag);

        int pageSize = 10;//한페이지10개
        int pageIndex = (page - 1) * pageSize;
        int totalPages = (int) Math.ceil((double) totalRequestCount / pageSize);

        if (totalPages <= 0) {
            totalPages = 1;
        }

        if (page > totalPages) {
            try {
                tag = URLEncoder.encode(tag, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
            return "redirect:/request/list/searchtag?page=" + totalPages +
                    "&tag=" + tag;
        }


        Map<String, Object> pagingParams = new HashMap<>();
        pagingParams.put("pageIndex", pageIndex);
        pagingParams.put("pageSize", pageSize);
        pagingParams.put("tag", tag);
        
        List<RequestDTO> list = requestBoardService.searchPagingTagList(pagingParams);



        //페이지네이션
        int maxPagesToShow = 10;
        int halfMaxPagesToShow = maxPagesToShow / 2;

        int startPage = Math.max(page - halfMaxPagesToShow, 1);
        int endPage = Math.min(page + halfMaxPagesToShow-1, totalPages);

        // maxPagesToShow 페이지보다 적은 경우 startPage 및 endPage 조정
        if (endPage - startPage + 1 < maxPagesToShow) {
            if (startPage == 1) {
                endPage = Math.min(totalPages, maxPagesToShow);
            } else {
                startPage = Math.max(1, endPage - maxPagesToShow + 1);
            }
        }

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("maxPagesToShow", maxPagesToShow);




        //모델에 정보 넣어서 뷰로 보냄.
        model.addAttribute("lists", list); //페이징처리된 게시글 DTO
        model.addAttribute("currentPage", page); //해당 페이지가 몇번째 페이지인지.
        model.addAttribute("totalPages", totalPages);//총 페이지가 몇번째 까지 있는가.
        model.addAttribute("totalRequestCount", totalRequestCount);//총 갯수.
        model.addAttribute("user", user);
        model.addAttribute("searchtag", tag);

        return "requestboard";

    }




}

//구현해야할 목록 --------------

//[필수]

//1. 글수정 -첨부파일(다중삭제에러), 용량제한.
//2. 게시판리스트 : 보여주는 형식 앨범형,(hover?)...(이미지 서버이미지 보여줘야하는데.얘가문제)
//3. 신고기능- 댓글, 글, 사용자..???
//4. 글 상세 - 게시글 목록버튼.
//5. 최그인기태그, 최근 등록태그들 모아서 옆에 보여줄수있나
//6. 배포..

//코드 정리..(,,,)

//[시간남으면 추가구현] --
//카테고리별분류 + 검색
//이미지 서버이미지 보여줘야하는데.....
//지도 : 미구현--->해야함
//API유해콘텐츠 : 미구현--->해야함
//댓글 페이징
//이전글 다음글 목록

//---------부분구현

//좋아요 : (좋아요등록O, 좋아요취소는 미구현)-필요시 구현예정.

//검색 : 전체 게시물에서 제목, 내용, 작성자로 조회가능.(카테고리별로는 불가)

//게시글 등록 : 부분구현.(불완전)

//게시글 수정 : 부분구현.(불완전)

//게시글 세부내용 : 세부내용, 댓글 조회, 좋아요까지 o(신고 미구현)

//코멘트 : 조회O, 등록o, 삭제o,작성자만 삭제가능o, 대댓구현 --->원본댓만 삭제되었을때 남기는 방법 조금 더 연구필요.

//게시판리스트 :카테고리별구현O

//---------완료
//첨부파일 :o 첨부, 다운, 조회,삭제o - 에러발생 : 다중삭제시
//해시태그 ok 등록,수정,삭제,조회,검색 가능.
//글 수정, post변경o
//게시글 삭제 : 구현O
//대댓글 등록: 구현(1단계대댓만 가능)
//필수--완료
//1. CSS o..
//2.글수정 - 글자수제한,
//3.글 상세 - 댓글 갯수
//4. 아이디 누르면, 검색으로 링크걸음

