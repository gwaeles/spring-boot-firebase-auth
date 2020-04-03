package fr.gwa.seed.service.impl

import com.google.firebase.auth.FirebaseToken
import fr.gwa.seed.db.MemberRepository
import fr.gwa.seed.model.Member
import fr.gwa.seed.services.impl.MemberServiceImpl
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier


class MemberServiceImplTest {

    @MockK
    lateinit var memberRepository: MemberRepository

    @MockK
    lateinit var fireBaseToken: FirebaseToken

    @InjectMockKs
    lateinit var service: MemberServiceImpl

    init {
        MockKAnnotations.init(this)
    }

    @Test
    fun retrieve_existing_member() {
        //GIVEN
        val uid = "A"
        val name = "Andrea"
        val role = "MEMBER"
        val slot = slot<Member>()

        every { fireBaseToken.uid } returns uid
        every { fireBaseToken.name } returns name
        every { memberRepository.findById(uid) } returns Mono.just(Member(uid, name, role))
        every { memberRepository.save(capture(slot)) } answers {
            Mono.just(slot.captured)
        }

        //WHEN
        StepVerifier.create(service.findByFirebaseToken(fireBaseToken))
                // THEN
                .expectNextMatches { member -> member.tokenUid == uid && member.name == name }
                .expectComplete()
                .verify()
    }

    @Test
    fun retrieve_updated_member() {
        //GIVEN
        val uid = "A"
        val oldname = "Andrea"
        val newname = "Antonio"
        val role = "MEMBER"
        val slot = slot<Member>()

        every { fireBaseToken.uid } returns uid
        every { fireBaseToken.name } returns newname
        every { memberRepository.findById(uid) } returns Mono.just(Member(uid, oldname, role))
        every { memberRepository.save(capture(slot)) } answers {
            Mono.just(slot.captured)
        }

        //WHEN
        StepVerifier.create(service.findByFirebaseToken(fireBaseToken))
                // THEN
                .expectNextMatches { member -> member.tokenUid == uid && member.name == newname }
                .expectComplete()
                .verify()
    }

    @Test
    fun retrieve_new_member() {
        //GIVEN
        val uid = "A"
        val name = "Andrea"
        val slot = slot<Member>()

        every { fireBaseToken.uid } returns uid
        every { fireBaseToken.name } returns name
        every { memberRepository.findById(uid) } returns Mono.empty()
        every { memberRepository.save(capture(slot)) } answers {
            Mono.just(slot.captured)
        }

        //WHEN
        StepVerifier.create(service.findByFirebaseToken(fireBaseToken))
                // THEN
                .expectNextMatches { member -> member.tokenUid == uid && member.name == name }
                .expectComplete()
                .verify()
    }
}