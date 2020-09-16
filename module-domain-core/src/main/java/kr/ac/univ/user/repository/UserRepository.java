package kr.ac.univ.user.repository;

import kr.ac.univ.common.domain.enums.ActiveStatus;
import kr.ac.univ.noticeBoard.domain.NoticeBoard;
import kr.ac.univ.user.domain.User;
import kr.ac.univ.user.domain.enums.AuthorityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Long countByUsername(String username);

    User findByUsername(String username);

    User findByUsernameAndAuthorityTypeIn(String username, AuthorityType[] authorityType);

    Page<User> findAllByUsernameContaining(Pageable pageable, String username);

    Page<User> findAllByKoreanNameContaining(Pageable pageable, String koreanName);

    Page<User> findAllByEmailContaining(Pageable pageable, String email);
}