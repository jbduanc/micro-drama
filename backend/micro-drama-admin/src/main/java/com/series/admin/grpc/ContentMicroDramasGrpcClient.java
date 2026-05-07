package com.series.admin.grpc;

import com.series.common.grpc.content.v1.ContentMicroDramasServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class ContentMicroDramasGrpcClient {

    @GrpcClient("content")
    private ContentMicroDramasServiceGrpc.ContentMicroDramasServiceBlockingStub stub;

    public ContentMicroDramasServiceGrpc.ContentMicroDramasServiceBlockingStub stub() {
        return stub;
    }
}

