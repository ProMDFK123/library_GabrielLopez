
/*
 * Copyright (c) 2026. Arquitectura de Sistemas, DISC, UCN, Antofagasta.
 */
package cl.ucn.disc.arqsist.library.service;

import cl.ucn.disc.arqsist.library.dao.MemberDao;
import cl.ucn.disc.arqsist.library.model.Member;
import java.util.List;

/** Provides member registration and query operations. */
public final class MemberService {

    /** Persistence dependency for members. */
    private final MemberDao memberDao;

    /**
     * Creates the member service.
     *
     * @param memberDao DAO used to persist and query members
     */
    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    /**
     * Registers a member after validating its information.
     *
     * @param member member to register
     * @return the registered member
     */
    public Member register(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        if (member.getName() == null || member.getName().isBlank()) {
            throw new IllegalArgumentException("Member name is required");
        }

        if (member.getEmail() == null || member.getEmail().isBlank()) {
            throw new IllegalArgumentException("Member email is required");
        }

        member.setName(member.getName().trim());
        member.setEmail(member.getEmail().trim());

        memberDao.create(member);
        return member;
    }

    /**
     * Finds a member by its identifier.
     *
     * @param id member identifier
     * @return the matching member
     * @throws NotFoundException if the member does not exist
     */
    public Member findById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Member ID must be positive");
        }

        Member member = memberDao.findById(id);

        if (member == null) {
            throw new NotFoundException("Member not found: " + id);
        }

        return member;
    }

    /**
     * Lists every registered member.
     *
     * @return all registered members
     */
    public List<Member> findAll() {
        return memberDao.findAll();
    }
}
