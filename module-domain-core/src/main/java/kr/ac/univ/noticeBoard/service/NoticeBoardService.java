package kr.ac.univ.noticeBoard.service;

import kr.ac.univ.noticeBoard.domain.NoticeBoard;
import kr.ac.univ.noticeBoard.repository.NoticeBoardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class NoticeBoardService {
    private final NoticeBoardRepository noticeBoardRepository;

    public NoticeBoardService(NoticeBoardRepository noticeBoardRepository) {
        this.noticeBoardRepository = noticeBoardRepository;
    }

    public Page<NoticeBoard> findNoticeBoardList(Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber() <= 0 ? 0 : pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.Direction.DESC, "idx");

        return noticeBoardRepository.findAll(pageable);
    }

    public Long insertNoticeBoard(NoticeBoard noticeBoard) {

        return noticeBoardRepository.save(noticeBoard).getIdx();
    }

    public NoticeBoard findNoticeBoardByIdx(Long idx) {

        return noticeBoardRepository.findById(idx).orElse(new NoticeBoard());
    }

    @Transactional
    public Long updateNoticeBoard(Long idx, NoticeBoard noticeBoard) {
        noticeBoardRepository.getOne(idx).update(noticeBoard);

        return noticeBoardRepository.save(noticeBoard).getIdx();
    }

    public void deleteNoticeBoardByIdx(Long idx) {
        noticeBoardRepository.deleteById(idx);
    }
}