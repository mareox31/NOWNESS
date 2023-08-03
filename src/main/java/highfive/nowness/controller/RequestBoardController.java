package highfive.nowness.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Resource;
import highfive.nowness.domain.User;
import highfive.nowness.dto.*;
import highfive.nowness.service.RequestBoardService;
import highfive.nowness.util.UserUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.metrics.StartupStep;
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

        System.out.println(hashtags +":"+ postId);

        //글에 대한 해시태그 저장
        requestBoardService.addTags(hashtags, postId);


        //파일저장 :DB와 서버에 저장
        String savePath = "c:/kdt/upload/nowness/"; //파일저장경로.

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
            String filePath = "c:/kdt/upload/nowness/" + fileData.getSaveName();

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

        System.out.println("파일들내용 : " +fileDatalist);


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



        System.out.println("파일들내용 : " +requestBoardService.getFileByContentsId(id));


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


//ㅌ스투ㅡ중----------
        // Update files
System.out.println("넘어온파일리스트 " + files.toString() + " 기존존재 세이브네임: " + existingSavenames);
        // Get the list of existing files for the current post
        List<FileData> existingFilesList = requestBoardService.getFileByContentsId(postId);


        System.out.println("기존 저장된 파일DTO: " +existingFilesList);

// Create a set to keep track of existing save names
        Set<String> existingSaveNames = existingFilesList.stream()
                .map(FileData::getSaveName)
                .collect(Collectors.toSet());


        System.out.println("기존 존재하는 DTO에서가져온.  existingSaveNames: "+ existingSaveNames);


        //새파일리스트를만든다.
// Create a list to store the new files
        List<FileData> newFilesList = new ArrayList<>();
// Create a list to store the files to keep (Comparison 1)
        List<FileData> filesToKeep = new ArrayList<>();

        // Process the uploaded files
        for (MultipartFile file : files) {
            if (file.isEmpty() || file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
                continue;
            }

            FileData fileData = new FileData();
            String savePath = "c:/kdt/upload/nowness/"; // 파일 저장 경로.

            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
            String originalFilenameWithoutExtension = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.'));
            String savedFileName = originalFilenameWithoutExtension + "_" + UUID.randomUUID().toString() + "." + fileExtension;

            fileData.setContentsid(postId); // Set the post ID
            fileData.setSaveName(savedFileName);
            fileData.setPath(savePath);
            fileData.setSize(file.getSize());
            fileData.setExt(fileExtension);

            // Set the original name (remove the UUID and extension)
            fileData.setOrginName(originalFilename);

            // Check if the file already exists in the database (based on savename)
            if (!existingSaveNames.contains(savedFileName)) {
                // File is new, save it to the database and add it to the newFilesList (Comparison 2)
                newFilesList.add(fileData);
                requestBoardService.saveFileData(fileData);
            } else {
                // File already exists, add it to the filesToKeep list (Comparison 1)
                filesToKeep.add(fileData);
            }

            System.out.println("새파일리스트2222 newFilesList: "+ newFilesList);

            // Save the new file to the server (regardless of whether it's new or existing)
            Path filePath = Paths.get(savePath, savedFileName);
            try {
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // Handle the exception as required
            }
        }

        System.out.println("존--------재세이브네임: "+ existingSaveNames);
// Identify existing files to delete (Comparison 3)
        if (existingSavenames != null && !existingSavenames.isEmpty()) {
            List<FileData> filesToDelete = existingFilesList.stream()
                    .filter(file -> !existingSaveNames.contains(file.getSaveName()))
                    .collect(Collectors.toList());


// Delete the identified files from the database and server
            for (FileData fileToDelete : filesToDelete) {
                requestBoardService.deleteFileById(fileToDelete.getId());

                Path filePath = Paths.get(fileToDelete.getPath(), fileToDelete.getSaveName());
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    // Handle the exception as required
                }
            }
        }
