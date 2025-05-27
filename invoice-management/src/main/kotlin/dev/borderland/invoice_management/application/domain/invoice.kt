package dev.borderland.invoice_management.application.domain

import java.time.LocalDate
import java.util.UUID

data class Invoice(
    val id: UUID,
    val number: String,
    val dueDate: LocalDate,
    val amount: Double
)