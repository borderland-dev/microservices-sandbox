package dev.borderland.invoice_management.application.usecases

import dev.borderland.invoice_management.application.domain.Invoice

interface CreateInvoiceUseCase {
    fun execute(
        number: String,
        amount: Double,
        dueDate: String
    ): Invoice
}