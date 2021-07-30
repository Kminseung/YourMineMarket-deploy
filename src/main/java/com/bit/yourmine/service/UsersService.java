package com.bit.yourmine.service;

import com.bit.yourmine.domain.chat.ChatRoom;
import com.bit.yourmine.domain.chat.ChatRoomRepository;
import com.bit.yourmine.domain.posts.Posts;
import com.bit.yourmine.domain.users.Role;
import com.bit.yourmine.domain.users.SessionUser;
import com.bit.yourmine.domain.users.Users;
import com.bit.yourmine.domain.users.UsersRepository;
import com.bit.yourmine.dto.admin.UsersResponseDto;
import com.bit.yourmine.dto.reviews.ReviewScoreSetDto;
import com.bit.yourmine.dto.users.PasswordModifyDto;
import com.bit.yourmine.dto.users.UserModifyDto;
import com.bit.yourmine.dto.users.UserSaveRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UsersService implements UserDetailsService {
    private final UsersRepository usersRepository;
    private final FileService fileService;
    private final ChatRoomRepository roomRepository;
    String idTest = "^[a-zA-z](?=.*[0-9]{1,16}).{7,16}";
    String pwTest = "(?=.*[0-9]{1,30})(?=.*[~`!@#$%\\^&*()-+=	]{1,30})(?=.*[a-zA-Z]{1,30}).{8,30}$";
    String numTest = "^[0-9]{3}-[0-9]{3,4}-[0-9]{4}$";

    public void save(UserSaveRequestDto users, String pass) {
        Users checkUser = null;
        try {
            checkUser = getUsers(users.getId());
        } catch (NoSuchElementException e) {
            System.out.println("가입된 계정 없음");
        }
        boolean check;
        check = checkUser == null;

        if (!check) {
            throw new IllegalArgumentException("sign up : un valid id");
        } else if (!Pattern.matches(idTest, users.getId())) {
            throw new IllegalArgumentException("sign up : un valid id");
        } else if (!Pattern.matches(pwTest, users.getPassword())) {
            throw new IllegalArgumentException("sign up : un valid password");
        } else if (!Pattern.matches(numTest, users.getPhone())) {
            throw new IllegalArgumentException("sign up : un valid phone");
        } else if (users.getName() == null) {
            throw new IllegalArgumentException("sign up : un valid name");
        } else if (users.getAddress() == null) {
            throw new IllegalArgumentException("sign up : un valid address");
        } else if (users.getDetailAddress() == null) {
            throw new IllegalArgumentException("sign up : un valid detail address");
        } else if (!pass.equals("pass")) {
            throw new IllegalArgumentException("sign up : un valid email");
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        users.setPassword(passwordEncoder.encode(users.getPassword()));
        users.setRole(Role.USER);
        users.setScore(50L);

        usersRepository.save(users.toEntity());
    }

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        try {
            Users users = getUsers(id);
            if (users == null) {
                return null;
            } else {
                List<GrantedAuthority> authorities = new ArrayList<>();
//                authorities.add(new SimpleGrantedAuthority(users.getRole().getValue()));
                return new User(users.getId(), users.getPassword(), authorities);
            }
        } catch (Exception e) {
            System.out.println("account empty");
        }

        return null;
    }

    public Users getUsers(String id) {
        return usersRepository.findById(id).get();
    }

    public SessionUser userModify(UserModifyDto modifyDto, SessionUser sessionUser) {
        if (modifyDto.getPhone() != null && !Pattern.matches(numTest, modifyDto.getPhone())) {
            throw new IllegalArgumentException("userModify : un valid phone");
        } else if (modifyDto.getName() == null) {
            throw new IllegalArgumentException("userModify : un valid name");
        }
        Users users = getUsers(modifyDto.getId());
        users.setName(modifyDto.getName());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>(authentication.getAuthorities());

        // 번호
        users.setPhone(modifyDto.getPhone());
        sessionUser.setPhone(modifyDto.getPhone());
        // 주소
        users.setAddress(modifyDto.getAddress());
        users.setDetailAddress(modifyDto.getDetailAddress());
        sessionUser.setAddress(modifyDto.getAddress());
        sessionUser.setDetailAddress(modifyDto.getDetailAddress());
        // 권한
        users.setRole(Role.USER);
        sessionUser.setRole(Role.USER);

        grantedAuthorities.add(new SimpleGrantedAuthority(Role.USER.getValue()));
        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(), authentication.getCredentials(), grantedAuthorities);

        SecurityContextHolder.getContext().setAuthentication(newAuthentication);

        usersRepository.save(users);
        return sessionUser;
    }

    public String profileModify(MultipartFile profile, String id) {

        Users users = getUsers(id);
        String saveFileName = fileService.fileSave(profile, "profile");
        users.setProfile(saveFileName);
        usersRepository.save(users);

        return saveFileName;

    }

    public void passwordModify(PasswordModifyDto modifyDto) {
        if (!Pattern.matches(pwTest, modifyDto.getPassword())) {
            throw new IllegalArgumentException("sign up : un valid password");
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Users users = getUsers(modifyDto.getId());
        users.setPassword(passwordEncoder.encode(modifyDto.getPassword()));
        usersRepository.save(users);
    }

    public Users delProfile(String id) {
        Users users = getUsers(id);
        users.setProfile(null);
        usersRepository.save(users);
        return users;
    }

    public void setScore(ReviewScoreSetDto scoreSetDto) {
        Users users = getUsers(scoreSetDto.getId());
        Long change = scoreSetDto.getScore() - 3;
        users.setScore(users.getScore() + change);
        usersRepository.save(users);
    }

    public Users findByEmail(String email) {
        return usersRepository.findByEmail(email).get();
    }

    public void leave(String id) {
        Users users = getUsers(id);
        for (Posts posts: users.getPosts()) {
            for (ChatRoom chatRoom : roomRepository.findAllByPostId(posts.getId())){
                roomRepository.buyerOut(chatRoom.getIdentify());
            }
        }
        List<ChatRoom> buyRooms = roomRepository.findBuyList("-" + id);
        for (ChatRoom chatRoom : buyRooms) {
            roomRepository.sellerOut(chatRoom.getIdentify());
        }
        usersRepository.delete(users);
    }

    // YourPage Service
    public UsersResponseDto findByName(String name) {
        Users entity = usersRepository.findByName(name).orElseThrow(() -> new IllegalArgumentException("해당 유저 없음!"));
        return new UsersResponseDto(entity);
    }
}
