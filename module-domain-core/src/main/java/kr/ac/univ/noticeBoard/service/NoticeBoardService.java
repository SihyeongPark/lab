package kr.ac.univ.noticeBoard.service;

import kr.ac.univ.common.domain.enums.ActiveStatus;
import kr.ac.univ.common.dto.SearchDto;
import kr.ac.univ.noticeBoard.dto.NoticeBoardDto;
import kr.ac.univ.noticeBoard.dto.mapper.NoticeBoardMapper;
import kr.ac.univ.util.AccessCheck;
import kr.ac.univ.noticeBoard.domain.NoticeBoard;
import kr.ac.univ.noticeBoard.repository.NoticeBoardRepository;
import kr.ac.univ.noticeBoard.repository.NoticeBoardRepositoryImpl;
import kr.ac.univ.user.repository.UserRepository;
import kr.ac.univ.util.NewIconCheck;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class NoticeBoardService {
    @Value("${module.name}")
    private String moduleName;
    private final NoticeBoardRepository noticeBoardRepository;
    private final NoticeBoardRepositoryImpl noticeBoardRepositoryImpl;
    private final UserRepository userRepository;

    public NoticeBoardService(NoticeBoardRepository noticeBoardRepository, NoticeBoardRepositoryImpl noticeBoardRepositoryImpl, UserRepository userRepository) {
        this.noticeBoardRepository = noticeBoardRepository;
        this.noticeBoardRepositoryImpl = noticeBoardRepositoryImpl;
        this.userRepository = userRepository;
    }

    public Page<NoticeBoardDto> findNoticeBoardList(Pageable pageable, SearchDto searchDto) {
        Page<NoticeBoard> noticeBoardList = null;
        Page<NoticeBoardDto> noticeBoardDtoList = null;

        pageable = PageRequest.of(pageable.getPageNumber() <= 0 ? 0 : pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.Direction.DESC, "idx");

        switch (searchDto.getSearchType()) {
            case "TITLE":
                if ("module-app-admin".equals(moduleName)) {
                    noticeBoardList = noticeBoardRepository.findAllByTitleContaining(pageable, searchDto.getKeyword());
                } else if ("module-app-web".equals(moduleName)) {
                    noticeBoardList = noticeBoardRepository.findAllByTitleContainingAndActiveStatusIs(pageable, searchDto.getKeyword(), ActiveStatus.ACTIVE);
                } else {
                    noticeBoardList = null;
                }
                break;
            case "CONTENT":
                if ("module-app-admin".equals(moduleName)) {
                    noticeBoardList = noticeBoardRepository.findAllByContentContaining(pageable, searchDto.getKeyword());
                } else if ("module-app-web".equals(moduleName)) {
                    noticeBoardList = noticeBoardRepository.findAllByContentContainingAndActiveStatusIs(pageable, searchDto.getKeyword(), ActiveStatus.ACTIVE);
                } else {
                    noticeBoardList = null;
                }
                break;
            case "ID":
                if ("module-app-admin".equals(moduleName)) {
                    noticeBoardList = noticeBoardRepository.findAllByCreatedByContaining(pageable, searchDto.getKeyword());
                } else if ("module-app-web".equals(moduleName)) {
                    noticeBoardList = noticeBoardRepository.findAllByCreatedByContainingAndActiveStatusIs(pageable, searchDto.getKeyword(), ActiveStatus.ACTIVE);
                } else {
                    noticeBoardList = null;
                }
                break;
            default:
                if ("module-app-admin".equals(moduleName)) {
                    noticeBoardList = noticeBoardRepository.findAll(pageable);
                } else if ("module-app-web".equals(moduleName)) {
                    noticeBoardList = noticeBoardRepository.findAllByActiveStatusIs(pageable, ActiveStatus.ACTIVE);
                } else {
                    noticeBoardList = null;
                }
                break;
        }

        noticeBoardDtoList = new PageImpl<NoticeBoardDto>(NoticeBoardMapper.INSTANCE.toDto(noticeBoardList.getContent()), pageable, noticeBoardList.getTotalElements());

        // NewIcon 판별
        for (NoticeBoardDto noticeBoardDto : noticeBoardDtoList) {
            noticeBoardDto.setNewIcon(NewIconCheck.isNew(noticeBoardDto.getCreatedDate()));
        }

        return noticeBoardDtoList;
    }

    public List<NoticeBoardDto> findNoticeBoardList() {
        List<NoticeBoardDto> noticeBoardDtoList = NoticeBoardMapper.INSTANCE.toDto(noticeBoardRepository.findTop10ByOrderByIdxDesc());

        // NewIcon 판별
        for (NoticeBoardDto noticeBoardDto : noticeBoardDtoList) {
            noticeBoardDto.setNewIcon(NewIconCheck.isNew(noticeBoardDto.getCreatedDate()));
        }

        return noticeBoardDtoList;
    }

    public Long insertNoticeBoard(NoticeBoardDto noticeBoardDto) {

        return noticeBoardRepository.save(NoticeBoardMapper.INSTANCE.toEntity(noticeBoardDto)).getIdx();
    }

    public NoticeBoardDto findNoticeBoardByIdx(Long idx) {
        NoticeBoardDto noticeBoardDto = NoticeBoardMapper.INSTANCE.toDto(noticeBoardRepository.findById(idx).orElse(new NoticeBoard()));

        // 권한 설정
        // Register: 로그인한 사용자 Register 접근 가능
        if (idx == 0) {
            noticeBoardDto.setAccess(true);
        }
        // Update: isAccess 메소드에 따라 접근 가능 및 불가
        else if (AccessCheck.isAccessInModuleWeb(noticeBoardDto.getCreatedBy())) {
            noticeBoardDto.setAccess(true);
        } else {
            noticeBoardDto.setAccess(false);
        }

        noticeBoardRepositoryImpl.updateViewsByIdx(idx);
        noticeBoardDto.setViews(noticeBoardDto.getViews() + 1);

        return noticeBoardDto;
    }

    @Transactional
    public Long updateNoticeBoard(Long idx, NoticeBoardDto noticeBoardDto) {
        NoticeBoard persistNoticeBoard = noticeBoardRepository.getOne(idx);
        NoticeBoard noticeBoard = NoticeBoardMapper.INSTANCE.toEntity(noticeBoardDto);

        persistNoticeBoard.update(noticeBoard);

        return noticeBoardRepository.save(noticeBoard).getIdx();
    }

    public void deleteNoticeBoardByIdx(Long idx) {
        noticeBoardRepository.deleteById(idx);
    }
}