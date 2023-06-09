package com.itunion.chatgpt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.kevinsawicki.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@Slf4j
@RestController
public class AIController
{
    @Value("${OPENAI_API_KEY}")
    private String openApiKey;

    @PostConstruct
    public void init()
    {
        log.info("openApiKey = {}", openApiKey);
    }

    /**
     * curl https://api.openai.com/v1/completions \
     * -H "Content-Type: application/json" \
     * -H "Authorization: Bearer $OPENAI_API_KEY" \
     * -d '{
     * "model": "text-davinci-003",
     * "prompt": "I am a highly intelligent question answering bot. If you ask me a question that is rooted in truth, I will give you the answer. If you ask me a question that is nonsense, trickery, or has no clear answer, I will respond with \"Unknown\".\n\nQ: What is human life expectancy in the United States?\nA: Human life expectancy in the United States is 78 years.\n\nQ: Who was president of the United States in 1955?\nA: Dwight D. Eisenhower was president of the United States in 1955.\n\nQ: Which party did he belong to?\nA: He belonged to the Republican Party.\n\nQ: What is the square root of banana?\nA: Unknown\n\nQ: How does a telescope work?\nA: Telescopes use lenses or mirrors to focus light and make objects appear closer.\n\nQ: Where were the 1992 Olympics held?\nA: The 1992 Olympics were held in Barcelona, Spain.\n\nQ: How many squigs are in a bonk?\nA: Unknown\n\nQ: Where is the Valley of Kings?\nA:",
     * "temperature": 0,
     * "max_tokens": 100,
     * "top_p": 1,
     * "frequency_penalty": 0.0,
     * "presence_penalty": 0.0,
     * "stop": ["\n"]
     * }'
     *
     * @return
     */
    @GetMapping("/api/send")
    public ResponseEntity<?> send(String text)
    {
        log.info("send text: {}", text);
        Message message = new Message();
        message.setPrompt(text);
        try
        {
            String requestBody = JSON.toJSONString(message);
            log.info("send requestBody: {}", requestBody);
            String body = HttpRequest.post("https://api.openai.com/v1/completions")
                    .contentType("application/json")
                    .authorization("Bearer " + openApiKey)
                    .send(requestBody)
                    .body();
            log.info("chat request text:{}, response: {}", text, body);
            return ResponseEntity.ok(body);
        } catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return ResponseEntity.ok(e.getMessage());
        }
    }

    @GetMapping("/api/chat")
    public ResponseEntity<?> chat(String text)
    {
        log.info("send text: {}", text);
        JSONObject message = new JSONObject();
        message.put("model", "gpt-3.5-turbo");
        JSONArray messages = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("role", "user");
        item.put("content", text);
        messages.add(item);
        message.put("messages", messages);
        try
        {
            String requestBody = message.toJSONString();
            log.info("send requestBody: {}", requestBody);
            String body = HttpRequest.post("https://api.openai.com/v1/chat/completions")
                    .contentType("application/json")
                    .authorization("Bearer " + openApiKey)
                    .send(requestBody)
                    .body();
            log.info("chat request text:{}, response: {}", text, body);
            JSONObject jsonObject = JSON.parseObject(body);
            JSONObject choices = jsonObject.getJSONArray("choices").getJSONObject(0);
            JSONObject content = choices.getJSONObject("message").getJSONObject("content");
            return ResponseEntity.ok(content);
        } catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return ResponseEntity.ok(e.getMessage());
        }
    }
}
