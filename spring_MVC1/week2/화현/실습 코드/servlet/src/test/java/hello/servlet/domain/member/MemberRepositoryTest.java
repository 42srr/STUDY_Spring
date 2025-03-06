package hello.servlet.domain.member;


import static org.assertj.core.api.Assertions.*;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MemberRepositoryTest {
    MemberRepository memberRepository = MemberRepository.getInstance();

    @AfterEach
    void afterEach() {
        memberRepository.clearStore();
    }

    @Test
    void save() {
        // Given
        Member member = new Member("hello", 20);

        // When
        Member savedMember = memberRepository.save(member);

        // Then
        Member findMember = memberRepository.findById(savedMember.getId());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    void findAll() {
        // Given
        Member member = new Member("member1", 20);
        Member member2 = new Member("member2", 30);

        memberRepository.save(member);
        memberRepository.save(member2);

        // When
        List<Member> result = memberRepository.findAll();

        // Then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(member, member2);
    }
}