//        //파일저장 :DB와 서버에 저장===================================테스트중
//
//        String savePath = "c:/kdt/upload/nowness/"; //파일저장경로.
//
//        for (MultipartFile file : files) {
//            if (file.isEmpty() || file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
//                continue;
//            }
//
//            FileData fileData = new FileData();
//
//            fileData.setContentsid(postId);//글번호저장.
//            //원본이름+ _ +uuid + . + 확장자로 저장됨.
//            String originalFilenameWithoutExtension = file.getOriginalFilename().substring(0, file.getOriginalFilename().lastIndexOf('.'));
//            String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);
//            String savedFileName = originalFilenameWithoutExtension + "_" + UUID.randomUUID().toString() + "." + fileExtension;
//
//            fileData.setSaveName(savedFileName);
//            fileData.setPath(savePath);
//            fileData.setSize(file.getSize());
//            fileData.setExt(fileExtension);
//
//            //파일 뒤에 _로 끝나는거 에러 방지.
//            String originalFilename = file.getOriginalFilename();
//            if (originalFilename != null) {
//                if (originalFilename.endsWith("_")) {
//                    String[] parts = originalFilename.split("_");
//                    if (parts.length >= 2) {
//                        fileData.setOrginName(parts[0]);
//                    }
//                } else {
//                    fileData.setOrginName(originalFilename);
//                }
//            }
//
//            // DB저장
//            requestBoardService.saveFileData(fileData);
//
//            // 서버에 저장
//            Path filePath = Paths.get(savePath, savedFileName);
//            try {
//                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//            } catch (IOException e) {
//            }
//        }


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


        int totalRequestCount = requestBoardService.searchTagListCount(tag);

        int pageSize = 10;//한페이지10개
        int pageIndex = (page - 1) * pageSize;
        int totalPages = (int) Math.ceil((double) totalRequestCount / pageSize);

        //없는 페이지 요구시 보정.
        if (page <= 0) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }


        Map<String, Object> pagingParams = new HashMap<>();
        pagingParams.put("pageIndex", pageIndex);
        pagingParams.put("pageSize", pageSize);
        pagingParams.put("tag", tag);
        
        List<RequestDTO> list = requestBoardService.searchPagingTagList(pagingParams);


        //모델에 정보 넣어서 뷰로 보냄.
        model.addAttribute("lists", list); //페이징처리된 게시글 DTO
        model.addAttribute("currentPage", page); //해당 페이지가 몇번째 페이지인지.
        model.addAttribute("totalPages", totalPages);//총 페이지가 몇번째 까지 있는가.
        model.addAttribute("totalRequestCount", totalRequestCount);//총 갯수.
        model.addAttribute("user", user);
        model.addAttribute("searchtag", tag);

        return "/requestboard";

    }




}

//구현해야할 목록 --------------

//리스트 - 왜......없는 페이지번호 쓰면, 에러로가고.. 페이지 보정이안됨??

//첨부파일 : 미구현--->해야함

//지도 : 미구현--->해야함

//API유해콘텐츠 : 미구현--->해야함

//---------부분구현

//좋아요 : (좋아요등록O, 좋아요취소는 미구현)-필요시 구현예정.

//검색 : 전체 게시물에서 제목, 내용, 작성자로 조회가능.(카테고리별로는 불가)

//게시글 등록 : 부분구현.(이미지멀티미디어 아직)

//게시글 수정 : 부분구현.(이미지멀티미디어 아직)

//게시글 세부내용 : 세부내용, 댓글 조회까지 o(신고, 좋아요,미구현)

//코멘트 : 조회O, 등록o, 삭제o,작성자만 삭제가능o --->대댓글 미구현.

//게시판리스트 :카테고리별구현O

//---------완료
//해시태그 ok 등록,수정,삭제,조회,검색 가능.
//글 수정, post변경o
//게시글 삭제 : 구현O
//대댓글 등록: 구현(1단계대댓만 가능)


//태그저장.테스트--------

//        requestBoardService.addTag(postData);
//        // Save hashtags to Tags table
//        for (String tag : hashtags) {
//            StartupStep.Tags tagData = new StartupStep.Tags();
//            tagData.setContentsId(postData.getId()); // Assuming you have a getId() method in Contents class
//            tagData.setTag(tag);
//            tagsService.addTag(tagData);
//        }
