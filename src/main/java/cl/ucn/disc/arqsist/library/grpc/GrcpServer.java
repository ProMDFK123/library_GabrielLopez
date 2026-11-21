
/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

/**
 * Starts and manages the gRPC server.
 */
public final class GrpcServer {

    /** Port used by the gRPC server. */
    private final int port;

    /** gRPC server instance. */
    private Server server;

    /**
     * Creates the gRPC server configuration.
     *
     * @param port port where the server will listen
     */
    public GrpcServer(int port) {
        this.port = port;
    }

    /**
     * Starts the server and registers the member service.
     *
     * @param memberService service handling member operations
     * @throws IOException if the server cannot start
     */
    public void start(MemberGrpcService memberService) throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(memberService)
                .build()
                .start();
    }

    /**
     * Stops the gRPC server.
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Waits until the server terminates.
     *
     * @throws InterruptedException if the waiting thread is interrupted
     */
    public void awaitTermination() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
