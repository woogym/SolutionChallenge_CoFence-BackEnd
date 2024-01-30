package com.gdsc.cofence.service.notice;

import com.gdsc.cofence.dto.noticeDto.noticeRequest.NoticeRegisterRequestDto;
import com.gdsc.cofence.dto.noticeDto.noticeResponse.NoticeRegisterResponseDto;
import com.gdsc.cofence.entity.notice.Notice;
import com.gdsc.cofence.entity.user.User;
import com.gdsc.cofence.exception.ErrorCode;
import com.gdsc.cofence.exception.model.CustomException;
import com.gdsc.cofence.repository.NoticeRepository;
import com.gdsc.cofence.repository.UserRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    public NoticeRegisterResponseDto registerNoticeOnlyAdmin(NoticeRegisterRequestDto requestDto, Principal principal) {
        Long userId = Long.parseLong(principal.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ID_EXCEPTION,
                        "사용지: " + ErrorCode.NOT_FOUND_ID_EXCEPTION));

        Notice notice = Notice.builder()
                .noticeSubject(requestDto.getNoticeSubject())
                .targetRole(requestDto.getTargetRoleType())
                .noticeDetail(requestDto.getNoticeDetail())
                .noticeImageUrl(requestDto.getNoticeImageUrls())
                .user(user)
                .build();

        noticeRepository.save(notice);

        return NoticeRegisterResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .noticeSubject(notice.getNoticeSubject())
                .userName(user.getUserName())
                .targetRoleType(notice.getTargetRole())
                .createdAt(notice.getCreatedAt())
                .noticeDetail(notice.getNoticeDetail())
                .noticeImage(notice.getNoticeImageUrl())
                .build();
    }
}