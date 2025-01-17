package kr.ac.univ.alumniAssociation.repository;

import kr.ac.univ.alumniAssociation.domain.AlumniAssociation;
import kr.ac.univ.common.domain.enums.ActiveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlumniAssociationRepository extends JpaRepository<AlumniAssociation, Long> {
    Page<AlumniAssociation> findAllByTitleContaining(Pageable pageable, String title);

    Page<AlumniAssociation> findAllByContentContaining(Pageable pageable, String content);

    Page<AlumniAssociation> findAllByCreatedByContaining(Pageable pageable, String username);


    Page<AlumniAssociation> findAllByActiveStatusIs(Pageable pageable, ActiveStatus activeStatus);

    Page<AlumniAssociation> findAllByTitleContainingAndActiveStatusIs(Pageable pageable, String title, ActiveStatus activeStatus, Long priority);

    Page<AlumniAssociation> findAllByContentContainingAndActiveStatusIs(Pageable pageable, String content, ActiveStatus activeStatus, Long priority);

    Page<AlumniAssociation> findAllByCreatedByContainingAndActiveStatusIs(Pageable pageable, String username, ActiveStatus activeStatus, Long priority);
}