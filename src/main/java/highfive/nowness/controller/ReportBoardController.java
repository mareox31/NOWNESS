package highfive.nowness.controller;

import highfive.nowness.dto.PostData;
import highfive.nowness.dto.ReportDTO;
import highfive.nowness.dto.ReportsDTO;
import highfive.nowness.dto.TagsDTO;
import highfive.nowness.service.ReportBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/report")
@Controller
@RequiredArgsConstructor
public class ReportBoardController {
    private final ReportBoardService reportBoardService;
    @GetMapping("/")
    public String mainpage() {
        return "main";
    }

    @GetMapping("/writer")
    public String boardWriteForm() {
        return "reportwriter";
    }

    // 글쓰기 저장
    @PostMapping("/writer")
    public String boardWriteForm( @RequestParam int userId, @RequestParam String contents, @RequestParam String title,
                                  @RequestParam int boardType, @RequestParam int locale, @RequestParam int subcategory) {

        PostData postData = new PostData();
        postData.setUserId(userId);
        postData.setContents(contents);
        postData.setTitle(title);
        postData.setBoardType(boardType);
        postData.setLocale(locale);
        postData.setSubcategory(subcategory);

        System.out.println(postData);

        reportBoardService.addPost(postData);

        return "redirect:/report/board";
    }

    // 게시글 출력, 게시글 목록 조회
    @GetMapping("/board")
    public String reportBoardList(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 10;
        int totalPosts;
        List<ReportDTO> posts;


        // boardType이 2인 게시글 수만 가져오도록 변경
        totalPosts = reportBoardService.getTotalPostsCountByBoardType(2);

        int totalPages = (int) Math.ceil((double) totalPosts / pageSize);

        page = Math.max(1, Math.min(page, totalPages));

        int offset = (page - 1) * pageSize;

        // boardType이 2인 게시글만 가져오도록 변경
        posts = reportBoardService.getPostsByBoardTypeAndPage(2, offset, pageSize);

        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "reportboard";
    }

    // 상세 페이지 조회
    @GetMapping("/board/{postId}")
    public String reportPostDetail(@PathVariable int postId, Model model) {
        ReportDTO post = reportBoardService.getPostById(postId);
        if (post == null) {
            return "redirect:/report/board";
        }

        // 게시물의 조회수를 증가시키고 DB에 조회수 증가
        reportBoardService.increasePostViewCount(postId);

        // 게시글의 태그 정보를 가져옵니다.
        List<TagsDTO> tags = reportBoardService.getTagsByContentsId(postId);

        model.addAttribute("post", post);
        model.addAttribute("tags", tags);
        return "reportpost";
    }

    // 게시글 목록 조회 및 검색(제목으로 구현)
    // 게시글 검색후 페이징처리 전체게시글로 가는 오류 수정
    @GetMapping("/board/search")
    public String reportBoardListBySearch(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(required = false) String searchTitle,
                                          Model model) {
        int pageSize = 10;
        int totalPosts;
        List<ReportDTO> posts;

        if (searchTitle != null && !searchTitle.isEmpty()) {
            // 검색어가 존재하는 경우 검색 결과만 가져옴
            totalPosts = reportBoardService.getTotalPostsCountByTitleAndBoardType(searchTitle, 2);
            posts = reportBoardService.getPostsByTitleAndBoardTypeAndPage(searchTitle, 2, page, pageSize);
        } else {
            // 검색어가 없는 경우 boardType이 2인 게시글 목록 1페이지를 가져옴
            totalPosts = reportBoardService.getTotalPostsCountByBoardType(2);
            int totalPages = (int) Math.ceil((double) totalPosts / pageSize);
            page = Math.max(1, Math.min(page, totalPages));
            int offset = (page - 1) * pageSize;
            posts = reportBoardService.getPostsByBoardTypeAndPage(2, offset, pageSize);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) totalPosts / pageSize));

        // 검색어를 URL 파라미터로 전달하여 페이지 이동에 유지되도록 함
        model.addAttribute("searchTitle", searchTitle);

        return "reportboard";
    }

    // 게시글 수정페이지로 이동
    @GetMapping("/board/{postId}/edit")
    public String editPostForm(@PathVariable int postId, Model model) {
        ReportDTO post = reportBoardService.getPostById(postId);
        if (post == null) {
            return "redirect:report/board";
        }

        model.addAttribute("post", post);
        return "reportedit";
    }

    // 게시글 삭제
    @GetMapping("/board/{postId}/delete")
    public String deletePost(@PathVariable int postId, Model model) {
        // 삭제하기 전에 게시글 정보를 확인하기 위해 게시글 정보를 가져옵니다.
        ReportDTO post = reportBoardService.getPostById(postId);
        if (post == null) {
            // 게시글이 존재하지 않을 경우 게시판으로 리다이렉트합니다.
            return "redirect:/report/board";
        }

        // 삭제 확인 페이지에 게시글 정보를 전달합니다.
        model.addAttribute("post", post);

        return "reportdeleteconfirmation"; // 삭제 확인 페이지에 해당하는 Thymeleaf 템플릿을 작성합니다.
    }

    // 게시글 삭제 처리를 위한 핸들러 메서드 추가
    @PostMapping("/board/{postId}/delete")
    public String processDeletePost(@PathVariable int postId) {
        // postId를 이용하여 게시글을 삭제합니다.
        reportBoardService.deletePostById(postId);

        // 삭제가 성공하면 게시판으로 리다이렉트합니다.
        return "redirect:/report/board";
    }

    // 게시글 신고
    @PostMapping("/board/{postId}/report")
    public String reportPost(@PathVariable int postId, @RequestParam int reportUserId,
                             @RequestParam String reportReason) {
        ReportsDTO reportsDTO = new ReportsDTO();
        reportsDTO.setReportedContentsId(postId);
        reportsDTO.setReportUserId(reportUserId);
        reportsDTO.setReportReason(reportReason);

        reportBoardService.reportPost(reportsDTO);

        return "redirect:/report/board/" + postId;
    }
}
