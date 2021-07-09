package com.bit.yourmain.service;

import com.bit.yourmain.domain.chat.ChatDB;
import com.bit.yourmain.domain.chat.ChatDBRepository;
import com.bit.yourmain.domain.chat.ChatRoom;
import com.bit.yourmain.domain.chat.ChatRoomRepository;
import com.bit.yourmain.domain.posts.Posts;
import com.bit.yourmain.dto.chat.ChatOutDto;
import com.bit.yourmain.dto.chat.ChatResponseDto;
import com.bit.yourmain.dto.chat.ChatRoomListDto;
import com.bit.yourmain.dto.chat.ReadCheckDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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
    }

    public List<ChatResponseDto> chatResponse(String roomId) throws Exception {
        List<ChatDB> chatDBS = dbRepository.findAllByChatRoomNoOrderByFulTime(
                roomRepository.findByIdentify(roomId).get().getNo());
        List<ChatResponseDto> responseDto = new ArrayList<>();
        for (ChatDB chatDB: chatDBS) {
            responseDto.add(new ChatResponseDto(chatDB));
        }
        return responseDto;
    }

    public List<ChatRoomListDto> getChatList(Long id) {
        List<ChatRoom> chatRooms = roomRepository.findAllByPostId(id);
        List<ChatRoomListDto> roomListDtos = new ArrayList<>();
        for (ChatRoom chatRoom: chatRooms) {
            ChatRoomListDto listDto = new ChatRoomListDto(chatRoom);
            listDto.setTitle(postsService.findById(listDto.getPostId()).getTitle());
            String data = listDto.getIdentify();
            listDto.setName(data.substring(data.indexOf("-")+1));
            roomListDtos.add(listDto);
        }
        return roomListDtos;
    }

    public List<ChatRoomListDto> getBuyList(String id) {
        List<ChatRoom> chatRooms = roomRepository.findBuyList("-" + id);
        List<ChatRoomListDto> roomListDtos = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            ChatRoomListDto listDto = new ChatRoomListDto(chatRoom);
            listDto.setTitle(postsService.findById(listDto.getPostId()).getTitle());
            roomListDtos.add(listDto);
        }
        return roomListDtos;
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
        ChatRoomListDto listDto = new ChatRoomListDto(chatRoom);
        listDto.setTitle(postsService.findById(listDto.getPostId()).getTitle());
        String profile = postsService.findById(chatRoom.getPostId()).getUsers().getProfile();
        if (profile == null) {
            listDto.setProfile("/img/default.jpeg");
        } else {
            listDto.setProfile("/profile/" + profile);
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

    public void readCheck(ReadCheckDto readCheckDto) {
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

    public void delRoom(Long id) {
        roomRepository.delete(roomRepository.findById(id).get());
    }
}
