package dev.borderland.invoice_management.application.ports

interface InvoicePort {
    fun createInvoice(
        number: String,
        amount: Double,
        dueDate: String
    ): String

    fun getInvoiceById(id: String): String

    fun updateInvoice(
        id: String,
        number: String,
        amount: Double,
        dueDate: String
    ): String

    fun deleteInvoice(id: String): Boolean
}