package fr.gwa.seed.model

import com.google.cloud.firestore.annotation.DocumentId
import org.springframework.cloud.gcp.data.firestore.Document

@Document(collectionName = "members")
data class Member(@DocumentId var tokenUid: String? = null,
                  var name: String? = null,
                  var role: String? = null)