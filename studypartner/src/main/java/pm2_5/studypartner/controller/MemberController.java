package pm2_5.studypartner.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pm2_5.studypartner.dto.member.MemberJoinDTO;
import pm2_5.studypartner.error.ApiException;
import pm2_5.studypartner.error.MemberErrorStatus;
import pm2_5.studypartner.service.MemberService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    // 아이디 중복 체크 메소드
    @GetMapping("/{username}")
    public ResponseEntity<String> checkIdGET(@PathVariable String username){

        if (memberService.checkIdDuplicate(username) == "exist"){

            return new ResponseEntity<>("exist", HttpStatus.CONFLICT);
        } else {
            return new ResponseEntity<>("available", HttpStatus.OK);
        }
    }

    // 회원 가입 메소드
    @PostMapping("")
    public ResponseEntity<MemberJoinDTO> signUpMember(
            @Valid @RequestBody MemberJoinDTO memberJoinDTO,
            BindingResult result) throws ApiException {

        // 입력값 검증 처리
        List<String> messages = new ArrayList<>();
        if (result.hasErrors()){
            result.getAllErrors().stream().forEach(objectError -> messages.add(objectError.getDefaultMessage()));

            throw new ApiException(MemberErrorStatus.VALIDATION_ERROR, StringUtils.join(messages, ','));
        }
        memberService.registerMember(memberJoinDTO);
        return new ResponseEntity<>(memberJoinDTO, HttpStatus.OK);
    }
}
