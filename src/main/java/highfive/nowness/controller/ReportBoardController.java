package highfive.nowness.controller;

import highfive.nowness.dto.PostData;
import highfive.nowness.dto.ReportDTO;
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
        return "/main";
    }

    @GetMapping("/writer")
    public String boardWriteForm() {
        return "/writer";
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
        int totalPosts = reportBoardService.getTotalPostsCount();
        int totalPages = (int) Math.ceil((double) totalPosts / pageSize);

        page = Math.max(1, Math.min(page, totalPages));

        int offset = (page - 1) * pageSize;
        List<ReportDTO> posts = reportBoardService.getPostsByPage(offset, pageSize);

        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "/reportboard";
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

        model.addAttribute("post", post);
        return "/post";
    }


}