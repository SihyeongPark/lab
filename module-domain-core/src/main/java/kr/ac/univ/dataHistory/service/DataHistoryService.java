package kr.ac.univ.dataHistory.service;

import kr.ac.univ.dataHistory.domain.DataHistory;
import kr.ac.univ.dataHistory.domain.enums.AudType;
import kr.ac.univ.dataHistory.dto.DataHistoryDto;
import kr.ac.univ.dataHistory.dto.mapper.DataHistoryMapper;
import kr.ac.univ.dataHistory.repository.DataHistoryRepository;
import kr.ac.univ.common.domain.enums.ActiveStatus;
import kr.ac.univ.common.dto.SearchDto;
import kr.ac.univ.dataHistory.repository.DataHistoryRepositoryImpl;
import kr.ac.univ.util.AccessCheck;
import kr.ac.univ.util.NewIconCheck;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataHistoryService {
    private final DataHistoryRepository dataHistoryRepository;
    private final DataHistoryRepositoryImpl dataHistoryRepositoryImpl;

    public DataHistoryService(DataHistoryRepository dataHistoryRepository, DataHistoryRepositoryImpl dataHistoryRepositoryImpl) {
        this.dataHistoryRepository = dataHistoryRepository;
        this.dataHistoryRepositoryImpl = dataHistoryRepositoryImpl;
    }

    public Page<DataHistoryDto> findDataHistoryList(Pageable pageable, SearchDto searchDto) {
        Page<DataHistory> historyList = null;
        Page<DataHistoryDto> historyDtoList = null;

        pageable = PageRequest.of(pageable.getPageNumber() <= 0 ? 0 : pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.Direction.DESC, "idx");

        switch (searchDto.getSearchType()) {
            case "AUD_MESSAGE":
                historyList = dataHistoryRepository.findAllByAudMessageContaining(pageable, searchDto.getKeyword());
                break;
            case "AUD_CLASS":
                historyList = dataHistoryRepository.findAllByAudClassContaining(pageable, searchDto.getKeyword());
                break;
            case "AUD_TYPE":
                historyList = dataHistoryRepository.findAllByAudTypeIs(pageable, AudType.valueOf(searchDto.getKeyword()));
                break;
            case "ID":
                historyList = dataHistoryRepository.findAllByCreatedByContaining(pageable, searchDto.getKeyword());
                break;
            default:
                historyList = dataHistoryRepository.findAll(pageable);
                break;
        }

        historyDtoList = new PageImpl<DataHistoryDto>(DataHistoryMapper.INSTANCE.toDto(historyList.getContent()), pageable, historyList.getTotalElements());

        // NewIcon 판별
        for (DataHistoryDto dataHistoryDto : historyDtoList) {
            dataHistoryDto.setNewIcon(NewIconCheck.isNew(dataHistoryDto.getCreatedDate()));
        }

        return historyDtoList;
    }

    public List<DataHistoryDto> findDataHistoryList() {
        List<DataHistoryDto> dataHistoryDtoList = DataHistoryMapper.INSTANCE.toDto(dataHistoryRepository.findTop10ByOrderByIdxDesc());

        // NewIcon 판별
        for (DataHistoryDto dataHistoryDto : dataHistoryDtoList) {
            dataHistoryDto.setNewIcon(NewIconCheck.isNew(dataHistoryDto.getCreatedDate()));
        }

        return dataHistoryDtoList;
    }

    public Long countDataHistory() {
        return dataHistoryRepository.countByAudTypeIs(AudType.INSERT) - dataHistoryRepository.countByAudTypeIs(AudType.DELETE);
    }

    public Long countLoginHistoryByDays(Long days) {
        return dataHistoryRepository.countAllByCreatedDateBetween(LocalDateTime.now().minusDays(days), LocalDateTime.now());
    }

    public Long insertDataHistory(DataHistoryDto dataHistoryDto) {
        return dataHistoryRepository.save(DataHistoryMapper.INSTANCE.toEntity(dataHistoryDto)).getIdx();
    }

    public DataHistoryDto findHistoryByIdx(Long idx) {
        DataHistoryDto dataHistoryDto = DataHistoryMapper.INSTANCE.toDto(dataHistoryRepository.findById(idx).orElse(new DataHistory()));

        // 권한 설정
        // Update: isAccess 메소드에 따라 접근 가능 및 불가
        if (AccessCheck.isAccessInModuleWeb(dataHistoryDto.getCreatedBy())) {
            dataHistoryDto.setAccess(true);
        } else {
            dataHistoryDto.setAccess(false);
        }

        dataHistoryRepositoryImpl.updateViewsByIdx(idx);
        dataHistoryDto.setViews(dataHistoryDto.getViews() + 1);

        return dataHistoryDto;
    }

    public Long updateDataHistory(Long idx, ActiveStatus activeStatus) {
        return dataHistoryRepositoryImpl.updateActiveStatusByIdx(idx, activeStatus);
    }

    public void deleteDataHistoryByIdx(Long idx) {
        dataHistoryRepository.deleteById(idx);
    }
}