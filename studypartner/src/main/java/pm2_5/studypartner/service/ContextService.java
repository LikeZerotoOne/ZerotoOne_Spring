package pm2_5.studypartner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pm2_5.studypartner.domain.Context;
import pm2_5.studypartner.domain.Document;
import pm2_5.studypartner.domain.Keyword;
import pm2_5.studypartner.dto.context.ContextsDTO;
import pm2_5.studypartner.dto.papago.TextTransReqDTO;
import pm2_5.studypartner.repository.ContextRepository;
import pm2_5.studypartner.repository.DocumentRepository;
import pm2_5.studypartner.util.OpenaiUtil;
import pm2_5.studypartner.util.NaverCloudUtil;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ContextService {

    public final NaverCloudUtil naverCloudUtil;
    public final OpenaiUtil openaiUtil;
    public final ContextRepository contextRepository;
    public final DocumentRepository documentRepository;

    public ContextsDTO registerContext(Long documentId) throws JsonProcessingException {

        // 이미 해당 documentId에 연관된 keyword가 있는지 확인
        List<Context> existingContexts = contextRepository.findContextsByDocumentId(documentId);

        if (!existingContexts.isEmpty()) {
            // 이미 연관된 키워드가 있을 경우 Bad Request 반환
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 문서에 문단 요약이 이미 존재합니다!");
        }

        
        // 해당 자료를 가져옴
        Document findDocument = documentRepository.findById(documentId).get();
        String translatedText = findDocument.getEnContent();
        
        // 역할 설정
        String system = """
                I need to provide a summary of the paragraphs from the data I provided. I'll tell you the steps, so follow them.
                1. Divide the paragraphs of the given material based on the following criteria. Start a new paragraph at the beginning of a new idea or topic, and each paragraph should form a unit of meaning. To emphasize specific information, you can use a paragraph containing that information to stand alone. You must break up at least two paragraphs. Each paragraph will have a key count in JSON sentence
                2. Create a summary of each paragraph. This will have a key summary in JSON sentence
                3. Count the number of paragraphs and let me know the number of paragraphs
                4. Please write JSON-style responses so that Jackson can paraphrase them by linking them with the summary. I'll show you an example of a response format in ```.
                ```
                {
                    "count": 3,
                    "contexts": [
                        {
                            "content": "Agile methodology is a methodology for realizing time-to-market in software development. In theory, it is a methodology for developing systems that are flexible, quickly adapt to change, and efficiently because people are more central than procedures. ",
                            "summary": "Agile methodology centers on people rather than processes."
                        },
                        {
                            "content": "Although the methodology has evolved and improved, there are still some drawbacks to the traditional methodology. First, it's difficult to accurately reflect the needs of users, who are asked periodically during each phase. There's no systematic way to align terms, and the user sees the system in action. Second, it's hard to adequately deal with constantly changing requirements without knowing exactly what you want. The Waterfall Model implementation process is a way to complete one phase and move on to the next. Project scheduling is much more challenging to reflect new requirements that are added beyond the paragraph phase. Third, the software modules developed may not combine well. The developed modules have to go to the testing phase to work with each other, but the interfaces between the modules are not available When IS issues arise, most critical system defects are found at the end of the project, which can be a very difficult task to deal with and leads to quality degradation problems. There is an absolute lack of time to reflect additional needs, and the relationship between deliverables and information about them depends on the version. It is difficult to manage systematically and independently, which puts a huge burden on maintenance and reflecting requirements. The quality of the program suffers.",
                            "summary": "Traditional methodologies face challenges in accurately reflecting user needs, dealing with dynamic requirement changes, and managing project communication, which often leads to quality issues at the project's end. Additionally, the integration of developed modules can be problematic, further burdening maintenance and requirements reflection, degrading software quality."
                        },
                        {
                            "content": "To solve these problems, agile methodologies typically emphasize individuals rather than processes and tools. They believe that interaction with customers is important, that working software is more important than documentation, that collaboration with customers is more important than contract negotiations, and that planning and adherence are important. Responding to change is more important than reacting to change. The purpose of the Agile methodology is to eliminate unnecessary processes. It is to increase software development productivity by avoiding and performing only the necessary activities. Cow. Increasing the flexibility and reusability of software for real-time enterprise implementations. This is similar to the goal of Service Created Architecture (SOA). There are several types of Agile methodologies, but they all have some common characteristics.",
                            "summary": "Agile methodologies prioritize individuals, customer interaction, working software, collaboration with customers and response to change over processes and tools. They aim to streamline software development productivity by eliminating unnecessary processes, increasing flexibility and reusability of software and aiming for real-time enterprise implementations, a goal similar to that of Service Oriented Architecture (SOA). There are various types of Agile methodologies, each sharing these common traits."
                        }
                    ]
                }
                ```
                """;

        // chat gpt의 응답을 추출
        String json = openaiUtil.extractContent(system, translatedText, false);
        // chat gpt의 응답을 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        ContextsDTO contextsDTO = objectMapper.readValue(json, ContextsDTO.class);

        ContextsDTO newContexts = new ContextsDTO(contextsDTO.getCount());

        // 각 keyword를 확인 및 저장
        for(ContextsDTO.ContextDTO context : contextsDTO.getContexts()) {
            // 키워드와 설명 번역
            TextTransReqDTO textTransReqDTO = new TextTransReqDTO(documentId, "en", "ko", context.getContent());
            String translatedContent = naverCloudUtil.translateText(textTransReqDTO);
            textTransReqDTO = new TextTransReqDTO(documentId, "en", "ko", context.getSummary());
            String translatedSummary = naverCloudUtil.translateText(textTransReqDTO);

            // 키워드 저장
            Context newContext = new Context(findDocument, translatedContent, translatedSummary);
            contextRepository.save(newContext);

            newContexts.getContexts().add(new ContextsDTO.ContextDTO(newContext.getContent(), newContext.getSummary()));
        }
        newContexts.setDocumentId(documentId);

        return newContexts;
    }

    // 문단 자료 조회
    public ContextsDTO findContexts(Long documentId){

        List<Context> contexts = contextRepository.findContextsByDocumentId(documentId);

        ContextsDTO contextsDTO = new ContextsDTO();
        List<ContextsDTO.ContextDTO> contextDTOList = new ArrayList<>();
        int count = 0;
        for(Context context : contexts){
            contextDTOList.add(new ContextsDTO.ContextDTO(context.getContent(), context.getSummary()));
            count += 1;
        }

        contextsDTO.setContexts(contextDTOList);
        contextsDTO.setCount(count);
        contextsDTO.setDocumentId(documentId);

        return contextsDTO;
    }

    // 키워드 자료 삭제
    public void deleteContexts (Long documentId){
        contextRepository.deleteContextsByDocumentId(documentId);
    }
}
