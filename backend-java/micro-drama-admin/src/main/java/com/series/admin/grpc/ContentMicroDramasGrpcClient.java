package com.series.admin.grpc;

import com.series.common.grpc.content.v1.ContentMicroDramasServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "grpc.client.content.enabled", havingValue = "true", matchIfMissing = false)
public class ContentMicroDramasGrpcClient {

    @GrpcClient("content")
    private ContentMicroDramasServiceGrpc.ContentMicroDramasServiceBlockingStub stub;

    public ContentMicroDramasServiceGrpc.ContentMicroDramasServiceBlockingStub stub() {
        return stub;
    }
}

