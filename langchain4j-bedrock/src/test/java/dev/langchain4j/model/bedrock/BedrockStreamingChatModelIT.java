package dev.langchain4j.model.bedrock;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.TestStreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;

@EnabledIfEnvironmentVariable(named = "AWS_SECRET_ACCESS_KEY", matches = ".+")
class BedrockStreamingChatModelIT {

    @Test
    void bedrockAnthropicStreamingChatModel() {
        // given
        BedrockAnthropicStreamingChatModel bedrockChatModel = BedrockAnthropicStreamingChatModel.builder()
                .temperature(0.5)
                .maxTokens(300)
                .region(Region.US_EAST_1)
                .maxRetries(1)
                .build();
        UserMessage userMessage = userMessage("What's the capital of Poland?");

        // when
        TestStreamingResponseHandler<AiMessage> handler = new TestStreamingResponseHandler<>();
        bedrockChatModel.generate(singletonList(userMessage), handler);
        Response<AiMessage> response = handler.get();

        // then
        assertThat(response.content().text()).contains("Warsaw");
    }

    @Test
    void injectClientToModelBuilder() {

        String serviceName = "custom-service-name";

        BedrockAnthropicStreamingChatModel model = BedrockAnthropicStreamingChatModel.builder()
                .asyncClient(new BedrockRuntimeAsyncClient() {
                    @Override
                    public String serviceName() {
                        return serviceName;
                    }

                    @Override
                    public void close() {}
                })
                .build();

        assertThat(model.getAsyncClient().serviceName()).isEqualTo(serviceName);
    }
}
