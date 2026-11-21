
/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.grpc;

import cl.ucn.disc.arqsist.library.model.Member;
import cl.ucn.disc.arqsist.library.service.NotFoundException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;

/**
 * Implements the gRPC operations for member management.
 */
public final class MemberGrpcService
        extends MemberServiceGrpc.MemberServiceImplBase {

    /** Application service used to manage members. */
    private final cl.ucn.disc.arqsist.library.service.MemberService memberService;

    /**
     * Creates the gRPC member service.
     *
     * @param memberService application service for member operations
     */
    public MemberGrpcService(
            cl.ucn.disc.arqsist.library.service.MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * Retrieves a member by identifier.
     *
     * @param request request containing the member ID
     * @param responseObserver observer receiving the result
     */
    @Override
    public void getMember(
            GetMemberRequest request,
            StreamObserver<MemberResponse> responseObserver) {

        try {
            Member member = memberService.findById(request.getId());

            responseObserver.onNext(toResponse(member));
            responseObserver.onCompleted();

        } catch (NotFoundException e) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription(e.getMessage())
                            .asRuntimeException());

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException());
        }
    }

    /**
     * Retrieves all registered members.
     *
     * @param request empty list request
     * @param responseObserver observer receiving the result
     */
    @Override
    public void listMembers(
            ListMembersRequest request,
            StreamObserver<ListMembersResponse> responseObserver) {

        try {
            ListMembersResponse.Builder response =
                    ListMembersResponse.newBuilder();

            for (Member member : memberService.findAll()) {
                response.addMembers(toResponse(member));
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();

        } catch (RuntimeException e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Unable to retrieve members")
                            .withCause(e)
                            .asRuntimeException());
        }
    }

    /**
     * Registers a new member.
     *
     * @param request request containing member information
     * @param responseObserver observer receiving the created member
     */
    @Override
    public void createMember(
            CreateMemberRequest request,
            StreamObserver<MemberResponse> responseObserver) {

        try {
            Member member = new Member(
                    request.getName(),
                    request.getEmail());

            Member created = memberService.register(member);

            responseObserver.onNext(toResponse(created));
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException());

        } catch (RuntimeException e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Unable to create member")
                            .withCause(e)
                            .asRuntimeException());
        }
    }

    /**
     * Converts a domain member into a gRPC response.
     *
     * @param member member to convert
     * @return gRPC representation of the member
     */
    private MemberResponse toResponse(Member member) {
        return MemberResponse.newBuilder()
                .setId(member.getId())
                .setName(member.getName())
                .setEmail(member.getEmail())
                .build();
    }
}
