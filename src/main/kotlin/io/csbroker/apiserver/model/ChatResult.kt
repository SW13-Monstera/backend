package io.csbroker.apiserver.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "chat_result")
class ChatResult(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_result_id")
    val id: Long? = null,

    // 연관관계로 엮을 수 있으나, 단순 데이터 저장용이기 때문에.. 굳이 엮지 않겠음.
    @Column(name = "email")
    val email: String,

    @Column(name = "question", columnDefinition = "VARCHAR(300)")
    val question: String,

    @Column(name = "answer", columnDefinition = "VARCHAR(1500)")
    val answer: String,
)