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

    /** Creates the member service.
     * @param memberDao DAO used to persist and query members
     */
    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    /** Registers a member.
     * @param member member to register
     * @return the registered member
     */
    public Member register(Member member) {
        memberDao.create(member);
        return member;
    }

    /** Lists every registered member.
     * @return all members
     */
    public List<Member> findAll() {return memberDao.findAll();
    }
}