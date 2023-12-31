package pm2_5.studypartner.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pm2_5.studypartner.dto.openai.OpenaiRespDTO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OpenaiUtil {

    @Value("${openai.my_key}")
    private String chatkey;

    public String extractContent(String system, String translatedText, boolean type) throws JsonProcessingException {
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + chatkey);

        String apiUrl = "https://api.openai.com/v1/chat/completions";

        // chat gpt 동작 지정
        Map<String, Object> message1 = new HashMap<>();
        message1.put("role", "system");
        message1.put("content", system);
        Map<String, Object> message2 = new HashMap<>();
        message2.put("role", "user");
        message2.put("content", translatedText);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-4");
        body.put("messages", Arrays.asList(message1, message2));
        body.put("temperature", 0);

        WebClient webClient = WebClient.create();

        // 요청 및 응답
        String response = webClient.post()
                .uri(apiUrl)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // JSON 매핑
        ObjectMapper objectMapper = new ObjectMapper();
        OpenaiRespDTO openaiRespDTO = objectMapper.readValue(response, OpenaiRespDTO.class);

        // 반환 content 작성
        StringBuilder sb = new StringBuilder();
        for(OpenaiRespDTO.Choice choice : openaiRespDTO.getChoices()) {
            String keywords = choice.getMessage().getContent();
            String json;
            if(type){
                json = extractList(keywords);
            } else{
                json = extractJson(keywords);
            }

            sb.append(json);
        }
        return sb.toString();
    }

    public static String extractJson(String input) {
        //JSON 스타일의 문자열 패턴을 정의합니다.
        String jsonStr = input.substring(input.indexOf("{"));

        return jsonStr;
    }

    public static String extractList(String input) {
        //JSON 스타일의 문자열 패턴을 정의합니다.
        String jsonStr = input.substring(input.indexOf("["));

        return jsonStr;
    }
}
