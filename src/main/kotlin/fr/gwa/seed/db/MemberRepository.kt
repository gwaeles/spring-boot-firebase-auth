package fr.gwa.seed.db

import fr.gwa.seed.model.Member
import org.springframework.cloud.gcp.data.firestore.FirestoreReactiveRepository

/**
 * @author
 *
 * Incredible : one line to manage CRUD access to DB !
 * Customizable with your own specific methods
 */
interface MemberRepository : FirestoreReactiveRepository<Member>