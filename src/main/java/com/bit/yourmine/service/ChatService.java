package com.bit.yourmine.service;

import com.bit.yourmine.domain.chat.ChatDB;
import com.bit.yourmine.domain.chat.ChatDBRepository;
import com.bit.yourmine.domain.chat.ChatRoom;
import com.bit.yourmine.domain.chat.ChatRoomRepository;
import com.bit.yourmine.domain.posts.Posts;
import com.bit.yourmine.dto.chat.ChatOutDto;
import com.bit.yourmine.dto.chat.ChatResponseDto;
import com.bit.yourmine.dto.chat.ChatRoomListDto;
import com.bit.yourmine.dto.chat.ReadCheckDto;
import com.bit.yourmine.dto.posts.PostsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository roomRepository;
    private final ChatDBRepository dbRepository;
    private final PostsService postsService;
    private final UsersService usersService;

    public void roomCheck(String roomId) {
        ChatRoom chatRoom = null;
        try {
            chatRoom = roomRepository.findByIdentify(roomId).get();
        } catch (Exception e) {
            System.out.println("null room");
        }
        if (chatRoom == null) {
            roomRepository.save(new ChatRoom(roomId));
        }
    }

    public void chatSave(String content, String speaker, String listener, LocalDateTime fulTime, String roomId) {
        dbRepository.save(new ChatDB(content, speaker, listener, fulTime, roomRepository.findByIdentify(roomId).get()));
        roomUpdate(roomId);
    }

    public List<ChatResponseDto> chatResponse(String roomId) throws NoSuchElementException{
        List<ChatDB> chatDBS = dbRepository.findAllByChatRoomNoOrderByFulTime(
                roomRepository.findByIdentify(roomId).get().getNo());
        List<ChatResponseDto> responseDto = new ArrayList<>();
        for (ChatDB chatDB: chatDBS) {
            responseDto.add(new ChatResponseDto(chatDB));
        }
        return responseDto;
    }

    public List<ChatRoomListDto> getSortList(String id) {
        List<ChatRoomListDto> roomSortList = new ArrayList<>();
        List<ChatRoom> buyRooms = roomRepository.findBuyList("-" + id);
        for (ChatRoom chatRoom : buyRooms) {
            if (chatRoom.getBuyerOut() != 1) {
                ChatRoomListDto dto = chatRoomToDto(chatRoom, id);
                roomSortList.add(dto);
            }
        }
        List<Posts> postsList = usersService.getUsers(id).getPosts();
        for (Posts posts: postsList) {
            List<ChatRoom> sellRoom = roomRepository.findAllByPostId(posts.getId());
            for (ChatRoom chatRoom : sellRoom) {
                if (chatRoom.getSellerOut() != 1) {
                    ChatRoomListDto dto = chatRoomToDto(chatRoom, id);
                    roomSortList.add(dto);
                }
            }
        }
        roomSortList = roomSortList.stream().sorted(Comparator.comparing(
                ChatRoomListDto::getLastTime).reversed()).collect(Collectors.toList());
        return roomSortList;
    }

    public ChatRoomListDto chatRoomToDto(ChatRoom chatRoom, String id) {
        PostsResponseDto posts = postsService.findById(chatRoom.getPostId());
        ChatRoomListDto listDto = new ChatRoomListDto(chatRoom);
        listDto.setTitle(posts.getTitle());
        if (id.equals(posts.getAuthor())) {
            String room = chatRoom.getIdentify();
            String profile = usersService.getUsers(room.substring(room.indexOf("-")+1)).getProfile();
            if (profile == null) {
                listDto.setProfile("/img/default.jpeg");
            } else {
                listDto.setProfile(profile);
            }
        } else {
            String profile = posts.getUsers().getProfile();
            if (profile == null) {
                listDto.setProfile("/img/default.jpeg");
            } else {
                listDto.setProfile(profile);
            }
        }
        Long count = 0L;
        for (ChatDB chatDB : chatRoom.getChatDBS()) {
            if (chatDB.getListener().equals(id) && chatDB.getRead() == null) {
                count++;
            }
        }
        listDto.setNewChatCount(count);
        return listDto;
    }

    public void readCheck(ReadCheckDto readCheckDto) throws NoSuchElementException {
        List<ChatDB> chatDBList = roomRepository.findByIdentify(readCheckDto.getRoomId()).get().getChatDBS();
        for (ChatDB db: chatDBList) {
            if (db.getRead() == null && !db.getSpeaker().equals(readCheckDto.getUserName())) {
                dbRepository.readCheck(db.getNo());
            }
        }
    }

    public boolean isNew(String id) {
        List<ChatDB> isNew = dbRepository.findAllByListener(id);
        return !isNew.isEmpty();
    }

    public void roomUpdate(String roomId) {
        ChatRoom chatRoom = roomRepository.findByIdentify(roomId).get();
        chatRoom.update(new Date());
        roomRepository.save(chatRoom);
    }

    public void chatOut(ChatOutDto chatOutDto) {
        if (chatOutDto.getPosition().equals("seller")) {
            roomRepository.sellerOut(chatOutDto.getIdentify());
        } else {
            roomRepository.buyerOut(chatOutDto.getIdentify());
        }
    }

    public void chatIn(String identify) {
        roomRepository.chatIn(identify);
    }

    public int getChatCount(Long no) {
        return roomRepository.findAllByPostId(no).size();
    }

    public void delRoom(Long id) {
        roomRepository.delete(roomRepository.findById(id).get());
    }
}
