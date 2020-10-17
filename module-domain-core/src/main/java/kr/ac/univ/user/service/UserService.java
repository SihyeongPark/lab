package kr.ac.univ.user.service;


import kr.ac.univ.common.domain.enums.ActiveStatus;
import kr.ac.univ.common.dto.SearchDto;
import kr.ac.univ.noticeBoard.dto.NoticeBoardDto;
import kr.ac.univ.noticeBoard.dto.mapper.NoticeBoardMapper;
import kr.ac.univ.user.domain.User;
import kr.ac.univ.user.domain.enums.AuthorityType;
import kr.ac.univ.user.dto.UserDto;
import kr.ac.univ.user.dto.UserPrincipal;
import kr.ac.univ.user.dto.mapper.UserMapper;
import kr.ac.univ.user.repository.UserRepository;
import kr.ac.univ.util.AccessCheck;
import kr.ac.univ.util.EmptyUtil;
import kr.ac.univ.util.NewIconCheck;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserDetailsService {
    @Value("${module.name}")
    private String moduleName;
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<UserDto> findUserList(Pageable pageable, SearchDto searchDto) {
        Page<User> userList = null;
        Page<UserDto> userDtoList = null;

        pageable = PageRequest.of(pageable.getPageNumber() <= 0 ? 0 : pageable.getPageNumber() - 1, pageable.getPageSize(), Sort.Direction.DESC, "idx");

        switch (searchDto.getSearchType()) {
            case "USER_ID":
                userList = userRepository.findAllByUsernameContaining(pageable, searchDto.getKeyword());
                break;
            case "KOREAN_NAME":
                userList = userRepository.findAllByKoreanNameContaining(pageable, searchDto.getKeyword());
                break;
            case "Email":
                userList = userRepository.findAllByEmailContaining(pageable, searchDto.getKeyword());
                break;
            default:
                userList = userRepository.findAll(pageable);
                break;
        }

        userDtoList = new PageImpl<UserDto>(UserMapper.INSTANCE.toDto(userList.getContent()), pageable, userList.getTotalElements());

        // NewIcon 판별
        for (UserDto userDto : userDtoList) {
            userDto.setNewIcon(NewIconCheck.isNew(userDto.getCreatedDate()));
        }

        return userDtoList;
    }

    /**
     * 회원 가입
     *
     * @param userDto
     * @return
     */
    @Transactional
    public Long joinUser(UserDto userDto) {
        // 비밀번호 암호화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

        return userRepository.save(UserMapper.INSTANCE.toEntity(userDto)).getIdx();
    }

    /**
     * 사용자 정의 로그인
     *
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;
        UserPrincipal userPrincipal = null;

        if ("module-app-admin".equals(moduleName)) {
            AuthorityType[] authorityType = {AuthorityType.ROOT, AuthorityType.MANAGER};

            user = userRepository.findByUsernameAndAuthorityTypeIn(username, authorityType);
        } else if ("module-app-web".equals(moduleName)) {
            user = userRepository.findByUsername(username);
        } else {
            user = null;
        }

        if (EmptyUtil.isEmpty(user)) {
            throw new BadCredentialsException(null);
        }

        userPrincipal = new UserPrincipal(user);

        return userPrincipal;
    }

    public UserDto findUserByIdx(Long idx) {
        UserDto userDto = UserMapper.INSTANCE.toDto(userRepository.findById(idx).orElse(new User()));

        if ("module-app-admin".equals(moduleName)) {
            // 권한 설정
            // Register: 로그인한 사용자 Register 접근 가능
            if (idx == 0) {
                userDto.setAccess(true);
            }
            // Update: isAccess 메소드에 따라 접근 가능 및 불가
            else if (AccessCheck.isAccessInModuleAdmin(userDto.getCreatedBy())) {
                userDto.setAccess(true);
            } else {
                userDto.setAccess(false);
            }
        } else {
            // Update: isAccess 메소드에 따라 접근 가능 및 불가
            if (AccessCheck.isAccessInModuleWeb(userDto.getCreatedBy())) {
                userDto.setAccess(true);
            } else {
                userDto.setAccess(false);
            }
        }

        return userDto;
    }

        @Transactional
    public Long updateUser(Long idx, UserDto userDto) {
        User persistUser = userRepository.getOne(idx);
        User user = UserMapper.INSTANCE.toEntity(userDto);

        persistUser.update(user, persistUser.getPassword());

        return userRepository.save(persistUser).getIdx();
    }

    public void deleteUserByIdx(Long idx) {
        userRepository.deleteById(idx);
    }

    public boolean isDupulicationUserByUsername(String username) {
        return (userRepository.countByUsername(username) > 0) ? true : false;
    }

}