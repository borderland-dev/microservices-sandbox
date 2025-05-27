package dev.borderland.invoice_management.application.usecases.impl

import dev.borderland.invoice_management.application.domain.Invoice
import dev.borderland.invoice_management.application.usecases.CreateInvoiceUseCase
import dev.borderland.invoice_management.application.ports.InvoicePort
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class CreateInvoiceUseCaseImpl(private val invoicePort: InvoicePort): CreateInvoiceUseCase {
    override fun execute(
        number: String,
        amount: Double,
        dueDate: String
    ): Invoice {
        // Create the invoice using the port
        val invoiceId = invoicePort.createInvoice(number, amount, dueDate)

        // Parse the date string to a LocalDate
        val parsedDueDate = LocalDate.parse(dueDate)

        // Return the domain object
        return Invoice(
            id = UUID.fromString(invoiceId),
            number = number,
            amount = amount,
            dueDate = parsedDueDate
        )
    }
}

