package kr.ac.univ.controller;

import kr.ac.univ.common.domain.enums.ActiveStatus;
import kr.ac.univ.common.validation.FileValidator;
import kr.ac.univ.exception.BusinessException;
import kr.ac.univ.exception.FileNumberExceededException;
import kr.ac.univ.exception.FileTypeException;
import kr.ac.univ.introduction.dto.IntroductionDto;
import kr.ac.univ.introduction.service.IntroductionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/introductions")
public class IntroductionRestController {
    private final IntroductionService introductionService;

    public IntroductionRestController(IntroductionService introductionService) {
        this.introductionService = introductionService;
    }

    @PostMapping
    public ResponseEntity<?> postIntroduction(@RequestBody @Valid IntroductionDto introductionDto) {
        if (introductionDto.getActiveStatus() == ActiveStatus.ACTIVE && introductionService.countAllByActiveStatus() > 0) {
            throw new BusinessException("Only one introduction must be activate.");
        }

        Long idx = introductionService.insertIntroduction(introductionDto);

        return new ResponseEntity<>(idx, HttpStatus.CREATED);
    }

    @PutMapping("/{idx}")
    public ResponseEntity<?> putIntroduction(@PathVariable("idx") Long idx, @RequestBody @Valid IntroductionDto introductionDto) {
        if (introductionDto.getActiveStatus() == ActiveStatus.ACTIVE && introductionService.countAllByActiveStatus() > 1) {
            throw new BusinessException("Only one introduction must be activate.");
        }

        introductionService.updateIntroduction(idx, introductionDto);

        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    @DeleteMapping("/{idx}")
    public ResponseEntity<?> deleteIntroduction(@PathVariable("idx") Long idx) throws Exception {
        introductionService.deleteIntroductionByIdx(idx);

        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
}