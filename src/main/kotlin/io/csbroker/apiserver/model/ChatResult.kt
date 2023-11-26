package io.csbroker.apiserver.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "chat_result")
class ChatResult(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_result_id")
    val id: Long = 0,

    // 연관관계로 엮을 수 있으나, 단순 데이터 저장용이기 때문에.. 굳이 엮지 않겠음.
    @Column(name = "email")
    val email: String,

    @Column(name = "question", columnDefinition = "VARCHAR(300)")
    val question: String,

    @Column(name = "answer", columnDefinition = "VARCHAR(1500)")
    val answer: String,
)
