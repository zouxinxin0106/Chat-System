package com.chat.service;

import com.chat.common.id.SnowflakeIdGenerator;
import com.chat.common.session.model.ConnectionInfo;
import com.chat.common.session.InMemorySessionRegistry;
import com.chat.common.session.SessionRegistry;
import com.chat.service.model.ChatMessage;
import com.chat.service.model.DeliveryStatus;
import com.chat.service.model.MessageType;
import com.chat.service.model.ProcessedMessage;
import com.chat.service.model.TextContent;
import com.chat.service.pipeline.MessagePipeline;
import com.chat.service.pipeline.stage.DecodeStage;
import com.chat.service.pipeline.stage.ValidateStage;
import com.chat.service.pipeline.stage.AuthorizeStage;
import com.chat.service.pipeline.stage.EnrichStage;
import com.chat.service.repository.inmemory.InMemoryMessageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatSystemIntegrationTest {
    private SnowflakeIdGenerator idGenerator;
    private InMemoryMessageRepository messageRepo;
    private SessionRegistry sessionRegistry;

    @BeforeEach
    void setUp() {
        idGenerator = new SnowflakeIdGenerator(1);
        messageRepo = new InMemoryMessageRepository();
        sessionRegistry = new InMemorySessionRegistry();
        sessionRegistry.start();
    }

    @AfterEach
    void tearDown() {
        sessionRegistry.stop();
    }

    @Test
    void testPipelineProcessing() throws Exception {
        ChatMessage inputMessage = ChatMessage.builder()
                .messageId("")
                .conversationId("conv1")
                .senderId("userA")
                .type(MessageType.MESSAGE_TYPE_TEXT)
                .content(new TextContent("Hello UserB!"))
                .correlationId("test-corr-1")
                .build();

        MessagePipeline pipeline = new MessagePipeline(
                new DecodeStage(),
                new ValidateStage(),
                new AuthorizeStage(),
                new EnrichStage(idGenerator)
        );

        ProcessedMessage processed = pipeline.process(inputMessage);

        assertNotNull(processed.getMessage().getMessageId());
        assertFalse(processed.getMessage().getMessageId().isEmpty());
        assertEquals("userA", processed.getMessage().getSenderId());
        assertEquals("conv1", processed.getMessage().getConversationId());
        assertTrue(processed.isValid());
        assertTrue(processed.isAuthorized());
    }

    @Test
    void testMessageRepository() {
        ChatMessage message = ChatMessage.builder()
                .messageId("msg-1")
                .conversationId("conv1")
                .senderId("userA")
                .type(MessageType.MESSAGE_TYPE_TEXT)
                .content(new TextContent("Test message"))
                .status(DeliveryStatus.DELIVERY_STATUS_SENT)
                .build();

        ChatMessage saved = messageRepo.save(message);
        assertEquals("msg-1", saved.getMessageId());

        List<ChatMessage> messages = messageRepo.getMessages("conv1", 10);
        assertEquals(1, messages.size());
        assertEquals("Test message", ((TextContent) messages.get(0).getContent()).text());
    }

    @Test
    void testSessionRegistryOperations() {
        String connId1 = "conn-test-1";
        String connId2 = "conn-test-2";

        sessionRegistry.upsertSession("userX", connId1, "gateway-1");
        sessionRegistry.upsertSession("userX", connId2, "gateway-2");

        List<ConnectionInfo> sessions = sessionRegistry.getUserSessions("userX");
        assertEquals(2, sessions.size());

        ConnectionInfo session = sessionRegistry.getSession(connId1).orElse(null);
        assertNotNull(session);
        assertEquals("gateway-1", session.gatewayInstanceId());
        assertEquals("userX", session.userId());

        sessionRegistry.removeSession(connId1);
        List<ConnectionInfo> sessionsAfter = sessionRegistry.getUserSessions("userX");
        assertEquals(1, sessionsAfter.size());
    }

    @Test
    void testEnrichStageAddsSequenceId() throws Exception {
        ChatMessage input = ChatMessage.builder()
                .messageId("")
                .conversationId("conv1")
                .senderId("userA")
                .type(MessageType.MESSAGE_TYPE_TEXT)
                .content(new TextContent("Test"))
                .correlationId("corr-1")
                .build();

        MessagePipeline pipeline = new MessagePipeline(
                new DecodeStage(),
                new ValidateStage(),
                new AuthorizeStage(),
                new EnrichStage(idGenerator)
        );

        ProcessedMessage result = pipeline.process(input);

        assertTrue(result.getSequenceId() > 0);
        assertNotNull(result.getMessage().getMessageId());
    }
}
