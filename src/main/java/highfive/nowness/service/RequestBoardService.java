package highfive.nowness.service;

import highfive.nowness.dto.*;
import highfive.nowness.repository.RequestBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.nodes.Tag;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestBoardService {

    private final RequestBoardRepository requestBoardRepository;


    // 페이징 처리 -'맵'사용(닉네임용리스트에사용)
    public List<RequestDTO> requestboardPagingList(Map<String, Integer> pagingParams) {
        return requestBoardRepository.boardPagingList(pagingParams);
    }

    //전체 글 (총 개수)
    public int getTotalRequestCount() {
        return requestBoardRepository.getTotalRequestCount();
    }

    //해당 번호 게시글 상세내용(DTO)
    public RequestDTO getBoard(int id) {
        return requestBoardRepository.getBoard(id);
    }

    //해당 게시글 닉네임
    public String getNicknameById(int id) {
        return requestBoardRepository.getNicknameById(id);
    }

    //리플쓴 유저id로 해당하는 닉네임 가져오기
    public String getNickname(int id) {
        return requestBoardRepository.getNickname(id);
    }


    //해당 게시글 좋아요 (총 개수)
    public int getLikes(int id) { return requestBoardRepository.getLikes(id); }

    //해당 게시글 조회수 +1 증가
    public void updateContentViews(int id) { requestBoardRepository.updateContentViews(id); }

    //해당 게시글 삭제  (안보이게 deleted =1 업데이트)
    public void deleteContents(int id) { requestBoardRepository.deleteContents(id); }

    //댓글 조회
    public List<RepliesDTO> getReply(int id) { return requestBoardRepository.getReply(id); }

    //자식댓글 개수
    public int childCommentsCount(int id) { return requestBoardRepository.childCommentsCount(id); }

    //댓글 개수(deleted=0)
    public int getRepliesCount(int id) { return requestBoardRepository.getRepliesCount(id); }


    //테스트 해당 글 댓글갯수 - postRepliesCount
    public int postRepliesCount(int id) { return requestBoardRepository.postRepliesCount(id); }


    //댓글 등록
    public boolean addReply(ReplyData replyData) {
        return requestBoardRepository.addReply(replyData);
    }

    //대댓글 등록
    public boolean add_reReply(ReplyData replyData) {
        return requestBoardRepository.add_reReply(replyData);
    }



    //댓글삭제
    public boolean deleteReply(int replyId) {
        return requestBoardRepository.deleteReply(replyId) > 0;
    }


    //게시글 리스트 가져올때(게시글0개여도 가능)
    public int getRequestsByBoardTypeCount(int boardType) {
        int count = requestBoardRepository.getRequestsByBoardTypeCount(boardType);
        return Math.max(count, 0);
    }

    //글저장(insert)
    public void addPost(PostData postData) { requestBoardRepository.addPost(postData); }

    //글 저장2(insert + 번호반환)
    public int addPost2(PostData postData) { return requestBoardRepository.addPost2(postData);}

    //글"수정" 저장(update)
    public void updatePost(PostData postData) { requestBoardRepository.updatePost(postData); }

    //태그추가테스트중 글"수정" 저장(update + 번호반환)--------------------
    public int updatePost2(PostData postData) { return requestBoardRepository.updatePost2(postData); }



    //게시글ajax테스트-카테고리분류된 DTO끌고오기.
    public List<RequestDTO> categoryListMap(Map<String, Integer> categoryListParams) {
        return requestBoardRepository.categoryListMap(categoryListParams);
    }

    //게시글ajax테스트-카테고리분류 + 페이징처리된. DTO끌고오기.
    public List<RequestDTO> categoryPagingList(Map<String, Integer> categoryListParams) {
        return requestBoardRepository.categoryPagingList(categoryListParams);
    }

    //카테고리별ajax게시물갯수.
    public int categoryListMapCount(Map<String, Integer> categoryListParams) {
        int count = requestBoardRepository.categoryListMapCount(categoryListParams);
        return Math.max(count, 0);
    }

    //검색 : 해당 키워드 조회 총 갯수
    public int searchListMapCount(Map<String, Object> searchListParams) {
        int count = requestBoardRepository.searchListMapCount(searchListParams);
        return Math.max(count, 0);
    }

    //검색 : 해당 키워드 조회 총 글 DTO
    public List<RequestDTO> searchPagingList(Map<String, Object> pagingParams) {
        return requestBoardRepository.searchPagingList(pagingParams);
    }


    //해당 게시글 좋아요 기록이 있는지 검사(개수)
    public int checkIfUserLikedPost(Map<String, Integer>likecheckParams) {
        return requestBoardRepository.checkIfUserLikedPost(likecheckParams);
    }

    //해당 게시글에 좋아요 기록 저장.
    public int insertLike(Map<String, Integer> insertLikeParams) {
       return requestBoardRepository.insertLike(insertLikeParams);
    }


    //해시태그 저장.
    public int addTags(List<String> hashtags, int contentsId) {
        if (hashtags == null || hashtags.isEmpty()) {
            return 0;
        }

        List<TagsDTO> tags = hashtags.stream().map(tag -> {
            TagsDTO newTag = new TagsDTO();
            newTag.setContentsid(contentsId);
            newTag.setTag(tag);
            return newTag;
        }).collect(Collectors.toList());

        return requestBoardRepository.addTags(tags);
    }


    //해당 게시글에 대한 태그 가져오기.
    public List<TagsDTO> getTags(int id) {
        return requestBoardRepository.getTags(id);
    }

    //태그 검색 : 태그에 해당하는 글 개수
    public int  searchTagListCount(String tag) {
        int count = requestBoardRepository.searchTagListCount(tag);
        return Math.max(count, 0);
    }

    //검색 : 해당 키워드 조회 총 글 DTO
    public List<RequestDTO> searchPagingTagList(Map<String, Object> pagingParams) {
        return requestBoardRepository.searchPagingTagList(pagingParams);
    }


    //태그삭제
    public int removeTags(List<String> hashtags, int contentsId) {
        if (hashtags == null || hashtags.isEmpty()) {
            return 0;
        }

        List<TagsDTO> tags = hashtags.stream().map(tag -> {
            TagsDTO newTag = new TagsDTO();
            newTag.setContentsid(contentsId);
            newTag.setTag(tag);
            return newTag;
        }).collect(Collectors.toList());

        return requestBoardRepository.removeTags(tags);
    }


    //파일 DB저장
    public void saveFileData(FileData fileData) {
        requestBoardRepository.saveFileData(fileData);
    }

    //파일 다운로드용 테스트--
    // 파일 다운로드용 테스트
    public FileData getFileById(long fileId) {
        return requestBoardRepository.getFileById(fileId);
    }

    //게시물에해당하는 id
    public List<FileData>  getFileByContentsId(int id) {
        return requestBoardRepository.getFileByContentsId(id);
    }

    //파일 삭제 (id)
    public void deleteFileById(long fileId) {
        requestBoardRepository.deleteFileById(fileId);
    }

    //파일 삭제(여러개)
    public void deleteFilesByIds(List<Long> fileIds) {
        requestBoardRepository.deleteFilesByIds(fileIds);
    }

}

