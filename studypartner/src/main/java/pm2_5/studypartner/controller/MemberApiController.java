package pm2_5.studypartner.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pm2_5.studypartner.dto.member.MemberDTO;
import pm2_5.studypartner.dto.member.MemberUpdateDTO;
import pm2_5.studypartner.error.ApiException;
import pm2_5.studypartner.error.MemberErrorStatus;
import pm2_5.studypartner.service.MemberService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Slf4j
public class MemberApiController {

    private final MemberService memberService;

    // 회원 정보 조회 메소드
    @GetMapping("/{memberId}")
    public MemberDTO getMember(@PathVariable Long memberId){

        return memberService.findMember(memberId);
    }

    // 회원 정보 수정 메소드
    @PutMapping("/{memberId}")
    public void updateMember(@PathVariable Long memberId, @Valid @RequestBody MemberUpdateDTO memberUpdateDTO,
                             BindingResult result){

        // 입력값 검증 처리
        List<String> messages = new ArrayList<>();
        if (result.hasErrors()){
            result.getAllErrors().stream().forEach(objectError -> messages.add(objectError.getDefaultMessage()));

            throw new ApiException(MemberErrorStatus.VALIDATION_ERROR, StringUtils.join(messages, ','));
        }

        memberService.modifyMember(memberId, memberUpdateDTO);
    }
}
